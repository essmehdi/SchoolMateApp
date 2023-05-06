package com.github.essmehdi.schoolmate.users.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.viewModels
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
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
      }

      usersListSwipeRefresh.setOnRefreshListener {
        viewModel.fetchUsers()
      }
    }

    viewModel.fetchUsers()
    viewModel.usersFetchStatus.observe(this) {
      when (it) {
        is BaseResponse.Loading -> {
          if (usersAdapter.data.isEmpty()) showLoading()
        }
        is BaseResponse.Error -> {
          dismissSwipe()
          handleError(it.code!!)
        }
        is BaseResponse.Success -> {
          dismissSwipe()
          showLoading(false)
          usersAdapter.updateData(it.data!!)
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
}