package com.github.essmehdi.schoolmate.users.models

import androidx.annotation.StringRes
import com.github.essmehdi.schoolmate.R
import com.google.gson.annotations.SerializedName

data class User(val id: Long, val firstName: String, val lastName: String, val email: String, val role: UserRole, val active: Boolean) {
  val fullName: String
    get() = "$firstName $lastName"

  @get:StringRes
  val roleNameString: Int
    get() = when (role) {
      UserRole.STUDENT -> R.string.user_role_student
      UserRole.ADEI -> R.string.user_role_adei
      UserRole.MODERATOR -> R.string.user_role_moderator
    }
}
