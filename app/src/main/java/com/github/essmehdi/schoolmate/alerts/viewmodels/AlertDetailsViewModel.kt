package com.github.essmehdi.schoolmate.alerts.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.essmehdi.schoolmate.alerts.models.Alert
import com.github.essmehdi.schoolmate.shared.api.Api
import com.github.essmehdi.schoolmate.shared.api.BaseResponse
import kotlinx.coroutines.launch

class AlertDetailsViewModel : ViewModel(){

    val alert: MutableLiveData<BaseResponse<Alert>> = MutableLiveData()
    val id: MutableLiveData<Long> = MutableLiveData()

    fun loadAlert(){
        alert.value = BaseResponse.Loading()
        viewModelScope.launch {
            Api.alertService.getAlertById(id.value!!).enqueue(object: retrofit2.Callback<Alert>{
                override fun onResponse(call: retrofit2.Call<Alert>, response: retrofit2.Response<Alert>) {
                    if (response.isSuccessful) {
                        alert.value = BaseResponse.Success(response.body()!!)
                    } else {
                        alert.value = BaseResponse.Error(response.code())
                    }
                }

                override fun onFailure(call: retrofit2.Call<Alert>, t: Throwable) {
                    alert.value = BaseResponse.Error(0)
                }
            })
        }
    }

}