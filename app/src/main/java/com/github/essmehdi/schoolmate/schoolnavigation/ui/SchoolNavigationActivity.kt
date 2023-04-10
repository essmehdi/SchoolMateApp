package com.github.essmehdi.schoolmate.schoolnavigation.ui

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Paint
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.preference.PreferenceManager
import com.github.essmehdi.schoolmate.R
import com.github.essmehdi.schoolmate.schoolnavigation.models.SchoolZone
import com.github.essmehdi.schoolmate.shared.api.Api
import com.github.essmehdi.schoolmate.shared.utils.GeoUtils
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.mapsforge.core.graphics.Color
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polygon
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay

class SchoolNavigationActivity : AppCompatActivity() {

  private lateinit var map: MapView
  private var schoolZones = emptyList<SchoolZone>()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_school_navigation)

    // Force light mode temporarily
    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

    // Initialize map
    Configuration.getInstance().load(applicationContext, PreferenceManager.getDefaultSharedPreferences(applicationContext))
    initMap()

    // Permission asker
    val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) {
      isGranted: Boolean ->
        if (isGranted) {
          enableLocation()
        } else {
          AlertDialog.Builder(this@SchoolNavigationActivity).apply {
            setTitle(R.string.school_navigation_location_permission_not_granted_alert_title)
            setMessage(R.string.school_navigation_location_permission_not_granted_alert_message)
            setNeutralButton(R.string.school_navigation_location_permission_not_granted_alert_neutral_button_text) { dialogInterface, _ ->
              dialogInterface.dismiss()
            }
            create().show()
          }
        }
    }

    // Check for permissions
    when {
      ContextCompat.checkSelfPermission(
        this,
        Manifest.permission.ACCESS_FINE_LOCATION
      ) == PackageManager.PERMISSION_GRANTED -> {
        enableLocation()
      }
      ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION) -> {
        AlertDialog.Builder(this@SchoolNavigationActivity).apply {
          setTitle(R.string.school_navigation_location_permission_educational_title)
          setMessage(R.string.school_navigation_location_permission_educational_message)
          setCancelable(false)
          setNegativeButton(R.string.school_navigation_location_permission_educational_negative_button_text) { dialogInterface, _ ->
            dialogInterface.dismiss()
          }
          setPositiveButton(R.string.school_navigation_location_permission_educational_positive_button_text) { dialogInterface, _ ->
            dialogInterface.dismiss()
            requestPermissionLauncher.launch(
              Manifest.permission.ACCESS_FINE_LOCATION)
          }
          create().show()
        }
      }
      else -> {
        requestPermissionLauncher.launch(
          Manifest.permission.ACCESS_FINE_LOCATION)
      }
    }

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
    // Create for each zone a polygon in the map
    val polygons: List<Polygon> = schoolZones.map { schoolZone ->
      // Map coordinates to GeoPoints
      val geopoints = schoolZone.geometry.points.map { GeoPoint(it.x, it.y) }

      val polygon = Polygon()
      polygon.apply {
        setOnClickListener(Polygon.OnClickListener { _, mapView, eventPos ->
          val fragment = SchoolZoneDetailsFragment.newInstance(schoolZone.name, schoolZone.description)
          fragment.show(supportFragmentManager, fragment.tag)

          return@OnClickListener true
        })
        fillColor = ResourcesCompat.getColor(resources, R.color.zone, theme)
        strokeWidth = 1f
        points = geopoints
        title = schoolZone.name
      }

      return@map polygon
    }
    // Draw all polygons in the map overlay
    polygons.forEach {
      // Text marker for the polygon
      val marker = Marker(map)
      marker.position = GeoUtils.centerOfPolygon(it.actualPoints)
      marker.title = it.title
      marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
      marker.textLabelBackgroundColor = Color.TRANSPARENT.ordinal
      marker.setTextIcon(it.title)

      map.overlayManager.addAll(listOf(it, marker))
    }
  }

  fun enableLocation() {
    Toast.makeText(this, "Enabling location...", Toast.LENGTH_SHORT).show()
    val locationOverlay = MyLocationNewOverlay(GpsMyLocationProvider(this), map)
    locationOverlay.enableMyLocation()
    map.overlayManager.add(locationOverlay)
  }

  fun showMessageDialog(@StringRes title: Int, @StringRes message: Int, @StringRes neutralButtonText: Int) {
    AlertDialog.Builder(this@SchoolNavigationActivity).apply {
      setTitle(title)
      setMessage(message)
      setNeutralButton(neutralButtonText) { dialogInterface, _ ->
        dialogInterface.dismiss()
      }
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