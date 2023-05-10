package com.github.essmehdi.schoolmate.complaints.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.github.essmehdi.schoolmate.complaints.models.Complaint
import com.github.essmehdi.schoolmate.shared.api.Api
import com.github.essmehdi.schoolmate.shared.api.BaseResponse
import com.github.essmehdi.schoolmate.shared.api.dto.MessageResponse
import com.github.essmehdi.schoolmate.shared.api.dto.PaginatedResponse
import com.github.essmehdi.schoolmate.shared.viewmodels.UserComplaintsViewModel
import com.github.essmehdi.schoolmate.users.models.User
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ComplaintsViewModel : UserComplaintsViewModel() {

    val complainant: MutableLiveData<User> = MutableLiveData<User>()
    val deleteStatus: MutableLiveData<BaseResponse<MessageResponse>?> = MutableLiveData(null)

    fun fetchComplaints(user: String = "all") {
        currentPageStatus.value = BaseResponse.Loading()
        if (currentPage.value?.last == true) {
            currentPageStatus.value = BaseResponse.Success(currentPage.value!!)
            return
        }
        viewModelScope.launch {
            Api
                .complaintService
                .getComplaints(
                    page=currentPage.value?.page?.plus(1) ?: 0,
                    sort = "${sortField.value},${sortOrder.value}",
                    type=complaintType.value!!,
                    user=user)
                .enqueue(object: Callback<PaginatedResponse<Complaint>> {
                    override fun onResponse(call: Call<PaginatedResponse<Complaint>>, response: Response<PaginatedResponse<Complaint>>) {
                        if (response.isSuccessful) {
                            currentPageStatus.value = BaseResponse.Success(response.body()!!)
                            currentPage.value = response.body()!!
                            complaints.value = (complaints.value ?: listOf()).plus(response.body()!!.results)
                        } else {
                            currentPageStatus.value = BaseResponse.Error(response.code())
                        }
                    }
                    override fun onFailure(call: Call<PaginatedResponse<Complaint>>, t: Throwable) {
                        currentPageStatus.value = BaseResponse.Error(0)
                    }
                })
        }
    }

    fun deleteComplaint(id: Long) {
        deleteStatus.value = BaseResponse.Loading()
        Api.complaintService.deleteComplaint(id).enqueue(object: Callback<MessageResponse> {
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

    fun fetchComplainant(){
        Api.authService.me().enqueue(object: Callback<User> {
            override fun onResponse(call: Call<User>, response: Response<User>) {
                if (response.isSuccessful) {
                    complainant.value = response.body()
                }
            }
            override fun onFailure(call: Call<User>, t: Throwable) {
            }
        })

    }

    fun changeComplaintType(type: String) {
        complaintType.value = type
        refresh()
    }

    fun refresh() {
        complaints.value = listOf()
        userComplaints.value = listOf()
        currentPage.value = null
        fetchComplaints()
        fetchUserComplaints("me")
    }

    fun changeOrder(order: String) {
        sortOrder.value = order
        refresh()
    }

}