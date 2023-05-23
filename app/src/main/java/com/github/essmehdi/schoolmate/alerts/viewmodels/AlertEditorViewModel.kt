package com.github.essmehdi.schoolmate.alerts.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.github.essmehdi.schoolmate.alerts.api.dto.AlertDto
import com.github.essmehdi.schoolmate.alerts.api.dto.EditAlertDto
import com.github.essmehdi.schoolmate.alerts.models.Alert
import com.github.essmehdi.schoolmate.documents.models.Document
import com.github.essmehdi.schoolmate.schoolnavigation.models.Point
import com.github.essmehdi.schoolmate.shared.api.Api
import com.github.essmehdi.schoolmate.shared.api.BaseResponse
import com.github.essmehdi.schoolmate.users.models.User
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AlertEditorViewModel: ViewModel() {

    val uploadStatus: MutableLiveData<BaseResponse<Alert>> = MutableLiveData()
    val selectedLocation: MutableLiveData<Point?> = MutableLiveData(null)
    val editMode: MutableLiveData<Boolean> = MutableLiveData(false)
    val editId: MutableLiveData<Long?> = MutableLiveData(null)
    val user: MutableLiveData<BaseResponse<User>> = MutableLiveData()

    private val requestCallback = object : Callback<Alert> {
        override fun onResponse(call: Call<Alert>, response: Response<Alert>) {
            if (response.isSuccessful) {
                uploadStatus.value = BaseResponse.Success(response.body()!!)
            } else {
                uploadStatus.value = BaseResponse.Error(response.code())
            }
        }

        override fun onFailure(call: Call<Alert>, t: Throwable) {
            uploadStatus.value = BaseResponse.Error(0)
        }
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

    fun addAlert(alertDto: AlertDto){

        Api.alertService.addUserAlert(alertDto).enqueue(requestCallback)
    }
    fun editAlert(editAlertDto:EditAlertDto){
        Api.alertService.editUserAlert(editId.value!!,editAlertDto).enqueue(requestCallback)
    }
}