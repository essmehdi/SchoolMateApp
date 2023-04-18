package com.github.essmehdi.schoolmate.documents.viewmodels

import android.annotation.SuppressLint
import android.content.ContentResolver
import android.net.Uri
import android.provider.OpenableColumns
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.essmehdi.schoolmate.documents.api.dto.UploadDocumentDto
import com.github.essmehdi.schoolmate.documents.models.Document
import com.github.essmehdi.schoolmate.documents.models.DocumentTag
import com.github.essmehdi.schoolmate.shared.api.Api
import com.github.essmehdi.schoolmate.shared.api.BaseResponse
import kotlinx.coroutines.launch
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.ByteArrayOutputStream

class DocumentEditorViewModel: ViewModel() {
  val editMode: MutableLiveData<Boolean> = MutableLiveData(false)
  val editId: MutableLiveData<Long?> = MutableLiveData(null)
  val uploadStatus: MutableLiveData<BaseResponse<Document>> = MutableLiveData()
  val documentTags: MutableLiveData<BaseResponse<List<DocumentTag>>> = MutableLiveData()
  val selectedTags: MutableLiveData<Set<Long>> = MutableLiveData(mutableSetOf())
  val selectedFile: MutableLiveData<Uri> = MutableLiveData()

  private val requestCallback = object : Callback<Document> {
    override fun onResponse(call: Call<Document>, response: Response<Document>) {
      if (response.isSuccessful) {
        uploadStatus.value = BaseResponse.Success(response.body()!!)
      } else {
        uploadStatus.value = BaseResponse.Error(response.code())
      }
    }

    override fun onFailure(call: Call<Document>, t: Throwable) {
      uploadStatus.value = BaseResponse.Error(0)
    }
  }

  fun selectTags(vararg ids: Long) {
    selectedTags.value = selectedTags.value!!.plus(ids.toList())
  }

  fun selectTag(id: Long) {
    selectedTags.value = selectedTags.value!!.plus(id)
  }

  fun removeTag(id: Long) {
    selectedTags.value = selectedTags.value!!.minus(id)
  }

  fun uploadDocument(uploadDocumentDto: UploadDocumentDto, filename: String, mediaType: MediaType, file: ByteArray) {
    uploadStatus.value = BaseResponse.Loading()
    val bodyFile = RequestBody.create(mediaType, file)
    val body = MultipartBody.Part.createFormData("file", filename, bodyFile)
    viewModelScope.launch {
      Api.documentsService.uploadDocument(uploadDocumentDto, body).enqueue(requestCallback)
    }
  }

  fun editDocument(uploadDocumentDto: UploadDocumentDto) {
    Api.documentsService.editDocument(editId.value!!, uploadDocumentDto).enqueue(requestCallback)
  }

  fun fetchDocumentTags() {
    documentTags.value = BaseResponse.Loading()
    Api.documentsService.getDocumentTags().enqueue(object: Callback<List<DocumentTag>> {
      override fun onResponse(
        call: Call<List<DocumentTag>>,
        response: Response<List<DocumentTag>>
      ) {
        if (response.isSuccessful) {
          documentTags.value = BaseResponse.Success(response.body()!!)
        } else {
          documentTags.value = BaseResponse.Error(response.code())
        }
      }

      override fun onFailure(call: Call<List<DocumentTag>>, t: Throwable) {
        documentTags.value = BaseResponse.Error(0)
      }
    })
  }

  @SuppressLint("Range")
  fun getFileName(contentResolver: ContentResolver): String {
    val cursor = contentResolver.query(selectedFile.value!!, null, null, null, null)
    cursor.use {
      if (cursor != null && cursor.moveToFirst()) {
        return cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME))
      } else {
        return ""
      }
    }
  }

  fun getFileContent(contentResolver: ContentResolver): ByteArray {
    val inputStream = contentResolver.openInputStream(selectedFile.value!!)
    val byteBuffer = ByteArrayOutputStream()
    val bufferSize = 1024
    val buffer = ByteArray(bufferSize)
    var len = 0
    while (inputStream!!.read(buffer).also { len = it } != -1) {
      byteBuffer.write(buffer, 0, len)
    }
    inputStream.close()
    return byteBuffer.toByteArray()
  }

  fun getFileMediaType(contentResolver: ContentResolver): String {
    return contentResolver.getType(selectedFile.value!!) ?: "application/octet-stream"
  }
}