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
  fun getAllSchoolZones(): Call<List<SchoolZone>>

  @GET("school-zones/{id}")
  fun getSchoolZone(id: Long): Call<SchoolZone>

  @POST("school-zones")
  fun createSchoolZone(@Body createSchoolZoneDto: CreateSchoolZoneDto): Call<SchoolZone>

  @PATCH("school-zones/{id}")
  fun editSchoolZone(id: Long, @Body createSchoolZoneDto: CreateSchoolZoneDto): Call<SchoolZone>

  @DELETE("school-zones/{id}")
  fun deleteSchoolZone(id: Long): Call<MessageResponse>
}