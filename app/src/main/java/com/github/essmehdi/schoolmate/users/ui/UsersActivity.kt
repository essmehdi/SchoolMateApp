package com.github.essmehdi.schoolmate.users.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.viewModels
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.essmehdi.schoolmate.R
import com.github.essmehdi.schoolmate.databinding.ActivityUsersBinding
import com.github.essmehdi.schoolmate.users.adapters.UsersAdapter
import com.github.essmehdi.schoolmate.shared.api.BaseResponse
import com.github.essmehdi.schoolmate.users.viewmodels.UsersViewModel

class UsersActivity : AppCompatActivity() {

  private lateinit var binding: ActivityUsersBinding
  private lateinit var usersAdapter: UsersAdapter
  private val viewModel: UsersViewModel by viewModels()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    binding = ActivityUsersBinding.inflate(layoutInflater)
    setContentView(binding.root)

    usersAdapter = UsersAdapter(listOf())
    binding.apply {
      usersList.apply {
        adapter = usersAdapter
        layoutManager = LinearLayoutManager(this@UsersActivity)
        addItemDecoration(DividerItemDecoration(this@UsersActivity, DividerItemDecoration.VERTICAL))
        addOnScrollListener(object : RecyclerView.OnScrollListener() {
          override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)
            if (dy > 0) {
              if (viewModel.currentPageStatus.value is BaseResponse.Loading) {
                return
              }
              val visibleItemCount = layoutManager?.childCount ?: 0
              val totalItemCount = layoutManager?.itemCount ?: 0
              val pastVisibleItems = (layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
              if (visibleItemCount + pastVisibleItems >= totalItemCount) {
                viewModel.loadUsers()
              }
            }
          }
        })
      }

      usersListSwipeRefresh.setOnRefreshListener {
        viewModel.refresh()
      }
    }

    viewModel.loadUsers()

    viewModel.showEmpty.observe(this) {
      showEmpty(it)
    }

    viewModel.users.observe(this) { users ->
      users?.let { usersAdapter.updateData(it) }
    }

    viewModel.currentPageStatus.observe(this) { currentPage ->
      when (currentPage) {
        is BaseResponse.Loading -> {
          if (viewModel.users.value.isNullOrEmpty()) {
            showLoading()
          } else {
            showLoading(false)
          }
        }
        is BaseResponse.Success -> {
          showLoading(false)
          dismissSwipe()
        }
        is BaseResponse.Error -> {
          handleError(currentPage.code!!)
          dismissSwipe()
        }
      }
    }
  }

  private fun dismissSwipe() {
    binding.usersListSwipeRefresh.isRefreshing = false
  }

  @Suppress("UNUSED_PARAMETER")
  private fun handleError(code: Int) {
    showError()
  }

  private fun showLoading(show: Boolean = true) {
    binding.usersLoading.loadingProgressBar.isVisible = true
    binding.usersLoading.loadingErrorMessage.isVisible = false
    binding.usersLoading.loadingOverlay.isVisible = show
  }

  private fun showError(show: Boolean = true, errorMsg: String? = null) {
    binding.usersLoading.loadingErrorMessage.apply {
      text = errorMsg ?: getString(R.string.unknown_error_occurred)
      isVisible = true
    }
    binding.usersLoading.loadingProgressBar.isVisible = false
    binding.usersLoading.loadingOverlay.isVisible = show
  }

  private fun showEmpty(show: Boolean = true) {
    binding.usersEmpty.root.isVisible = show
  }
}