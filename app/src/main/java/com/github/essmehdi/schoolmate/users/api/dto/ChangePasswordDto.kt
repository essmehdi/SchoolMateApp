package com.github.essmehdi.schoolmate.users.api.dto

data class ChangePasswordDto(val oldPassword: String, val password: String, val confirmation: String)
