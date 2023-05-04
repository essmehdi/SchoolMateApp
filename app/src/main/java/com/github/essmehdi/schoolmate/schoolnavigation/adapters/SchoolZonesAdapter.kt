package com.github.essmehdi.schoolmate.schoolnavigation.adapters

import android.annotation.SuppressLint
import android.content.Intent
import android.util.TypedValue
import android.view.*
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.github.essmehdi.schoolmate.R
import com.github.essmehdi.schoolmate.databinding.SchoolZoneItemBinding
import com.github.essmehdi.schoolmate.databinding.UserRowBinding
import com.github.essmehdi.schoolmate.schoolnavigation.models.SchoolZone
import com.github.essmehdi.schoolmate.schoolnavigation.ui.SchoolZoneEditorFragment
import com.github.essmehdi.schoolmate.schoolnavigation.ui.SchoolZonesActivity
import com.github.essmehdi.schoolmate.users.models.User
import com.github.essmehdi.schoolmate.users.models.UserRole
import com.github.essmehdi.schoolmate.users.ui.UserDetailsActivity
import java.util.*

class SchoolZonesAdapter(var data: List<SchoolZone>): RecyclerView.Adapter<SchoolZonesAdapter.SchoolZoneItemViewHolder>() {

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SchoolZoneItemViewHolder {
    val binding =
      SchoolZoneItemBinding
        .inflate(LayoutInflater.from(parent.context), parent, false)
    return SchoolZoneItemViewHolder(binding)
  }

  override fun getItemCount(): Int {
    return data.size
  }

  override fun onBindViewHolder(holder: SchoolZoneItemViewHolder, position: Int) {
    holder.bind(data[position])
  }

  @SuppressLint("NotifyDataSetChanged")
  fun updateData(newData: List<SchoolZone>) {
    this.data = newData
    notifyDataSetChanged()
  }

  inner class SchoolZoneItemViewHolder(private val binding: SchoolZoneItemBinding): RecyclerView.ViewHolder(binding.root) {
    fun bind(schoolZone: SchoolZone) {
      binding.schoolZoneItemName.text = schoolZone.name
      binding.schoolZoneItemDescription.text = schoolZone.description

      binding.root.setOnClickListener {
        val fragment = SchoolZoneEditorFragment.newInstance(schoolZone)
        fragment.show((binding.root.context as SchoolZonesActivity).supportFragmentManager, fragment.tag)
      }
    }
  }
}