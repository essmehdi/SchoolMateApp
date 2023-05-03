package com.github.essmehdi.schoolmate.complaints.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.essmehdi.schoolmate.R
import com.github.essmehdi.schoolmate.complaints.adapters.ComplaintsListAdapter
import com.github.essmehdi.schoolmate.complaints.viewmodels.ComplaintsViewModel
import com.github.essmehdi.schoolmate.databinding.FragmentAllComplaintsBinding
import com.github.essmehdi.schoolmate.shared.api.BaseResponse

class AllComplaintsFragment : Fragment() {

    companion object {
        fun newInstance() = AllComplaintsFragment()
    }

    private lateinit var binding: FragmentAllComplaintsBinding
    private lateinit var complaintsAdapter: ComplaintsListAdapter
    private val viewModel: ComplaintsViewModel by viewModels(ownerProducer = { requireActivity() })

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAllComplaintsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // TODO: get complaints by type
        viewModel.fetchComplaints(
            type = "all",
            user = "all"
        )
        complaintsAdapter = ComplaintsListAdapter(listOf(), viewModel)
        binding.allComplaintsList.apply {
            adapter = complaintsAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }

        binding.allComplaintsSwipeRefresh.layoutTransition?.setAnimateParentHierarchy(false)

        viewModel.showEmpty.observe(viewLifecycleOwner) {
            showEmpty(it)
        }

        viewModel.complaints.observe(viewLifecycleOwner) {complaints ->
            complaints?.let { complaintsAdapter.updateComplaints(it) }
        }

        viewModel.currentPageStatus.observe(viewLifecycleOwner) { currentPage ->
            when (currentPage) {
                is BaseResponse.Loading -> {
                    if (viewModel.complaints.value == null || viewModel.complaints.value!!.isEmpty()) {
                        showLoading()
                    } else {
                        showLoading(false)
                    }
                }
                is BaseResponse.Error -> {
                    handleError(currentPage.code!!)
                }
                else -> {
                    showLoading(false)
                }
            }
        }
    }


    private fun showEmpty(show: Boolean = true) {
        binding.allComplaintsEmpty.root.isVisible = show
    }

    private fun showLoading(show: Boolean = true) {
        binding.allComplaintsLoading.loadingOverlay.isVisible = show
    }

    private fun handleError(code: Int) {
        binding.allComplaintsLoading.loadingErrorMessage.text = getString(R.string.unknown_error_occurred)
        binding.allComplaintsLoading.loadingErrorMessage.visibility = View.VISIBLE
        binding.allComplaintsLoading.loadingProgressBar.visibility = View.GONE
        showLoading()
    }


}