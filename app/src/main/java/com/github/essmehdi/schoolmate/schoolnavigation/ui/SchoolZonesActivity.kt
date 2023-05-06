package com.github.essmehdi.schoolmate.schoolnavigation.ui

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.essmehdi.schoolmate.R
import com.github.essmehdi.schoolmate.databinding.ActivitySchoolZonesBinding
import com.github.essmehdi.schoolmate.schoolnavigation.adapters.SchoolZonesAdapter
import com.github.essmehdi.schoolmate.schoolnavigation.viewmodels.SchoolZonesViewModel
import com.github.essmehdi.schoolmate.shared.api.BaseResponse

class SchoolZonesActivity : AppCompatActivity() {

  private lateinit var binding: ActivitySchoolZonesBinding
  private lateinit var zonesAdapter: SchoolZonesAdapter
  private val viewModel: SchoolZonesViewModel by viewModels()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    binding = ActivitySchoolZonesBinding.inflate(layoutInflater)
    setContentView(binding.root)

    supportActionBar?.setDisplayHomeAsUpEnabled(true)

    binding.schoolZonesListSwipeRefresh.setOnRefreshListener {
      viewModel.fetchZones()
    }

    zonesAdapter = SchoolZonesAdapter(listOf())
    binding.schoolZonesList.apply {
      adapter = zonesAdapter
      layoutManager = LinearLayoutManager(this@SchoolZonesActivity)
    }

    viewModel.fetchStatus.observe(this) {
      when (it) {
        is BaseResponse.Loading -> {
          if (zonesAdapter.data.isEmpty()) showLoading()
        }
        is BaseResponse.Error -> {
          dismissSwipe()
          handleError(it.code!!)
        }
        is BaseResponse.Success -> {
          dismissSwipe()
          showLoading(false)
        }
      }
    }

    viewModel.schoolZones.observe(this) {
      zonesAdapter.updateData(it)
    }
  }

  override fun onCreateOptionsMenu(menu: Menu?): Boolean {
    val inflater = menuInflater
    inflater.inflate(R.menu.school_zones_activity_menu, menu)
    return true
  }

  override fun onOptionsItemSelected(item: MenuItem): Boolean {
    return when (item.itemId) {
      android.R.id.home -> {
          onBackPressedDispatcher.onBackPressed()
          true
      }
      R.id.school_zone_editor_menu_add -> {
        showAddFragment()
        true
      }
      else -> super.onOptionsItemSelected(item)
    }
  }

  private fun showAddFragment() {
    SchoolZoneEditorFragment.newInstance().apply {
      show(supportFragmentManager, tag)
    }
  }

  private fun dismissSwipe() {
    binding.schoolZonesListSwipeRefresh.isRefreshing = false
  }

  @Suppress("UNUSED_PARAMETER")
  private fun handleError(code: Int) {
    showError()
  }

  private fun showLoading(show: Boolean = true) {
    binding.schoolZonesLoading.loadingProgressBar.isVisible = true
    binding.schoolZonesLoading.loadingErrorMessage.isVisible = false
    binding.schoolZonesLoading.loadingOverlay.isVisible = show
  }

  private fun showError(show: Boolean = true, errorMsg: String? = null) {
    binding.schoolZonesLoading.loadingErrorMessage.apply {
      text = errorMsg ?: getString(R.string.unknown_error_occurred)
      isVisible = true
    }
    binding.schoolZonesLoading.loadingProgressBar.isVisible = false
    binding.schoolZonesLoading.loadingOverlay.isVisible = show
  }
}