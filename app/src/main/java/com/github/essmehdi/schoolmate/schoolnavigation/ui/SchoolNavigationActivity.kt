package com.github.essmehdi.schoolmate.schoolnavigation.ui

import android.graphics.Paint
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.preference.PreferenceManager
import com.github.essmehdi.schoolmate.R
import com.github.essmehdi.schoolmate.schoolnavigation.models.SchoolZone
import com.github.essmehdi.schoolmate.shared.api.Api
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Polygon

class SchoolNavigationActivity : AppCompatActivity() {

  private lateinit var map: MapView
  private var schoolZones = emptyList<SchoolZone>()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_school_navigation)

    Configuration.getInstance().load(applicationContext, PreferenceManager.getDefaultSharedPreferences(applicationContext))
    initMap()

    // Fetch school zones
    GlobalScope.launch {
      try {
        val response = Api.schoolZonesService.getAllSchoolZones()
        if (response.isSuccessful) {
          schoolZones = response.body() ?: emptyList()
          populateMap()
        } else {
          Log.e("API Status", response.errorBody().toString())
        }
      } catch (e: Exception) {
        e.printStackTrace()
      }
    }
  }
  
  fun initMap() {
    map = findViewById(R.id.map)
    map.setTileSource(TileSourceFactory.MAPNIK)
    map.setMultiTouchControls(true) // Enables pinch to zoom & other gestures
    map.minZoomLevel = 19.2 // Zoom on ENSIAS
    map.mapOrientation = -37.5f

    val controller = map.controller
    controller.setZoom(19.2)
    // Set center to ENSIAS
    val startPoint = GeoPoint(33.98429267486034, -6.8675806261125905)
    controller.setCenter(startPoint)
  }

  fun populateMap() {
    val polygons: List<Polygon> = schoolZones.map { schoolZone ->
      // Create for each zone a polygon in the map
      val polygon = Polygon(map)
      val geopoints = schoolZone.geometry.points.map { GeoPoint(it.x, it.y) }
      polygon.fillPaint.apply {
        style = Paint.Style.FILL
        strokeWidth = 0F
        color = ResourcesCompat.getColor(resources, R.color.purple_700, theme)
      }
      polygon.points = geopoints
      polygon.title = schoolZone.name
      polygon.subDescription = schoolZone.description
      return@map polygon
    }
    // Draw all polygons in the map overlay
    polygons.forEach {
      map.overlayManager.add(it)
    }
  }

  override fun onResume() {
    super.onResume()
    map.onResume()
  }

  override fun onPause() {
    super.onPause()
    map.onPause()
  }
}