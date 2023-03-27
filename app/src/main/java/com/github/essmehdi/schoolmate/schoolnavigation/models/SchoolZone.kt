package com.github.essmehdi.schoolmate.schoolnavigation.models

data class SchoolZone(val id: Long, val name: String, val description: String, val geometry: Geometry)
data class Geometry(val points: List<Point>)
data class Point(val x: Double, val y: Double)
