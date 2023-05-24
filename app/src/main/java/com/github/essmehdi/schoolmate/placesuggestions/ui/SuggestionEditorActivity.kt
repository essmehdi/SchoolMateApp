package com.github.essmehdi.schoolmate.placesuggestions.ui

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.github.essmehdi.schoolmate.R
import com.github.essmehdi.schoolmate.databinding.ActivitySuggestionEditorBinding
import com.github.essmehdi.schoolmate.placesuggestions.api.dto.CreateSuggestionDto
import com.github.essmehdi.schoolmate.placesuggestions.api.dto.EditSuggestionDto
import com.github.essmehdi.schoolmate.placesuggestions.enumerations.SuggestionType
import com.github.essmehdi.schoolmate.placesuggestions.models.PlaceSuggestions
import com.github.essmehdi.schoolmate.placesuggestions.viewmodels.SuggestionEditorViewModel
import com.github.essmehdi.schoolmate.schoolnavigation.models.Point
import com.github.essmehdi.schoolmate.shared.api.BaseResponse
import com.github.essmehdi.schoolmate.shared.ui.MapSelectorActivity
import com.google.android.material.radiobutton.MaterialRadioButton

class SuggestionEditorActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySuggestionEditorBinding
    private val viewModel: SuggestionEditorViewModel by viewModels()
    private lateinit var pickerLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySuggestionEditorBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.suggestionsToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_navigation_left)

        if (intent.hasExtra("id")) {
            viewModel.editMode.value = true
        }
        // if in editor mode, fetch the suggestion and set its content to the layout items
        if (viewModel.editMode.value == true) {
            binding.suggestionEditorTitle.text = getString(R.string.edit_suggestion_title)
            binding.suggestionUploadFormSendButton.text = getString(R.string.action_edit_suggestion)
            val id = intent.getLongExtra("id", 0)
            viewModel.loadSuggestion(id)
            viewModel.suggestion.observe(this) {
                when (it) {
                    is BaseResponse.Loading -> {
                        showLoading()
                    }

                    is BaseResponse.Success -> {
                        showLoading(false)
                        fillPage()
                    }

                    is BaseResponse.Error -> {
                        showLoading(false)
                        handleError(it.code!!)
                    }
                }
            }
        } else {
            binding.suggestionEditorTitle.text = getString(R.string.add_suggestion_title)
            showLoading(false)
        }

        pickerLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    val points = (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        result.data?.getSerializableExtra(
                            MapSelectorActivity.EXTRA_RESULT_SINGLE_POINT,
                            ArrayList::class.java
                        )
                    } else {
                        result.data?.getSerializableExtra(MapSelectorActivity.EXTRA_RESULT_SINGLE_POINT)
                    }) as? ArrayList<*> ?: emptyList()
                    val point = (points.toList() as List<Point>)[0]
                    viewModel.selectedPoint.value = listOf(point.x, point.y)
                }
            }

        listOf(
            binding.suggestionEditorStudyTypeRadioButton,
            binding.suggestionEditorFoodTypeRadioButton,
            binding.suggestionEditorEntertainmentRadioButton,
            binding.suggestionEditorOtherRadioButton
        ).forEach { radioButton ->
            radioButton.setOnClickListener {
                singleCheck(it as MaterialRadioButton)
            }
        }

        binding.suggestionEditorAreaChangeButton.setOnClickListener {
            val intent = Intent(this, MapSelectorActivity::class.java)
            intent.putExtra("singleMode", true)
            if (!viewModel.selectedPoint.value.isNullOrEmpty())
                intent.putExtra(
                    "defaultPoints",
                    arrayListOf(
                        Point(
                            viewModel.selectedPoint.value!![0],
                            viewModel.selectedPoint.value!![1]
                        )
                    )
                )
            pickerLauncher.launch(intent)
        }

        viewModel.selectedPoint.observe(this) {
            binding.suggestionEditorAreaChangeButton.text =
                if (!it.isNullOrEmpty())
                    getString(R.string.suggestions_edior_area_button_change)
                else
                    getString(R.string.label_suggestion_location)
        }

        //setting the send form button
        binding.suggestionUploadFormSendButton.setOnClickListener {
            sendForm()
        }
    }

    private fun singleCheck(target: MaterialRadioButton) {
        listOf(
            binding.suggestionEditorStudyTypeRadioButton,
            binding.suggestionEditorFoodTypeRadioButton,
            binding.suggestionEditorEntertainmentRadioButton,
            binding.suggestionEditorOtherRadioButton
        ).forEach {
            it.isChecked = it.id == target.id
        }
    }

    private fun sendForm() {
        val description = binding.suggestionDescriptionEditorEdittext.text.toString()
        if (description.isBlank()) {
            binding.suggestionDescriptionEdittextLayout.error =
                getString(R.string.error_suggestion_description_required)
            return
        }
        var type = if (binding.suggestionEditorStudyTypeRadioButton.isChecked) {
            SuggestionType.StudyPlace
        } else if (binding.suggestionEditorFoodTypeRadioButton.isChecked) {
            SuggestionType.FoodPlace
        } else if (binding.suggestionEditorEntertainmentRadioButton.isChecked) {
            SuggestionType.Entertainment
        } else {
            SuggestionType.Other
        }
        //--------------------------------------
        if (viewModel.editMode.value == true) {
            val editDto = EditSuggestionDto(description, type, viewModel.selectedPoint.value)
            viewModel.editSuggestion(editDto)
        } else {
            if (viewModel.selectedPoint.value.isNullOrEmpty()) {
                Toast.makeText(this, R.string.error_suggestion_location_required, Toast.LENGTH_LONG).show()
                return
            }
            val createDto = CreateSuggestionDto(description, type, viewModel.selectedPoint.value!!)
            viewModel.addSuggestion(createDto)
        }

        viewModel.uploadStatus.observe(this) {
            when (it) {
                is BaseResponse.Loading -> {
                    showLoading()
                }

                is BaseResponse.Success -> {
                    showLoading(false)
                    if (viewModel.editMode.value != true) {
                        val intent = Intent(this, SuggestionDetailsActivity::class.java)
                        intent.putExtra("id", viewModel.editId.value)
                        startActivity(intent)
                    } else {
                        setResult(RESULT_OK)
                        finish()
                    }
                }

                is BaseResponse.Error -> {
                    showLoading(false)
                    handleError(it.code!!)
                }
            }
        }
    }

    private fun handleError(code: Int) {
        if (code == 400) {
            Toast.makeText(this, R.string.error_wrong_field_value, Toast.LENGTH_LONG).show()
        } else {
            Toast.makeText(this, R.string.unknown_error_occurred, Toast.LENGTH_LONG).show()
        }
    }

    private fun fillPage() {
        binding.suggestionDescriptionEditorEdittext.setText(viewModel.suggestion.value!!.data!!.description)
        when (viewModel.suggestion.value!!.data!!.suggestiontype) {
            SuggestionType.FoodPlace -> {
                binding.suggestionEditorFoodTypeRadioButton.isChecked = true
            }

            SuggestionType.StudyPlace -> {
                binding.suggestionEditorStudyTypeRadioButton.isChecked = true
            }

            SuggestionType.Entertainment -> {
                binding.suggestionEditorEntertainmentRadioButton.isChecked = true
            }

            SuggestionType.Other -> {
                binding.suggestionEditorOtherRadioButton.isChecked = true
            }
        }
        viewModel.selectedPoint.value = listOf(
            viewModel.suggestion.value!!.data!!.coordinates.x,
            viewModel.suggestion.value!!.data!!.coordinates.y
        )
        // TODO map fill
    }

    private fun showLoading(show: Boolean = true) {
        binding.suggestionsLoading.loadingOverlay.isVisible = show
    }

}


// intent = Intent ("context", MapSelectorActivity::class.java)
// intent.putExtra ("singleMode", true)
//intent.putExtra ("defaultPoints", array d points)
