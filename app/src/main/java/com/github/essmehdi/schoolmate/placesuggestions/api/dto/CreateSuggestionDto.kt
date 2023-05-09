package com.github.essmehdi.schoolmate.placesuggestions.api.dto

import com.github.essmehdi.schoolmate.placesuggestions.enumerations.SuggestionType

data class CreateSuggestionDto (
    val description : String,
    val type : SuggestionType,
    val coordinates : List<Double>,)