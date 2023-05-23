package com.github.essmehdi.schoolmate.placesuggestions.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.github.essmehdi.schoolmate.placesuggestions.ui.AllSuggestionsFragment
import com.github.essmehdi.schoolmate.placesuggestions.ui.MySuggestionsFragment

class SuggestionsViewPagerAdapter(fragmentActivity: FragmentActivity): FragmentStateAdapter(fragmentActivity) {

    override fun getItemCount(): Int {
        return 2
    }

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> AllSuggestionsFragment.newInstance()
            1 -> MySuggestionsFragment.newInstance()
            else -> throw IllegalStateException("Invalid position $position")
        }
    }
}