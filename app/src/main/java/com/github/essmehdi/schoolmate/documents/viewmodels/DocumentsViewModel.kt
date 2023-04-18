package com.github.essmehdi.schoolmate.documents.viewmodels

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.essmehdi.schoolmate.R
import com.github.essmehdi.schoolmate.documents.models.Document
import com.github.essmehdi.schoolmate.shared.api.Api
import com.github.essmehdi.schoolmate.shared.api.BaseResponse
import com.github.essmehdi.schoolmate.shared.api.dto.PaginatedResponse
import com.github.essmehdi.schoolmate.shared.utils.PrefsManager
import com.github.essmehdi.schoolmate.shared.utils.Utils
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class DocumentsViewModel: ViewModel() {
  private val sortField: MutableLiveData<String> = MutableLiveData("uploadedAt")
  private val sortOrder: MutableLiveData<String> = MutableLiveData("desc")
  val documents: MutableLiveData<List<Document>> = MutableLiveData()
  val currentPage: MutableLiveData<BaseResponse<PaginatedResponse<Document>>?> = MutableLiveData()

  fun loadDocuments() {
    if (currentPage.value?.data?.last == true) {
      return
    }
    currentPage.value = BaseResponse.Loading()
    viewModelScope.launch {
      Api.documentsService.getAllDocuments(currentPage.value?.data?.page?.plus(1) ?: 0, sort = "${sortField.value},${sortOrder.value}").enqueue(object: Callback<PaginatedResponse<Document>> {
        override fun onResponse(
          call: Call<PaginatedResponse<Document>>,
          response: Response<PaginatedResponse<Document>>
        ) {
          if (response.isSuccessful) {
            currentPage.value = BaseResponse.Success(response.body()!!)
            documents.value = (documents.value ?: listOf()).plus(response.body()!!.results)
          } else {
            currentPage.value = BaseResponse.Error(response.code())
          }
        }

        override fun onFailure(call: Call<PaginatedResponse<Document>>, t: Throwable) {
          currentPage.value = BaseResponse.Error(0)
        }
      })
    }
  }

  fun deleteDocument(document: Document) {

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

  fun refresh() {
    currentPage.value = null
    documents.value = listOf()
    loadDocuments()
  }

  fun changeSortField(field: String) {
    sortField.value = field
    refresh()
  }

  fun changeOrder(order: String) {
    sortOrder.value = order
    refresh()
  }
}