package com.github.essmehdi.schoolmate.complaints.api

import com.github.essmehdi.schoolmate.complaints.api.dto.*
import com.github.essmehdi.schoolmate.complaints.enumerations.ComplaintStatus
import com.github.essmehdi.schoolmate.complaints.models.Complaint
import com.github.essmehdi.schoolmate.shared.api.dto.MessageResponse
import com.github.essmehdi.schoolmate.shared.api.dto.PaginatedResponse
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.*

interface ComplaintService {
    // 3 overloads for createComplaint to handle different types of complaints
    @POST("complaints")
    fun createComplaint(@Body createRoomComplaintDto: CreateRoomComplaintDto): Call<Complaint>

    @POST("complaints")
    fun createComplaint(@Body createBuildingComplaintDto: CreateBuildingComplaintDto): Call<Complaint>

    @POST("complaints")
    fun createComplaint(@Body createFacilityComplaintDto: CreateFacilityComplaintDto): Call<Complaint>
    // ---------------------------------------------------------------

    @GET("complaints")
    fun getComplaints(
        @Query("page") page: Long = 0,
        @Query("sort") sort: String = "date,desc",
        @Query("type") type: String,
        @Query("user") user: String
    ): Call<PaginatedResponse<Complaint>>

    @GET("complaints-by-status")
    fun getComplaintsByStatusAndHandler(
        @Query("page") page: Long = 0,
        @Query("sort") sort: String = "date,desc",
        @Query("status") status: ComplaintStatus?,
        @Query("user") handler: String?,
        @Query("type") type: String?
    ): Call<PaginatedResponse<Complaint>>

    @GET("complaints/count-by-handler/{id}")
    fun getComplaintsCountByHandler(@Path("id") id: Long): Call<Int>


    @GET("complaints/{id}")
    fun getComplaintById(@Path("id") id: Long): Call<Complaint>

    @PATCH("complaints/{id}/handling")
    fun updateComplaintStatusAndHandler(
        @Path("id") id: Long,
        @Body editComplaintStatusAndHandlerDto: EditComplaintStatusAndHandlerDto
    ): Call<Complaint>

    // 3 overloads for updateComplaintDetails to handle different types of complaints
    @PATCH("complaints/{id}/details")
    fun updateComplaintDetails(
        @Path("id") id: Long,
        @Body createRoomComplaintDto: CreateRoomComplaintDto
    ): Call<Complaint>

    @PATCH("complaints/{id}/details")
    fun updateComplaintDetails(
        @Path("id") id: Long,
        @Body createBuildingComplaintDto: CreateBuildingComplaintDto
    ): Call<Complaint>

    @PATCH("complaints/{id}/details")
    fun updateComplaintDetails(
        @Path("id") id: Long,
        @Body createFacilityComplaintDto: CreateFacilityComplaintDto
    ): Call<Complaint>
    // ---------------------------------------------------------------

    @DELETE("complaints/{id}")
    fun deleteComplaint(@Path("id") id: Long): Call<MessageResponse>
}