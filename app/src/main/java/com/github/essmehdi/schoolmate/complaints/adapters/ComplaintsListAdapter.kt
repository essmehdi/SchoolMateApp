package com.github.essmehdi.schoolmate.complaints.adapters

import android.annotation.SuppressLint
import android.content.Intent
import android.util.Log
import android.view.*
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.github.essmehdi.schoolmate.R
import com.github.essmehdi.schoolmate.complaints.models.BuildingComplaint
import com.github.essmehdi.schoolmate.complaints.models.Complaint
import com.github.essmehdi.schoolmate.complaints.models.FacilitiesComplaint
import com.github.essmehdi.schoolmate.complaints.models.RoomComplaint
import com.github.essmehdi.schoolmate.complaints.ui.ComplaintDetailsActivity
import com.github.essmehdi.schoolmate.complaints.ui.ComplaintEditorActivity
import com.github.essmehdi.schoolmate.complaints.viewmodels.ComplaintsViewModel
import com.github.essmehdi.schoolmate.databinding.ComplaintItemBinding
import com.github.essmehdi.schoolmate.shared.utils.Utils
import org.joda.time.format.DateTimeFormat

class ComplaintsListAdapter(var data: List<Complaint>?, val viewModel: ComplaintsViewModel?=null, val launcher: ActivityResultLauncher<Intent>?=null) : RecyclerView.Adapter<ComplaintsListAdapter.ComplaintsViewHolder>(){


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ComplaintsViewHolder {
        val binding = ComplaintItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ComplaintsViewHolder(binding)
    }

    override fun getItemCount(): Int {
        // show only 4 items in the list if complaintsViewModel is null (home activity)
        if(viewModel == null && data!=null && data!!.isNotEmpty()) return 4
        return data?.size?: 0
    }

    override fun onBindViewHolder(holder: ComplaintsViewHolder, position: Int) {
        data?.let { holder.bind(it[position]) }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateComplaints(newData: List<Complaint>) {
        data = newData
        notifyDataSetChanged()
    }


    inner class ComplaintsViewHolder(private val binding: ComplaintItemBinding) : RecyclerView.ViewHolder(binding.root),
        View.OnCreateContextMenuListener, MenuItem.OnMenuItemClickListener {

        init {
            binding.root.setOnCreateContextMenuListener(this)
        }

        fun bind(complaint: Complaint) {
            val formatter = DateTimeFormat.forPattern("yyyy-MM-dd")
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

        }

        override fun onCreateContextMenu(
            menu: ContextMenu?,
            v: View?,
            menuInfo: ContextMenu.ContextMenuInfo?
        ) {
            if(viewModel != null) {
                menu?.add(this.adapterPosition, 1, 1, binding.root.context.getString(R.string.label_complaint_item_context_menu_view))?.setOnMenuItemClickListener(this)
                menu?.add(this.adapterPosition, 2, 2, binding.root.context.getString(R.string.label_complaint_item_context_menu_edit))?.setOnMenuItemClickListener(this)
                menu?.add(this.adapterPosition, 3, 3, binding.root.context.getString(R.string.label_complaint_item_context_menu_delete))?.setOnMenuItemClickListener(this)
                if(data!![adapterPosition].complainant.id != viewModel.complainant.value?.id){
                    // Make the edit and delete options disabled
                    menu?.getItem(1)?.isEnabled = false
                    menu?.getItem(2)?.isEnabled = false
                }
            }
        }

        override fun onMenuItemClick(item: MenuItem): Boolean {
            val currentComplaint = data!![adapterPosition]
            return when (item.order) {
                1 -> {
                    val intent = Intent(binding.root.context, ComplaintDetailsActivity::class.java)
                    intent.putExtra("complaintId", currentComplaint.id)
                    launcher?.launch(intent)
                    true
                }
                2 -> {
                    val intent = Intent(binding.root.context, ComplaintEditorActivity::class.java)
                    // if the complaint is passed to the editor, it will be edited
                    // otherwise, a new complaint will be created
                    intent.putExtra("complaintId", currentComplaint.id)
                    launcher?.launch(intent)
                    true
                }
                3 -> {
                    val builder = AlertDialog.Builder(binding.root.context)
                    builder.setTitle(binding.root.context.getString(R.string.label_complaint_item_context_menu_delete))
                    builder.setMessage(binding.root.context.getString(R.string.confirm_delete_complaint))
                    builder.setPositiveButton(binding.root.context.getString(R.string.label_yes)) { _, _ ->
                        // Delete the complaint
                        viewModel?.deleteComplaint(currentComplaint.id!!)
                    }
                    builder.setNegativeButton(binding.root.context.getString(R.string.label_no)) { dialog, _ ->
                        // Dismiss the dialog
                        dialog.dismiss()
                    }
                    builder.create().show()
                    true
                }
                else -> false
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
    }

}