package com.github.essmehdi.schoolmate.complaints.viewmodels

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.github.essmehdi.schoolmate.complaints.api.dto.CreateBuildingComplaintDto
import com.github.essmehdi.schoolmate.complaints.api.dto.CreateComplaintDto
import com.github.essmehdi.schoolmate.complaints.api.dto.CreateFacilityComplaintDto
import com.github.essmehdi.schoolmate.complaints.api.dto.CreateRoomComplaintDto
import com.github.essmehdi.schoolmate.complaints.models.Complaint
import com.github.essmehdi.schoolmate.shared.api.Api
import com.github.essmehdi.schoolmate.shared.api.BaseResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ComplaintEditorViewModel : ViewModel() {
    val editMode: MutableLiveData<Boolean> = MutableLiveData(false)
    val editId: MutableLiveData<Long?> = MutableLiveData(null)
    val complaint: MutableLiveData<BaseResponse<Complaint>> = MutableLiveData()
    val editStatus: MutableLiveData<BaseResponse<Complaint>> = MutableLiveData(null)

    // the edit complaint needs to take as
    fun editComplaint(createComplaintDto: CreateComplaintDto){
        editStatus.value = BaseResponse.Loading()
        val apiMethod = when(createComplaintDto){
            is CreateRoomComplaintDto -> Api.complaintService.updateComplaintDetails(editId.value!!, createComplaintDto)
            is CreateBuildingComplaintDto -> Api.complaintService.updateComplaintDetails(editId.value!!, createComplaintDto)
            is CreateFacilityComplaintDto -> Api.complaintService.updateComplaintDetails(editId.value!!, createComplaintDto)
            else -> throw Exception("Invalid complaint type")
        }
        apiMethod.enqueue(object : Callback<Complaint> {
            override fun onResponse(call: Call<Complaint>, response: Response<Complaint>) {
                if (response.isSuccessful) {
                    editStatus.value = BaseResponse.Success(response.body()!!)
                } else {
                    editStatus.value = BaseResponse.Error(response.errorBody()?.string(),response.code())
                }
            }
            override fun onFailure(call: Call<Complaint>, t: Throwable) {
                editStatus.value = BaseResponse.Error(0)
            }
        })
    }

    fun fetchComplaintToEdit(){
        complaint.value = BaseResponse.Loading()
        Api.complaintService.getComplaintById(editId.value!!).enqueue(object: Callback<Complaint> {
            override fun onResponse(call: Call<Complaint>, response: Response<Complaint>) {
                if (response.isSuccessful) {
                    complaint.value = BaseResponse.Success(response.body()!!)
                } else {
                    complaint.value = BaseResponse.Error(response.code())
                }
            }
            override fun onFailure(call: Call<Complaint>, t: Throwable) {
                complaint.value = BaseResponse.Error(0)
            }
        })
    }

    fun createComplaint(createComplaintDto: CreateComplaintDto){
        editStatus.value = BaseResponse.Loading()
        val apiMethod = when(createComplaintDto){
            is CreateRoomComplaintDto -> Api.complaintService.createComplaint(createComplaintDto)
            is CreateBuildingComplaintDto -> Api.complaintService.createComplaint(createComplaintDto)
            is CreateFacilityComplaintDto -> Api.complaintService.createComplaint(createComplaintDto)
            else -> throw Exception("Invalid complaint type")
        }
        apiMethod.enqueue(object : Callback<Complaint> {
            override fun onResponse(call: Call<Complaint>, response: Response<Complaint>) {
                if (response.isSuccessful) {
                    editStatus.value = BaseResponse.Success(response.body()!!)
                } else {
                    editStatus.value = BaseResponse.Error(response.errorBody()?.string(),response.code())
                }
            }
            override fun onFailure(call: Call<Complaint>, t: Throwable) {
                Log.e("ComplaintEditor", "onFailure: ", t)
                editStatus.value = BaseResponse.Error(0)
            }
        })
    }
}