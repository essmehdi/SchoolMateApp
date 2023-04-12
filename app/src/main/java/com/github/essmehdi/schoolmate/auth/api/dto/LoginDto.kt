package com.github.essmehdi.schoolmate.auth.api.dto

import com.google.gson.annotations.SerializedName

/**
 * DTO for login
 * @param username user's email (yes email :))
 * @param password user's password
 */
data class LoginDto(@SerializedName("username") val username: String, @SerializedName("password") val password: String)
