package com.github.essmehdi.schoolmate.alerts.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import com.github.essmehdi.schoolmate.R
import com.github.essmehdi.schoolmate.alerts.viewmodels.AlertDetailsViewModel
import com.github.essmehdi.schoolmate.alerts.viewmodels.AlertsViewModel
import com.github.essmehdi.schoolmate.databinding.ActivityAlertDetailsBinding
import com.github.essmehdi.schoolmate.shared.api.BaseResponse

class AlertDetailsActivity : AppCompatActivity() {
    private val viewModel: AlertDetailsViewModel by viewModels()
    private lateinit var binding: ActivityAlertDetailsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAlertDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.alertDetailsToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_navigation_left)

        if(intent.hasExtra("alertId")){
            viewModel.id.value = intent.getLongExtra("alertId",0)
            viewModel.loadAlert()
        }
        //var user = viewModel1.user.value
        viewModel.alert.observe(this){
            if(it is BaseResponse.Success){
                binding.alertTitle.text = it.data!!.title
                binding.alertDate.text= it.data.date
                binding.alertType.text = it.data.type.toString()
                binding.alertStatus.text = it.data.status.toString()
                binding.alertMaker.text = it.data.user.fullName
                binding.alertDescription.text = it.data.description
                //clicklisner to redirect to the AlertMapFragment
                binding.assignAlertMapButton.setOnClickListener {
                    // show the map fragment
                    val fragment = AlertMapFragment(viewModel.alert.value!!.data!!.coordinates, viewModel.alert.value!!.data!!.title)
                    fragment.show(supportFragmentManager, "AlertMapFragment")
                }
                showLoading(false)
            }
            else if(it is BaseResponse.Loading){
                showLoading()
            }
            else if(it is BaseResponse.Error){
                handleError(0)
            }
        }

    }
    private fun showLoading(show: Boolean = true) {
        binding.alertLoading.loadingOverlay.isVisible = show
    }

    private fun handleError(code: Int) {
        binding.alertLoading.loadingErrorMessage.text = getString(R.string.unknown_error_occurred)
        binding.alertLoading.loadingErrorMessage.visibility = View.VISIBLE
        binding.alertLoading.loadingProgressBar.visibility = View.GONE
        showLoading()
    }
}