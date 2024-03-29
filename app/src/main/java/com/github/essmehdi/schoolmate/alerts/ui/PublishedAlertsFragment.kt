package com.github.essmehdi.schoolmate.alerts.ui

import android.content.Intent
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
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.essmehdi.schoolmate.R
import com.github.essmehdi.schoolmate.alerts.adapters.MyAlertsListAdapter
import com.github.essmehdi.schoolmate.alerts.viewmodels.PublishedAlertsFragmentViewModel
import com.github.essmehdi.schoolmate.databinding.FragmentMyAlertsBinding
import com.github.essmehdi.schoolmate.databinding.FragmentPublishedAlertsBinding
import com.github.essmehdi.schoolmate.shared.api.BaseResponse
import com.github.essmehdi.schoolmate.users.models.User

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [PublishedAlertsFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class PublishedAlertsFragment : Fragment() {

    private lateinit var binding: FragmentPublishedAlertsBinding
    private val viewModel: PublishedAlertsFragmentViewModel by viewModels()
    private lateinit var alertAdapter: MyAlertsListAdapter
    private lateinit var editorLauncher: ActivityResultLauncher<Intent>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {

        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentPublishedAlertsBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        editorLauncher=registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
            if (it.resultCode== AppCompatActivity.RESULT_OK){

                viewModel.loadAllAlerts()
            }
        }
        viewModel.loadAllAlerts()
        alertAdapter =  MyAlertsListAdapter(listOf(),viewModel)

        binding.myPublishedAlertsList.apply {
            adapter = alertAdapter
            layoutManager = LinearLayoutManager(requireContext())
            addItemDecoration(DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL))
        }
        viewModel.showEmpty.observe(viewLifecycleOwner){
            showEmpty(it)
        }

        viewModel.alerts.observe(viewLifecycleOwner){ alerts ->
            alerts?.let { alertAdapter.updateData(it) }
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
        binding.myPublishedAlertsSwipeRefresh.layoutTransition?.setAnimateParentHierarchy(false)
        binding.myPublishedAlertsSwipeRefresh.setOnRefreshListener {
            viewModel.refresh()
            binding.myPublishedAlertsSwipeRefresh.isRefreshing = false
        }
    }

    private fun handleError(code: Int) {
        binding.myPublishedAlertsLoading.loadingErrorMessage.text = getString(R.string.unknown_error_occurred)
        binding.myPublishedAlertsLoading.loadingErrorMessage.visibility = View.VISIBLE
        binding.myPublishedAlertsLoading.loadingProgressBar.visibility = View.GONE
        showLoading()
    }

    private fun showLoading(show: Boolean = true) {
       // binding.myAlertsLoading.loadingOverlay.isVisible = show
        binding.myPublishedAlertsLoading.loadingOverlay.isVisible= show
    }

    private fun showEmpty(show: Boolean = true) {
        binding.myPublishedAlertsListEmpty.root.isVisible = show
    }


    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment MyAlertsFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            MyAlertsFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }

        fun newInstance(): Fragment {
            return PublishedAlertsFragment()

        }
    }
}