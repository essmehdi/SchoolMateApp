package com.github.essmehdi.schoolmate.alerts.api.dto

import com.github.essmehdi.schoolmate.alerts.models.AlertStatus
import com.github.essmehdi.schoolmate.alerts.models.AlertType

data class AlertDto(
    val title:String,
    val description:String,
    val type:AlertType,
    val coordinates:List<Double>,
    val status:AlertStatus
)
