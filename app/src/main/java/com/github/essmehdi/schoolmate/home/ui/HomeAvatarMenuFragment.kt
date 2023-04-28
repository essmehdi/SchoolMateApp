package com.github.essmehdi.schoolmate.schoolnavigation.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.github.essmehdi.schoolmate.R
import com.github.essmehdi.schoolmate.auth.ui.LoginActivity
import com.github.essmehdi.schoolmate.databinding.FragmentHomeAvatarMenuBinding
import com.github.essmehdi.schoolmate.shared.api.Api
import com.github.essmehdi.schoolmate.shared.utils.PrefsManager
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

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
  }

  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    binding = FragmentHomeAvatarMenuBinding.inflate(inflater, container, false)
    return binding.root
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    binding.homeAvatarLogout.setOnClickListener {
      logout()
    }
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