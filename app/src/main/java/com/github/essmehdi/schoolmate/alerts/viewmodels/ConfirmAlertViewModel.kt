package com.github.essmehdi.schoolmate.alerts.viewmodels

import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.github.essmehdi.schoolmate.alerts.models.Alert
import com.github.essmehdi.schoolmate.shared.api.Api
import com.github.essmehdi.schoolmate.shared.api.BaseResponse
import com.github.essmehdi.schoolmate.shared.api.dto.PaginatedResponse
import com.github.essmehdi.schoolmate.users.models.User
import retrofit2.Callback
import retrofit2.Response

class ConfirmAlertViewModel : ViewModel(){
    val alerts: MutableLiveData<List<Alert>> = MutableLiveData()
    val showEmpty: MediatorLiveData<Boolean> = MediatorLiveData()
    val id: MutableLiveData<Long> = MutableLiveData()
    val currentPageStatus: MutableLiveData<BaseResponse<PaginatedResponse<Alert>>> =
        MutableLiveData()
    val currentPage: MutableLiveData<PaginatedResponse<Alert>?> = MutableLiveData()
    val user: MutableLiveData<BaseResponse<User>> = MutableLiveData()

    init {
        trackEmpty()
    }
    //fetching all the alerts that the field status is pending
    fun loadPendingAlerts(){
        currentPageStatus.value = BaseResponse.Loading()
        if (currentPage.value?.last == true) {
            currentPageStatus.value = BaseResponse.Success(currentPage.value!!)
            return
        }
        Api.alertService.getPendingAlerts(currentPage.value?.page?.plus(1) ?: 0).enqueue(object :
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

    //method that will be called when the user click on the confirm button
    fun confirmAlert(id: Long){
        Api.alertService.confirmUserAlert(id).enqueue(object : Callback<Alert>{
            override fun onResponse(call: retrofit2.Call<Alert>, response: Response<Alert>) {
                if (response.isSuccessful){
                    loadPendingAlerts()
                }
                else
                    currentPageStatus.value = BaseResponse.Error(response.code())
            }

            override fun onFailure(call: retrofit2.Call<Alert>, t: Throwable) {
                currentPageStatus.value = BaseResponse.Error(0)
            }

        })
    }
    //method that will be called when the user click on the reject button
    fun rejectAlert(id: Long){
        Api.alertService.cancelUserAlert(id).enqueue(object : Callback<Alert>{
            override fun onResponse(call: retrofit2.Call<Alert>, response: Response<Alert>) {
                if (response.isSuccessful){
                    loadPendingAlerts()
                }
                else
                    currentPageStatus.value = BaseResponse.Error(response.code())
            }

            override fun onFailure(call: retrofit2.Call<Alert>, t: Throwable) {
                currentPageStatus.value = BaseResponse.Error(0)
            }

        })
    }
    private fun trackEmpty() {
        if (showEmpty.hasActiveObservers()) return

        showEmpty.addSource(alerts) {
            showEmpty.value = it.isEmpty() == true && currentPageStatus.value is BaseResponse.Success

        }
        showEmpty.addSource(currentPageStatus) {
            showEmpty.value = alerts.value?.isEmpty() == true && it is BaseResponse.Success
        }
    }

    fun refresh() {
        currentPage.value = null
        alerts.value = listOf()
        loadPendingAlerts()
    }

}