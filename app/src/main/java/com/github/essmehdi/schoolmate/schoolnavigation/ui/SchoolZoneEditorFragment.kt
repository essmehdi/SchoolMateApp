package com.github.essmehdi.schoolmate.schoolnavigation.ui

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.github.essmehdi.schoolmate.R
import com.github.essmehdi.schoolmate.databinding.FragmentSchoolZoneEditorBinding
import com.github.essmehdi.schoolmate.schoolnavigation.api.dto.CreateSchoolZoneDto
import com.github.essmehdi.schoolmate.schoolnavigation.api.dto.EditSchoolZoneDto
import com.github.essmehdi.schoolmate.schoolnavigation.models.Point
import com.github.essmehdi.schoolmate.schoolnavigation.models.SchoolZone
import com.github.essmehdi.schoolmate.schoolnavigation.viewmodels.SchoolZoneEditorViewModel
import com.github.essmehdi.schoolmate.schoolnavigation.viewmodels.SchoolZonesViewModel
import com.github.essmehdi.schoolmate.shared.api.BaseResponse
import com.github.essmehdi.schoolmate.shared.ui.MapSelectorActivity
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

private const val ARG_ZONE = "zone"

/**
 * A simple [Fragment] subclass.
 * Use the [SchoolZoneEditorFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class SchoolZoneEditorFragment : BottomSheetDialogFragment() {

  private lateinit var binding: FragmentSchoolZoneEditorBinding
  private lateinit var mapSelectorLauncher: ActivityResultLauncher<Intent>

  private val viewModel: SchoolZoneEditorViewModel by viewModels()
  private val parentViewModel: SchoolZonesViewModel by viewModels({ requireActivity() })
  private var zone: SchoolZone? = null
  private var editMode = false

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    arguments?.let {
      val passedZone = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        it.getSerializable(ARG_ZONE, SchoolZone::class.java)
      } else {
        @Suppress("DEPRECATION")
        it.getSerializable(ARG_ZONE) as SchoolZone
      }
      if (passedZone != null) {
        editMode = true
        zone = passedZone
      }
    }
  }

  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View {
    binding = FragmentSchoolZoneEditorBinding.inflate(inflater, container, false)
    return binding.root
  }

  @Suppress("UNCHECKED_CAST", "DEPRECATION")
  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    mapSelectorLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
      if (result.resultCode == Activity.RESULT_OK) {
        val points = (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
          result.data?.getSerializableExtra(MapSelectorActivity.EXTRA_RESULT_MULTIPLE_POINTS, ArrayList::class.java)
        } else {
          result.data?.getSerializableExtra(MapSelectorActivity.EXTRA_RESULT_MULTIPLE_POINTS)
        }) as? ArrayList<*> ?: emptyList()
        viewModel.chosenPoints.value = points.toList() as List<Point>
      }
    }

    if (editMode) {
      binding.schoolZoneEditorTitleText.text = requireContext().getString(R.string.school_zone_editor_edit_title)
      binding.schoolZoneEditorConfirmAddEditButton.text = requireContext().getString(R.string.label_school_zone_editor_confirm_edit_button)
      binding.schoolZoneEditorDeleteButton.isVisible = true
      zone!!.let {
        binding.schoolZoneEditorNameEditText.setText(it.name)
        binding.schoolZoneEditorDescriptionEditText.setText(it.description)
        viewModel.chosenPoints.value = it.geometry.points
      }
    }

    binding.schoolZoneEditorAreaChangeButton.setOnClickListener {
      mapSelectorLauncher.launch(Intent(requireContext(), MapSelectorActivity::class.java).apply {
        if (editMode) {
          putExtra(MapSelectorActivity.EXTRA_DEFAULT_POINTS, viewModel.chosenPoints.value!! as ArrayList<Point>)
        }
      })
    }

    binding.schoolZoneEditorConfirmAddEditButton.setOnClickListener {
      validateAndSend()
    }

    if (editMode) {
      binding.schoolZoneEditorDeleteButton.setOnClickListener {
        viewModel.deleteZone(zone!!.id)
      }
    }

    viewModel.chosenPoints.observe(viewLifecycleOwner) {
      binding.schoolZoneEditorAreaChangeButton.apply {
        text = requireContext().resources.getQuantityString(R.plurals.label_school_zone_editor_chosen_points, it.size, it.size)
      }
    }

    viewModel.status.observe(viewLifecycleOwner) {
      if (it is BaseResponse.Loading) {
        binding.schoolZoneEditorConfirmAddEditButton.text =
          if (editMode) getString(R.string.label_school_zone_editor_confirm_edit_button_loading)
          else getString(R.string.label_school_zone_editor_confirm_add_button_loading)
      } else {
        binding.schoolZoneEditorConfirmAddEditButton.text =
          if (editMode) getString(R.string.label_school_zone_editor_confirm_edit_button)
          else getString(R.string.label_school_zone_editor_confirm_add_button)
      }
      if (it is BaseResponse.Error) {
        Toast.makeText(requireContext(), R.string.unknown_error_occurred, Toast.LENGTH_SHORT).show()
      }
      if (it is BaseResponse.Success) {
        parentViewModel.fetchZones()
        dismiss()
      }
    }

    viewModel.deleteStatus.observe(viewLifecycleOwner) {
      if (it is BaseResponse.Loading) {
        binding.schoolZoneEditorDeleteButton.text = getString(R.string.label_school_zone_editor_delete_button_loading)
      } else {
        binding.schoolZoneEditorDeleteButton.text = getString(R.string.label_school_zone_editor_delete_button)
      }
      if (it is BaseResponse.Error) {
          Toast.makeText(requireContext(), R.string.unknown_error_occurred, Toast.LENGTH_SHORT).show()
      }
      if (it is BaseResponse.Success) {
          parentViewModel.fetchZones()
          dismiss()
      }
    }
  }

  private fun validateAndSend() {
    val name = binding.schoolZoneEditorNameEditText.text.toString()
    val description = binding.schoolZoneEditorDescriptionEditText.text.toString()
    val points = viewModel.chosenPoints.value!!
    if (points.isEmpty()) {
      Toast.makeText(requireContext(), R.string.label_school_zone_editor_points_required, Toast.LENGTH_SHORT).show()
      return
    }
    if (name.isEmpty()) {
      binding.schoolZoneEditorNameTextInputLayout.error = getString(R.string.label_school_zone_editor_name_required)
      return
    }
    if (description.isEmpty()) {
      binding.schoolZoneEditorDescriptionTextInputLayout.error = getString(R.string.label_school_zone_editor_description_required)
      return
    }
    val processedPoints = viewModel.chosenPoints.value!!.map {
      listOf(it.x, it.y)
    }
    if (editMode) {
      val dto = EditSchoolZoneDto(name, description, processedPoints)
      viewModel.editZone(zone!!.id, dto)
    } else {
      val dto = CreateSchoolZoneDto(name, description, processedPoints)
      viewModel.addZone(dto)
    }
  }

  companion object {
    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param schoolZone School zone to edit (optional).
     * @return A new instance of fragment SchoolZoneEditorFragment.
     */
    @JvmStatic
    fun newInstance(schoolZone: SchoolZone? = null) =
      SchoolZoneEditorFragment().apply {
        schoolZone?.let {
          arguments = Bundle().apply {
            putSerializable(ARG_ZONE, it)
          }
        }
      }
  }
}