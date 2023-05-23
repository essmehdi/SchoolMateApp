package com.github.essmehdi.schoolmate.alerts.models

import java.io.Serializable

enum class AlertStatus : Serializable {
    PENDING,
    CONFIRMED,
    CANCELLED
}