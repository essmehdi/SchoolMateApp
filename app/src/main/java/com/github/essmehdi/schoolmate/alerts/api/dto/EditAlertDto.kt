package com.github.essmehdi.schoolmate.alerts.api.dto


import com.github.essmehdi.schoolmate.alerts.models.AlertType
import com.github.essmehdi.schoolmate.schoolnavigation.models.Point

data class EditAlertDto(
    val title: String?,
    val description: String?,
    val type: AlertType?,
    val coordinates: List<Double>?,
)


