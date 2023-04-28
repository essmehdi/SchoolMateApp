package com.github.essmehdi.schoolmate.auth.api.dto

data class RegisterDto(val firstName: String, val lastName: String, val email: String, val password: String, val confirmation: String)
