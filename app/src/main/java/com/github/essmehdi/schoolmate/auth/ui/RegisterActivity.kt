package com.github.essmehdi.schoolmate.auth.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import androidx.core.widget.doOnTextChanged
import com.github.essmehdi.schoolmate.R
import com.github.essmehdi.schoolmate.auth.viewmodels.RegisterViewModel
import com.github.essmehdi.schoolmate.databinding.ActivityRegisterBinding
import com.github.essmehdi.schoolmate.shared.api.BaseResponse

class RegisterActivity : AppCompatActivity() {

  private lateinit var binding: ActivityRegisterBinding
  private val viewModel: RegisterViewModel by viewModels()
  private val dismissErrorsOnTextChange = object : TextWatcher {
    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
    override fun afterTextChanged(s: Editable?) {
      clearErrorIfFocused()
    }
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    binding = ActivityRegisterBinding.inflate(layoutInflater)
    setContentView(binding.root)

    // Set up register button
    binding.registerButton.setOnClickListener { validateAndSubmit() }

    // Set up text change listeners
    setupTextChangeListeners()

    // Observe register status
    viewModel.registerStatus.observe(this) {
      when (it) {
        is BaseResponse.Success -> {
          showLoading(false)
          val intent = Intent().apply {
            putExtra("email", binding.registerEmailEdittext.text.toString())
            putExtra("password", binding.registerPasswordEdittext.text.toString())
          }
          setResult(RESULT_OK, intent)
          finish()
        }
        is BaseResponse.Loading -> {
          showLoading()
        }
        is BaseResponse.Error -> {
          showLoading(false)
          handleError(it.code!!)
        }
      }
    }
  }

  private fun showLoading(show: Boolean = true) {
    binding.registerButton.isEnabled = !show
    binding.registerButton.text = getString(if (show) R.string.label_register_button_loading else R.string.label_register_button)
  }

  // Setup text change listeners to dismiss errors
  private fun setupTextChangeListeners() {
    binding.registerFirstNameEdittext.addTextChangedListener(dismissErrorsOnTextChange)
    binding.registerLastNameEdittext.addTextChangedListener(dismissErrorsOnTextChange)
    binding.registerEmailEdittext.addTextChangedListener(dismissErrorsOnTextChange)
    binding.registerPasswordEdittext.addTextChangedListener(dismissErrorsOnTextChange)
    binding.registerConfirmPasswordEdittext.addTextChangedListener(dismissErrorsOnTextChange)
  }

  // Clear error if the field is focused
  private fun clearErrorIfFocused() {
    if (binding.registerFirstNameEdittext.isFocused) binding.registerFirstNameLayout.error = null
    if (binding.registerLastNameEdittext.isFocused) binding.registerLastNameLayout.error = null
    if (binding.registerEmailEdittext.isFocused) binding.registerEmailLayout.error = null
    if (binding.registerPasswordEdittext.isFocused) binding.registerPasswordLayout.error = null
    if (binding.registerConfirmPasswordEdittext.isFocused) binding.registerConfirmPasswordLayout.error = null
  }

  // Clear all errors
  private fun clearErrors() {
    binding.registerFirstNameLayout.error = null
    binding.registerLastNameLayout.error = null
    binding.registerEmailLayout.error = null
    binding.registerPasswordLayout.error = null
    binding.registerConfirmPasswordLayout.error = null
  }

  // Handles register errors
  private fun handleError(code: Int) {
    // Handle email already taken
    if (code == 409) {
      binding.registerEmailLayout.error = getString(R.string.error_register_email_already_used)
    } else {
      Toast.makeText(this, getString(R.string.unknown_error_occurred), Toast.LENGTH_SHORT).show()
    }
  }

  // Validate input fields and submit
  private fun validateAndSubmit() {
    // Clear errors before validating
    clearErrors()

    // Get values from input fields
    val firstName = binding.registerFirstNameEdittext.text.toString()
    val lastName = binding.registerLastNameEdittext.text.toString()
    val email = binding.registerEmailEdittext.text.toString()
    val password = binding.registerPasswordEdittext.text.toString()
    val confirmation = binding.registerConfirmPasswordEdittext.text.toString()

    Log.d("RegisterActivity", "validateAndSubmit: $firstName $lastName $email $password $confirmation ${PASSWORD_REGEX.matchEntire(password)}")

    // Flag to check if validation passes
    var pass = true

    // Validate first name
    if (firstName.isEmpty()) {
      binding.registerFirstNameLayout.error = getString(R.string.error_register_first_name_empty)
      pass = false
    }

    // Validate last name
    if (lastName.isEmpty()) {
      binding.registerLastNameLayout.error = getString(R.string.error_register_last_name_empty)
      pass = false
    }

    // Validate email
    if (email.isEmpty()) {
      binding.registerEmailLayout.error = getString(R.string.error_register_email_empty)
      pass = false
    } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
      binding.registerEmailLayout.error = getString(R.string.error_register_email_invalid)
      pass = false
    } else if (!email.endsWith("@um5.ac.ma")) {
      binding.registerEmailLayout.error = getString(R.string.error_register_email_invalid_school_email)
      pass = false
    }

    // Validate password
    if (password.isEmpty()) {
      binding.registerPasswordLayout.error = getString(R.string.error_register_password_empty)
      pass = false
    } else if (password.length < 8) {
      binding.registerPasswordLayout.error = getString(R.string.error_register_password_too_short)
      pass = false
    } else if (PASSWORD_REGEX.matchEntire(password) == null) {
      binding.registerPasswordLayout.error = getString(R.string.error_register_password_invalid)
      pass = false
    }

    // Validate password confirmation
    if (confirmation.isEmpty()) {
      binding.registerConfirmPasswordLayout.error = getString(R.string.error_register_confirm_password_empty)
      pass = false
    } else if (confirmation != password) {
      binding.registerConfirmPasswordLayout.error = getString(R.string.error_register_passwords_mismatch)
      pass = false
    }

    // If all fields are valid, submit the form
    if (pass) viewModel.register(firstName, lastName, email, password, confirmation)
  }

  companion object {
    // Password regex for 1 uppercase, 1 lowercase, 1 digit, 1 special character and 8 characters minimum
    val PASSWORD_REGEX = "^(?=.*\\d)(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{8,100}$".toRegex()
  }
}