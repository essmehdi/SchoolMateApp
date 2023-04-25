package com.github.essmehdi.schoolmate.alerts.api.dto

import android.graphics.Point
import com.github.essmehdi.schoolmate.alerts.models.AlertType

data class EditAlertDto(
    val title: String?,
    val description: String?,
    val type: AlertType?,
    val coordinates: Point?
)
