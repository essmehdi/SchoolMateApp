package com.github.essmehdi.schoolmate.documents.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.essmehdi.schoolmate.placesuggestions.api.dto.CreateSuggestionDto
import com.github.essmehdi.schoolmate.placesuggestions.api.dto.EditSuggestionDto
import com.github.essmehdi.schoolmate.placesuggestions.enumerations.SuggestionType
import com.github.essmehdi.schoolmate.placesuggestions.models.PlaceSuggestions
import com.github.essmehdi.schoolmate.shared.api.Api
import com.github.essmehdi.schoolmate.shared.api.BaseResponse
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SuggestionEditorViewModel: ViewModel() {
    val editMode: MutableLiveData<Boolean> = MutableLiveData(false)
    val editId: MutableLiveData<Long?> = MutableLiveData(null)
    val uploadStatus: MutableLiveData<BaseResponse<PlaceSuggestions>> = MutableLiveData()
    val selectedType: MutableLiveData<SuggestionType?> = MutableLiveData()

    private val requestCallback = object : Callback<PlaceSuggestions> {
        override fun onResponse(call: Call<PlaceSuggestions>, response: Response<PlaceSuggestions>) {
            if (response.isSuccessful) {
                uploadStatus.value = BaseResponse.Success(response.body()!!)
            } else {
                uploadStatus.value = BaseResponse.Error(response.code())
            }
        }

        override fun onFailure(call: Call<PlaceSuggestions>, t: Throwable) {
            uploadStatus.value = BaseResponse.Error(0)
        }
    }

    fun selectType(type: SuggestionType) {
        selectedType.value = type
    }

    fun removeType() {
        selectedType.value = null
    }

    fun addSuggestion(createSuggestionDto: CreateSuggestionDto) {
        uploadStatus.value = BaseResponse.Loading()
        viewModelScope.launch {
            Api.suggestionsService.suggestPlace(createSuggestionDto).enqueue(requestCallback)
        }
    }

    fun editSuggestion(editSuggestionDto: EditSuggestionDto) {
        Api.suggestionsService.editSuggestion(editSuggestionDto,editId.value!!).enqueue(requestCallback)
    }
}