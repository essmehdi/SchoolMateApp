package com.github.essmehdi.schoolmate.shared.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.ViewGroup
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.preference.PreferenceManager
import com.github.essmehdi.schoolmate.R
import com.github.essmehdi.schoolmate.databinding.ActivityMapSelectorBinding
import com.github.essmehdi.schoolmate.schoolnavigation.models.Point
import com.github.essmehdi.schoolmate.shared.utils.GeoUtils
import com.github.essmehdi.schoolmate.shared.viewmodels.MapSelectorViewModel
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.MapQuestTileSource
import org.osmdroid.tileprovider.tilesource.OnlineTileSourceBase
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.util.MapTileIndex
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polygon
import org.osmdroid.views.overlay.infowindow.MarkerInfoWindow

/**
 * This activity is used to select a point or points on the map
 * and return the selected point(s) to the caller activity.
 * The intent should contain the following extras:
 * - `singleMode` (boolean): whether to select a single point or multiple points
 * - `defaultPoints` (ArrayList<Point>): the default point(s) to be selected
 */
class MapSelectorActivity : AppCompatActivity() {

  companion object {
    const val EXTRA_SINGLE_MODE = "singleMode"
    const val EXTRA_DEFAULT_POINTS = "defaultPoints"
    const val EXTRA_RESULT_SINGLE_POINT = "point"
    const val EXTRA_RESULT_MULTIPLE_POINTS = "points"
  }

  private lateinit var binding: ActivityMapSelectorBinding
  private lateinit var map: MapView
  private val viewModel: MapSelectorViewModel by viewModels()
  private var singleMode = false

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    binding = ActivityMapSelectorBinding.inflate(layoutInflater)
    setContentView(binding.root)

    Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this));

    WindowCompat.setDecorFitsSystemWindows(window, false)
    WindowCompat.getInsetsController(window, window.decorView).apply {
      isAppearanceLightNavigationBars = true
      isAppearanceLightStatusBars = true
    }

    listOf(
      binding.mapSelectorNoticeCard,
      binding.mapSelectorBackButton
    ).forEach {
      ViewCompat.setOnApplyWindowInsetsListener(it) { view, windowInsets ->
        val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
        view.updateLayoutParams<ViewGroup.MarginLayoutParams> {
          topMargin = insets.top + 5
        }

        WindowInsetsCompat.CONSUMED
      }
    }

    // Check if the activity is in single mode
    if (intent.hasExtra(EXTRA_SINGLE_MODE)) {
      singleMode = intent.getBooleanExtra(EXTRA_SINGLE_MODE, false)
    }

    // Change the notice text if the activity is in single mode
    if (singleMode) {
      binding.mapSelectorNoticeText.text = getString(R.string.school_zone_selector_notice_single_mode)
    }

    // Check if there are default points
    if (intent.hasExtra(EXTRA_DEFAULT_POINTS)) {
      val defaultPoints = intent.getSerializableExtra(EXTRA_DEFAULT_POINTS)
      if (defaultPoints is ArrayList<*>) {
        viewModel.points.value = defaultPoints.filterIsInstance<Point>()
        if (defaultPoints.size > 1 && singleMode) {
          Log.e("MapSelectorActivity", "Multiple points passed to single mode activity")
          setResult(RESULT_CANCELED)
          finish()
        }
      }
    }

    // Initialize the map
    initMap()

    binding.mapSelectorBackButton.setOnClickListener {
      onBackPressedDispatcher.onBackPressed()
    }

    binding.mapSelectorAddFab.setOnClickListener {
      addPoint()
    }

    binding.mapSelectorResetFab.setOnClickListener {
      viewModel.clearPoints()
    }

    binding.mapSelectorConfirmFab.setOnClickListener {
      viewModel.points.value?.let {
        if (it.isNotEmpty()) {
          setResult(RESULT_OK, Intent().apply {
            putExtra(
              if (singleMode) EXTRA_RESULT_SINGLE_POINT else EXTRA_RESULT_MULTIPLE_POINTS,
              if (singleMode) arrayListOf(it[0]) else arrayListOf(*it.toTypedArray())
            )
          })
          finish()
        }
      }
    }

    viewModel.points.observe(this) {
      binding.mapSelectorConfirmFab.isEnabled = it.isNotEmpty()
      binding.mapSelectorResetFab.isEnabled = it.isNotEmpty()
      populateMap()
    }
  }

  /**
   * Fill the map with selected points
   */
  private fun populateMap() {
    map.overlays.clear()

    // Create the polygon if the activity is not in single mode
    if (!singleMode && viewModel.points.value!!.size > 2) {
      val polygon = Polygon().apply {
        points = viewModel.points.value!!.map { GeoPoint(it.x, it.y) }
        fillPaint.apply {
          color = ResourcesCompat.getColor(resources, R.color.primary_transparent, theme)
        }
        outlinePaint.apply {
          color = ResourcesCompat.getColor(resources, R.color.primary_variant, theme)
          strokeWidth = 1f
        }
      }
      map.overlays.add(polygon)
    }

    // Create point marker
    viewModel.points.value?.forEach {
      val marker = Marker(map)
      marker.id = it.hashCode().toString()
      marker.position = GeoPoint(it.x, it.y)
      marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
      marker.setOnMarkerClickListener { clickedMarker, _ ->
        viewModel.removePointByHash(clickedMarker.id.toInt())
        true
      }
      map.overlays.add(marker)
    }

    map.invalidate()
  }

  /**
   * Initializes the map
   */
  private fun initMap() {
    map = binding.map
    map.apply {
      setTileSource(TileSourceFactory.MAPNIK)
      setMultiTouchControls(true) // Enables pinch to zoom & other gestures
    }

    map.controller.apply {
      setZoom(19.2)
      viewModel.points.value!!.let {
        val center = if (it.isNotEmpty()) {
          // Set center to the center of the default polygon
          GeoUtils.centerOfPolygon(it.map { point -> GeoPoint(point.x, point.y) })
        } else {
          // Set center to ENSIAS
          GeoPoint(33.98429267486034, -6.8675806261125905)
        }
        setCenter(center)
      }
    }
  }

  /**
   * Adds a point to the selected points
   */
  private fun addPoint() {
    viewModel.addPointFromGeoPoint(map.mapCenter, singleMode)
  }
}