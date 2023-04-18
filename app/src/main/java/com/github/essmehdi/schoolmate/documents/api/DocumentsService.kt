package com.github.essmehdi.schoolmate.documents.api

import com.github.essmehdi.schoolmate.documents.api.dto.UploadDocumentDto
import com.github.essmehdi.schoolmate.documents.models.Document
import com.github.essmehdi.schoolmate.documents.models.DocumentTag
import com.github.essmehdi.schoolmate.shared.api.dto.MessageResponse
import com.github.essmehdi.schoolmate.shared.api.dto.PaginatedResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.*

interface DocumentsService {
  @GET("documents")
  fun getAllDocuments(
    @Query("page") page: Long = 0,
    @Query("sort") sort: String = "uploadedAt,desc",
    @Query("tags") vararg tags: String
  ): Call<PaginatedResponse<Document>>

  @POST("documents")
  @Multipart
  fun uploadDocument(@Part("data") uploadDocumentDto: UploadDocumentDto, @Part file: MultipartBody.Part): Call<Document>

  @PATCH("documents/{id}")
  fun editDocument(@Path("id") id: Long, @Body uploadDocumentDto: UploadDocumentDto): Call<Document>

  @HEAD("documents/{id}/file")
  fun downloadDocumentHeaders(@Path("id") id: Long): Call<Void>

  @DELETE("documents/{id}")
  fun deleteDocument(@Path("id") id: Long): Call<MessageResponse>

  @GET("documents/tags")
  fun getDocumentTags(): Call<List<DocumentTag>>
}