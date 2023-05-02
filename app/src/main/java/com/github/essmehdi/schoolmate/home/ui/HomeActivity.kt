package com.github.essmehdi.schoolmate.home.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.essmehdi.schoolmate.R
import com.github.essmehdi.schoolmate.auth.models.User
import com.github.essmehdi.schoolmate.complaints.adapters.ComplaintsListAdapter
import com.github.essmehdi.schoolmate.databinding.ActivityHomeBinding
import com.github.essmehdi.schoolmate.documents.ui.DocumentsActivity
import com.github.essmehdi.schoolmate.home.viewmodels.HomeViewModel
import com.github.essmehdi.schoolmate.schoolnavigation.ui.SchoolNavigationActivity
import com.github.essmehdi.schoolmate.shared.api.BaseResponse

class HomeActivity : AppCompatActivity() {

  private lateinit var binding: ActivityHomeBinding
  private lateinit var viewModel: HomeViewModel
  private lateinit var complaintsListAdapter: ComplaintsListAdapter

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    binding = ActivityHomeBinding.inflate(layoutInflater)
    setContentView(binding.root)

    binding.apply {
      navigationHomeButton.homeButtonRoot.setOnClickListener {
        val intent = Intent(this@HomeActivity, SchoolNavigationActivity::class.java)
        startActivity(intent)
      }

      documentsHomeButton.homeButtonRoot.setOnClickListener {
        val intent = Intent(this@HomeActivity, DocumentsActivity::class.java)
        startActivity(intent)
      }
    }

    viewModel = ViewModelProvider(this)[HomeViewModel::class.java]

    viewModel.fetchUserData()

    viewModel.user.observe(this) {
      when (it) {
        is BaseResponse.Success -> handleUserSuccess(it.data!!)
        is BaseResponse.Loading -> {}
        is BaseResponse.Error -> handleUserError(it.message!!)
      }
    }

    // Complaints list setup -----------------------------------------------
    viewModel.fetchUserComplaints()
    complaintsListAdapter = ComplaintsListAdapter(listOf())
    binding.complaintsList.apply {
      adapter = complaintsListAdapter
      layoutManager = LinearLayoutManager(this@HomeActivity, RecyclerView.VERTICAL, false)
      addOnScrollListener(object : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
          super.onScrolled(recyclerView, dx, dy)
          if (dy > 0) {
            if (viewModel.currentPageStatus.value is BaseResponse.Loading) {
              return
            }
            val visibleItemCount = layoutManager?.childCount ?: 0
            val totalItemCount = layoutManager?.itemCount ?: 0
            val pastVisibleItems = (layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
            if (visibleItemCount + pastVisibleItems >= totalItemCount) {
              viewModel.fetchUserComplaints()
            }
          }
        }
      })
    }
    // ---------------------------------------------------------------------
  }

  private fun handleUserError(message: String) {
    binding.homeLoading.loadingProgressBar.visibility = View.GONE
    binding.homeLoading.loadingErrorMessage.apply {
      text = getString(R.string.unknown_error_occurred)
      visibility = View.GONE
    }

  }

  private fun handleUserSuccess(user: User) {
    binding.firstNameText.text = user.firstName
    binding.homeLoading.loadingOverlay.visibility = View.GONE
  }
}