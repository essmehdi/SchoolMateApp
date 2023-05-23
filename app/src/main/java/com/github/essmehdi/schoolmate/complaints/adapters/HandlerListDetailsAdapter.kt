package com.github.essmehdi.schoolmate.complaints.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.github.essmehdi.schoolmate.R
import com.github.essmehdi.schoolmate.complaints.models.Handler
import com.github.essmehdi.schoolmate.complaints.viewmodels.ComplaintDetailsViewModel
import com.github.essmehdi.schoolmate.databinding.ComplaintHandlerItemBinding

class HandlerListDetailsAdapter(var data: List<Handler>, val viewModel: ComplaintDetailsViewModel) : RecyclerView.Adapter<HandlerListDetailsAdapter.HandlersListDetailsViewHolder>() {

    lateinit var onClickAssignAndDissmiss: OnClickAssignAndDissmiss

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HandlersListDetailsViewHolder {
        val binding = ComplaintHandlerItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return HandlersListDetailsViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return data.size
    }

    override fun onBindViewHolder(
        holder: HandlersListDetailsViewHolder,
        position: Int
    ) {
        data.let { holder.bind(it[position]) }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateData(newData: List<Handler>) {
        this.data = newData
        notifyDataSetChanged()
    }


    inner class HandlersListDetailsViewHolder(val binding: ComplaintHandlerItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(handler: Handler) {
            binding.handlerName.text = handler.handler.fullName
            binding.handledComplaints.text = binding.root.context.getString(R.string.handled_complaints_preview, handler.complaintsCount.toString())
            binding.assignComplaintHandler.visibility = ViewGroup.VISIBLE
            binding.seeHandlerComplaints.visibility = ViewGroup.GONE
            binding.assignComplaintHandler.setOnClickListener {
                onClickAssignAndDissmiss.onClickAssignAndDissmiss(handler.handler.fullName, handler.handler.id)
            }


        }
    }
}

interface  OnClickAssignAndDissmiss {
    fun onClickAssignAndDissmiss(handlerName: String? = null, handlerId: Long? = null)
}