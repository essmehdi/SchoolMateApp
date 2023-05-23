package com.github.essmehdi.schoolmate.placesuggestions.ui

import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.preference.PreferenceManager
import com.github.essmehdi.schoolmate.R
import com.github.essmehdi.schoolmate.databinding.ActivitySuggestionDetailsBinding
import com.github.essmehdi.schoolmate.placesuggestions.models.PlaceSuggestions
import com.github.essmehdi.schoolmate.placesuggestions.viewmodels.SuggestionDetailsViewModel
import com.github.essmehdi.schoolmate.shared.api.BaseResponse
import com.github.essmehdi.schoolmate.shared.utils.GeoUtils
import com.github.essmehdi.schoolmate.shared.utils.Utils
import org.joda.time.format.DateTimeFormat
import org.mapsforge.core.graphics.Color
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

class SuggestionDetailsActivity : AppCompatActivity() {

    private lateinit var map: MapView
    private lateinit var binding: ActivitySuggestionDetailsBinding
    private val viewModel: SuggestionDetailsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySuggestionDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.suggestionDetailsToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_navigation_left)

        Configuration.getInstance().load(applicationContext, PreferenceManager.getDefaultSharedPreferences(applicationContext))

        val id = intent.getLongExtra("id", 0)
        viewModel.fetchSuggestion(id)
        //observe activity fetching
        viewModel.suggestion.observe(this) {
            when (it) {
                is BaseResponse.Loading -> {
                    showLoading()
                }
                is BaseResponse.Success -> {
                    showLoading(false)
                    fillPage(it.data)
                    initMap()
                }
                is BaseResponse.Error -> {
                    showLoading(false)
                    handleError(it.code!!)
                }
            }
        }

    }

    private fun initMap() {
        map = binding.suggestionDetailsMap
        map.apply {
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(true) // Enables pinch to zoom & other gestures
            minZoomLevel = 19.2 // Zoom on ENSIAS
        }

        val coordinates = viewModel.suggestion.value!!.data!!.coordinates

        map.controller.apply {
            setZoom(19.2)
            // Set center to suugestion location
            setCenter(GeoPoint(coordinates.x, coordinates.y))
        }

        val marker = Marker(map).apply {
            position = GeoPoint(coordinates.x, coordinates.y)
            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
            infoWindow = null
        }
        map.overlayManager.add(marker)
    }

    private fun handleError(code: Int) {
        binding.suggestionsLoading.loadingErrorMessage.text = getString(R.string.unknown_error_occurred)
        binding.suggestionsLoading.loadingErrorMessage.visibility = View.VISIBLE
        binding.suggestionsLoading.loadingProgressBar.visibility = View.GONE
        showLoading()
    }

    private fun fillPage(placeSuggestion: PlaceSuggestions?) {
        if (placeSuggestion != null) {
            val formatter = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZZ")
            val dateTime = formatter.parseDateTime(placeSuggestion.date)
            binding.suggestionDetailsSuggester.text = placeSuggestion.user.fullName
            binding.suggestionDetailsDescription.text = placeSuggestion.description
            binding.suggestionDetailsDate.text = Utils.calculatePastTime(dateTime.toDate(), binding.root.context) ?: ""
            binding.suggestionDetailsType.text = placeSuggestion.suggestiontype.name
        }
    }

    private fun showLoading(show: Boolean = true) {
        binding.suggestionsLoading.loadingOverlay.isVisible = show
    }
}