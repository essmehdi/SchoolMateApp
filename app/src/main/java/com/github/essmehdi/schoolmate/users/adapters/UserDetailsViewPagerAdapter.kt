package com.github.essmehdi.schoolmate.users.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.github.essmehdi.schoolmate.users.ui.UserDocumentsFragment
import com.github.essmehdi.schoolmate.users.ui.UserPlacesFragment

class UserDetailsViewPagerAdapter(fragmentActivity: FragmentActivity): FragmentStateAdapter(fragmentActivity) {
  override fun getItemCount(): Int = 2

  override fun createFragment(position: Int): Fragment = when (position) {
    0 -> UserDocumentsFragment.newInstance()
    else -> UserPlacesFragment.newInstance()
  }
}