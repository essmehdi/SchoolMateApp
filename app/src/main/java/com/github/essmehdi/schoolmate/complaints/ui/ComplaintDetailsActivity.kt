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
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.essmehdi.schoolmate.R
import com.github.essmehdi.schoolmate.complaints.adapters.HandlerListDetailsAdapter
import com.github.essmehdi.schoolmate.complaints.adapters.OnClickAssignAndDissmiss
import com.github.essmehdi.schoolmate.complaints.api.dto.EditComplaintStatusAndHandlerDto
import com.github.essmehdi.schoolmate.complaints.enumerations.ComplaintStatus
import com.github.essmehdi.schoolmate.complaints.enumerations.FacilityType
import com.github.essmehdi.schoolmate.complaints.models.BuildingComplaint
import com.github.essmehdi.schoolmate.complaints.models.FacilitiesComplaint
import com.github.essmehdi.schoolmate.complaints.models.RoomComplaint
import com.github.essmehdi.schoolmate.complaints.ui.ComplaintEditorActivity.Companion.RESULT_ACTION_CREATED
import com.github.essmehdi.schoolmate.complaints.ui.ComplaintEditorActivity.Companion.RESULT_ACTION_UPDATED
import com.github.essmehdi.schoolmate.complaints.viewmodels.ComplaintDetailsViewModel
import com.github.essmehdi.schoolmate.databinding.ActivityComplaintDetailsBinding
import com.github.essmehdi.schoolmate.databinding.ComplaintHandlersListBinding
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
    private lateinit var handlersAdapter: HandlerListDetailsAdapter

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

        // Set the swipe refresh listener
        binding.allComplaintsSwipeRefresh.layoutTransition?.setAnimateParentHierarchy(false)
        binding.allComplaintsSwipeRefresh.setOnRefreshListener {
            viewModel.refresh()
            // cancel the refresh animation after the data is fetched
            binding.allComplaintsSwipeRefresh.isRefreshing = false
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
                        if(viewModel.connectedUser.value?.role == "ADEI"){
                            // We need to check if the complaint is assigned to the current user
                            // If not, we need to hide the edit buttons
                            if(it.data?.handler?.id != viewModel.connectedUser.value?.id && !it.data?.status?.equals(ComplaintStatus.PENDING)!!) {
                                binding.complaintAssignButton.visibility = View.GONE
                                binding.complaintStatusChange.visibility = View.GONE
                            }
                        }
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

        // When the user is a handler, observe the edit status (if they changed the status or assigned a handler)
        viewModel.editStatus.observe(this) {
            when (it) {
                is BaseResponse.Loading -> {
                    showLoading()
                    Snackbar.make(binding.root, R.string.loading_complaint_edit, Snackbar.LENGTH_INDEFINITE).show()
                }
                is BaseResponse.Success -> {
                    showLoading(true)
                    viewModel.refresh()
                }
                is BaseResponse.Error -> {
                    showLoading(false)
                    Snackbar.make(binding.root, R.string.unknown_error_occurred, Snackbar.LENGTH_SHORT).show()
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
        if(complaint?.complainant?.id  == viewModel.connectedUser.value?.id) {
            binding.complaintEditButton.visibility = View.VISIBLE
            binding.complaintDeleteButton.visibility = View.VISIBLE
            setActionButtons()
        }

        // For the complaint handler, show the necessary buttons
        if(viewModel.connectedUser.value?.role.equals("ADEI")){ //
            viewModel.fetchCurrentHandlerComplaintsCount()
            if(viewModel.complaint.value?.data?.status?.name.equals("PENDING") || viewModel.complaint.value?.data?.handler?.id == viewModel.connectedUser.value!!.id) {
                binding.complaintAssignButton.visibility = View.VISIBLE
                binding.complaintStatusChange.visibility = View.VISIBLE
                setHandlerActionButtons(complaint?.status?.name.equals("PENDING"))
            }
        }
    }

    private fun setHandlerActionButtons(pending: Boolean = true) {
        binding.complaintStatusChange.setOnClickListener(){
            if(pending){
                showImpossibleStatusChangeDialog()
            } else {
                showStatusChangeDialog()
            }
        }
        binding.complaintAssignButton.setOnClickListener(){
            showAssignHandlersDialog()
        }
    }

    private fun showAssignHandlersDialog() {
        // Initialize the list of handlers
        viewModel.refreshHandlers()
        handlersAdapter = HandlerListDetailsAdapter(viewModel.handlersList.value!!, viewModel)
        val popupBinding = ComplaintHandlersListBinding.inflate(layoutInflater)
        popupBinding.handlersList.apply {
            adapter = handlersAdapter
            addItemDecoration(DividerItemDecoration(popupBinding.root.context, DividerItemDecoration.VERTICAL))
            layoutManager = LinearLayoutManager(
                this@ComplaintDetailsActivity,
                LinearLayoutManager.VERTICAL,
                false
            )
            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    if (dy > 0) {
                        if (viewModel.currentHandlersPageStatus.value is BaseResponse.Loading) {
                            return
                        }
                        val visibleItemCount = layoutManager?.childCount ?: 0
                        val totalItemCount = layoutManager?.itemCount ?: 0
                        val pastVisibleItems =
                            (layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
                        if (visibleItemCount + pastVisibleItems >= totalItemCount) {
                            viewModel.fetchHandlersAndComplaints()
                        }
                    }
                }
            })
        }

        viewModel.handlersList.observe(this) {
            if(it == null) {
                popupBinding.handlersLoading.loadingOverlay.visibility = View.VISIBLE
            }
            popupBinding.handlersLoading.loadingOverlay.visibility = View.GONE
            it?.let { handlersAdapter.updateData(it) }
        }

        // Set the current handler first
        popupBinding.assignCurrentComplaintHandler.visibility = View.VISIBLE
        popupBinding.seeCurrentHandlerComplaints.visibility = View.GONE
        popupBinding.currentHandlerName.text = getString(R.string.current_user_display_word)
        popupBinding.currentHandlerHandledComplaints.text = getString(R.string.handled_complaints_preview, viewModel.currentHandlerComplaintsCount.value.toString())

        val dialog = AlertDialog.Builder(this)
            .setView(popupBinding.root)
            .setCancelable(true)
            .setNeutralButton(R.string.label_close_status_filter) { dialogInterface, _ ->
                dialogInterface.dismiss()
            }
            .show()

        popupBinding.assignCurrentComplaintHandler.setOnClickListener {
            // Assign to the current handler
            dialog.dismiss()
            showConfirmAssignDialog(viewModel.connectedUser.value!!.id)
        }

        // This method is called from the adapter when the user clicks on a handler
        handlersAdapter.onClickAssignAndDissmiss = object : OnClickAssignAndDissmiss {
            override fun onClickAssignAndDissmiss(handlerName: String?, handlerId: Long?) {
                // dissmiss the dialog
                dialog.dismiss()
                if(handlerId != null &&  handlerName != null){
                    showConfirmAssignDialog(handlerId, handlerName)
                }
            }
        }
    }

    private fun showConfirmAssignDialog(id: Long, name: String? = null) {
        // Confirmation dialog for assigning the complaint to the connected user
        if(viewModel.connectedUser.value!!.id == id){
            val builder = AlertDialog.Builder(this)
            builder.apply {
                setTitle(getString(R.string.label_complaint_item_context_menu_assign_handler))
                setMessage(getString(R.string.label_complaint_item_context_menu_take_complaint_message))
                setPositiveButton(getString(R.string.label_confirm_general)) { _, _ ->
                    val editComplaintStatusAndHandlerDto = EditComplaintStatusAndHandlerDto(
                        null,
                        id
                    )
                    viewModel.editComplaintStautsAndHandler(editComplaintStatusAndHandlerDto)
                    viewModel.refresh()
                    Snackbar.make(binding.root, R.string.handler_changed_to_me, Snackbar.LENGTH_SHORT).show()
                }
                setNegativeButton(getString(R.string.label_cancel)) { dialog, _ ->
                    dialog.dismiss()
                }
            }
            builder.create().show()
            return
        } else {
            // Confirmation dialog for assigning the complaint to another handler
            val builder = AlertDialog.Builder(this)
            builder.apply {
                setTitle(getString(R.string.label_complaint_item_context_menu_assign_handler))
                setMessage(getString(R.string.label_complaint_item_context_menu_assign_handler_message))
                setPositiveButton(getString(R.string.label_confirm_general)) { _, _ ->
                    val editComplaintStatusAndHandlerDto = EditComplaintStatusAndHandlerDto(
                        null,
                        id
                    )
                    viewModel.editComplaintStautsAndHandler(editComplaintStatusAndHandlerDto)
                    viewModel.refresh()
                    Snackbar.make(binding.root, getString(R.string.label_complaint_item_context_menu_assign_handler_success, name!!), Snackbar.LENGTH_LONG).show()
                }
                setNegativeButton(getString(R.string.label_cancel)) { dialog, _ ->
                    dialog.dismiss()
                }
            }
            builder.create().show()
            return
        }
    }

    private fun showStatusChangeDialog() {
        // Create an array of the status except the current one and PENDING
        val statusList = ComplaintStatus.values().filter { it != viewModel.complaint.value?.data?.status && it != ComplaintStatus.PENDING }
        val statusNames = arrayOf(*statusList.map { it.name }.toTypedArray())
        var newStatus = -1
        val builder = AlertDialog.Builder(binding.root.context)
        builder.apply {
            setTitle(binding.root.context.getString(R.string.label_complaint_item_context_menu_status_change))
            setSingleChoiceItems(
                statusNames,
                newStatus
            ) { _, which ->
                newStatus = which
            }
            setPositiveButton(binding.root.context.getString(R.string.label_confirm)) { dialog, _ ->
                // Dismiss the dialog
                dialog.dismiss()
                // Change the status
                if(newStatus != -1){
                    showConfirmNewStatusDialog(statusList[newStatus].name)
                }
            }
            setNegativeButton(binding.root.context.getString(R.string.label_cancel)) { dialog, _ ->
                // Dismiss the dialog
                dialog.dismiss()
            }
        }
        builder.create().show()
    }

    private fun showConfirmNewStatusDialog(status: String){
        val builder = AlertDialog.Builder(binding.root.context)
        builder.apply {
            setTitle(binding.root.context.getString(R.string.label_complaint_item_context_menu_status_change))
            setMessage(binding.root.context.getString(R.string.label_complaint_item_context_menu_status_change_message, viewModel.complaint.value?.data?.status?.name))
            setPositiveButton(binding.root.context.getString(R.string.label_confirm)) { _, _ ->
                // Change the status
                val editComplaintStatusAndHandlerDto = EditComplaintStatusAndHandlerDto(status, null)
                viewModel.editComplaintStautsAndHandler(editComplaintStatusAndHandlerDto)
                showLoading()
                viewModel.refresh()
                Snackbar.make(binding.root, getString(R.string.complaint_status_changed, status), Snackbar.LENGTH_SHORT).show()
            }
            setNegativeButton(binding.root.context.getString(R.string.label_cancel)) { dialog, _ ->
                // Dismiss the dialog
                dialog.dismiss()
            }
        }.create().show()
    }

    private fun showImpossibleStatusChangeDialog() {
        val builder = AlertDialog.Builder(binding.root.context)
        builder.setTitle(binding.root.context.getString(R.string.label_complaint_item_context_menu_status_change))
            .setMessage(binding.root.context.getString(R.string.impossible_status_change))
            .setPositiveButton(binding.root.context.getString(R.string.label_ok)) { dialog, _ ->
                // Dismiss the dialog
                dialog.dismiss()
            }
        builder.create().show()
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