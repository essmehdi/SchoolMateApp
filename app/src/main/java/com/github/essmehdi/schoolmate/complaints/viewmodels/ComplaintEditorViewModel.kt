package com.github.essmehdi.schoolmate.complaints.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.github.essmehdi.schoolmate.complaints.api.dto.CreateComplaintDto
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
    val editStatus: MutableLiveData<BaseResponse<Complaint>> = MutableLiveData()

    fun editComplaint(createComplaintDto: CreateComplaintDto){
        editStatus.value = BaseResponse.Loading()
        Api.complaintService.updateComplaintDetails(editId.value!!, createComplaintDto).enqueue(object : retrofit2.Callback<Complaint> {
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
        Api.complaintService.createComplaint(createComplaintDto).enqueue(object : Callback<Complaint> {
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
}