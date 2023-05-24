package com.github.essmehdi.schoolmate.users.viewmodels

import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.github.essmehdi.schoolmate.placesuggestions.models.PlaceSuggestions
import com.github.essmehdi.schoolmate.shared.api.Api
import com.github.essmehdi.schoolmate.shared.api.BaseResponse
import com.github.essmehdi.schoolmate.shared.api.dto.MessageResponse
import com.github.essmehdi.schoolmate.shared.api.dto.PaginatedResponse
import com.github.essmehdi.schoolmate.users.models.User
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class UserPlacesViewModel : ViewModel() {
    val suggestions: MutableLiveData<List<PlaceSuggestions>> = MutableLiveData()
    val currentPage: MutableLiveData<PaginatedResponse<PlaceSuggestions>?> = MutableLiveData()
    val currentPageStatus: MutableLiveData<BaseResponse<PaginatedResponse<PlaceSuggestions>>> = MutableLiveData()
    val deleteStatus: MutableLiveData<BaseResponse<MessageResponse>?> = MutableLiveData(null)
    val currentUser: MutableLiveData<User> = MutableLiveData<User>()
    val showEmpty: MediatorLiveData<Boolean> = MediatorLiveData()
    val id: MutableLiveData<Long> = MutableLiveData()


    init {
        trackEmpty()
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

    fun loadUserSuggestions(){
        Api.suggestionsService.getUserSuggestions(
            page=currentPage.value?.page?.plus(1) ?: 0,
            id=id.value!!
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

    fun refresh(){
        suggestions.value = listOf()
        currentPage.value = null
        loadUserSuggestions()
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
}