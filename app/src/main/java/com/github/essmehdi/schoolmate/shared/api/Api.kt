package com.github.essmehdi.schoolmate.shared.api

import com.github.essmehdi.schoolmate.schoolnavigation.api.SchoolZonesService
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object Api {

  private const val BASE_URL = "http://100.91.177.135:9080/schoolmate/api/"

  private val retrofit = Retrofit.Builder()
    .addConverterFactory(GsonConverterFactory.create())
    .baseUrl(BASE_URL)
    .build()

  val schoolZonesService: SchoolZonesService by lazy {
    retrofit.create(SchoolZonesService::class.java)
  }
}