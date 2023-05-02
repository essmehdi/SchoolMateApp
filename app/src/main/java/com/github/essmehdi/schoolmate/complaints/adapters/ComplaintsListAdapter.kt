package com.github.essmehdi.schoolmate.complaints.adapters

import android.content.Intent
import android.view.*
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.github.essmehdi.schoolmate.R
import com.github.essmehdi.schoolmate.complaints.models.Complaint
import com.github.essmehdi.schoolmate.complaints.viewmodels.ComplaintsViewModel
import com.github.essmehdi.schoolmate.databinding.ComplaintItemBinding
import com.github.essmehdi.schoolmate.shared.utils.Utils
import org.joda.time.format.DateTimeFormat

class ComplaintsListAdapter(var data: List<Complaint>?, val viewModel: ComplaintsViewModel?=null) : RecyclerView.Adapter<ComplaintsListAdapter.ComplaintsViewHolder>(){


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ComplaintsViewHolder {
        val binding = ComplaintItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ComplaintsViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return data?.size?: 0
    }

    override fun onBindViewHolder(holder: ComplaintsViewHolder, position: Int) {
        data?.let { holder.bind(it[position]) }
    }

    inner class ComplaintsViewHolder(private val binding: ComplaintItemBinding) : RecyclerView.ViewHolder(binding.root),
        View.OnCreateContextMenuListener, MenuItem.OnMenuItemClickListener {

        init {
            binding.root.setOnCreateContextMenuListener(this)
        }

        fun bind(complaint: Complaint) {
            val formatter = DateTimeFormat.forPattern("yyyy-MM-dd")
            val date = formatter.parseDateTime(complaint.getDate())
            val dateText =
                ("Submitted " + Utils.calculatePastTime(date.toDate(), binding.root.context))

            binding.complaintTitle.text = complaint.getDescription().subSequence(0, 20)
            binding.complaintDate.text = dateText
            binding.complaintStatus.text = complaint.getStatus().name
            when (complaint.getStatus().name) {
                "PENDING" -> {
                    binding.complaintStatus.setTextColor(ContextCompat.getColor(binding.root.context, R.color.complaint_status_pending))
                }
                "ASSIGNED" -> {
                    binding.complaintStatus.setTextColor(ContextCompat.getColor(binding.root.context, R.color.complaint_status_assigned))
                }
                "CONFIRMED" -> {
                    binding.complaintStatus.setTextColor(ContextCompat.getColor(binding.root.context, R.color.complaint_status_confirmed))
                }
                "REJECTED" -> {
                    binding.complaintStatus.setTextColor(ContextCompat.getColor(binding.root.context, R.color.complaint_status_rejected))
                }
                "RESOLVED" -> {
                    binding.complaintStatus.setTextColor(ContextCompat.getColor(binding.root.context, R.color.complaint_status_resolved))
                }
                "UNRESOLVED" -> {
                    binding.complaintStatus.setTextColor(ContextCompat.getColor(binding.root.context, R.color.complaint_status_unresolved))
                }
            }

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
            }
        }

        override fun onMenuItemClick(item: MenuItem): Boolean {
            val currentComplaint = data!![adapterPosition]
            return when (item.order) {
                1 -> {
                    viewModel?.getComplaint(currentComplaint.getId()!!)
                    true
                }
                2 -> {
                    //val intent: Intent = Intent(binding.root.context, ComplaintEditorActivity::class.java)
                    true
                }
                3 -> {
                    viewModel?.deleteComplaint(currentComplaint.getId()!!)
                    true
                }
                else -> false
            }
        }
    }

}