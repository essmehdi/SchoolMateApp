package com.github.essmehdi.schoolmate.complaints.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import com.github.essmehdi.schoolmate.R
import com.github.essmehdi.schoolmate.complaints.adapters.ComplaintsViewPagerAdapter
import com.github.essmehdi.schoolmate.complaints.ui.ComplaintDetailsActivity.Companion.RESULT_ACTION_DELETED
import com.github.essmehdi.schoolmate.complaints.viewmodels.ComplaintsViewModel
import com.github.essmehdi.schoolmate.databinding.ActivityComplaintsBinding
import com.github.essmehdi.schoolmate.shared.api.BaseResponse
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayoutMediator

class ComplaintsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityComplaintsBinding
    lateinit var launcher: ActivityResultLauncher<Intent>
    private val viewModel: ComplaintsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityComplaintsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.complaintsToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_navigation_left)

        launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                viewModel.refresh()
            } else if(result.resultCode == RESULT_ACTION_DELETED) {
                Snackbar.make(binding.root, R.string.success_complaint_deletion, Snackbar.LENGTH_SHORT).show()
                viewModel.refresh()
            }
        }

        viewModel.fetchComplainant()
        viewModel.fetchUserComplaints("me")
        viewModel.fetchComplaints()

        setupViewPager()
        binding.complaintAddButton.setOnClickListener {
            goToComplaintEditor()
        }

        viewModel.deleteStatus.observe(this) {
            when (it) {
                is BaseResponse.Success -> {
                    Snackbar.make(binding.root, R.string.success_complaint_deletion, Snackbar.LENGTH_SHORT).show()
                    viewModel.refresh()
                }
                is BaseResponse.Loading -> {
                    Snackbar.make(binding.root, R.string.loading_complaint_deletion, Snackbar.LENGTH_INDEFINITE).show()
                }
                is BaseResponse.Error -> {
                    Snackbar.make(binding.root, R.string.error_complaint_deletion, Snackbar.LENGTH_SHORT).show()
                }
                else -> {}
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.complaints_list_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.complaints_menu_sort_asc -> {
                item.isChecked = !item.isChecked
                viewModel.changeOrder("asc")
                true
            }
            R.id.complaints_menu_sort_desc -> {
                item.isChecked = !item.isChecked
                viewModel.changeOrder("desc")
                true
            }
            R.id.complaints_type_menu_all -> {
                item.isChecked = !item.isChecked
                viewModel.changeComplaintType("all")
                true
            }
            R.id.complaints_type_menu_building -> {
                item.isChecked = !item.isChecked
                viewModel.changeComplaintType("building")
                true
            }
            R.id.complaints_type_menu_room -> {
                item.isChecked = !item.isChecked
                viewModel.changeComplaintType("room")
                true
            }
            R.id.complaints_type_menu_facilities -> {
                item.isChecked = !item.isChecked
                viewModel.changeComplaintType("facilities")
                true
            }
            else -> super.onOptionsItemSelected(item)

        }
    }

    private fun setupViewPager() {
        binding.complaintsViewPager.adapter = ComplaintsViewPagerAdapter(this)
        TabLayoutMediator(binding.complaintsTabLayout, binding.complaintsViewPager) { tab, position ->
            tab.text = when (position) {
                0 -> getString(R.string.user_complaints_title)
                else -> getString(R.string.all_complaints_title)
            }
        }.attach()
    }

    private fun goToComplaintEditor() {
        // Let's create a dialog to ask the user for the complaint type (building, room, facility)
        val builder = AlertDialog.Builder(this)
        builder.setView(R.layout.complaint_type_dialog)
        // access the card view in the dialog
        val dialog = builder.create()
        // set the background of the dialog to transparent
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.show()
        dialog.findViewById<androidx.cardview.widget.CardView>(R.id.building_complaint_card)?.setOnClickListener {
            val intent = Intent(this, ComplaintEditorActivity::class.java)
            intent.putExtra("complaint_type", "building")
            launcher.launch(intent)
            dialog.dismiss()
        }
        dialog.findViewById<androidx.cardview.widget.CardView>(R.id.room_complaint_card)?.setOnClickListener {
            val intent = Intent(this, ComplaintEditorActivity::class.java)
            intent.putExtra("complaint_type", "room")
            launcher.launch(intent)
            dialog.dismiss()
        }
        dialog.findViewById<androidx.cardview.widget.CardView>(R.id.facilities_complaint_card)?.setOnClickListener {
            val intent = Intent(this, ComplaintEditorActivity::class.java)
            intent.putExtra("complaint_type", "facility")
            launcher.launch(intent)
            dialog.dismiss()
        }

    }
}