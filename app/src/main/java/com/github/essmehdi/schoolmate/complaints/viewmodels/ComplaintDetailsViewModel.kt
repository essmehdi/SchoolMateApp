package com.github.essmehdi.schoolmate.complaints.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.essmehdi.schoolmate.users.models.User
import com.github.essmehdi.schoolmate.complaints.api.dto.EditComplaintStatusAndHandlerDto
import com.github.essmehdi.schoolmate.complaints.models.Complaint
import com.github.essmehdi.schoolmate.complaints.models.Handler
import com.github.essmehdi.schoolmate.shared.api.Api
import com.github.essmehdi.schoolmate.shared.api.BaseResponse
import com.github.essmehdi.schoolmate.shared.api.dto.MessageResponse
import com.github.essmehdi.schoolmate.shared.api.dto.PaginatedResponse
import com.github.essmehdi.schoolmate.users.models.UserRole
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ComplaintDetailsViewModel : ViewModel() {

    // For all users
    val id: MutableLiveData<Long> = MutableLiveData<Long>()
    val connectedUser: MutableLiveData<User> = MutableLiveData<User>() // The user that is connected to the app
    val complaint: MutableLiveData<BaseResponse<Complaint>> = MutableLiveData<BaseResponse<Complaint>>()
    val deleteStatus: MutableLiveData<BaseResponse<MessageResponse>?> = MutableLiveData(null)

    // For the handlers - this is the edit status of the complaint when they try to change its status/assignee
    val editStatus: MutableLiveData<BaseResponse<Complaint>> = MutableLiveData(null)

    // For the handlers
    val currentHandlerComplaintsCount: MutableLiveData<Long> = MutableLiveData()
    val handlersList: MutableLiveData<List<Handler>> = MutableLiveData() // Doesn't contain the current handler
    val currentHandlersPageStatus: MutableLiveData<BaseResponse<PaginatedResponse<User>>> = MutableLiveData()
    val currentHandlersPage: MutableLiveData<PaginatedResponse<User>?> = MutableLiveData()


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
                    connectedUser.value = response.body()
                }
            }
            override fun onFailure(call: Call<User>, t: Throwable) {
            }
        })
    }

    fun fetchHandlersAndComplaints() {

        viewModelScope.launch {
            Api
                .usersService
                .getAllUsers(
                    page = currentHandlersPage.value?.page?.plus(1) ?: 0,
                    sort = "lastName,desc",
                    role = UserRole.ADEI
                ).enqueue(object : Callback<PaginatedResponse<User>> {
                    override fun onResponse(
                        call: Call<PaginatedResponse<User>>,
                        response: Response<PaginatedResponse<User>>
                    ) {
                        if (response.isSuccessful) {
                            currentHandlersPageStatus.value = BaseResponse.Success(response.body()!!)
                            // As we have the list of handlers, we can fetch the complaints count for each one
                            val handlers = response.body()!!.results
                            handlers.forEach { handler ->
                                Api
                                    .complaintService
                                    .getComplaintsCountByHandler(handler.id)
                                    .enqueue(object : Callback<Int> {
                                        override fun onResponse(
                                            call: Call<Int>,
                                            response: Response<Int>
                                        ) {
                                            if (response.isSuccessful) {
                                                if(handler.id != connectedUser.value!!.id){
                                                    handlersList.value = (handlersList.value ?: listOf()).plus(
                                                        Handler(
                                                            handler,
                                                            response.body()!!
                                                        )
                                                    )
                                                }
                                            }
                                        }
                                        override fun onFailure(call: Call<Int>, t: Throwable) {
                                        }
                                    })
                            }
                        } else {
                            currentHandlersPageStatus.value = BaseResponse.Error(response.code())
                        }
                    }
                    override fun onFailure(call: Call<PaginatedResponse<User>>, t: Throwable) {
                        currentHandlersPageStatus.value = BaseResponse.Error(0)
                    }
                }
                )
        }
    }

    fun fetchCurrentHandlerComplaintsCount(){
        viewModelScope.launch {
            // get handled complaints
            Api.complaintService.getComplaintsCountByHandler(connectedUser.value!!.id)
                .enqueue(object : Callback<Int> {
                    override fun onResponse(
                        call: Call<Int>,
                        response: Response<Int>
                    ) {
                        if (response.isSuccessful) {
                            currentHandlerComplaintsCount.value = response.body()!!.toLong()
                        }
                    }
                    override fun onFailure(call: Call<Int>, t: Throwable) {
                    }
                })
        }
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

    fun refreshHandlers() {
        currentHandlersPage.value = null
        handlersList.value = listOf()
        fetchHandlersAndComplaints()
    }

    fun refresh() {
        complaint.value = BaseResponse.Loading()
        getComplaint()
    }
}