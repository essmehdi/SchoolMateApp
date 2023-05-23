package com.github.essmehdi.schoolmate.alerts.adapters

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.ViewModel
import androidx.recyclerview.widget.RecyclerView
import com.github.essmehdi.schoolmate.alerts.models.Alert
import com.github.essmehdi.schoolmate.alerts.ui.AlertDetailsActivity
import com.github.essmehdi.schoolmate.alerts.viewmodels.ConfirmAlertViewModel
import com.github.essmehdi.schoolmate.databinding.ConfirmalertitemBinding
import com.github.essmehdi.schoolmate.shared.utils.Utils
import com.google.android.material.snackbar.Snackbar
import org.joda.time.format.DateTimeFormat

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
            val formatter = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZZ")
            val dateTime = formatter.parseDateTime(alert.date)

            binding.alertTitre.text = alert.title
            binding.alertDescription.text = Utils.calculatePastTime(dateTime.toDate(), binding.root.context) ?: ""
            binding.confirmButton.setOnClickListener {
                viewModel.confirmAlert(alert.id)
                // show snack bar
                Snackbar.make(binding.root, "Alert confirmed", Snackbar.LENGTH_LONG).show()
            }
            binding.cancelButton.setOnClickListener {
                viewModel.rejectAlert(alert.id)
                // show snack bar
                Snackbar.make(binding.root, "Alert rejected", Snackbar.LENGTH_LONG).show()
            }

            binding.root.setOnClickListener {
                val intent = Intent(binding.root.context, AlertDetailsActivity::class.java)
                intent.putExtra("alertId", alert.id)
                binding.root.context.startActivity(intent)
            }
        }
    }

}