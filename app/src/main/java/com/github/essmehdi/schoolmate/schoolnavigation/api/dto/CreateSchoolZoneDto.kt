package com.github.essmehdi.schoolmate.schoolnavigation.api.dto

data class CreateSchoolZoneDto(val name: String, val description: String, val geometry: List<List<Double>>)
