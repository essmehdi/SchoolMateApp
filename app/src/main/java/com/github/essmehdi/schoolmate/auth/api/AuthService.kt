package com.github.essmehdi.schoolmate.auth.api

import com.github.essmehdi.schoolmate.auth.api.dto.LoginDto
import com.github.essmehdi.schoolmate.auth.api.dto.LoginResponse
import com.github.essmehdi.schoolmate.auth.api.dto.RegisterDto
import com.github.essmehdi.schoolmate.users.models.User
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.POST

interface AuthService {
  @POST("login")
  @Headers("Content-Type: application/json")
  fun login(@Body loginDto: LoginDto): Call<LoginResponse>

  @POST("users")
  @Headers("Content-Type: application/json")
  fun register(@Body registerDto: RegisterDto): Call<User>

  @GET("me")
  fun me(): Call<User>


}