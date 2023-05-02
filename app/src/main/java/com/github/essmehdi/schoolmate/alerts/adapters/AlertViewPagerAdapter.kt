package com.github.essmehdi.schoolmate.alerts.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.github.essmehdi.schoolmate.alerts.ui.MyAlertsFragment
import com.github.essmehdi.schoolmate.alerts.ui.PublishedAlertsFragment

class AlertViewPagerAdapter(fragmentActivity: FragmentActivity): FragmentStateAdapter(fragmentActivity) {
    override fun getItemCount(): Int = 2

    override fun createFragment(position: Int): Fragment = when (position) {
        0 -> MyAlertsFragment.newInstance()
        else -> PublishedAlertsFragment.newInstance()
    }
}