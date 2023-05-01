package com.github.essmehdi.schoolmate.complaints.api

import com.github.essmehdi.schoolmate.complaints.api.dto.CreateComplaintDto
import com.github.essmehdi.schoolmate.complaints.api.dto.EditComplaintStatusAndHandlerDto
import com.github.essmehdi.schoolmate.complaints.models.Complaint
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST

interface ComplaintService {
    @POST("complaints")
    fun createComplaint(@Body createComplaintDto: CreateComplaintDto): Call<Response<Complaint>>

    @GET("complaints")
    fun getComplaints(
        type: @ParameterName(name = "type") String,
        user: @ParameterName(name = "user") String
    ): Call<Response<List<Complaint>>>

    @GET("complaints/{id}")
    fun getComplaintById(id: @ParameterName(name = "id") Long): Call<Complaint>

    @PATCH("complaints/{id}/handling")
    fun updateComplaintStatusAndHandler(
        id: @ParameterName(name = "id") Long,
        @Body editComplaintStatusAndHandlerDto: EditComplaintStatusAndHandlerDto
    ): Call<Complaint>

    @PATCH("complaints/{id}/details")
    fun updateComplaintDetails(
        id: @ParameterName(name = "id") Long,
        @Body createComplaintDto: CreateComplaintDto
    ): Call<Complaint>

    @DELETE("complaints/{id}")
    fun deleteComplaint(id: @ParameterName(name = "id") Long): Call<Void>
}