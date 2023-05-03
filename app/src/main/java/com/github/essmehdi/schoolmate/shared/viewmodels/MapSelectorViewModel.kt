package com.github.essmehdi.schoolmate.shared.viewmodels

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.github.essmehdi.schoolmate.schoolnavigation.models.Point
import org.osmdroid.api.IGeoPoint

class MapSelectorViewModel: ViewModel() {
  val points: MutableLiveData<List<Point>> = MutableLiveData(listOf())

  fun addPointFromGeoPoint(point: IGeoPoint, singleMode: Boolean) {
    if (singleMode) {
      points.value = listOf(Point(point.latitude, point.longitude))
    } else {
      points.value = points.value?.plus(Point(point.latitude, point.longitude))
    }
  }

  fun removePointByHash(hash: Int) {
    Log.d("MapSelectorViewModel", "Removing point with hash $hash")
    points.value = points.value?.filter { it.hashCode() != hash }
  }

  fun clearPoints() {
    points.value = listOf()
  }
}