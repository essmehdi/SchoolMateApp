package com.github.essmehdi.schoolmate.alerts.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.preference.PreferenceManager
import com.github.essmehdi.schoolmate.R
import com.github.essmehdi.schoolmate.alerts.viewmodels.AlertDetailsViewModel
import com.github.essmehdi.schoolmate.databinding.FragmentAlertMapBinding
import com.github.essmehdi.schoolmate.shared.utils.GeoUtils
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import org.mapsforge.core.graphics.Color
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker


class AlertMapFragment(val points: List<Double>, val title: String) : BottomSheetDialogFragment() {

    private lateinit var binding: FragmentAlertMapBinding
    private lateinit var map: MapView


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentAlertMapBinding.inflate(inflater, container, false)
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Configuration.getInstance().load(binding.root.context, PreferenceManager.getDefaultSharedPreferences(binding.root.context))
        initMap()

        // dismiss the current dialog
        binding.closeMapButton.setOnClickListener {
            dismiss()
        }
    }
    private fun initMap() {
        map = binding.mapView
        map.apply {
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(true) // Enables pinch to zoom & other gestures
            minZoomLevel = 19.2
            mapOrientation = -37.5f
        }

        map.controller.apply {
            setZoom(19.2)
            setCenter(GeoPoint(points[0], points[1]))

        }

        val marker = Marker(map).apply {
            position = GeoPoint(points[0], points[1])
            title = title
            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
            textLabelBackgroundColor = Color.TRANSPARENT.ordinal
            infoWindow = null
        }

        map.overlayManager.add(marker)
    }


}