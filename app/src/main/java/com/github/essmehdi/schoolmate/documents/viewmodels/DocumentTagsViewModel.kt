package com.github.essmehdi.schoolmate.documents.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.github.essmehdi.schoolmate.documents.api.dto.DocumentTagDetails
import com.github.essmehdi.schoolmate.documents.models.DocumentTag
import com.github.essmehdi.schoolmate.shared.api.Api
import com.github.essmehdi.schoolmate.shared.api.BaseResponse
import com.github.essmehdi.schoolmate.shared.api.dto.MessageResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class DocumentTagsViewModel : ViewModel() {
  val documentTags: MutableLiveData<BaseResponse<List<DocumentTag>>> = MutableLiveData()
  val deleteStatus: MutableLiveData<BaseResponse<MessageResponse>> = MutableLiveData()
  val addStatus: MutableLiveData<BaseResponse<DocumentTag>> = MutableLiveData()
  val editStatus: MutableLiveData<BaseResponse<DocumentTag>> = MutableLiveData()

  fun loadDocumentTags() {
    documentTags.value = BaseResponse.Loading()
    Api.documentsService.getDocumentTags().enqueue(object : Callback<List<DocumentTag>> {
      override fun onResponse(
        call: Call<List<DocumentTag>>,
        response: Response<List<DocumentTag>>
      ) {
        documentTags.value = if (response.isSuccessful) {
          BaseResponse.Success(response.body()!!)
        } else {
          BaseResponse.Error(response.code())
        }
      }

      override fun onFailure(call: Call<List<DocumentTag>>, t: Throwable) {
        documentTags.value = BaseResponse.Error(0)
      }
    })
  }

  fun addTag(name: String) {
    addStatus.value = BaseResponse.Loading()
    Api.documentsService.addDocumentTag(DocumentTagDetails(name)).enqueue(object : Callback<DocumentTag> {
      override fun onResponse(call: Call<DocumentTag>, response: Response<DocumentTag>) {
        if (response.isSuccessful) {
          addStatus.value = BaseResponse.Success(response.body()!!)
          loadDocumentTags()
        } else {
          addStatus.value = BaseResponse.Error(response.code())
        }
      }

      override fun onFailure(call: Call<DocumentTag>, t: Throwable) {
        addStatus.value = BaseResponse.Error(0)
      }
    })
  }

  fun editTag(id: Long, name: String) {
    editStatus.value = BaseResponse.Loading()
    Api.documentsService.editDocumentTag(id, DocumentTagDetails(name)).enqueue(object : Callback<DocumentTag> {
      override fun onResponse(call: Call<DocumentTag>, response: Response<DocumentTag>) {
        if (response.isSuccessful) {
          editStatus.value = BaseResponse.Success(response.body()!!)
          loadDocumentTags()
        } else {
          editStatus.value = BaseResponse.Error(response.code())
        }
      }

      override fun onFailure(call: Call<DocumentTag>, t: Throwable) {
        editStatus.value = BaseResponse.Error(0)
      }
    })
  }

  fun deleteTag(id: Long) {
    deleteStatus.value = BaseResponse.Loading()
    Api.documentsService.deleteDocumentTag(id).enqueue(object : Callback<MessageResponse> {
      override fun onResponse(call: Call<MessageResponse>, response: Response<MessageResponse>) {
        if (response.isSuccessful) {
          deleteStatus.value = BaseResponse.Success(response.body()!!)
          loadDocumentTags()
        } else {
          deleteStatus.value = BaseResponse.Error(response.code())
        }
      }

      override fun onFailure(call: Call<MessageResponse>, t: Throwable) {
        deleteStatus.value = BaseResponse.Error(0)
      }
    })
  }

}