package com.github.essmehdi.schoolmate.users.api

import com.github.essmehdi.schoolmate.users.models.User
import retrofit2.Call
import retrofit2.http.GET

interface UsersService {
  @GET("users")
  fun getAllUsers(): Call<List<User>>

  @GET("users/{id}")
  fun getUserById(): Call<User>
}