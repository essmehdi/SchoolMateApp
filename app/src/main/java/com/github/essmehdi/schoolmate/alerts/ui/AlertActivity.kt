package com.github.essmehdi.schoolmate.alerts.ui
import android.content.Intent
import android.view.Menu
import android.view.MenuItem
import android.view.View

import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.essmehdi.schoolmate.alerts.viewmodels.AlertViewModel
import com.github.essmehdi.schoolmate.alerts.adapters.AlertListAdapter
import com.github.essmehdi.schoolmate.shared.api.BaseResponse
import com.google.android.material.chip.Chip
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.github.essmehdi.schoolmate.R
import com.github.essmehdi.schoolmate.databinding.ActivityAlertBinding

class AlertActivity : AppCompatActivity(){

    private lateinit var binding: ActivityAlertBinding
    private lateinit var viewModel: AlertViewModel
    private lateinit var adapter: AlertListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAlertBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}

