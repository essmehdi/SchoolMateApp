package com.github.essmehdi.schoolmate.alerts.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.github.essmehdi.schoolmate.alerts.ui.ConfirmAlertFragment
import com.github.essmehdi.schoolmate.alerts.ui.MyAlertsFragment
import com.github.essmehdi.schoolmate.alerts.ui.PublishedAlertsFragment

class AlertViewPagerAdapter(fragmentActivity: FragmentActivity, var admin:Boolean = false ): FragmentStateAdapter(fragmentActivity) {
    override fun getItemCount(): Int = if (admin) 3 else 2

    override fun createFragment(position: Int,): Fragment = when (position) {
        0 -> MyAlertsFragment.newInstance()
        1 ->PublishedAlertsFragment.newInstance()
        else -> ConfirmAlertFragment.newInstance()

    }
}