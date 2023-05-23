package com.github.essmehdi.schoolmate.complaints.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.github.essmehdi.schoolmate.complaints.ui.AllComplaintsFragment
import com.github.essmehdi.schoolmate.complaints.ui.UserComplaintsFragment

class ComplaintsViewPagerAdapter(fragmentActivity: FragmentActivity) : FragmentStateAdapter(fragmentActivity) {

    override fun getItemCount(): Int {
        return 2
    }

    override fun createFragment(position: Int): Fragment {
        return when(position) {
            0 -> UserComplaintsFragment.newInstance()
            1 -> AllComplaintsFragment.newInstance()
            else -> throw IllegalStateException("Invalid position $position")
        }
    }

}