package com.github.essmehdi.schoolmate.complaints.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.github.essmehdi.schoolmate.R
import com.github.essmehdi.schoolmate.complaints.models.Complaint
import com.github.essmehdi.schoolmate.complaints.models.Handler
import com.github.essmehdi.schoolmate.complaints.viewmodels.HandlerComplaintsViewModel
import com.github.essmehdi.schoolmate.databinding.ComplaintHandlerItemBinding

class HandlersListAdapter(var data: List<Handler>, val viewModel: HandlerComplaintsViewModel, val assign: Boolean) : RecyclerView.Adapter<HandlersListAdapter.HandlersListViewHolder>() {

    lateinit var onClickAssignHandlerListenerDissmisser: OnClickAssignHandlerListenerDissmisser

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HandlersListViewHolder {
        val binding = ComplaintHandlerItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return HandlersListViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return data.size
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateData(newData: List<Handler>) {
        this.data = newData
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: HandlersListViewHolder, position: Int) {
        data.let { holder.bind(it[position]) }
    }

    inner class HandlersListViewHolder(val binding: ComplaintHandlerItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(handler: Handler) {
            if(handler.handler.id != viewModel.currentHandler.value?.handler?.id) {
                binding.handlerName.text = handler.handler.fullName
                binding.handledComplaints.text = binding.root.context.getString(R.string.handled_complaints_preview, handler.complaintsCount.toString())
                if(assign) {
                    binding.assignComplaintHandler.visibility = ViewGroup.VISIBLE
                    binding.seeHandlerComplaints.visibility = ViewGroup.GONE
                    binding.assignComplaintHandler.setOnClickListener {
                         // TODO
                    }
                } else {
                    binding.assignComplaintHandler.visibility = ViewGroup.GONE
                    binding.seeHandlerComplaints.visibility = ViewGroup.VISIBLE
                    binding.seeHandlerComplaints.setOnClickListener {
                        viewModel.changeHandler(handler.handler.id)
                        onClickAssignHandlerListenerDissmisser.onClickAssignHandlerAndDismiss()
                    }
                }
            } else {
                binding.root.visibility = ViewGroup.GONE
                binding.root.layoutParams = RecyclerView.LayoutParams(0, 0)
                // Remove the decoration from the item
                binding.root.background = null
            }
        }
    }
}

interface  OnClickAssignHandlerListenerDissmisser {
    fun onClickAssignHandlerAndDismiss()
}