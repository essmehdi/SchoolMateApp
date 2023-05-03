package com.github.essmehdi.schoolmate.alerts.adapters

import android.annotation.SuppressLint
import android.view.ContextMenu
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModel
import androidx.recyclerview.widget.RecyclerView
import com.github.essmehdi.schoolmate.alerts.models.Alert
import com.github.essmehdi.schoolmate.alerts.viewmodels.AlertViewModel
import com.github.essmehdi.schoolmate.databinding.AlertItemBinding

class MyAlertsListAdapter(var data:List<Alert>?,val viewModel: AlertViewModel,val shared:Boolean=false): RecyclerView.Adapter<MyAlertsListAdapter.MyAlertsListViewHolder>(){


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
    inner class MyAlertsListViewHolder(private val binding:AlertItemBinding):RecyclerView.ViewHolder(binding.root),
        View.OnCreateContextMenuListener, MenuItem.OnMenuItemClickListener {


        init {
            binding.root.setOnCreateContextMenuListener(this)
        }

        fun bind(alert: Alert) {
            binding.alertTitre.text = alert.title
            binding.alertDescription.text = alert.description
            binding.alertStatus.text = alert.status.toString()

        }

        override fun onCreateContextMenu(
            menu: ContextMenu?,
            v: View?,
            menuInfo: ContextMenu.ContextMenuInfo?
        ) {
            if (shared) return
            menu?.add(Menu.NONE, 1, 1, "Modifier")?.setOnMenuItemClickListener(this)
            menu?.add(Menu.NONE, 2, 2, "Supprimer")?.setOnMenuItemClickListener(this)

        }

        override fun onMenuItemClick(item: MenuItem): Boolean {
            val currentAlert = data!![adapterPosition]
            return when (item.itemId) {
                1 -> {
                    // viewModel.updateAlert(currentAlert)
                    true
                }

                2 -> {
                    viewModel.deleteAlert(currentAlert)
                    true

                }

                else ->
                    false
            }

        }
        }
}



