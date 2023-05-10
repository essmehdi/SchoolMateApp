package com.github.essmehdi.schoolmate.alerts.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.ViewModel
import androidx.recyclerview.widget.RecyclerView
import com.github.essmehdi.schoolmate.alerts.models.Alert
import com.github.essmehdi.schoolmate.alerts.viewmodels.ConfirmAlertViewModel
import com.github.essmehdi.schoolmate.databinding.ConfirmalertitemBinding

class ConfirmAlertsListAdapter(var data:List<Alert>?,
                               val viewModel: ConfirmAlertViewModel,
                               val shared:Boolean=false): RecyclerView.Adapter<ConfirmAlertsListAdapter.MyAlertsListViewHolder>(){


    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ConfirmAlertsListAdapter.MyAlertsListViewHolder {
        val binding=ConfirmalertitemBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return MyAlertsListViewHolder(binding)
    }
    override fun onBindViewHolder(
        holder: ConfirmAlertsListAdapter.MyAlertsListViewHolder,
        position: Int
    ) {
       data?.let { holder.bind(it[position]) }
    }
    override fun getItemCount(): Int {
           return data?.size?:0
    }
    fun updateData(data:List<Alert>){
        this.data=data
        notifyDataSetChanged()
    }
    inner class MyAlertsListViewHolder(private val binding:ConfirmalertitemBinding):RecyclerView.ViewHolder(binding.root){
        fun bind(alert: Alert) {
            binding.alertTitre.text = alert.title
            binding.alertDescription.text = alert.description
            binding.confirmButton.setOnClickListener {
                viewModel.confirmAlert(alert.id)
            }
            binding.cancelButton.setOnClickListener {
                viewModel.rejectAlert(alert.id)
            }
        }
    }

}