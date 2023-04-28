package com.github.essmehdi.schoolmate.users.ui

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.github.essmehdi.schoolmate.R
import com.github.essmehdi.schoolmate.users.viewmodels.UserPlacesViewModel

class UserPlacesFragment : Fragment() {

  companion object {
    fun newInstance() = UserPlacesFragment()
  }

  private lateinit var viewModel: UserPlacesViewModel

  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    return inflater.inflate(R.layout.fragment_user_places, container, false)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    viewModel = ViewModelProvider(this)[UserPlacesViewModel::class.java]
  }

}