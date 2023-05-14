package com.github.essmehdi.schoolmate.alerts.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import com.github.essmehdi.schoolmate.R

import com.github.essmehdi.schoolmate.alerts.adapters.AlertViewPagerAdapter
import com.github.essmehdi.schoolmate.alerts.viewmodels.AlertsViewModel
import com.github.essmehdi.schoolmate.alerts.viewmodels.MyAlertsViewModel
import com.github.essmehdi.schoolmate.databinding.ActivityAlertBinding
import com.github.essmehdi.schoolmate.shared.api.BaseResponse
import com.github.essmehdi.schoolmate.users.models.UserRole
import com.google.android.material.tabs.TabLayoutMediator

class AlertActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAlertBinding
    private lateinit var editorLauncher: ActivityResultLauncher<Intent>
    private val viewModel: AlertsViewModel by viewModels()
    private lateinit var pagerAdapter: AlertViewPagerAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityAlertBinding.inflate(layoutInflater)
        setContentView(binding.root)

        editorLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                // TODO
            }
        }

        binding.fab.setOnClickListener {
            val intent = Intent(this, AddAlertActivity::class.java)
            editorLauncher.launch(intent)
        }
        viewModel.fetchUser()
        setupViewPager()
        viewModel.user.observe(this){
            if (it is BaseResponse.Success && it.data!!.role== UserRole.MODERATOR){
                pagerAdapter.admin=true
                pagerAdapter.notifyDataSetChanged()
            }
        }
    }
    private fun setupViewPager(){
        pagerAdapter = AlertViewPagerAdapter(this)
        binding.alertViewPager.adapter= pagerAdapter
        TabLayoutMediator(binding.alertTabLayout,binding.alertViewPager){tab,position->
            when(position){
                0->tab.text=getString(R.string.my_alerts)
                1->tab.text=getString(R.string.published_alerts)
                else->tab.text=getString(R.string.confirm_alerts)
            }
        }.attach()

    }
}