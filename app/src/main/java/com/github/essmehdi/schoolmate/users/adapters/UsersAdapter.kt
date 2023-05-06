package com.github.essmehdi.schoolmate.users.adapters

import android.annotation.SuppressLint
import android.content.Intent
import android.util.TypedValue
import android.view.*
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.github.essmehdi.schoolmate.R
import com.github.essmehdi.schoolmate.databinding.UserRowBinding
import com.github.essmehdi.schoolmate.users.models.User
import com.github.essmehdi.schoolmate.users.models.UserRole
import com.github.essmehdi.schoolmate.users.ui.UserDetailsActivity
import java.util.*

class UsersAdapter(var data: List<User>): RecyclerView.Adapter<UsersAdapter.UserRowViewHolder>() {

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserRowViewHolder {
    val binding =
      UserRowBinding
        .inflate(LayoutInflater.from(parent.context), parent, false)
    return UserRowViewHolder(binding)
  }

  override fun getItemCount(): Int {
    return data.size
  }

  override fun onBindViewHolder(holder: UserRowViewHolder, position: Int) {
    holder.bind(data[position])
  }

  @SuppressLint("NotifyDataSetChanged")
  fun updateData(newData: List<User>) {
    this.data = newData
    notifyDataSetChanged()
  }

  inner class UserRowViewHolder(private val binding: UserRowBinding): RecyclerView.ViewHolder(binding.root) {

    fun bind(user: User) {
      @ColorRes val primaryColor = TypedValue().apply {
        itemView.context.theme.resolveAttribute(com.google.android.material.R.attr.colorPrimary, this, true)
      }.resourceId

      @ColorRes val secondaryColor = TypedValue().apply {
        itemView.context.theme.resolveAttribute(com.google.android.material.R.attr.colorSecondary, this, true)
      }.resourceId

      binding.userRowFullNameText.text = user.fullName
      binding.userRowEmailText.text = user.email
      binding.userRowRoleText.apply {
        text = context.getString(user.roleNameString)
        when (user.role) {
          UserRole.ADEI -> {
            setTextColor(ContextCompat.getColor(context, R.color.ensias))
          }
          UserRole.MODERATOR -> {
            setTextColor(ContextCompat.getColor(context, secondaryColor))
          }
          else -> {
            setTextColor(ContextCompat.getColor(context, primaryColor))
          }
        }
      }

      binding.root.apply {
        setOnClickListener {
          context.startActivity(Intent(context, UserDetailsActivity::class.java).apply {
            putExtra("userId", user.id)
          })
        }
      }
    }
  }
}