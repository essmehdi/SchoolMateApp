package com.github.essmehdi.schoolmate.users.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.core.view.isVisible
import com.github.essmehdi.schoolmate.R
import com.github.essmehdi.schoolmate.databinding.ActivityUserDetailsBinding
import com.github.essmehdi.schoolmate.shared.api.BaseResponse
import com.github.essmehdi.schoolmate.users.adapters.UserDetailsViewPagerAdapter
import com.github.essmehdi.schoolmate.users.models.User
import com.github.essmehdi.schoolmate.users.models.UserRole
import com.github.essmehdi.schoolmate.users.viewmodels.UserDetailsViewModel
import com.google.android.material.tabs.TabLayoutMediator

class UserDetailsActivity : AppCompatActivity() {

  private lateinit var binding: ActivityUserDetailsBinding
  private val viewModel: UserDetailsViewModel by viewModels()
  private var menu: Menu? = null

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    binding = ActivityUserDetailsBinding.inflate(layoutInflater)
    setContentView(binding.root)

    setSupportActionBar(binding.userDetailsToolbar)
    supportActionBar?.setDisplayHomeAsUpEnabled(true)
    supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_navigation_left_colored)

    val userId = intent.getLongExtra("userId", 0)
    if (userId == 0L) {
      Log.e("UserDetailsActivity", "User id is not provided")
      finish()
      return
    }

    viewModel.fetchUser(userId)

    viewModel.user.observe(this) {
      when (it) {
        is BaseResponse.Loading -> {
          showLoading()
        }
        is BaseResponse.Error -> {
          handleError(it.code!!)
        }
        is BaseResponse.Success -> {
          showLoading(false)
          fillData(it.data!!)
        }
      }
    }

    viewModel.showPrivilegeMenu.observe(this) { shouldShowMenu ->
      Log.d("UserDetailsActivity", "shouldShowMenu: $shouldShowMenu")
      menu?.findItem(R.id.user_details_menu_edit_privileges)?.apply {
        isVisible = shouldShowMenu
        isEnabled = shouldShowMenu
      }
    }

    setupViewPager()
  }

  override fun onCreateOptionsMenu(menu: Menu?): Boolean {
    menuInflater.inflate(R.menu.user_details_menu, menu)
    this.menu = menu!!
    return true
  }

  override fun onOptionsItemSelected(item: MenuItem): Boolean {
    return when (item.itemId) {
      R.id.user_details_menu_edit_privileges_student -> {
        viewModel.changeUserStatus(UserRole.STUDENT)
        true
      }
      R.id.user_details_menu_edit_privileges_adei -> {
        viewModel.changeUserStatus(UserRole.ADEI)
        true
      }
      R.id.user_details_menu_edit_privileges_moderator -> {
        viewModel.changeUserStatus(UserRole.MODERATOR)
        true
      }
      android.R.id.home -> {
        onBackPressedDispatcher.onBackPressed()
        true
      }
      else -> {
        super.onOptionsItemSelected(item)
      }
    }
  }

  private fun setupViewPager() {
    binding.userDetailsViewPager.adapter = UserDetailsViewPagerAdapter(this)
    TabLayoutMediator(binding.userDetailsTabLayout, binding.userDetailsViewPager) { tab, position ->
      tab.text = when (position) {
        0 -> getString(R.string.user_details_tab_documents_title)
        else -> getString(R.string.user_details_tab_place_suggestions_title)
      }
    }.attach()
  }

  private fun fillData(user: User) {
    Log.d("UserDetailsActivity", "Fill user: $user")

    // Fill user data
    binding.userDetailsNameText.text = user.fullName
    binding.userDetailsEmailText.text = user.email
    binding.userDetailsRoleChip.text = getString(user.roleNameString)

    supportActionBar?.title = user.fullName
  }

  @Suppress("UNUSED_PARAMETER")
  private fun handleError(code: Int) {
    showError()
  }

  private fun showLoading(show: Boolean = true) {
    binding.userDetailsLoading.loadingProgressBar.isVisible = true
    binding.userDetailsLoading.loadingErrorMessage.isVisible = false
    binding.userDetailsLoading.loadingOverlay.isVisible = show
  }

  private fun showError(show: Boolean = true, errorMsg: String? = null) {
    binding.userDetailsLoading.loadingErrorMessage.apply {
      text = errorMsg ?: getString(R.string.unknown_error_occurred)
      isVisible = true
    }
    binding.userDetailsLoading.loadingProgressBar.isVisible = false
    binding.userDetailsLoading.loadingOverlay.isVisible = show
  }
}