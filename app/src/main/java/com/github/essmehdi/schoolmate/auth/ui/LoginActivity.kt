package com.github.essmehdi.schoolmate.auth.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.github.essmehdi.schoolmate.R
import com.github.essmehdi.schoolmate.auth.api.dto.LoginDto
import com.github.essmehdi.schoolmate.auth.viewmodels.LoginViewModel
import com.github.essmehdi.schoolmate.databinding.ActivityLoginBinding
import com.github.essmehdi.schoolmate.home.ui.HomeActivity
import com.github.essmehdi.schoolmate.shared.api.BaseResponse

class LoginActivity : AppCompatActivity() {

  private lateinit var binding: ActivityLoginBinding
  private val viewModel: LoginViewModel by viewModels()
  private lateinit var registerLauncher: ActivityResultLauncher<Intent>

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    binding = ActivityLoginBinding.inflate(layoutInflater)
    setContentView(binding.root)

    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

    registerLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
      if (result.resultCode == RESULT_OK) {
        val email = result.data?.getStringExtra("email")
        val password = result.data?.getStringExtra("password")
        email?.let { binding.emailEdittext.setText(it) }
        password?.let { binding.passwordEdittext.setText(it) }
      }
    }

    binding.loginButton.setOnClickListener {
      validateInput()?.let { viewModel.login(it) }
    }

    binding.loginRegisterButton.setOnClickListener {
      registerLauncher.launch(Intent(this, RegisterActivity::class.java))
    }

    viewModel.checkAuth()

    viewModel.authed.observe(this) {
      when (it) {
        is BaseResponse.Success -> {
          if (it.data == true) proceed() else binding.loading.loadingOverlay.visibility = View.GONE
        }
        is BaseResponse.Error -> {
          binding.loading.loadingOverlay.visibility = View.GONE
        }
        else -> {}
      }
    }

    viewModel.result.observe(this) {
      when (it) {
        is BaseResponse.Loading -> {
          handleLoading()
        }
        is BaseResponse.Error -> {
          restoreUI()
          handleError(it.message, it.code!!)
        }
        is BaseResponse.Success -> {
          restoreUI()
          proceed()
        }
      }
    }
  }

  /**
   * Sends UI to the next activity (Successful auth)
   */
  private fun proceed() {
    val intent = Intent(this, HomeActivity::class.java)
    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
    startActivity(intent)
  }

  /**
   * Handles UI in case of sign in error
   */
  @Suppress("UNUSED_PARAMETER")
  private fun handleError(message: String?, code: Int) {
   if (code == 400) {
     binding.emailEdittext.error = getString(R.string.invalid_credentials)
   } else {
     Toast.makeText(this, R.string.unknown_error_occurred, Toast.LENGTH_LONG).show()
   }
  }

  /**
   * Handles UI when sign in process is loading
   */
  private fun handleLoading() {
    binding.loginButton.isActivated = false
    binding.loginButton.text = getString(R.string.loading_sign_in)
  }

  /**
   * Restore loading UI to default UI
   */
  private fun restoreUI() {
    binding.loginButton.isActivated = true
    binding.loginButton.text = getString(R.string.action_sign_in_short)
  }

  /**
   * Validates form input
   */
  private fun validateInput(): LoginDto? {
    val emailEditable = binding.emailEdittext.text
    val passwordEditable = binding.passwordEdittext.text
    Log.i("Email", emailEditable.toString())
    Log.i("Password", passwordEditable.toString())
    if (emailEditable == null || emailEditable.toString().isBlank()) {
      binding.emailEdittext.error = getString(R.string.required_email)
      return null
    }

    if (passwordEditable == null || passwordEditable.toString().isBlank()) {
      binding.passwordEdittext.error = getString(R.string.required_password)
      return null
    }

    return LoginDto(emailEditable.toString(), passwordEditable.toString())
  }
}