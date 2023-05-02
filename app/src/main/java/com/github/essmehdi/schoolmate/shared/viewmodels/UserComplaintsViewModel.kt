package com.github.essmehdi.schoolmate.shared.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.essmehdi.schoolmate.complaints.models.Complaint
import com.github.essmehdi.schoolmate.documents.models.Document
import com.github.essmehdi.schoolmate.shared.api.Api
import com.github.essmehdi.schoolmate.shared.api.BaseResponse
import com.github.essmehdi.schoolmate.shared.api.dto.MessageResponse
import com.github.essmehdi.schoolmate.shared.api.dto.PaginatedResponse
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

open class UserComplaintsViewModel : ViewModel() {
    val sortField: MutableLiveData<String> = MutableLiveData("date")
    val sortOrder: MutableLiveData<String> = MutableLiveData("desc")
    val complaints: MutableLiveData<List<Complaint>> = MutableLiveData()
    val currentPageStatus: MutableLiveData<BaseResponse<PaginatedResponse<Complaint>>> = MutableLiveData()
    val currentPage: MutableLiveData<PaginatedResponse<Complaint>?> = MutableLiveData()
    var userComplaints : MutableLiveData<BaseResponse<List<Complaint>>> = MutableLiveData()

    fun fetchUserComplaints() {
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
                    type="all",
                    user="me")
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
                    userComplaints.value = BaseResponse.Error(0)
                }
            })
        }
    }
}