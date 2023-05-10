package com.github.essmehdi.schoolmate.complaints.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View.*
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import com.github.essmehdi.schoolmate.R
import com.github.essmehdi.schoolmate.complaints.api.dto.CreateBuildingComplaintDto
import com.github.essmehdi.schoolmate.complaints.api.dto.CreateComplaintDto
import com.github.essmehdi.schoolmate.complaints.api.dto.CreateFacilityComplaintDto
import com.github.essmehdi.schoolmate.complaints.api.dto.CreateRoomComplaintDto
import com.github.essmehdi.schoolmate.complaints.enumerations.BuildingProb
import com.github.essmehdi.schoolmate.complaints.enumerations.FacilityType
import com.github.essmehdi.schoolmate.complaints.enumerations.RoomProb
import com.github.essmehdi.schoolmate.complaints.models.BuildingComplaint
import com.github.essmehdi.schoolmate.complaints.models.FacilitiesComplaint
import com.github.essmehdi.schoolmate.complaints.models.RoomComplaint
import com.github.essmehdi.schoolmate.complaints.viewmodels.ComplaintEditorViewModel
import com.github.essmehdi.schoolmate.databinding.ActivityComplaintEditorBinding
import com.github.essmehdi.schoolmate.shared.api.BaseResponse

class ComplaintEditorActivity : AppCompatActivity() {

    companion object {
        const val RESULT_ACTION_CREATED = 11
        const val RESULT_ACTION_UPDATED = 22
    }

    private lateinit var binding: ActivityComplaintEditorBinding
    private var autoCompleteTextViewType: AutoCompleteTextView? = null
    private var autoCompleteTextViewBuilding: AutoCompleteTextView? = null
    private var autoCompleteTextViewRoom: AutoCompleteTextView? = null
    private var autoCompleteTextViewFacility: AutoCompleteTextView? = null
    private var arrayAdapter: ArrayAdapter<String>? = null
    private val viewModel: ComplaintEditorViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityComplaintEditorBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.complaintsEditorToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_navigation_left)

        // Set up the complaint type dropdown
        autoCompleteTextViewType = binding.complaintType
        arrayAdapter = ArrayAdapter(this, R.layout.list_item_dropdown, resources.getStringArray(R.array.complaint_type_options))
        autoCompleteTextViewType!!.setAdapter(arrayAdapter)
        autoCompleteTextViewType!!.setOnItemClickListener(){ _, _, position, _ ->
            showFields(position)
        }

        // show a toast when reaching the max length of the description field using the text watcher
        binding.complaintDescriptionEditText.doOnTextChanged { text, _, _, _ ->
            if(text!!.length == resources.getInteger(R.integer.max_complaint_description_length)) {
                Toast.makeText(this, resources.getString(R.string.complaint_description_max_length), Toast.LENGTH_SHORT).show()
            }
        }


        if(intent.hasExtra("complaintId")) {
            // Set the page title
            binding.editOrAddComplaintTextView.text = resources.getString(R.string.add_edit_complaint_text_view, "Edit")
            val complaintId = intent.getLongExtra("complaintId", 0)
            viewModel.editMode.value = true
            viewModel.editId.value = complaintId
            viewModel.fetchComplaintToEdit()
            viewModel.complaint.observe(this) {
                when (it) {
                    is BaseResponse.Success -> {
                        showLoading(false)
                        showComplaintDetails()
                    }
                    is BaseResponse.Error -> {
                        showLoading(false)
                        handleComplaintLoadingError(it.code!!)
                    }
                    is BaseResponse.Loading -> {
                        showLoading(true)
                    }
                    else -> {}
                }
            }
        } else if(intent.hasExtra("complaint_type")){
            showLoading(false)
            // Set the page title
            binding.editOrAddComplaintTextView.text = resources.getString(R.string.add_edit_complaint_text_view, "Add")
            when (intent.getStringExtra("complaint_type")) {
                "building" -> {
                    binding.complaintType.setText(resources.getStringArray(R.array.complaint_type_options)[0], false)
                    showFields(0)
                }
                "room" -> {
                    binding.complaintType.setText(resources.getStringArray(R.array.complaint_type_options)[1], false)
                    showFields(1)
                }
                else -> {
                    binding.complaintType.setText(resources.getStringArray(R.array.complaint_type_options)[2], false)
                    showFields(2)
                }
            }
        } else { // If no complaint type is passed (shouldn't happen) set the default complaint type to building
            showLoading(false)
            binding.complaintType.setText(resources.getStringArray(R.array.complaint_type_options)[0], false)
            showFields(0)
        }

        binding.complaintSubmitButton.setOnClickListener{
            sendForm()
        }

        viewModel.editStatus.observe(this) {
            when (it) {
                null -> {
                    restoreButton()
                }
                is BaseResponse.Loading -> {
                    buttonLoading()
                }
                is BaseResponse.Success -> {
                    // If the complaint was edited from the details activity, return to the details activity with the result
                    if(intent.hasExtra("source")){
                        if(viewModel.editMode.value!!) {
                            setResult(RESULT_ACTION_UPDATED, Intent().putExtra("complaintId", viewModel.editId.value!!))
                        } else {
                            setResult(RESULT_ACTION_CREATED, Intent().putExtra("complaintId", it.data!!.id))
                        }
                        finish()
                    } else {
                        // else go to the complaint details activity by starting a new intent
                        val intent = Intent(this, ComplaintDetailsActivity::class.java)
                        intent.putExtra("edited", viewModel.editMode.value!!)
                        intent.putExtra("complaintId", if (viewModel.editMode.value!!) viewModel.editId.value!! else it.data!!.id)
                        startActivity(intent)
                    }
                }
                is BaseResponse.Error -> {
                    handleError(it.code!!, it.message)
                }
            }
        }
    }

    private fun showComplaintDetails() {
        // Set complaint type and its details depending on the type
        when(viewModel.complaint.value?.data){
            is BuildingComplaint -> {
                binding.complaintType.setText(resources.getStringArray(R.array.complaint_type_options)[0], false)
                showFields(0)
                val complaint = viewModel.complaint.value?.data as BuildingComplaint
                binding.buildingComplaintCard.buildingProblem.setText(complaint.buildingProb.name, false)
                binding.buildingComplaintCard.buildingEditText.setText(complaint.building)
            }
            is RoomComplaint -> {
                binding.complaintType.setText(resources.getStringArray(R.array.complaint_type_options)[1], false)
                showFields(1)
                val complaint = viewModel.complaint.value?.data as RoomComplaint
                binding.roomComplaintCard.roomProblem.setText(complaint.roomProb.name, false)
                binding.roomComplaintCard.roomNumberEditText.setText(complaint.room)
            }
            else -> { // Facilities complaint
                binding.complaintType.setText(resources.getStringArray(R.array.complaint_type_options)[2], false)
                showFields(2)
                val complaint = viewModel.complaint.value?.data as FacilitiesComplaint
                binding.facilitiesComplaintCard.facilityType.setText(complaint.facilityType.name, false)
                if(complaint.facilityType == FacilityType.CLASS) {
                    binding.facilitiesComplaintCard.classroomNameField.visibility = VISIBLE
                    binding.facilitiesComplaintCard.classroomNameEditText.setText(complaint.className)
                    // the facility type shouldn't be changed in edit mode if it's a class problem
                    // (just like in the complaint type)
                    binding.facilitiesComplaintCard.facilityTypeInputLayout.isEnabled = false
                }
            }
        }
        // Set complaint description
        binding.complaintDescriptionEditText.setText(viewModel.complaint.value?.data?.description)
    }

    private fun showFields(position: Int) {
        // If the edit mode is on, the complaint type is already set and cannot be changed
        if(viewModel.editMode.value!!) {
            binding.complaintTypeInputLayout.isEnabled = false
        }
        when(position) {
            0 -> { // Building
                binding.buildingComplaintCard.root.visibility = VISIBLE
                binding.roomComplaintCard.root.visibility = GONE
                binding.facilitiesComplaintCard.root.visibility = GONE
                // Set building problem options
                autoCompleteTextViewBuilding = binding.buildingComplaintCard.buildingProblem
                arrayAdapter = ArrayAdapter(this, R.layout.list_item_dropdown, BuildingProb.values().map { it.name })
                autoCompleteTextViewBuilding!!.setAdapter(arrayAdapter)
            }
            1 -> { // Room
                binding.buildingComplaintCard.root.visibility = GONE
                binding.roomComplaintCard.root.visibility = VISIBLE
                binding.facilitiesComplaintCard.root.visibility = GONE
                // Set room problem options
                autoCompleteTextViewRoom = binding.roomComplaintCard.roomProblem
                arrayAdapter = ArrayAdapter(this, R.layout.list_item_dropdown, RoomProb.values().map { it.name })
                autoCompleteTextViewRoom!!.setAdapter(arrayAdapter)
            }
            2 -> { // Facilities
                binding.buildingComplaintCard.root.visibility = GONE
                binding.roomComplaintCard.root.visibility = GONE
                binding.facilitiesComplaintCard.root.visibility = VISIBLE
                // Set facility type options
                autoCompleteTextViewFacility = binding.facilitiesComplaintCard.facilityType
                arrayAdapter = ArrayAdapter(this, R.layout.list_item_dropdown, FacilityType.values().map { it.name })
                autoCompleteTextViewFacility!!.setAdapter(arrayAdapter)
                // Set an action listener to show the class room number field if the selected facility type is class room
                autoCompleteTextViewFacility!!.setOnItemClickListener { _, _, position, _ ->
                    binding.facilitiesComplaintCard.classroomNameField.visibility = if (position == FacilityType.CLASS.ordinal) VISIBLE else GONE
                }
            }
        }

    }

    private fun sendForm() {
        if(validateForm()){
            // Create the dto object to send
            val complaintType = autoCompleteTextViewType!!.text.toString()
            val description = binding.complaintDescriptionEditText.text.toString()
            val dto: CreateComplaintDto
            when(complaintType) {
                getString(R.string.building_complaint) -> {
                    val buildingProblem = autoCompleteTextViewBuilding!!.text.toString()
                    val building = binding.buildingComplaintCard.buildingEditText.text.toString().replace("\\s+".toRegex(), "")
                    dto = CreateBuildingComplaintDto(building, BuildingProb.valueOf(buildingProblem))
                    dto.type = getString(R.string.building_complaint_type_dto)
                }
                getString(R.string.room_complaint) -> {
                    val roomProblem = autoCompleteTextViewRoom!!.text.toString()
                    val room = binding.roomComplaintCard.roomNumberEditText.text.toString().replace("\\s+".toRegex(), "")
                    dto = CreateRoomComplaintDto(room, RoomProb.valueOf(roomProblem))
                    dto.type = getString(R.string.room_complaint_type_dto)
                }
                else -> {
                    val facilityType = autoCompleteTextViewFacility!!.text.toString()
                    val classroomName = binding.facilitiesComplaintCard.classroomNameEditText.text.toString().replace("\\s+".toRegex(), " ")
                    Log.d("classroomName", classroomName)
                    // if the classroom name is not empty, send the complaint with the classroom name
                    // we are sure that the classroom name is not empty if the facility type is class room (thanks to the validation)
                    if(classroomName.isNotEmpty())
                        dto = CreateFacilityComplaintDto(FacilityType.valueOf(facilityType), classroomName)
                    else
                        dto = CreateFacilityComplaintDto(FacilityType.valueOf(facilityType))
                    dto.type = getString(R.string.facilities_complaint_type_dto)
                }
            }
            // Set the description and type
            dto.description = description
            // Send the complaint
            if(viewModel.editMode.value!!) {
                viewModel.editComplaint(dto)
            } else {
                viewModel.createComplaint(dto)
            }
        } else {
            Toast.makeText(this, getString(R.string.complaint_form_invalid), Toast.LENGTH_SHORT).show()
        }
    }

    private fun validateForm() : Boolean {
        var valid = true
        val complaintType = autoCompleteTextViewType!!.text.toString()
        val description = binding.complaintDescriptionEditText.text.toString()
        if (complaintType.isEmpty()) {
            autoCompleteTextViewType?.error = getString(R.string.complaint_type_required)
            valid = false
        }
        if (description.isEmpty()) {
            binding.complaintDescriptionEditText.error = getString(R.string.complaint_description_required)
            valid = false
        }
        if(complaintType == getString(R.string.building_complaint)){
            val buildingProblem = autoCompleteTextViewBuilding!!.text.toString()
            val building = binding.buildingComplaintCard.buildingEditText.text.toString()
            if (buildingProblem.isEmpty()) {
                autoCompleteTextViewBuilding?.error = getString(R.string.building_problem_required)
                valid = false
            }
            if (building.isEmpty()) {
                binding.buildingComplaintCard.buildingEditText.error = getString(R.string.building_required)
                valid = false
            }
        } else if(complaintType == getString(R.string.room_complaint)){
            val roomProblem = autoCompleteTextViewRoom!!.text.toString()
            val room = binding.roomComplaintCard.roomNumberEditText.text.toString()
            if (roomProblem.isEmpty()) {
                autoCompleteTextViewRoom?.error = getString(R.string.room_problem_required)
                valid = false
            }
            if (room.isEmpty()) {
                binding.roomComplaintCard.roomNumberEditText.error = getString(R.string.room_required)
                valid = false
            }
        } else if(complaintType == getString(R.string.facility_complaint)){
            val facilityType = autoCompleteTextViewFacility!!.text.toString()
            val classroomName = binding.facilitiesComplaintCard.classroomNameEditText.text.toString()
            if (facilityType.isEmpty()) {
                autoCompleteTextViewFacility?.error = getString(R.string.facility_type_required)
                valid = false
            }
            if (classroomName.isEmpty() && facilityType == FacilityType.CLASS.name) {
                binding.facilitiesComplaintCard.classroomNameEditText.error = getString(R.string.classroom_name_required)
                valid = false
            }
        }
        return valid
    }

    private fun showLoading(show: Boolean = false) {
       binding.complaintEditLoading.loadingOverlay.isVisible = show
    }

    private fun buttonLoading() {
        binding.complaintSubmitButton.apply {
            text = getString(
                if (viewModel.editMode.value == true) R.string.updating_complaint else R.string.submitting_complaint)
            isActivated = false
        }
    }

    private fun restoreButton() {
        binding.complaintSubmitButton.apply {
            text = if (viewModel.editMode.value == true) getString(R.string.edit_complaint) else getString(R.string.submit_complaint)
            isActivated = true
        }
    }

    private fun handleError(code: Int, message: String?) {
        if (code == 400) {
            Toast.makeText(this, R.string.error_wrong_field_value, Toast.LENGTH_LONG).show()
            //Toast.makeText(this, message, Toast.LENGTH_LONG).show()
        } else {
            Toast.makeText(this, R.string.unknown_error_occurred, Toast.LENGTH_LONG).show()
        }
        restoreButton()
    }

    private fun handleComplaintLoadingError(code: Int) {
        binding.complaintEditLoading.loadingErrorMessage.text = getString(R.string.unknown_error_occurred)
        binding.complaintEditLoading.loadingErrorMessage.visibility = VISIBLE
        binding.complaintEditLoading.loadingProgressBar.visibility = GONE
        showLoading()
    }
}