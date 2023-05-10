package com.github.essmehdi.schoolmate.complaints.viewmodels

import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.essmehdi.schoolmate.auth.models.User
import com.github.essmehdi.schoolmate.complaints.api.dto.EditComplaintStatusAndHandlerDto
import com.github.essmehdi.schoolmate.complaints.enumerations.ComplaintStatus
import com.github.essmehdi.schoolmate.complaints.models.Complaint
import com.github.essmehdi.schoolmate.complaints.models.Handler
import com.github.essmehdi.schoolmate.shared.api.Api
import com.github.essmehdi.schoolmate.shared.api.BaseResponse
import com.github.essmehdi.schoolmate.shared.api.dto.PaginatedResponse
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class HandlerComplaintsViewModel : ViewModel() {

    // Parameters
    val sortField: MutableLiveData<String> = MutableLiveData("date")
    val sortOrder: MutableLiveData<String> = MutableLiveData("desc")
    val handler: MutableLiveData<String?> = MutableLiveData()
    val complaintStatus: MutableLiveData<ComplaintStatus?> = MutableLiveData()
    val complaintType: MutableLiveData<String?> = MutableLiveData("all")

    // Complaints Data
    val currentHandler: MutableLiveData<Handler> = MutableLiveData()
    val complaints: MutableLiveData<List<Complaint>> = MutableLiveData()

    // Handlers Data (Map of handler -> complaints count)
    val handlersList: MutableLiveData<List<Handler>> = MutableLiveData()
    val currentHandlersPageStatus: MutableLiveData<BaseResponse<PaginatedResponse<User>>> = MutableLiveData()
    val currentHandlersPage: MutableLiveData<PaginatedResponse<User>?> = MutableLiveData()

    // Pagination
    val currentPageStatus: MutableLiveData<BaseResponse<PaginatedResponse<Complaint>>> = MutableLiveData()
    val currentPage: MutableLiveData<PaginatedResponse<Complaint>?> = MutableLiveData()

    // Empty state
    val showEmpty: MediatorLiveData<Boolean> = MediatorLiveData()

    // edit complaint
    val editStatus: MutableLiveData<BaseResponse<Complaint>> = MutableLiveData()

    init {
        trackEmpty()
    }

    fun fetchComplaints() {
        currentPageStatus.value = BaseResponse.Loading()
        if (currentPage.value?.last == true) {
            currentPageStatus.value = BaseResponse.Success(currentPage.value!!)
            return
        }
        viewModelScope.launch {
            Api
                .complaintService
                .getComplaintsByStatusAndHandler(
                    page = currentPage.value?.page?.plus(1) ?: 0,
                    sort = "${sortField.value},${sortOrder.value}",
                    status = complaintStatus.value,
                    type = complaintType.value,
                    handler = if (handler.value == null) null else handler.value
                ).enqueue(object : Callback<PaginatedResponse<Complaint>> {
                    override fun onResponse(
                        call: Call<PaginatedResponse<Complaint>>,
                        response: Response<PaginatedResponse<Complaint>>
                    ) {
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

    fun fetchCurrentHandler(){
        viewModelScope.launch {
            Api
                .authService
                .me()
                .enqueue(object : Callback<User> {
                    override fun onResponse(
                        call: Call<User>,
                        response: Response<User>
                    ) {
                        if (response.isSuccessful) {
                            currentHandler.value = Handler(response.body()!!, 0)
                            // get handled complaints
                            Api.complaintService.getComplaintsCountByHandler(response.body()!!.id)
                                .enqueue(object : Callback<Int> {
                                    override fun onResponse(
                                        call: Call<Int>,
                                        response: Response<Int>
                                    ) {
                                        if (response.isSuccessful) {
                                            currentHandler.value!!.complaintsCount = response.body()!!
                                        }
                                    }
                                    override fun onFailure(call: Call<Int>, t: Throwable) {
                                    }
                                })
                        }
                    }
                    override fun onFailure(call: Call<User>, t: Throwable) {
                    }
                })
        }
    }

    fun fetchHandlersAndComplaints() {

        viewModelScope.launch {
            Api
                .usersService
                .getUsers(
                    page = currentHandlersPage.value?.page?.plus(1) ?: 0,
                    sort = "lastName,desc",
                    role = "ADEI"
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
                                                handlersList.value = (handlersList.value ?: listOf()).plus(
                                                    Handler(
                                                        handler,
                                                        response.body()!!
                                                    )
                                                )
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
                        currentPageStatus.value = BaseResponse.Error(0)
                    }
                }
                )
        }
    }

    fun editComplaintStautsAndHandler(id: Long, editComplaintStatusAndHandlerDto: EditComplaintStatusAndHandlerDto){
        editStatus.value = BaseResponse.Loading()
        Api.complaintService.updateComplaintStatusAndHandler(id, editComplaintStatusAndHandlerDto).enqueue(object: Callback<Complaint> {
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
        currentPage.value = null
        complaints.value = listOf()
        fetchComplaints()
    }

    private fun trackEmpty() {
        // Check if showEmpty has already been tracked
        if (showEmpty.hasActiveObservers()) return

        showEmpty.addSource(complaints) {
            showEmpty.value = it.isEmpty() && currentPageStatus.value is BaseResponse.Success
        }
        showEmpty.addSource(currentPageStatus) {
            showEmpty.value = complaints.value?.isEmpty() == true && it is BaseResponse.Success
        }
    }

    fun changeOrder(order: String) {
        sortOrder.value = order
        refresh()
    }

    fun changeStatus(status: ComplaintStatus? = null) {
        // if the status is pending, we should change the handler to null (all) before changing the status
        // because pending complaints are not assigned to a handler
        if (status == ComplaintStatus.PENDING) {
            handler.value = null
        }
        complaintStatus.value = status
        refresh()
    }

    fun changeType(type: String) {
        complaintType.value = type
        refresh()
    }

    fun changeHandler(handlerId: Long? = null) {
        // If the status is pending, we should change it to null (all) before changing the handler
        // because pending complaints are not assigned to a handler
        if (complaintStatus.value == ComplaintStatus.PENDING && handlerId != null) {
            this.complaintStatus.value = null
        }
        if(handlerId == null) {
            this.handler.value = null
        } else if(handlerId == 0L) {
            this.handler.value = "me"
        } else {
            this.handler.value = handlerId.toString()
        }
        refresh()
    }

    fun refreshHandlers() {
        currentHandlersPage.value = null
        handlersList.value = listOf()
        fetchHandlersAndComplaints()
    }


}