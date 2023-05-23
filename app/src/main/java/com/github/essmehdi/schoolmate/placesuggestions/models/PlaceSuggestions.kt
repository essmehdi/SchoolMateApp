package com.github.essmehdi.schoolmate.placesuggestions.models

import com.github.essmehdi.schoolmate.placesuggestions.enumerations.SuggestionType
import com.github.essmehdi.schoolmate.users.models.User

data class PlaceSuggestions (
    val id: Long,
    val user: User,
    val description: String,
    val suggestiontype: SuggestionType,
    val date: String,
    val coordinates: Point)

data class Point (val x: Double, val y: Double)