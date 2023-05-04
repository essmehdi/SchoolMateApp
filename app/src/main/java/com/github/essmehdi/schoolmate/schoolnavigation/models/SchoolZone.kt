package com.github.essmehdi.schoolmate.schoolnavigation.models

import java.io.Serializable

data class SchoolZone(val id: Long, val name: String, val description: String, val geometry: Geometry): Serializable
data class Geometry(val points: List<Point>): Serializable
data class Point(val x: Double, val y: Double): Serializable
