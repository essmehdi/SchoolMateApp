package com.github.essmehdi.schoolmate.placesuggestions.viewmodels

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
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SuggestionsViewModel : ViewModel(){
    private val sortField: MutableLiveData<String> = MutableLiveData("date")
    private val sortOrder: MutableLiveData<String> = MutableLiveData("desc")
    val suggestions: MutableLiveData<List<PlaceSuggestions>> = MutableLiveData()
    val currentPageStatus: MutableLiveData<BaseResponse<PaginatedResponse<PlaceSuggestions>>> = MutableLiveData()
    val currentPage: MutableLiveData<PaginatedResponse<PlaceSuggestions>?> = MutableLiveData()
    val filterType: MutableLiveData<SuggestionType?> = MutableLiveData()
    val deleteStatus: MutableLiveData<BaseResponse<MessageResponse>?> = MutableLiveData(null)


    val id: MutableLiveData<Long> = MutableLiveData()

    fun loadSuggestions(id: Long? = null){

        id?.let { this.id.value = it }
        currentPageStatus.value = BaseResponse.Loading()
        if (currentPage.value?.last == true){
            currentPageStatus.value = BaseResponse.Success(currentPage.value!!)
            return
        }
        viewModelScope.launch {
            //if id isn't null,
             if (id==0.toLong())
                Api.suggestionsService.getCurrentUserSuggestions(
                currentPage.value?.page?.plus(1) ?: 0,
                sort = "${sortField.value},${sortOrder.value}",
            )  else if(id == null)
                Api.suggestionsService.getAllSuggestions(
                currentPage.value?.page?.plus(1) ?: 0,
                sort = "${sortField.value},${sortOrder.value}",
            ) else Api.suggestionsService.getUserSuggestions(
                currentPage.value?.page?.plus(1) ?: 0,
                sort = "${sortField.value},${sortOrder.value}",
                id = id
            )
        }
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

    fun addFilterType(type: SuggestionType) {
        filterType.value = type
    }

    fun removeFilterType(type: SuggestionType) {
        filterType.value = null
    }

    fun refresh(){
        suggestions.value = listOf()
        currentPage.value = null
        loadSuggestions(id.value)
    }

    fun changeSortfield(field: String){
        sortField.value = field
        refresh()
    }

    fun changeSortOder(order: String){
        sortOrder.value = order
        refresh()
    }
}