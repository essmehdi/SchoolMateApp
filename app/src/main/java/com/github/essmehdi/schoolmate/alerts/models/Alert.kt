package com.github.essmehdi.schoolmate.alerts.models


import com.github.essmehdi.schoolmate.users.models.User
import java.io.Serializable

data class Alert(val id: Long,
                 val user: User,
                 val title: String,
                 val description: String,
                 val type: AlertType,
                 val coordinates: List<Double>,
                 val status: AlertStatus,
                 val date: String):Serializable

data class Point(val x: Double, val y: Double):Serializable
