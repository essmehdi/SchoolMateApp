package com.github.essmehdi.schoolmate.alerts.adapters

import android.annotation.SuppressLint
import android.content.Intent
import android.util.Log
import android.view.ContextMenu
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.recyclerview.widget.RecyclerView
import com.github.essmehdi.schoolmate.R
import com.github.essmehdi.schoolmate.alerts.models.Alert
import com.github.essmehdi.schoolmate.alerts.ui.AlertDetailsActivity
import com.github.essmehdi.schoolmate.alerts.viewmodels.MyAlertsViewModel
import com.github.essmehdi.schoolmate.databinding.AlertItemBinding
import com.github.essmehdi.schoolmate.shared.utils.Utils
import org.joda.time.format.DateTimeFormat

class MyAlertsListAdapter(

    var data:List<Alert>?,
    val viewModel: ViewModel,
    val userAlerts:Boolean=false): RecyclerView.Adapter<MyAlertsListAdapter.MyAlertsListViewHolder>(){


    lateinit var listener: OnEditMenuItemClickedListener
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): MyAlertsListAdapter.MyAlertsListViewHolder {
        val binding=AlertItemBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return MyAlertsListViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: MyAlertsListAdapter.MyAlertsListViewHolder,
        position: Int
    ) {
       data?.let { holder.bind(it[position]) }
    }

    override fun getItemCount(): Int {
           return data?.size?:0
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateData(data:List<Alert>){
        this.data=data
        notifyDataSetChanged()
    }
    fun setOnEditMenuItemClickedListener(listener: OnEditMenuItemClickedListener) {
        this.listener = listener
    }
    inner class MyAlertsListViewHolder(private val binding:AlertItemBinding):RecyclerView.ViewHolder(binding.root),
        View.OnCreateContextMenuListener, MenuItem.OnMenuItemClickListener {


        init {
            binding.root.setOnCreateContextMenuListener(this)
        }

        fun bind(alert: Alert) {
            val formatter = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZZ")
            val dateTime = formatter.parseDateTime(alert.date)

            binding.alertTitre.text = alert.title
            binding.alertDescription.text = Utils.calculatePastTime(dateTime.toDate(), binding.root.context) ?: ""
            binding.alertStatus.text = alert.status.toString()
            binding.alertStatus.setTextColor(ContextCompat.getColor(binding.root.context, getColor(alert.status.toString())))
            binding.root.setOnClickListener {
                val intent = Intent(binding.root.context, AlertDetailsActivity::class.java)
                intent.putExtra("alertId", alert.id)
                binding.root.context.startActivity(intent)
            }
        }

        override fun onCreateContextMenu(
            menu: ContextMenu?,
            v: View?,
            menuInfo: ContextMenu.ContextMenuInfo?
        ) {
            if (!userAlerts) return
            menu?.add(Menu.NONE, 1, 1, "Modifier")?.setOnMenuItemClickListener(this)
            menu?.add(Menu.NONE, 2, 2, "Supprimer")?.setOnMenuItemClickListener(this)

        }

        override fun onMenuItemClick(item: MenuItem): Boolean {
            Log.d("MyAlertsListAdapter", "onMenuItemClick: ${data!![bindingAdapterPosition]}")
            val currentAlert = data!![bindingAdapterPosition]
            return when (item.itemId) {
                1 -> {
                   listener.onEditMenuItemClickedListener(currentAlert)
                    true
                }

                2 -> {
                    //cast to AlertViewModel
                    (viewModel as MyAlertsViewModel).deleteAlert(currentAlert)
                    true

                }

                else ->
                    false
            }

        }
        }

    private fun getColor(status: String): Int {
        return when (status) {
            "PENDING" -> R.color.alerts_pending
            "CONFIRMED" -> R.color.alerts_confirmed
            "CANCELLED" -> R.color.alerts_canceled
            else -> {
                R.color.alerts_pending
            }
        }
    }

}


interface OnEditMenuItemClickedListener {
    fun onEditMenuItemClickedListener(alert: Alert)
}
