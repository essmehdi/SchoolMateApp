package com.github.essmehdi.schoolmate.placesuggestions.ui

import android.os.Bundle
import android.util.Log
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
import com.github.essmehdi.schoolmate.databinding.FragmentAllSuggestionsBinding
import com.github.essmehdi.schoolmate.placesuggestions.adapters.SuggestionsListAdapter
import com.github.essmehdi.schoolmate.placesuggestions.viewmodels.SuggestionsViewModel
import com.github.essmehdi.schoolmate.shared.api.BaseResponse

class AllSuggestionsFragment : Fragment() {
    private lateinit var binding: FragmentAllSuggestionsBinding
    private val viewModel: SuggestionsViewModel by viewModels(ownerProducer = {requireActivity()})
    private lateinit var suggestionAdapter: SuggestionsListAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentAllSuggestionsBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val activity = requireActivity() as SuggestionsActivity
        suggestionAdapter = SuggestionsListAdapter(listOf(), viewModel, activity.launcher)
        binding.suggestionsList.apply{
            adapter = suggestionAdapter
            addItemDecoration(DividerItemDecoration(requireContext(),DividerItemDecoration.VERTICAL))
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
                            viewModel.loadSuggestions()
                        }
                    }
                }
            })
        }

        binding.suggestionsSwipe.layoutTransition?.setAnimateParentHierarchy(false)
        binding.suggestionsSwipe.setOnRefreshListener{
            viewModel.refresh()
        }

        viewModel.showEmpty.observe(viewLifecycleOwner) {
            showEmpty(it)
        }

        // Update adapter on data change
        viewModel.suggestions.observe(viewLifecycleOwner) { suggestionsList ->
            suggestionsList?.let { suggestionAdapter.updateData(it) }
        }

        viewModel.currentPageStatus.observe(viewLifecycleOwner) { currentPage ->
            when (currentPage) {
                is BaseResponse.Loading -> {
                    showLoading()
                }
                is BaseResponse.Success -> {
                    showLoading(false)
                    binding.suggestionsSwipe.isRefreshing = false
                }
                is BaseResponse.Error -> {
                    handleError(currentPage.code!!)
                    binding.suggestionsSwipe.isRefreshing = false
                }
                null -> {}
            }
        }
    }

    private fun showEmpty(show: Boolean = true) {
        binding.suggestionsLoading.root.isVisible = show
    }

    private fun showLoading(show: Boolean = true) {
        binding.suggestionsLoading.loadingOverlay.isVisible = show
    }

    @Suppress("UNUSED_PARAMETER")
    private fun handleError(code: Int) {
        binding.suggestionsLoading.loadingErrorMessage.text = getString(R.string.unknown_error_occurred)
        binding.suggestionsLoading.loadingErrorMessage.visibility = View.VISIBLE
        binding.suggestionsLoading.loadingProgressBar.visibility = View.GONE
        showLoading()
    }
    //fetchAllSuggestions()
    //observe loaded suggestions = connect with recycler view
    companion object {
        @JvmStatic
        fun newInstance() =
            AllSuggestionsFragment()
    }
}