package com.github.essmehdi.schoolmate.auth.models

data class User(val id: Long, val firstName: String, val lastName: String, val email: String, val active: Boolean){
    val fullName: String
        get() = "$firstName $lastName"
}
