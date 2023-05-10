package com.github.essmehdi.schoolmate.alerts.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.github.essmehdi.schoolmate.shared.api.Api
import com.github.essmehdi.schoolmate.shared.api.BaseResponse
import com.github.essmehdi.schoolmate.users.models.User
import retrofit2.Response

class AlertsViewModel : ViewModel(){

    val user: MutableLiveData<BaseResponse<User>> = MutableLiveData()


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
}