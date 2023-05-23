package com.github.essmehdi.schoolmate.placesuggestions.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.github.essmehdi.schoolmate.placesuggestions.models.PlaceSuggestions
import com.github.essmehdi.schoolmate.shared.api.Api
import com.github.essmehdi.schoolmate.shared.api.BaseResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SuggestionDetailsViewModel: ViewModel() {

    val suggestion: MutableLiveData<BaseResponse<PlaceSuggestions>> = MutableLiveData()

    fun fetchSuggestion(id: Long) {
        suggestion.value = BaseResponse.Loading()
        Api.suggestionsService.getSuggestion(id).enqueue(object :
            Callback<PlaceSuggestions> {
            override fun onResponse(call: Call<PlaceSuggestions>, response: Response<PlaceSuggestions>) {
                if (response.isSuccessful) {
                    suggestion.value = BaseResponse.Success(response.body()!!)
                } else {
                    suggestion.value = BaseResponse.Error(response.code())
                }
            }

            override fun onFailure(call: Call<PlaceSuggestions>, t: Throwable) {
                suggestion.value = BaseResponse.Error(0)
            }
        })
    }
}