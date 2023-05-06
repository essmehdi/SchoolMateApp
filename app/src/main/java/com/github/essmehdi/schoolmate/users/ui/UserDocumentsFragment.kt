package com.github.essmehdi.schoolmate.users.ui

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.essmehdi.schoolmate.R
import com.github.essmehdi.schoolmate.databinding.FragmentUserDocumentsBinding
import com.github.essmehdi.schoolmate.documents.adapters.DocumentsListAdapter
import com.github.essmehdi.schoolmate.documents.viewmodels.DocumentsViewModel
import com.github.essmehdi.schoolmate.shared.api.BaseResponse
import com.github.essmehdi.schoolmate.users.viewmodels.UserDetailsViewModel

class UserDocumentsFragment : Fragment() {

  companion object {
    fun newInstance() = UserDocumentsFragment()
  }

  private lateinit var binding: FragmentUserDocumentsBinding
  private lateinit var viewModel: DocumentsViewModel
  private lateinit var documentsAdapter: DocumentsListAdapter
  private val activityViewModel: UserDetailsViewModel by viewModels(ownerProducer = { requireActivity() })

  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View {
    binding = FragmentUserDocumentsBinding.inflate(inflater, container, false)
    return binding.root
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    viewModel = ViewModelProvider(this)[DocumentsViewModel::class.java]

    activityViewModel.user.observe(viewLifecycleOwner) {
      if (it is BaseResponse.Success) {
        viewModel.loadDocuments(it.data!!.id)
      }
    }

    documentsAdapter = DocumentsListAdapter(listOf(), viewModel, true)
    binding.userDetailsSharedDocumentsList.apply {
      adapter = documentsAdapter
      layoutManager = LinearLayoutManager(requireContext())
    }

    binding.userDetailsSharedDocumentsSwipeRefresh.layoutTransition?.setAnimateParentHierarchy(false)

    viewModel.showEmpty.observe(viewLifecycleOwner) {
      showEmpty(it)
    }

    viewModel.documents.observe(viewLifecycleOwner) { documents ->
      documents?.let { documentsAdapter.updateData(it) }
    }

    viewModel.currentPageStatus.observe(viewLifecycleOwner) { currentPage ->
      when (currentPage) {
        is BaseResponse.Loading -> {
          if (viewModel.documents.value == null || viewModel.documents.value!!.isEmpty()) {
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
    binding.userDetailsSharedDocumentsEmpty.root.isVisible = show
  }

  private fun showLoading(show: Boolean = true) {
    binding.userDetailsSharedDocumentsLoading.loadingOverlay.isVisible = show
  }

  @Suppress("UNUSED_PARAMETER")
  private fun handleError(code: Int) {
    binding.userDetailsSharedDocumentsLoading.loadingErrorMessage.text = getString(R.string.unknown_error_occurred)
    binding.userDetailsSharedDocumentsLoading.loadingErrorMessage.visibility = View.VISIBLE
    binding.userDetailsSharedDocumentsLoading.loadingProgressBar.visibility = View.GONE
    showLoading()
  }

}