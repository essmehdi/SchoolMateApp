package com.github.essmehdi.schoolmate.complaints.models

import com.github.essmehdi.schoolmate.users.models.User


data class Handler(val handler: User, var complaintsCount: Int)
