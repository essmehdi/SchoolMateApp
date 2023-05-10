package com.github.essmehdi.schoolmate.home.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.essmehdi.schoolmate.R
import com.github.essmehdi.schoolmate.auth.models.User
import com.github.essmehdi.schoolmate.complaints.adapters.ComplaintsListAdapter
import com.github.essmehdi.schoolmate.complaints.ui.ComplaintEditorActivity
import com.github.essmehdi.schoolmate.complaints.ui.ComplaintsActivity
import com.github.essmehdi.schoolmate.complaints.ui.HandlerComplaintsActivity
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

      seeAllComplaintsButton.setOnClickListener {
        if(viewModel.user.value?.data?.role == "STUDENT") {
          val intent = Intent(this@HomeActivity, ComplaintsActivity::class.java)
          startActivity(intent)
        } else {
          val intent = Intent(this@HomeActivity, HandlerComplaintsActivity::class.java)
          startActivity(intent)
        }
      }

      makeComplaintButtonHome.setOnClickListener {
          goToComplaintEditor()
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
    viewModel.fetchUserComplaints(if(viewModel.user.value?.data?.role == "STUDENT") "me" else "all")
    complaintsListAdapter = ComplaintsListAdapter(listOf())
    binding.complaintsList.apply {
      adapter = complaintsListAdapter
      addItemDecoration(DividerItemDecoration(this@HomeActivity, DividerItemDecoration.VERTICAL))
      layoutManager = LinearLayoutManager(this@HomeActivity, RecyclerView.VERTICAL, false)
    }

    viewModel.showEmpty.observe(this) {
      showEmpty(it)
    }

    viewModel.userComplaints.observe(this) { userComplaints ->
      userComplaints?.let { complaintsListAdapter.updateComplaints(it) }
    }

    viewModel.currentPageStatus.observe(this) { currentPage ->
      when (currentPage) {
        is BaseResponse.Loading -> {
          if (viewModel.userComplaints.value == null || viewModel.userComplaints.value!!.isEmpty()) {
            showLoading()
          } else {
            showLoading(false)
          }
        }
        is BaseResponse.Success -> {
          showLoading(false)
        }
        is BaseResponse.Error -> {
          handleComplaintError(currentPage.code!!)
        }
        null -> {}
      }

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

  private fun showEmpty(show: Boolean = true) {
    binding.complaintsEmptyView.root.isVisible = show
  }

  private fun showLoading(show: Boolean = true) {
    binding.complaintsLoading.loadingOverlay.isVisible = show
  }

  private fun handleComplaintError(code: Int) {
    binding.complaintsLoading.loadingErrorMessage.text = getString(R.string.unknown_error_occurred)
    binding.complaintsLoading.loadingErrorMessage.visibility = View.VISIBLE
    binding.complaintsLoading.loadingProgressBar.visibility = View.GONE
    showLoading()
  }

  private fun goToComplaintEditor() {
    // Let's create a dialog to ask the user for the complaint type (building, room, facility)
    val builder = AlertDialog.Builder(this)
    builder.setView(R.layout.complaint_type_dialog)
    // access the card view in the dialog
    val dialog = builder.create()
    // set the background of the dialog to transparent
    dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
    dialog.show()
    dialog.findViewById<androidx.cardview.widget.CardView>(R.id.building_complaint_card)?.setOnClickListener {
      val intent = Intent(this, ComplaintEditorActivity::class.java)
      intent.putExtra("complaint_type", "building")
      startActivity(intent)
      dialog.dismiss()
    }
    dialog.findViewById<androidx.cardview.widget.CardView>(R.id.room_complaint_card)?.setOnClickListener {
      val intent = Intent(this, ComplaintEditorActivity::class.java)
      intent.putExtra("complaint_type", "room")
      startActivity(intent)
      dialog.dismiss()
    }
    dialog.findViewById<androidx.cardview.widget.CardView>(R.id.facilities_complaint_card)?.setOnClickListener {
      val intent = Intent(this, ComplaintEditorActivity::class.java)
      intent.putExtra("complaint_type", "facility")
      startActivity(intent)
      dialog.dismiss()
    }

  }

}