package com.github.essmehdi.schoolmate.schoolnavigation.ui

import android.Manifest
import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.ViewGroup.MarginLayoutParams
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.preference.PreferenceManager
import com.github.essmehdi.schoolmate.R
import com.github.essmehdi.schoolmate.databinding.ActivitySchoolNavigationBinding
import com.github.essmehdi.schoolmate.schoolnavigation.viewmodels.SchoolNavigationViewModel
import com.github.essmehdi.schoolmate.shared.api.BaseResponse
import com.github.essmehdi.schoolmate.shared.utils.GeoUtils
import org.mapsforge.core.graphics.Color
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay

class SchoolNavigationActivity : AppCompatActivity() {

  private lateinit var map: MapView
  private val viewModel: SchoolNavigationViewModel by viewModels()
  private lateinit var binding: ActivitySchoolNavigationBinding

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    binding = ActivitySchoolNavigationBinding.inflate(layoutInflater)
    setContentView(binding.root)

    WindowCompat.setDecorFitsSystemWindows(window, false)
    WindowCompat.getInsetsController(window, window.decorView).apply {
      isAppearanceLightNavigationBars = true
      isAppearanceLightStatusBars = true
    }

    listOf(
      binding.schoolNavigationBackButton,
      binding.schoolNavigationRefreshCard,
      binding.schoolNavigationEditButton
    ).forEach {
      ViewCompat.setOnApplyWindowInsetsListener(it) { view, windowInsets ->
        val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
        view.updateLayoutParams<MarginLayoutParams> {
          topMargin = insets.top + 5
        }

        WindowInsetsCompat.CONSUMED
      }
    }

    // Force light mode temporarily
    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

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

    viewModel.fetchStatus.observe(this) {
      showLoadingIndicator(it is BaseResponse.Loading)
      if (it is BaseResponse.Error) {
        handleError(it.code!!)
      }
    }

    viewModel.schoolZonesPolygons.observe(this) { populateMap() }

    binding.schoolNavigationBackButton.setOnClickListener {
      onBackPressedDispatcher.onBackPressed()
    }

    binding.schoolNavigationEditButton.setOnClickListener {
      startActivity(Intent(this, SchoolZonesActivity::class.java))
    }
  }

  private fun showLoadingIndicator(show: Boolean = true) {
    Log.d("SchoolNavActivity", "Show loading indicator: $show")
    binding.schoolNavigationRefreshCard.apply {
      animate()
        .setInterpolator(AccelerateDecelerateInterpolator())
        .alpha(if (show) 1f else 0f)
        .setListener(object : AnimatorListenerAdapter() {
          override fun onAnimationEnd(animation: Animator) {
            super.onAnimationEnd(animation)
            isVisible = show
          }
        })
    }
  }

  private fun initMap() {
    map = binding.map
    map.apply {
      setTileSource(TileSourceFactory.MAPNIK)
      setMultiTouchControls(true) // Enables pinch to zoom & other gestures
      minZoomLevel = 19.2 // Zoom on ENSIAS
      mapOrientation = -37.5f
    }

    map.controller.apply {
      setZoom(19.2)
      // Set center to ENSIAS
      setCenter(GeoPoint(33.98429267486034, -6.8675806261125905))
    }
  }

  /**
   * Populates map with fetched zones polygons
   */
  private fun populateMap() {
    viewModel.schoolZonesPolygons.value?.forEach {
      it.apply {
        setOnClickListener { _, _, _ ->
          val fragment = SchoolZoneDetailsFragment.newInstance(it.title, it.subDescription)
          fragment.show(supportFragmentManager, fragment.tag)
          true
        }
        fillPaint.apply {
          color = ResourcesCompat.getColor(resources, R.color.primary, theme)
        }
        outlinePaint.apply {
          color = ResourcesCompat.getColor(resources, R.color.primary_variant, theme)
          strokeWidth = 1f
        }
      }

      val marker = Marker(map).apply {
        position = GeoUtils.centerOfPolygon(it.actualPoints)
        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
        textLabelBackgroundColor = Color.TRANSPARENT.ordinal
        setTextIcon(it.title)
      }
      map.overlayManager.addAll(listOf(it, marker))
    }
    map.invalidate()
  }

  /**
   * Enables location in the map
   */
  private fun enableLocation() {
    Toast.makeText(this, "Enabling location...", Toast.LENGTH_SHORT).show()
    val locationOverlay = MyLocationNewOverlay(GpsMyLocationProvider(this), map)
    locationOverlay.enableMyLocation()
    map.overlayManager.add(locationOverlay)
  }

  private fun handleError(code: Int) {
    Toast.makeText(this, R.string.unknown_error_occurred, Toast.LENGTH_LONG).show()
  }

  override fun onResume() {
    super.onResume()
    viewModel.fetchZones()
    map.onResume()
  }

  override fun onPause() {
    super.onPause()
    map.onPause()
  }
}