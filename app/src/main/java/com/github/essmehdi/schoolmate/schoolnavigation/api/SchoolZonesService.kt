package com.github.essmehdi.schoolmate.schoolnavigation.api

import com.github.essmehdi.schoolmate.schoolnavigation.api.dto.CreateSchoolZoneDto
import com.github.essmehdi.schoolmate.schoolnavigation.api.dto.EditSchoolZoneDto
import com.github.essmehdi.schoolmate.schoolnavigation.models.SchoolZone
import com.github.essmehdi.schoolmate.shared.api.dto.MessageResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

interface SchoolZonesService {
  @GET("school-zones")
  fun getAllSchoolZones(): Call<List<SchoolZone>>

  @POST("school-zones")
  fun createSchoolZone(@Body createSchoolZoneDto: CreateSchoolZoneDto): Call<SchoolZone>

  @PATCH("school-zones/{id}")
  fun editSchoolZone(@Path("id") id: Long, @Body editSchoolZoneDto: EditSchoolZoneDto): Call<SchoolZone>

  @DELETE("school-zones/{id}")
  fun deleteSchoolZone(@Path("id") id: Long): Call<MessageResponse>
}