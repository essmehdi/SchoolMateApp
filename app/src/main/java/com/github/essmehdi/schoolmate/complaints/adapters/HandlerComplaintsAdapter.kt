package com.github.essmehdi.schoolmate.complaints.adapters

import android.annotation.SuppressLint
import android.content.Intent
import android.view.*
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.github.essmehdi.schoolmate.R
import com.github.essmehdi.schoolmate.complaints.api.dto.EditComplaintStatusAndHandlerDto
import com.github.essmehdi.schoolmate.complaints.models.BuildingComplaint
import com.github.essmehdi.schoolmate.complaints.models.Complaint
import com.github.essmehdi.schoolmate.complaints.models.FacilitiesComplaint
import com.github.essmehdi.schoolmate.complaints.models.RoomComplaint
import com.github.essmehdi.schoolmate.complaints.ui.ComplaintDetailsActivity
import com.github.essmehdi.schoolmate.complaints.viewmodels.HandlerComplaintsViewModel
import com.github.essmehdi.schoolmate.databinding.ComplaintItemBinding
import com.github.essmehdi.schoolmate.shared.utils.Utils
import com.google.android.material.snackbar.Snackbar
import org.joda.time.format.DateTimeFormat

class HandlerComplaintsAdapter(var data: List<Complaint>, val viewModel: HandlerComplaintsViewModel, val launcher: ActivityResultLauncher<Intent>) : RecyclerView.Adapter<HandlerComplaintsAdapter.ComplaintViewHolder>(){


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ComplaintViewHolder {
        val binding = ComplaintItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ComplaintViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return data.size
    }

    override fun onBindViewHolder(holder: ComplaintViewHolder, position: Int) {
        data.let { holder.bind(it[position]) }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateData(newData: List<Complaint>) {
        this.data = newData
        notifyDataSetChanged()
    }

    inner class ComplaintViewHolder(private val binding: ComplaintItemBinding) : RecyclerView.ViewHolder(binding.root),
        View.OnCreateContextMenuListener, MenuItem.OnMenuItemClickListener {

        init{
            binding.root.setOnCreateContextMenuListener(this)
        }

        fun bind(complaint: Complaint){
            val formatter = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZZ")
            val date = formatter.parseDateTime(complaint.date)
            val dateText = ("Submitted " + Utils.calculatePastTime(date.toDate(), binding.root.context))

            // the title of the complaint will be: firstName lastName • Room/Building/Facilities
            var title = complaint.complainant.fullName
            title += if(complaint is RoomComplaint){
                " • Room"
            } else if (complaint is BuildingComplaint) {
                " • Building"
            } else if (complaint is FacilitiesComplaint) {
                " • Facilities"
            } else {
                " • Unknown"
            }

            binding.complaintTitle.text = title
            binding.complaintDate.text = dateText
            binding.complaintStatus.text = complaint.status.name
            binding.complaintStatus.setTextColor(ContextCompat.getColor(binding.root.context, getStatusColor(complaint.status.name)))
            binding.root.setOnClickListener {
                val intent = Intent(binding.root.context, ComplaintDetailsActivity::class.java)
                intent.putExtra("complaintId", complaint.id)
                launcher?.launch(intent)
            }
        }

        override fun onCreateContextMenu(
            menu: ContextMenu?,
            v: View?,
            menuInfo: ContextMenu.ContextMenuInfo?
        ) {
            menu?.add(this.adapterPosition, 1, 1, binding.root.context.getString(R.string.label_complaint_item_context_menu_view))?.setOnMenuItemClickListener(this)
            menu?.add(this.adapterPosition, 2, 2, binding.root.context.getString(R.string.label_complaint_item_context_menu_handle_complaint))?.setOnMenuItemClickListener(this)
            // If the complaint is already assigned, disable the handle complaint option
            if(data[this.adapterPosition].handler != null){ // Means the complaint is not pending
                menu?.getItem(1)?.isEnabled = false // handle complaint (order 2)
            }
        }

        override fun onMenuItemClick(item: MenuItem): Boolean {
            when(item.itemId){
                1 -> {
                    // Go to complaint details
                    val intent = Intent(binding.root.context, ComplaintDetailsActivity::class.java)
                    intent.putExtra("complaintId", data[this.adapterPosition].id)
                    launcher.launch(intent)
                }
                2 -> {
                    val position = this.adapterPosition
                    // Display the dialog to confirm handling the complaint
                    AlertDialog.Builder(binding.root.context).apply {
                        setTitle(R.string.label_complaint_item_context_menu_handle_complaint)
                        setMessage(R.string.label_complaint_item_context_menu_handle_complaint_message)
                        setPositiveButton(R.string.label_complaint_item_context_menu_handle_complaint_positive_button) { dialog, _ ->
                            val editComplaintStatusAndHandlerDto = EditComplaintStatusAndHandlerDto(null, viewModel.currentHandler.value?.handler?.id)
                            viewModel.editComplaintStautsAndHandler(data[position].id!!, editComplaintStatusAndHandlerDto)
                            viewModel.refresh()
                            updateData(viewModel.complaints.value!!)
                            dialog.dismiss()
                            Snackbar.make(binding.root, R.string.handler_changed_to_me, Snackbar.LENGTH_SHORT).show()
                        }
                        setNegativeButton(R.string.label_complaint_item_context_menu_handle_complaint_negative_button) { dialog, _ ->
                            dialog.dismiss()
                        }
                    }.create().show()
                }
            }
            return true
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

    }
}