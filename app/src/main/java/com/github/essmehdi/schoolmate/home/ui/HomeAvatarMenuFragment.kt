package com.github.essmehdi.schoolmate.home.ui

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import com.github.essmehdi.schoolmate.R
import com.github.essmehdi.schoolmate.auth.ui.LoginActivity
import com.github.essmehdi.schoolmate.auth.ui.RegisterActivity
import com.github.essmehdi.schoolmate.databinding.DialogChangePasswordBinding
import com.github.essmehdi.schoolmate.databinding.DialogEditProfileBinding
import com.github.essmehdi.schoolmate.databinding.FragmentHomeAvatarMenuBinding
import com.github.essmehdi.schoolmate.home.viewmodels.HomeViewModel
import com.github.essmehdi.schoolmate.shared.api.Api
import com.github.essmehdi.schoolmate.shared.api.BaseResponse
import com.github.essmehdi.schoolmate.shared.api.dto.MessageResponse
import com.github.essmehdi.schoolmate.shared.utils.PrefsManager
import com.github.essmehdi.schoolmate.users.api.dto.ChangePasswordDto
import com.github.essmehdi.schoolmate.users.api.dto.EditUserDto
import com.github.essmehdi.schoolmate.users.models.User
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/**
 * A simple [Fragment] subclass.
 * Use the [HomeAvatarMenuFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class HomeAvatarMenuFragment : BottomSheetDialogFragment() {

  private lateinit var binding: FragmentHomeAvatarMenuBinding
  private val activityViewModel: HomeViewModel by activityViewModels()

  private lateinit var editObserver: Observer<in BaseResponse<User>>
  private lateinit var changePasswordObserver: Observer<in BaseResponse<MessageResponse>>

  private var editProfileDialog: AlertDialog? = null
  private var changePasswordDialog: AlertDialog? = null

  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View {
    // Set an observer for editing
    editObserver = Observer {
      if (it is BaseResponse.Success) {
        Toast.makeText(requireContext(), R.string.home_dialog_edit_profile_success, Toast.LENGTH_SHORT).show()
        editProfileDialog?.dismiss()
        dialog?.dismiss()
      } else if (it is BaseResponse.Error) {
        Toast.makeText(requireContext(), R.string.home_dialog_edit_profile_error, Toast.LENGTH_SHORT).show()
      }
    }

    // Set an observer for changing password
    changePasswordObserver = Observer {
      if (it is BaseResponse.Success) {
        Toast.makeText(requireContext(), R.string.home_dialog_change_password_success, Toast.LENGTH_SHORT).show()
        changePasswordDialog?.dismiss()
        dialog?.dismiss()
      } else if (it is BaseResponse.Error) {
        Toast.makeText(requireContext(), R.string.home_dialog_change_password_error, Toast.LENGTH_SHORT).show()
      }
    }

    binding = FragmentHomeAvatarMenuBinding.inflate(inflater, container, false)
    return binding.root
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    binding.homeAvatarChangePassword.setOnClickListener {
      showChangePasswordDialog()
    }
    binding.homeAvatarEditProfile.setOnClickListener {
      showEditDialog()
    }
    binding.homeAvatarLogout.setOnClickListener {
      logout()
    }
  }

  private fun showChangePasswordDialog() {
    val changePasswordDialogBinding = DialogChangePasswordBinding.inflate(layoutInflater)

    changePasswordDialog = AlertDialog.Builder(requireContext()).apply {
      setTitle(R.string.home_dialog_change_password_title)
      setView(changePasswordDialogBinding.root)
      setCancelable(true)
    }.create()

    changePasswordDialogBinding.homeDialogChangePasswordButton.setOnClickListener {
      sendChangePasswordData(changePasswordDialogBinding)
    }

    changePasswordDialog?.show()
  }

  private fun sendChangePasswordData(
    changePasswordDialogBinding: DialogChangePasswordBinding,
  ) {
    // Get edit text values
    val oldPassword = changePasswordDialogBinding.homeDialogChangePasswordOldPasswordEditText.text.toString()
    val newPassword = changePasswordDialogBinding.homeDialogChangePasswordNewPasswordEditText.text.toString()
    val newPasswordConfirmation = changePasswordDialogBinding.homeDialogChangePasswordConfirmPasswordEditText.text.toString()

    // Validate data
    if (oldPassword.isBlank()) {
      changePasswordDialogBinding.homeDialogChangePasswordOldPasswordInputLayout.error = getString(R.string.error_home_dialog_change_password_old_password_required)
      return
    } else {
      changePasswordDialogBinding.homeDialogChangePasswordOldPasswordInputLayout.error = null
    }

    if (newPassword.isBlank()) {
      changePasswordDialogBinding.homeDialogChangePasswordNewPasswordInputLayout.error = getString(R.string.error_home_dialog_change_password_new_password_required)
      return
    } else {
      changePasswordDialogBinding.homeDialogChangePasswordNewPasswordInputLayout.error = null
    }

    if (newPasswordConfirmation.isBlank()) {
      changePasswordDialogBinding.homeDialogChangePasswordConfirmPasswordInputLayout.error = getString(R.string.error_home_dialog_change_password_confirm_password_required)
      return
    } else {
      changePasswordDialogBinding.homeDialogChangePasswordConfirmPasswordInputLayout.error = null
    }

    if (RegisterActivity.PASSWORD_REGEX.matchEntire(newPassword) == null) {
      changePasswordDialogBinding.homeDialogChangePasswordNewPasswordInputLayout.error = getString(R.string.error_register_password_invalid)
      return
    } else {
      changePasswordDialogBinding.homeDialogChangePasswordNewPasswordInputLayout.error = null
    }

    // Verifies password match
    if (newPassword != newPasswordConfirmation) {
      changePasswordDialogBinding.homeDialogChangePasswordConfirmPasswordInputLayout.error = getString(R.string.error_home_dialog_change_password_confirm_password_not_matching)
      return
    } else {
      changePasswordDialogBinding.homeDialogChangePasswordConfirmPasswordInputLayout.error = null
    }

    // Send request to server
    val changePasswordDto = ChangePasswordDto(oldPassword, newPassword, newPasswordConfirmation)
    activityViewModel.changePassword(changePasswordDto)

    // Observe response
    activityViewModel.changePasswordStatus.observe(viewLifecycleOwner, changePasswordObserver)
  }

  override fun onDismiss(dialog: DialogInterface) {
    activityViewModel.changePasswordStatus.removeObserver(changePasswordObserver)
    activityViewModel.user.removeObserver(editObserver)
    super.onDismiss(dialog)
  }

  private fun showEditDialog() {
    val editProfileDialogBinding = DialogEditProfileBinding.inflate(layoutInflater)

    fillProfileData(editProfileDialogBinding)

    editProfileDialog = AlertDialog.Builder(requireContext()).apply {
      setTitle(R.string.home_dialog_edit_profile_title)
      setView(editProfileDialogBinding.root)
      setCancelable(true)
    }.create()

    editProfileDialogBinding.homeDialogEditButton.setOnClickListener {
      sendEditData(editProfileDialogBinding)
    }

    editProfileDialog?.show()
  }

  private fun fillProfileData(editProfileDialogBinding: DialogEditProfileBinding) {
    val user = activityViewModel.user.value!!.data!!
    editProfileDialogBinding.homeDialogEditProfileFirstnameEditText.setText(user.firstName)
    editProfileDialogBinding.homeDialogEditProfileLastnameEditText.setText(user.lastName)
    editProfileDialogBinding.homeDialogEditProfileEmailEditText.setText(user.email)
  }

  private fun sendEditData(editProfileDialogBinding: DialogEditProfileBinding) {
    // Get first name from edit text
    val firstName = editProfileDialogBinding.homeDialogEditProfileFirstnameEditText.text.toString()
    if (firstName.isEmpty()) {
      editProfileDialogBinding.homeDialogEditProfileFirstnameInputLayout.error = getString(R.string.error_home_dialog_edit_profile_firstname_required)
      return
    } else {
      editProfileDialogBinding.homeDialogEditProfileFirstnameInputLayout.error = null
    }

    // Get last name from edit text
    val lastName = editProfileDialogBinding.homeDialogEditProfileLastnameEditText.text.toString()
    if (lastName.isEmpty()) {
      editProfileDialogBinding.homeDialogEditProfileLastnameInputLayout.error = getString(R.string.error_home_dialog_edit_profile_lastname_required)
      return
    } else {
      editProfileDialogBinding.homeDialogEditProfileLastnameInputLayout.error = null
    }

    // Get email from edit text
    val email = editProfileDialogBinding.homeDialogEditProfileEmailEditText.text.toString()
    if (email.isEmpty()) {
      editProfileDialogBinding.homeDialogEditProfileEmailInputLayout.error =
        getString(R.string.error_home_dialog_edit_profile_email_required)
      return
    } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
      editProfileDialogBinding.homeDialogEditProfileEmailInputLayout.error = getString(R.string.error_register_email_invalid)
      return
    } else {
      editProfileDialogBinding.homeDialogEditProfileEmailInputLayout.error = null
    }

    val editUserDto = EditUserDto(firstName, lastName, email)
    activityViewModel.editUserData(editUserDto)

    // Observes response
    activityViewModel.user.observe(viewLifecycleOwner, editObserver)
  }

  private fun logout() {
    loadingLogoutButton()
    Api.authService.logout().enqueue(object: Callback<Void> {
      override fun onResponse(call: Call<Void>, response: Response<Void>) {
        if (response.isSuccessful) {
          PrefsManager.clearString(requireContext(), PrefsManager.USER_COOKIE)
          startActivity(Intent(requireContext(), LoginActivity::class.java))
        } else {
          loadingLogoutButton(false)
        }
      }

      override fun onFailure(call: Call<Void>, t: Throwable) {
        loadingLogoutButton(false)
      }
    })
  }

  private fun loadingLogoutButton(loading: Boolean = true) {
    if (!loading) {
      // The only case we restore the button is on failure for now
      Toast.makeText(requireContext(), R.string.unknown_error_occurred, Toast.LENGTH_LONG).show()
    }
    this.isCancelable = !loading
    binding.homeAvatarLogout.apply {
      isEnabled = !loading
      text = getString(if (loading) R.string.home_avatar_menu_logout_loading else R.string.home_avatar_menu_logout)
    }
  }

  companion object {
    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment SchoolZoneDetailsFragment.
     */
    @JvmStatic
    fun newInstance() = HomeAvatarMenuFragment()
  }
}