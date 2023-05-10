package com.github.essmehdi.schoolmate.complaints.viewmodels

import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.github.essmehdi.schoolmate.auth.models.User
import com.github.essmehdi.schoolmate.complaints.api.dto.EditComplaintStatusAndHandlerDto
import com.github.essmehdi.schoolmate.complaints.enumerations.ComplaintStatus
import com.github.essmehdi.schoolmate.complaints.models.Complaint
import com.github.essmehdi.schoolmate.shared.api.Api
import com.github.essmehdi.schoolmate.shared.api.BaseResponse
import com.github.essmehdi.schoolmate.shared.api.dto.MessageResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ComplaintDetailsViewModel : ViewModel() {

    val id: MutableLiveData<Long> = MutableLiveData<Long>()
    val currentComplainant: MutableLiveData<User> = MutableLiveData<User>()
    val complaint: MutableLiveData<BaseResponse<Complaint>> = MutableLiveData<BaseResponse<Complaint>>()
    val deleteStatus: MutableLiveData<BaseResponse<MessageResponse>?> = MutableLiveData(null)

    // For the handlers - this is the edit status of the complaint when they try to change its status/assignee
    val editStatus: MutableLiveData<BaseResponse<Complaint>> = MutableLiveData(null)


    fun getComplaint() {
        complaint.value = BaseResponse.Loading()
        Api.complaintService.getComplaintById(id.value!!).enqueue(object: Callback<Complaint> {
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

    fun deleteComplaint() {
        deleteStatus.value = BaseResponse.Loading()
        Api.complaintService.deleteComplaint(id.value!!).enqueue(object: Callback<MessageResponse> {
            override fun onResponse(call: Call<MessageResponse>, response: Response<MessageResponse>) {
                if (response.isSuccessful) {
                    deleteStatus.value = BaseResponse.Success(response.body()!!)
                } else {
                    deleteStatus.value = BaseResponse.Error(response.code())
                }
            }
            override fun onFailure(call: Call<MessageResponse>, t: Throwable) {
                deleteStatus.value = BaseResponse.Error(0)
            }
        })
    }

    fun fetchCurrentComplainant(){
        Api.authService.me().enqueue(object: Callback<User> {
            override fun onResponse(call: Call<User>, response: Response<User>) {
                if (response.isSuccessful) {
                    currentComplainant.value = response.body()
                }
            }
            override fun onFailure(call: Call<User>, t: Throwable) {
            }
        })
    }

    fun editComplaintStautsAndHandler(editComplaintStatusAndHandlerDto: EditComplaintStatusAndHandlerDto){
        editStatus.value = BaseResponse.Loading()
        Api.complaintService.updateComplaintStatusAndHandler(id.value!!, editComplaintStatusAndHandlerDto).enqueue(object: Callback<Complaint> {
            override fun onResponse(call: Call<Complaint>, response: Response<Complaint>) {
                if (response.isSuccessful) {
                    editStatus.value = BaseResponse.Success(response.body()!!)
                } else {
                    editStatus.value = BaseResponse.Error(response.code())
                }
            }
            override fun onFailure(call: Call<Complaint>, t: Throwable) {
                editStatus.value = BaseResponse.Error(0)
            }
        })
    }

    fun refresh() {
        complaint.value = BaseResponse.Loading()
        getComplaint()
    }
}