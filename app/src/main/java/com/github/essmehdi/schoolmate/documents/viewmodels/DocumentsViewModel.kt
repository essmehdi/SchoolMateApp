package com.github.essmehdi.schoolmate.documents.viewmodels

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.widget.Toast
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.essmehdi.schoolmate.R
import com.github.essmehdi.schoolmate.documents.models.Document
import com.github.essmehdi.schoolmate.documents.models.DocumentTag
import com.github.essmehdi.schoolmate.shared.api.Api
import com.github.essmehdi.schoolmate.shared.api.BaseResponse
import com.github.essmehdi.schoolmate.shared.api.dto.MessageResponse
import com.github.essmehdi.schoolmate.shared.api.dto.PaginatedResponse
import com.github.essmehdi.schoolmate.shared.utils.PrefsManager
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class DocumentsViewModel: ViewModel() {
  private val sortField: MutableLiveData<String> = MutableLiveData("uploadedAt")
  private val sortOrder: MutableLiveData<String> = MutableLiveData("desc")
  val documents: MutableLiveData<List<Document>> = MutableLiveData()
  val currentPageStatus: MutableLiveData<BaseResponse<PaginatedResponse<Document>>> = MutableLiveData()
  val currentPage: MutableLiveData<PaginatedResponse<Document>?> = MutableLiveData()
  val deleteStatus: MutableLiveData<BaseResponse<MessageResponse>?> = MutableLiveData(null)

  val documentTags: MutableLiveData<BaseResponse<List<DocumentTag>>> = MutableLiveData()
  val filterTags: MutableLiveData<Set<DocumentTag>> = MutableLiveData(setOf())
  val oldFilterTags: MutableLiveData<Set<DocumentTag>> = MutableLiveData(setOf())

  val showEmpty: MediatorLiveData<Boolean> = MediatorLiveData()

  val id: MutableLiveData<Long> = MutableLiveData()

  init {
    trackEmpty()
  }

  fun loadDocuments(id: Long? = null) {
    id?.let { this.id.value = it }
    currentPageStatus.value = BaseResponse.Loading()
    if (currentPage.value?.last == true) {
      currentPageStatus.value = BaseResponse.Success(currentPage.value!!)
      return
    }
    viewModelScope.launch {
      // If id is null, then we are loading the current user's documents
      val apiMethod = if (id == null) Api.documentsService.getAllDocuments(
        currentPage.value?.page?.plus(1) ?: 0,
        sort = "${sortField.value},${sortOrder.value}",
        tags = filterTags.value?.map { it.id.toString() }?.toTypedArray() ?: arrayOf()
      ) else Api.documentsService.getOtherUserDocuments(
        id,
        currentPage.value?.page?.plus(1) ?: 0,
        sort = "${sortField.value},${sortOrder.value}",
        tags = filterTags.value?.map { it.id.toString() }?.toTypedArray() ?: arrayOf()
      )

      apiMethod
        .enqueue(object: Callback<PaginatedResponse<Document>> {
          override fun onResponse(
            call: Call<PaginatedResponse<Document>>,
            response: Response<PaginatedResponse<Document>>
          ) {
            if (response.isSuccessful) {
              currentPageStatus.value = BaseResponse.Success(response.body()!!)
              currentPage.value = response.body()!!
              documents.value = (documents.value ?: listOf()).plus(response.body()!!.results)
            } else {
              currentPageStatus.value = BaseResponse.Error(response.code())
            }
          }

          override fun onFailure(call: Call<PaginatedResponse<Document>>, t: Throwable) {
            currentPageStatus.value = BaseResponse.Error(0)
          }
        })
    }
  }

  fun deleteDocument(document: Document) {
    deleteStatus.value = BaseResponse.Loading()
    Api.documentsService.deleteDocument(document.id).enqueue(object: Callback<MessageResponse> {
      override fun onResponse(call: Call<MessageResponse>, response: Response<MessageResponse>) {
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

  fun downloadDocument(document: Document, context: Context) {
    Api.documentsService.downloadDocumentHeaders(document.id).enqueue(object: Callback<Void> {
      override fun onResponse(call: Call<Void>, response: Response<Void>) {
        if (response.isSuccessful) {
          val contentDisposition = response.headers().get("Content-Disposition")
          val filename =
            contentDisposition!!
              .split("filename=")[1]
              .replace("\"", "")
              .trim()

          val request = DownloadManager.Request(Uri.parse("${Api.BASE_URL}documents/${document.id}/file"))
          request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE)
          request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
          request.addRequestHeader("Cookie", PrefsManager.getString(context, "user_cookie"))
          request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, filename)
          (context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager).enqueue(request)
        } else {
          Toast.makeText(context, R.string.unknown_error_occurred, Toast.LENGTH_LONG).show()
        }
      }

      override fun onFailure(call: Call<Void>, t: Throwable) {
        Toast.makeText(context, R.string.unknown_error_occurred, Toast.LENGTH_LONG).show()
      }
    })
  }

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

  fun addFilterTag(tag: DocumentTag) {
    filterTags.value = filterTags.value?.plus(tag)
  }

  fun removeFilterTag(tag: DocumentTag) {
    filterTags.value = filterTags.value?.minus(tag)
  }

  fun clearFilter() {
    filterTags.value = setOf()
  }

  fun refresh() {
    documents.value = listOf()
    currentPage.value = null
    if (id.value == null) {
      loadDocuments()
    } else {
      loadDocuments(id.value)
    }
  }

  fun changeSortField(field: String) {
    sortField.value = field
    refresh()
  }

  fun changeOrder(order: String) {
    sortOrder.value = order
    refresh()
  }

  fun trackEmpty() {
    // Check if showEmpty has already been tracked
    if (showEmpty.hasActiveObservers()) return

    showEmpty.addSource(documents) {
      showEmpty.value = it.isEmpty() && currentPageStatus.value is BaseResponse.Success
    }
    showEmpty.addSource(currentPageStatus) {
      showEmpty.value = documents.value?.isEmpty() == true && it is BaseResponse.Success
    }
  }
}