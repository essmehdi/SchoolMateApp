package com.github.essmehdi.schoolmate.alerts.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.viewModels
import androidx.activity.viewModels
import com.github.essmehdi.schoolmate.R

import com.github.essmehdi.schoolmate.alerts.adapters.AlertViewPagerAdapter
import com.github.essmehdi.schoolmate.alerts.viewmodels.AlertViewModel
import com.github.essmehdi.schoolmate.databinding.ActivityAlertBinding
import com.google.android.material.tabs.TabLayoutMediator

class AlertActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAlertBinding
    //private val viewModel: AlertViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityAlertBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupViewPager()
    }
    private fun setupViewPager(){
        binding.alertViewPager.adapter= AlertViewPagerAdapter(this)
        TabLayoutMediator(binding.alertTabLayout,binding.alertViewPager){tab,position->
            when(position){
                0->tab.text=getString(R.string.my_alerts)
                else->tab.text=getString(R.string.published_alerts)
            }
        }.attach()
    }
}