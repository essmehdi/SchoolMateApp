package com.github.essmehdi.schoolmate.complaints.api

import com.github.essmehdi.schoolmate.complaints.api.dto.CreateComplaintDto
import com.github.essmehdi.schoolmate.complaints.api.dto.EditComplaintStatusAndHandlerDto
import com.github.essmehdi.schoolmate.complaints.models.Complaint
import com.github.essmehdi.schoolmate.shared.api.dto.MessageResponse
import com.github.essmehdi.schoolmate.shared.api.dto.PaginatedResponse
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.*

interface ComplaintService {
    @POST("complaints")
    fun createComplaint(@Body createComplaintDto: CreateComplaintDto): Call<Complaint>

    @GET("complaints")
    fun getComplaints(
        @Query("page") page: Long = 0,
        @Query("sort") sort: String = "date,desc",
        @Query("type") type: String,
        @Query("user") user: String
    ): Call<PaginatedResponse<Complaint>>

    @GET("complaints/{id}")
    fun getComplaintById(@Path("id") id: Long): Call<Complaint>

    @PATCH("complaints/{id}/handling")
    fun updateComplaintStatusAndHandler(
        @Path("id") id: Long,
        @Body editComplaintStatusAndHandlerDto: EditComplaintStatusAndHandlerDto
    ): Call<Complaint>

    @PATCH("complaints/{id}/details")
    fun updateComplaintDetails(
        @Path("id") id: Long,
        @Body createComplaintDto: CreateComplaintDto
    ): Call<Complaint>

    @DELETE("complaints/{id}")
    fun deleteComplaint(@Path("id") id: Long): Call<MessageResponse>
}