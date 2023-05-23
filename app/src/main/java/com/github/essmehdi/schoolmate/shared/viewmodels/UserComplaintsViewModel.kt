package com.github.essmehdi.schoolmate.shared.viewmodels

import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.essmehdi.schoolmate.complaints.models.Complaint
import com.github.essmehdi.schoolmate.shared.api.Api
import com.github.essmehdi.schoolmate.shared.api.BaseResponse
import com.github.essmehdi.schoolmate.shared.api.dto.PaginatedResponse
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

open class UserComplaintsViewModel : ViewModel() {

    val sortField: MutableLiveData<String> = MutableLiveData("date")
    val sortOrder: MutableLiveData<String> = MutableLiveData("desc")
    val complaintType: MutableLiveData<String> = MutableLiveData("all")

    val complaints: MutableLiveData<List<Complaint>> = MutableLiveData()
    val userComplaints: MutableLiveData<List<Complaint>> = MutableLiveData()
    val currentPageStatus: MutableLiveData<BaseResponse<PaginatedResponse<Complaint>>> = MutableLiveData()
    val currentPage: MutableLiveData<PaginatedResponse<Complaint>?> = MutableLiveData()

    val showEmpty: MediatorLiveData<Boolean> = MediatorLiveData()

    init {
        trackEmpty()
    }

    fun fetchUserComplaints(user: String) {
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
                        userComplaints.value = (userComplaints.value ?: listOf()).plus(response.body()!!.results)
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
}