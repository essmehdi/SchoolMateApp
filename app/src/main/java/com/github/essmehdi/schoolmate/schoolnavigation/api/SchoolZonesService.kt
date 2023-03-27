package com.github.essmehdi.schoolmate.schoolnavigation.api

import com.github.essmehdi.schoolmate.schoolnavigation.api.dto.CreateSchoolZoneDto
import com.github.essmehdi.schoolmate.schoolnavigation.models.SchoolZone
import com.github.essmehdi.schoolmate.shared.api.dto.MessageResponse
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST

interface SchoolZonesService {
  @GET("school-zones")
  suspend fun getAllSchoolZones(): Response<List<SchoolZone>>

  @GET("school-zones/{id}")
  suspend fun getSchoolZone(id: Long): Response<SchoolZone>

  @POST("school-zones")
  suspend fun createSchoolZone(@Body createSchoolZoneDto: CreateSchoolZoneDto): Response<SchoolZone>

  @PATCH("school-zones/{id}")
  suspend fun editSchoolZone(id: Long, @Body createSchoolZoneDto: CreateSchoolZoneDto): Response<SchoolZone>

  @DELETE("school-zones/{id}")
  suspend fun deleteSchoolZone(id: Long): Response<MessageResponse>
}