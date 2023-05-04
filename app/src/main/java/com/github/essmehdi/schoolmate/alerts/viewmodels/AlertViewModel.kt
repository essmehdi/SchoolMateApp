package com.github.essmehdi.schoolmate.alerts.viewmodels

import android.telecom.Call
import android.util.Log
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.essmehdi.schoolmate.alerts.models.Alert
import com.github.essmehdi.schoolmate.shared.api.Api
import com.github.essmehdi.schoolmate.shared.api.BaseResponse
import com.github.essmehdi.schoolmate.shared.api.dto.MessageResponse
import com.github.essmehdi.schoolmate.shared.api.dto.PaginatedResponse
import com.github.essmehdi.schoolmate.users.models.User
import kotlinx.coroutines.launch
import retrofit2.Response
import retrofit2.Callback


class AlertViewModel : ViewModel() {
    private val sortOrder: MutableLiveData<String> = MutableLiveData("desc")
    val alerts: MutableLiveData<List<Alert>> = MutableLiveData()
    val showEmpty: MediatorLiveData<Boolean> = MediatorLiveData()
    val id: MutableLiveData<Long> = MutableLiveData()
    val currentPageStatus: MutableLiveData<BaseResponse<PaginatedResponse<Alert>>> =
        MutableLiveData()
    val currentPage: MutableLiveData<PaginatedResponse<Alert>?> = MutableLiveData()
    val deleteStatus: MutableLiveData<BaseResponse<MessageResponse>?> = MutableLiveData(null)
    val user: MutableLiveData<BaseResponse<User>> = MutableLiveData()


    init {
        trackEmpty()
    }

    fun fetchUser() {
        user.value = BaseResponse.Loading()
        Api.authService.me().enqueue(object : retrofit2.Callback<User> {
            override fun onResponse(call: retrofit2.Call<User>, response: Response<User>) {
                if (response.isSuccessful) {
                    user.value = BaseResponse.Success(response.body()!!)
                } else {
                    user.value = BaseResponse.Error(response.code())
                }
            }

            override fun onFailure(call: retrofit2.Call<User>, t: Throwable) {
                user.value = BaseResponse.Error(0)
            }

        })
    }


    fun loadAlerts(id: Long) {
        id.let { this.id.value = it }
        currentPageStatus.value = BaseResponse.Loading()
        if (currentPage.value?.last == true) {
            currentPageStatus.value = BaseResponse.Success(currentPage.value!!)
            return
        }
        Api.alertService.getAllUserAlerts(currentPage.value?.page?.plus(1) ?: 0).enqueue(object :
            Callback<PaginatedResponse<Alert>> {
            override fun onResponse(
                call: retrofit2.Call<PaginatedResponse<Alert>>,
                response: Response<PaginatedResponse<Alert>>
            ) {
                if (response.isSuccessful) {
                    currentPageStatus.value = BaseResponse.Success(response.body()!!)
                    currentPage.value = response.body()!!
                    alerts.value =
                        (alerts.value ?: listOf()).plus(response.body()!!.results)
                } else {
                    currentPageStatus.value = BaseResponse.Error(response.code())
                }
            }

            override fun onFailure(call: retrofit2.Call<PaginatedResponse<Alert>>, t: Throwable) {
                currentPageStatus.value = BaseResponse.Error(0)
            }

        })
    }

    fun deleteAlert(alert: Alert) {
        deleteStatus.value = BaseResponse.Loading()
        Api.alertService.deleteUserAlert(alert.id)
            .enqueue(object : retrofit2.Callback<MessageResponse> {
                override fun onResponse(
                    call: retrofit2.Call<MessageResponse>,
                    response: Response<MessageResponse>
                ) {
                    if (response.isSuccessful) {
                        deleteStatus.value = BaseResponse.Success(response.body()!!)
                    } else {
                        deleteStatus.value = BaseResponse.Error(response.code())
                    }
                }
                override fun onFailure(call: retrofit2.Call<MessageResponse>, t: Throwable) {
                    deleteStatus.value = BaseResponse.Error(0)
                }

            })
    }

    private fun trackEmpty() {
        if (showEmpty.hasActiveObservers()) return

        showEmpty.addSource(alerts) {
            showEmpty.value = it.isEmpty() && currentPageStatus.value is BaseResponse.Success
        }
        showEmpty.addSource(currentPageStatus) {
            showEmpty.value = alerts.value?.isEmpty() == true && it is BaseResponse.Success
        }
    }
}