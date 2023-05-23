package com.github.essmehdi.schoolmate.placesuggestions.viewmodels

import android.util.Log
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.essmehdi.schoolmate.placesuggestions.enumerations.SuggestionType
import com.github.essmehdi.schoolmate.placesuggestions.models.PlaceSuggestions
import com.github.essmehdi.schoolmate.shared.api.Api
import com.github.essmehdi.schoolmate.shared.api.BaseResponse
import com.github.essmehdi.schoolmate.shared.api.dto.MessageResponse
import com.github.essmehdi.schoolmate.shared.api.dto.PaginatedResponse
import com.github.essmehdi.schoolmate.users.models.User
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

open class SuggestionsViewModel(): ViewModel() {

    private val sortField: MutableLiveData<String> = MutableLiveData("date")
    private val sortOrder: MutableLiveData<String> = MutableLiveData("desc")

    val suggestions: MutableLiveData<List<PlaceSuggestions>> = MutableLiveData()
    val mySuggestions: MutableLiveData<List<PlaceSuggestions>> = MutableLiveData()

    // all suggestions
    val currentPageStatus: MutableLiveData<BaseResponse<PaginatedResponse<PlaceSuggestions>>> = MutableLiveData()
    val currentPage: MutableLiveData<PaginatedResponse<PlaceSuggestions>?> = MutableLiveData()

    // Mine
    val myCurrentPageStatus: MutableLiveData<BaseResponse<PaginatedResponse<PlaceSuggestions>>> = MutableLiveData()
    val myCurrentPage: MutableLiveData<PaginatedResponse<PlaceSuggestions>?> = MutableLiveData()

    val deleteStatus: MutableLiveData<BaseResponse<MessageResponse>?> = MutableLiveData(null)

    val currentUser: MutableLiveData<User> = MutableLiveData<User>()

    val showEmpty: MediatorLiveData<Boolean> = MediatorLiveData()

    val showMineEmpty: MediatorLiveData<Boolean> = MediatorLiveData()

    init {
        trackEmpty()
        trackMySuggestionsEmpty()
    }

    fun fetchCurrentUser(){
        Api.authService.me().enqueue(object : Callback<User> {
            override fun onResponse(call: Call<User>, response: Response<User>) {
                if(response.isSuccessful){
                    currentUser.value = response.body()
                }
            }
            override fun onFailure(call: Call<User>, t: Throwable) {

            }
        })
    }



    fun deleteSuggestion(id: Long) {
        deleteStatus.value = BaseResponse.Loading()
        Api.suggestionsService.deleteSuggestion(id).enqueue(object: Callback<MessageResponse> {
            override fun  onResponse(call: Call<MessageResponse>, response: Response<MessageResponse>) {
                if (response.isSuccessful) {
                    deleteStatus.value = BaseResponse.Success(response.body()!!)

                } else {
                    deleteStatus.value = BaseResponse.Error(response.code())
                }
            }
            override fun onFailure(call: Call<MessageResponse>, t: Throwable) {
                deleteStatus.value = BaseResponse.Error(0)
            }
        })
    }

    // All complaints
    fun loadSuggestions() {
        currentPageStatus.value = BaseResponse.Loading()
        if (currentPage.value?.last == true) {
            currentPageStatus.value = BaseResponse.Success(currentPage.value!!)
            return
        }
        viewModelScope.launch {
            //if id isn't null,
            Api.suggestionsService.getAllSuggestions(
                currentPage.value?.page?.plus(1) ?: 0,
                sort = "${sortField.value},${sortOrder.value}"
            ).enqueue(object : Callback<PaginatedResponse<PlaceSuggestions>> {
                override fun onResponse(
                    call: Call<PaginatedResponse<PlaceSuggestions>>,
                    response: Response<PaginatedResponse<PlaceSuggestions>>
                ) {
                    if (response.isSuccessful) {
                        currentPageStatus.value = BaseResponse.Success(response.body()!!)
                        currentPage.value = response.body()!!
                        suggestions.value =
                            (suggestions.value ?: listOf()).plus(response.body()!!.results)
                    } else {
                        currentPageStatus.value = BaseResponse.Error(response.code())
                    }
                }

                override fun onFailure(
                    call: Call<PaginatedResponse<PlaceSuggestions>>,
                    t: Throwable
                ) {
                    currentPageStatus.value = BaseResponse.Error(0)
                }
            })
        }
    }

    // To get current user complaints
    fun loadCurrentUserSuggestions(){
        myCurrentPageStatus.value = BaseResponse.Loading()
        Api.suggestionsService.getCurrentUserSuggestions(
            myCurrentPage.value?.page?.plus(1) ?: 0,
            sort = "${sortField.value},${sortOrder.value}",
        )
            .enqueue(object: Callback<PaginatedResponse<PlaceSuggestions>> {
                override fun onResponse(
                    call: Call<PaginatedResponse<PlaceSuggestions>>,
                    response: Response<PaginatedResponse<PlaceSuggestions>>
                ) {
                    if (response.isSuccessful) {
                        myCurrentPageStatus.value = BaseResponse.Success(response.body()!!)
                        myCurrentPage.value = response.body()!!
                        mySuggestions.value = (mySuggestions.value ?: listOf()).plus(response.body()!!.results)
                    } else {
                        myCurrentPageStatus.value = BaseResponse.Error(response.code())
                    }
                }

                override fun onFailure(call: Call<PaginatedResponse<PlaceSuggestions>>, t: Throwable) {
                    myCurrentPageStatus.value = BaseResponse.Error(0)
                }
            })
    }

    fun refresh(){
        suggestions.value = listOf()
        mySuggestions.value = listOf()
        currentPage.value = null
        myCurrentPage.value = null
        loadCurrentUserSuggestions()
        loadSuggestions()
    }

    fun changeSortOder(order: String){
        sortOrder.value = order
        refresh()
    }

    fun trackEmpty() {
        // Check if showEmpty has already been tracked
        if (showEmpty.hasActiveObservers()) return

        showEmpty.addSource(suggestions) {
            showEmpty.value = it.isEmpty() && currentPageStatus.value is BaseResponse.Success
        }
        showEmpty.addSource(currentPageStatus) {
            showEmpty.value = suggestions.value?.isEmpty() == true && it is BaseResponse.Success
        }
    }

    fun trackMySuggestionsEmpty() {
        // Check if showEmpty has already been tracked
        if (showMineEmpty.hasActiveObservers()) return

        showMineEmpty.addSource(mySuggestions) {
            showMineEmpty.value = it.isEmpty() && myCurrentPageStatus.value is BaseResponse.Success
        }
        showMineEmpty.addSource(myCurrentPageStatus) {
            showMineEmpty.value = mySuggestions.value?.isEmpty() == true && it is BaseResponse.Success
        }
    }


}