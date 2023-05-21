package com.github.essmehdi.schoolmate.alerts.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.essmehdi.schoolmate.R
import com.github.essmehdi.schoolmate.alerts.adapters.ConfirmAlertsListAdapter
import com.github.essmehdi.schoolmate.alerts.viewmodels.ConfirmAlertViewModel
import com.github.essmehdi.schoolmate.databinding.FragmentConfirmAlertBinding
import com.github.essmehdi.schoolmate.shared.api.BaseResponse
import com.github.essmehdi.schoolmate.users.models.User

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [ConfirmAlertFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ConfirmAlertFragment : Fragment() {
    // TODO: Rename and change types of parameters
     private lateinit var binding: FragmentConfirmAlertBinding
     private val viewModel: ConfirmAlertViewModel by viewModels()
    private lateinit var alertAdapter: ConfirmAlertsListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            //param1 = it.getString(ARG_PARAM1)
           // param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentConfirmAlertBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.loadPendingAlerts()
        alertAdapter = ConfirmAlertsListAdapter(listOf(),viewModel,true)
        binding.alertsList.apply {
            adapter = alertAdapter
            layoutManager = LinearLayoutManager(requireContext())
            addItemDecoration(DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL))
        }
        binding.alertsSwipeRefresh.layoutTransition?.setAnimateParentHierarchy(false)
        viewModel.showEmpty.observe(viewLifecycleOwner){
            showEmpty(it)
        }
        viewModel.alerts.observe(viewLifecycleOwner){
           alerts -> alerts?.let {
                alertAdapter.updateData(it)
            }
        }
        viewModel.currentPageStatus.observe(viewLifecycleOwner) { currentPage ->
            when (currentPage) {
                is BaseResponse.Loading -> {
                    if (viewModel.alerts.value == null || viewModel.alerts.value!!.isEmpty()) {
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

        binding.alertsSwipeRefresh.layoutTransition?.setAnimateParentHierarchy(false)
        binding.alertsSwipeRefresh.setOnRefreshListener {
            viewModel.refresh()
            binding.alertsSwipeRefresh.isRefreshing=false
        }

    }
    private fun handleError(code: Int) {
        binding.alertsLoading.loadingErrorMessage.text = getString(R.string.unknown_error_occurred)
        binding.alertsLoading.loadingErrorMessage.visibility = View.VISIBLE
        binding.alertsLoading.loadingProgressBar.visibility = View.GONE
        showLoading()
    }

    private fun showLoading(show: Boolean = true) {
        binding.alertsLoading.loadingOverlay.isVisible = show
    }

    private fun showEmpty(show: Boolean = true) {
        binding.alertsEmpty.root.isVisible = show

    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment ConfirmAlertFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ConfirmAlertFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }

        fun newInstance(): Fragment {
            return ConfirmAlertFragment()
        }
    }
}