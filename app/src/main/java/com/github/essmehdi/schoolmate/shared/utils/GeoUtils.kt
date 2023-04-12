package com.github.essmehdi.schoolmate.shared.utils

import org.osmdroid.util.GeoPoint

object GeoUtils {
  fun centerOfPolygon(points: List<GeoPoint>): GeoPoint {
    var size = points.size
    var x = points.get(0).latitude
    var y = points.get(0).longitude
    points.forEachIndexed { index, geoPoint ->
      if (index != 0) {
        x += geoPoint.latitude
        y += geoPoint.longitude
      }
    }
    return GeoPoint(x / size, y / size)
  }
}