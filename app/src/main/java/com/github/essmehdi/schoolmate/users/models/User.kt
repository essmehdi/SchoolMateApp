package com.github.essmehdi.schoolmate.users.models

import java.io.Serializable

data class User(val id: Long, val firstName: String, val lastName: String, val email: String, val role: UserRole, val active: Boolean) :
  Serializable {
  val fullName: String
    get() = "$firstName $lastName"
}
