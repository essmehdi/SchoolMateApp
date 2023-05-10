package com.github.essmehdi.schoolmate.complaints.ui

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.essmehdi.schoolmate.R
import com.github.essmehdi.schoolmate.complaints.adapters.HandlerComplaintsAdapter
import com.github.essmehdi.schoolmate.complaints.adapters.HandlersListAdapter
import com.github.essmehdi.schoolmate.complaints.adapters.OnClickDissmiss
import com.github.essmehdi.schoolmate.complaints.enumerations.ComplaintStatus
import com.github.essmehdi.schoolmate.complaints.viewmodels.HandlerComplaintsViewModel
import com.github.essmehdi.schoolmate.databinding.ActivityHandlerComplaintsBinding
import com.github.essmehdi.schoolmate.databinding.ComplaintHandlersListBinding
import com.github.essmehdi.schoolmate.shared.api.BaseResponse

class HandlerComplaintsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHandlerComplaintsBinding
    private val viewModel: HandlerComplaintsViewModel by viewModels()
    private lateinit var complaintsAdapter: HandlerComplaintsAdapter
    private lateinit var handlersListAdapter: HandlersListAdapter
    private lateinit var launcher: ActivityResultLauncher<Intent>

    @SuppressLint("NotifyDataSetChanged")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHandlerComplaintsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.handlerComplaintsToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_navigation_left)

        binding.allComplaintsSwipeRefresh.layoutTransition?.setAnimateParentHierarchy(false)
        binding.allComplaintsSwipeRefresh.setOnRefreshListener {
            viewModel.refresh()
            // cancel the refresh animation after the data is fetched
            binding.allComplaintsSwipeRefresh.isRefreshing = false
        }

        launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                viewModel.refresh()
            }
        }

        viewModel.fetchCurrentHandler()
        viewModel.fetchComplaints()

        // Initialize recycler view adapter
        complaintsAdapter = HandlerComplaintsAdapter(listOf(), viewModel, launcher)

        binding.handlerComplaintsList.complaintsListMain.apply {
            adapter = complaintsAdapter
            addItemDecoration(DividerItemDecoration(binding.root.context, DividerItemDecoration.VERTICAL))
            layoutManager = LinearLayoutManager(
                this@HandlerComplaintsActivity,
                LinearLayoutManager.VERTICAL,
                false
            )
            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    if (dy > 0) {
                        if (viewModel.currentPageStatus.value is BaseResponse.Loading) {
                            return
                        }
                        val visibleItemCount = layoutManager?.childCount ?: 0
                        val totalItemCount = layoutManager?.itemCount ?: 0
                        val pastVisibleItems =
                            (layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
                        if (visibleItemCount + pastVisibleItems >= totalItemCount) {
                            viewModel.fetchComplaints()
                        }
                    }
                }
            })
        }

        viewModel.complaints.observe(this){complaints ->
            complaints?.let {complaintsAdapter.updateData(it)}
        }

        viewModel.showEmpty.observe(this) {
            showEmpty(it)
        }

        viewModel.currentPageStatus.observe(this) { currentPage ->
            when (currentPage) {
                is BaseResponse.Loading -> {
                    if (viewModel.complaints.value == null || viewModel.complaints.value!!.isEmpty()) {
                        showLoading()
                    } else {
                        showLoading(false)
                    }
                }
                is BaseResponse.Success -> {
                    showLoading(false)
                }
                is BaseResponse.Error -> {
                    handleError(currentPage.code!!)
                }
                else -> {}
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.handler_complaints_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            // Newest or oldest
            R.id.handler_complaints_menu_sort_asc -> {
                item.isChecked = !item.isChecked
                viewModel.changeOrder("asc")
                true
            }
            R.id.handler_complaints_menu_sort_desc -> {
                item.isChecked = !item.isChecked
                viewModel.changeOrder("desc")
                true
            }
            // Filter by status
            R.id.handler_complaints_status_menu -> {
                item.isChecked = !item.isChecked
                showStatusFilterDialog()
                true
            }
            // Filter by handler
            R.id.handler_complaint_menu_filter_by_handler -> {
                item.isChecked = !item.isChecked
                showHandlersPopup()
                true
            }
            // Filter by type
            R.id.handler_complaints_type_menu_all -> {
                item.isChecked = !item.isChecked
                viewModel.changeType("all")
                true
            }
            R.id.handler_complaints_type_menu_building -> {
                item.isChecked = !item.isChecked
                viewModel.changeType("building")
                true
            }
            R.id.handler_complaints_type_menu_room -> {
                item.isChecked = !item.isChecked
                viewModel.changeType("room")
                true
            }
            R.id.handler_complaints_type_menu_facilities -> {
                item.isChecked = !item.isChecked
                viewModel.changeType("facilities")
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showHandlersPopup() {
        // Initialize the handlers list adapter
        viewModel.refreshHandlers()
        handlersListAdapter = HandlersListAdapter(listOf(), viewModel)
        val popupBinding = ComplaintHandlersListBinding.inflate(layoutInflater)
        popupBinding.handlersList.apply {
            adapter = handlersListAdapter
            addItemDecoration(DividerItemDecoration(popupBinding.root.context, DividerItemDecoration.VERTICAL))
            layoutManager = LinearLayoutManager(
                this@HandlerComplaintsActivity,
                LinearLayoutManager.VERTICAL,
                false
            )
            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    if (dy > 0) {
                        if (viewModel.currentHandlersPageStatus.value is BaseResponse.Loading) {
                            return
                        }
                        val visibleItemCount = layoutManager?.childCount ?: 0
                        val totalItemCount = layoutManager?.itemCount ?: 0
                        val pastVisibleItems =
                            (layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
                        if (visibleItemCount + pastVisibleItems >= totalItemCount) {
                            viewModel.fetchHandlersAndComplaints()
                        }
                    }
                }
            })
        }

        viewModel.handlersList.observe(this) {
            if(it == null) {
                popupBinding.handlersLoading.loadingOverlay.visibility = View.VISIBLE
            }
            popupBinding.handlersLoading.loadingOverlay.visibility = View.GONE
            it?.let { handlersListAdapter.updateData(it) }
        }

        // Set the current handler first
        popupBinding.currentHandlerName.text = getString(R.string.current_user_display_word)
        popupBinding.currentHandlerHandledComplaints.text = getString(R.string.handled_complaints_preview, viewModel.currentHandler.value?.complaintsCount.toString())

        val dialog = AlertDialog.Builder(this)
            .setView(popupBinding.root)
            .setCancelable(true)
            .setNeutralButton(R.string.label_close_status_filter) { dialogInterface, _ ->
                dialogInterface.dismiss()
            }
            .setNegativeButton(R.string.label_clear_filter) { dialogInterface, _ ->
                viewModel.changeHandler(null)
                dialogInterface.dismiss()
            }
            .show()

        popupBinding.seeCurrentHandlerComplaints.setOnClickListener {
            viewModel.changeHandler(0)
            dialog.dismiss()
        }

        handlersListAdapter.onClickDissmiss = object : OnClickDissmiss {
            override fun onClickDismiss() {
                // Dismiss the popup
                dialog.dismiss()
            }
        }
    }

    private fun showStatusFilterDialog() {
        // Create an array of all status
        val allStatus = ComplaintStatus.values().map { it.name }
        val items = arrayOf("All", *allStatus.toTypedArray())

        val currentStatus = viewModel.complaintStatus.value ?: 0
        val checkedItem = allStatus.indexOf(currentStatus)
        AlertDialog.Builder(this)
            .setTitle(R.string.filter_by_status)
            .setCancelable(true)
            .setSingleChoiceItems(
                items,
                checkedItem
            ) { dialog, which ->
                // if which is 0, it means all status (which is described by null in backend)
                viewModel.changeStatus(
                    if(which == 0) null else ComplaintStatus.valueOf(items[which])
                )
                dialog.dismiss()
            }
            .setNeutralButton(R.string.label_close_status_filter) { dialogInterface, _ ->
                dialogInterface.dismiss()
            }
            .create().show()
    }

    private fun showEmpty(show: Boolean = true) {
        binding.handlerComplaintsList.complaintsEmpty.root.isVisible = show
    }

    private fun showLoading(show: Boolean = true) {
        binding.handlerComplaintsList.complaintsLoading.loadingOverlay.isVisible = show
    }

    private fun handleError(code: Int) {
        binding.handlerComplaintsList.complaintsLoading.loadingErrorMessage.text = getString(R.string.unknown_error_occurred)
        binding.handlerComplaintsList.complaintsLoading.loadingErrorMessage.visibility = View.VISIBLE
        binding.handlerComplaintsList.complaintsLoading.loadingProgressBar.visibility = View.GONE
        showLoading()
    }
}