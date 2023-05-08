package com.github.essmehdi.schoolmate.complaints.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.github.essmehdi.schoolmate.R
import com.github.essmehdi.schoolmate.complaints.enumerations.FacilityType
import com.github.essmehdi.schoolmate.complaints.models.BuildingComplaint
import com.github.essmehdi.schoolmate.complaints.models.FacilitiesComplaint
import com.github.essmehdi.schoolmate.complaints.models.RoomComplaint
import com.github.essmehdi.schoolmate.complaints.ui.ComplaintEditorActivity.Companion.RESULT_ACTION_CREATED
import com.github.essmehdi.schoolmate.complaints.ui.ComplaintEditorActivity.Companion.RESULT_ACTION_UPDATED
import com.github.essmehdi.schoolmate.complaints.viewmodels.ComplaintDetailsViewModel
import com.github.essmehdi.schoolmate.databinding.ActivityComplaintDetailsBinding
import com.github.essmehdi.schoolmate.shared.api.BaseResponse
import com.github.essmehdi.schoolmate.shared.utils.Utils
import com.google.android.material.snackbar.Snackbar
import org.joda.time.format.DateTimeFormat

class ComplaintDetailsActivity : AppCompatActivity() {

    companion object {
        const val RESULT_ACTION_DELETED = 1
    }
    private lateinit var binding: ActivityComplaintDetailsBinding
    lateinit var launcher: ActivityResultLauncher<Intent>
    private val viewModel: ComplaintDetailsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityComplaintDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.complaintsDetailsToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_navigation_left)

        launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if(result.resultCode == RESULT_ACTION_CREATED) {
                viewModel.id.value = result.data?.getLongExtra("complaintId", 0)
                viewModel.refresh()
                Snackbar.make(binding.root, getString(R.string.complaint_created, "submitted"), Snackbar.LENGTH_SHORT).show()
            } else if(result.resultCode == RESULT_ACTION_UPDATED) {
                viewModel.id.value = result.data?.getLongExtra("complaintId", 0)
                viewModel.refresh()
                Snackbar.make(binding.root, getString(R.string.complaint_created, "updated"), Snackbar.LENGTH_SHORT).show()
            }
        }

        if(intent.hasExtra("complaintId")) {
            val complaintId = intent.getLongExtra("complaintId", 0)
            viewModel.id.value = complaintId
            viewModel.fetchCurrentComplainant()
            viewModel.getComplaint()
            viewModel.complaint.observe(this) {
                when (it) {
                    is BaseResponse.Success -> {
                        showLoading(false)
                        showComplaintDetails()
                        if(intent.hasExtra("edited"))
                            Snackbar.make(binding.root, getString(R.string.complaint_created, if (intent.getBooleanExtra("edited",false)) "updated" else "submitted"), Snackbar.LENGTH_SHORT).show()
                    }
                    is BaseResponse.Loading -> {
                        showLoading()
                    }
                    is BaseResponse.Error -> {
                        showLoading(false)
                        handleError(0)
                    }
                    else -> {}
                }
            }
        }

        viewModel.deleteStatus.observe(this) {
            when (it) { // when the complaint is deleted, the activity will be closed
                is BaseResponse.Loading -> {
                    Snackbar.make(binding.root, R.string.loading_complaint_deletion, Snackbar.LENGTH_INDEFINITE).show()
                }
                is BaseResponse.Error -> {
                    Snackbar.make(binding.root, R.string.error_complaint_deletion, Snackbar.LENGTH_SHORT).show()
                }
                else -> {}
            }
        }
    }

    private fun showComplaintDetails() {
        val complaint = viewModel.complaint.value?.data
        // Set the complainant name
        val complainant = complaint?.complainant?.firstName + " " + complaint?.complainant?.lastName
        binding.complainantName.text = complainant
        // Set the complaint date
        val formatter = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZZ")
        val date = formatter.parseDateTime(complaint?.date)
        binding.complaintDate.text = Utils.calculatePastTime(date.toDate(), binding.root.context)
        // Set the complaint status (color and text)
        binding.complaintStatus.text = complaint?.status?.name
        binding.complaintStatus.setTextColor(ContextCompat.getColor(binding.root.context, getStatusColor(complaint?.status?.name)))
        // Set the complaint handler
        val handler = if(complaint?.handler == null) "Not assigned yet"
                    else complaint.handler!!.fullName
        binding.complaintHandler.text = handler
        // test the complaint type to set specific fields
        if(complaint is RoomComplaint){
            binding.complaintType.text = getString(R.string.room_complaint)
            binding.complaintProblem.text = complaint.roomProb.name
            binding.roomComplaintCard.visibility = View.VISIBLE
            binding.complaintRoom.text = complaint.room
        } else if (complaint is BuildingComplaint) {
            binding.complaintType.text = getString(R.string.building_complaint)
            binding.complaintProblem.text = complaint.buildingProb.name
            binding.buildingComplaintCard.visibility = View.VISIBLE
            binding.complaintBuilding.text = complaint.building
        } else if (complaint is FacilitiesComplaint) {
            binding.complaintType.text = getString(R.string.facility_complaint)
            binding.complaintProblem.text = complaint.facilityType.name
            // Only show the class name if the complaint is about a class
            if(complaint.facilityType == FacilityType.CLASS){
                binding.facilityComplaintCard.visibility = View.VISIBLE
                binding.complaintFacility.text = complaint.className
            }
        }
        // Set the complaint description
        binding.complaintDescription.text = complaint?.description
        // Set the complaint edit and delete buttons
        if(complaint?.complainant?.id  == viewModel.currentComplainant.value?.id) {
            binding.complaintEditButton.visibility = View.VISIBLE
            binding.complaintDeleteButton.visibility = View.VISIBLE
            setActionButtons()
        }
    }

    private fun setActionButtons() {
        binding.complaintEditButton.setOnClickListener {
            if(viewModel.complaint.value?.data?.status?.name == "PENDING") {
                val intent = Intent(this, ComplaintEditorActivity::class.java)
                intent.putExtra("complaintId", viewModel.id.value)
                intent.putExtra("source", "detailsActivity")
                launcher.launch(intent)
            } else {
                // show a dialog to inform the user that the complaint can't be edited
                val builder = AlertDialog.Builder(binding.root.context)
                builder.setTitle(binding.root.context.getString(R.string.label_complaint_item_context_menu_edit))
                    .setMessage(binding.root.context.getString(R.string.complaint_not_editable))
                    .setPositiveButton(binding.root.context.getString(R.string.label_ok)) { dialog, _ ->
                        // Dismiss the dialog
                        dialog.dismiss()
                    }
                builder.create().show()
            }
        }
        binding.complaintDeleteButton.setOnClickListener {
            // show a dialog to confirm the deletion
            val builder = AlertDialog.Builder(binding.root.context)
            builder.setTitle(binding.root.context.getString(R.string.label_complaint_item_context_menu_delete))
                .setMessage(binding.root.context.getString(R.string.confirm_delete_complaint))
                .setPositiveButton(binding.root.context.getString(R.string.label_yes)) { _, _ ->
                    // Delete the complaint
                    viewModel?.deleteComplaint()
                    // Go back to the previous activity
                    setResult(Companion.RESULT_ACTION_DELETED)
                    finish()
                }
                .setNegativeButton(binding.root.context.getString(R.string.label_no)) { dialog, _ ->
                    // Dismiss the dialog
                    dialog.dismiss()
                }
            builder.create().show()
        }
    }

    private fun getStatusColor(name: String?): Int {
        return when(name) {
            "PENDING" -> {
                R.color.complaint_status_pending
            }
            "ASSIGNED" -> {
                R.color.complaint_status_assigned
            }
            "CONFIRMED" -> {
                R.color.complaint_status_confirmed
            }
            "REJECTED" -> {
                R.color.complaint_status_rejected
            }
            "RESOLVED" -> {
                R.color.complaint_status_resolved
            }
            "UNRESOLVED" -> {
                R.color.complaint_status_unresolved
            }
            "RESOLVING" -> {
                R.color.complaint_status_resolving
            }
            else -> {
                R.color.complaint_status_pending
            }
        }
    }

    private fun showLoading(show: Boolean = true) {
        binding.complaintLoading.loadingOverlay.isVisible = show
    }

    private fun handleError(code: Int) {
        binding.complaintLoading.loadingErrorMessage.text = getString(R.string.unknown_error_occurred)
        binding.complaintLoading.loadingErrorMessage.visibility = View.VISIBLE
        binding.complaintLoading.loadingProgressBar.visibility = View.GONE
        showLoading()
    }

}