package com.github.essmehdi.schoolmate.users.ui

import android.content.Intent
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.essmehdi.schoolmate.R
import com.github.essmehdi.schoolmate.databinding.FragmentUserPlacesBinding
import com.github.essmehdi.schoolmate.placesuggestions.adapters.SuggestionsListAdapter
import com.github.essmehdi.schoolmate.shared.api.BaseResponse
import com.github.essmehdi.schoolmate.users.viewmodels.UserDetailsViewModel
import com.github.essmehdi.schoolmate.users.viewmodels.UserPlacesViewModel
import com.google.android.material.snackbar.Snackbar

class UserPlacesFragment : Fragment() {

  companion object {
    fun newInstance() = UserPlacesFragment()
  }

  private lateinit var binding: FragmentUserPlacesBinding
  private lateinit var viewModel: UserPlacesViewModel
  private lateinit var placesAdapter: SuggestionsListAdapter
  private lateinit var launcher: ActivityResultLauncher<Intent>
  private val activityViewModel: UserDetailsViewModel by viewModels(ownerProducer = { requireActivity() })


  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    binding = FragmentUserPlacesBinding.inflate(inflater, container, false)
    return binding.root
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    viewModel = ViewModelProvider(this)[UserPlacesViewModel::class.java]

    launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
      if (result.resultCode == AppCompatActivity.RESULT_OK) {
        viewModel.refresh()
      }
    }

    viewModel.fetchCurrentUser()

    activityViewModel.user.observe(viewLifecycleOwner) {
      if (it is BaseResponse.Success) {
        viewModel.id.value = it.data!!.id
        viewModel.loadUserSuggestions()
      }
    }

    placesAdapter = SuggestionsListAdapter(listOf(), viewModel, launcher)
    binding.userSuggestionsList.apply {
      adapter = placesAdapter
      layoutManager = LinearLayoutManager(requireContext())
    }

    binding.userSuggestionsSwipe.layoutTransition?.setAnimateParentHierarchy(false)
    binding.userSuggestionsSwipe.setOnRefreshListener {
      viewModel.refresh()
    }

    viewModel.showEmpty.observe(viewLifecycleOwner) {
      showEmpty(it)
    }

    viewModel.deleteStatus.observe(viewLifecycleOwner){
      when (it) {
        is BaseResponse.Success -> {
          Snackbar.make(binding.root, R.string.success_suggestion_deletion, Snackbar.LENGTH_SHORT).show()
          viewModel.refresh()
        }
        is BaseResponse.Loading -> {
          Snackbar.make(binding.root, R.string.loading_suggestion_deletion, Snackbar.LENGTH_INDEFINITE).show()
        }
        is BaseResponse.Error -> {
          Snackbar.make(binding.root, R.string.error_suggestion_deletion, Snackbar.LENGTH_SHORT).show()
        }
        else -> {}
      }
    }

    viewModel.suggestions.observe(viewLifecycleOwner){suggestions ->
      suggestions?.let { placesAdapter.updateData(it) }
    }

    viewModel.currentPageStatus.observe(viewLifecycleOwner) { currentPage ->
      when (currentPage) {
        is BaseResponse.Loading -> {
          if (viewModel.suggestions.value == null || viewModel.suggestions.value!!.isEmpty()) {
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
        null -> {}
      }
    }
  }


  private fun showEmpty(show: Boolean = true) {
    binding.userSuggestionsEmpty.root.isVisible = show
  }

  private fun showLoading(show: Boolean = true) {
    binding.userSuggestionsLoading.loadingOverlay.isVisible = show
  }

  @Suppress("UNUSED_PARAMETER")
  private fun handleError(code: Int) {
    binding.userSuggestionsLoading.loadingErrorMessage.text = getString(R.string.unknown_error_occurred)
    binding.userSuggestionsLoading.loadingErrorMessage.visibility = View.VISIBLE
    binding.userSuggestionsLoading.loadingProgressBar.visibility = View.GONE
    showLoading()
  }
}