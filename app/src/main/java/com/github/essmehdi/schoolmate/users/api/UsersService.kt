package com.github.essmehdi.schoolmate.users.api

import com.github.essmehdi.schoolmate.shared.api.dto.MessageResponse
import com.github.essmehdi.schoolmate.shared.api.dto.PaginatedResponse
import com.github.essmehdi.schoolmate.users.api.dto.ChangePasswordDto
import com.github.essmehdi.schoolmate.users.api.dto.ChangePrivilegeDto
import com.github.essmehdi.schoolmate.users.api.dto.EditUserDto
import com.github.essmehdi.schoolmate.users.models.User
import com.github.essmehdi.schoolmate.users.models.UserRole
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.Path
import retrofit2.http.Query

interface UsersService {
  @GET("users")
  fun getAllUsers(
    @Query("page") page: Long? = 0,
    @Query("size") size: Int? = 30,
    @Query("sort") sort: String? = "lastName,desc",
    @Query("email") email: String? = null,
    @Query("search") search: String? = null,
    @Query("role") role: UserRole? = null
  ): Call<PaginatedResponse<User>>

  @GET("users/{id}")
  fun getUserById(@Path("id") id: Long): Call<User>

  @PATCH("users/{id}")
  fun changeUserPrivilege(@Path("id") id: Long, @Body changePrivilegeDto: ChangePrivilegeDto): Call<User>

  @PATCH("users/{id}")
  fun editProfileData(@Path("id") id: Long, @Body editUserDto: EditUserDto): Call<User>

  @PATCH("me/reset-password")
  fun changePassword(@Body changePasswordDto: ChangePasswordDto): Call<MessageResponse>
}