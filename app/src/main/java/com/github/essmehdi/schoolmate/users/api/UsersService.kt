package com.github.essmehdi.schoolmate.users.api

import com.github.essmehdi.schoolmate.auth.models.User
import com.github.essmehdi.schoolmate.shared.api.dto.PaginatedResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface UsersService {

    @GET("users")
    fun getUsers(
        @Query("page") page: Long = 0,
        @Query("sort") sort: String = "date,desc",
        @Query("search") search: String? = null,
        @Query("email") email: String? = null,
        @Query("role") role: String? = null
    ): Call<PaginatedResponse<User>>


}