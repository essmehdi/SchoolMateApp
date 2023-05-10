package com.github.essmehdi.schoolmate.alerts.ui

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.RadioButton
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.github.essmehdi.schoolmate.R
import com.github.essmehdi.schoolmate.alerts.api.dto.AlertDto
import com.github.essmehdi.schoolmate.alerts.api.dto.EditAlertDto
import com.github.essmehdi.schoolmate.alerts.models.Alert
import com.github.essmehdi.schoolmate.alerts.models.AlertType
import com.github.essmehdi.schoolmate.alerts.viewmodels.AlertEditorViewModel
import com.github.essmehdi.schoolmate.databinding.ActivityAddAlertBinding
import com.github.essmehdi.schoolmate.schoolnavigation.models.Point
import com.github.essmehdi.schoolmate.shared.api.BaseResponse
import com.github.essmehdi.schoolmate.shared.ui.MapSelectorActivity


class AddAlertActivity : AppCompatActivity() {

    private lateinit var locationLauncher: ActivityResultLauncher<Intent>
    private lateinit var binding: ActivityAddAlertBinding
    private lateinit var viewModel: AlertEditorViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddAlertBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (intent.hasExtra("alert")) {
            val alert = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                intent.getSerializableExtra("alert", Alert::class.java)
            } else {
                intent.getSerializableExtra("alert")
            } as Alert
            enableEditMode(alert)
        }
        locationLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
            result ->
            if (result.resultCode == RESULT_OK) {
                val point = result.data?.extras?.getParcelable(MapSelectorActivity.EXTRA_RESULT_SINGLE_POINT) as? ArrayList<Point>?
                if(point != null){
                    viewModel.selectedLocation.value = point[0]
                }
                else{
                    Toast.makeText(this, R.string.error_zone_result, Toast.LENGTH_SHORT).show()
                }
            }
        }
        binding.alertEditorConfirmAddEditButton.setOnClickListener{sendAlert()}
        binding.alertEditorAreaChangeButton.setOnClickListener {
            val intent = Intent(this, MapSelectorActivity::class.java)
            locationLauncher.launch(intent)
        }

        viewModel.uploadStatus.observe(this) {
            when (it) {
                null -> {
                    restoreButton()
                }
                is BaseResponse.Loading -> {
                    buttonLoading()
                }
                is BaseResponse.Success -> {
                    setResult(RESULT_OK)
                    finish()
                }
                is BaseResponse.Error -> {
                    handleError(it.code!!)
                }
            }
        }
    }
    private fun sendAlert() {

        val title= binding.alertEditorNameEditText.text?.toString()
        if(title.isNullOrEmpty()){
            binding.alertEditorNameTextInputLayout.error = "Title is required"
            return
        } else {
            binding.alertEditorNameTextInputLayout.error = null
        }
        val description = binding.alertEditorDescriptionEditText.text?.toString()
        if(description.isNullOrEmpty()){
            binding.alertEditorDescriptionTextInputLayout.error = "Description is required"
            return
        } else {
            binding.alertEditorDescriptionTextInputLayout.error = null
        }
        val type = when(binding.alertEditorAreaRadioGroup.checkedRadioButtonId){
            R.id.alert_editor_area_thieves_radio_button -> "THIEVES"
            R.id.alert_editor_area_pharmacy_radio_button -> "PHARMACY"
            R.id.alert_editor_area_library_radio_button -> "LIBRARY"
            else -> null
        }
        if(type == null){
            Toast.makeText(this, R.string.error_type_result, Toast.LENGTH_SHORT).show()
            return
        }
        val point = viewModel.selectedLocation.value
        if(point == null){
            Toast.makeText(this, R.string.error_zone_alert_result, Toast.LENGTH_SHORT).show()
            return
        }
        if (viewModel.editMode.value == true){
            val alertEditDto = EditAlertDto(title, description, AlertType.valueOf(type), listOf(point.x, point.y))
            viewModel.editAlert(alertEditDto)
        } else{

            val alertCreateDto = AlertDto(title, description, AlertType.valueOf(type), listOf(point.x, point.y))
            viewModel.addAlert(alertCreateDto)
        }
    }

    private fun buttonLoading() {
        binding.alertEditorConfirmAddEditButton.apply {
            text = getString(
                if (viewModel.editMode.value == true) R.string.label_editing_form else R.string.label_uploading_form)
            isActivated = false
        }

    }
    private fun restoreButton() {
        binding.alertEditorConfirmAddEditButton.apply {
            text = if (viewModel.editMode.value == true) getString(R.string.action_send_edit_form) else getString(R.string.action_send_upload_form)
            isActivated = true
        }
    }

    private fun enableEditMode(alert: Alert) {
        viewModel.editMode.value = true

        viewModel.editId.value = alert.id

        binding.apply {
            alertEditorTitleText.text= "Edit Alert"
            alertEditorNameEditText.setText(alert.title)
            alertEditorDescriptionEditText.setText(alert.description)
            val type = alert.type
            if (type ==AlertType.THIEVES) {
                alertEditorAreaThievesRadioButton.isChecked = true
            } else if (type ==AlertType.PHARMACY) {
                alertEditorAreaPharmacyRadioButton.isChecked = true
            } else if (type==AlertType.LIBRARY){
                alertEditorAreaLibraryRadioButton.isChecked = true
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
}
