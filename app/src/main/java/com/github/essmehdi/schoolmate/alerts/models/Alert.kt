package com.github.essmehdi.schoolmate.alerts.models

import android.graphics.Point
import com.github.essmehdi.schoolmate.auth.models.User

data class Alert(val id: Long,
                 val user: User,
                 val title: String,
                 val description: String,
                 val type: AlertType,
                 val coordinates: Point,
                 val status: AlertStatus)
