package com.github.essmehdi.schoolmate.complaints.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.essmehdi.schoolmate.R
import com.github.essmehdi.schoolmate.complaints.adapters.ComplaintsListAdapter
import com.github.essmehdi.schoolmate.complaints.viewmodels.ComplaintsViewModel
import com.github.essmehdi.schoolmate.databinding.FragmentAllComplaintsBinding
import com.github.essmehdi.schoolmate.shared.api.BaseResponse
import com.google.android.material.snackbar.Snackbar

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
    ): View {
        binding = FragmentAllComplaintsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val activity = requireActivity() as ComplaintsActivity
        complaintsAdapter = ComplaintsListAdapter(listOf(), viewModel, activity.launcher)
        binding.allComplaintsList.apply {
            adapter = complaintsAdapter
            addItemDecoration(DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL))
            layoutManager = LinearLayoutManager(requireContext())
            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    if (dy > 0) {
                        if (viewModel.currentPageStatus.value is BaseResponse.Loading) {
                            return
                        }
                        val visibleItemCount = layoutManager?.childCount ?: 0
                        val totalItemCount = layoutManager?.itemCount ?: 0
                        val pastVisibleItems = (layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
                        if (visibleItemCount + pastVisibleItems >= totalItemCount) {
                            viewModel.fetchComplaints()
                        }
                    }
                }
            })
        }

        binding.allComplaintsSwipeRefresh.layoutTransition?.setAnimateParentHierarchy(false)
        binding.allComplaintsSwipeRefresh.setOnRefreshListener {
            viewModel.refresh()
            // cancel the refresh animation after the data is fetched
            binding.allComplaintsSwipeRefresh.isRefreshing = false
        }

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