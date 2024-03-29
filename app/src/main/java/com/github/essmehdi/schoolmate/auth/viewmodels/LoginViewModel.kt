package com.github.essmehdi.schoolmate.auth.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.essmehdi.schoolmate.auth.api.dto.LoginDto
import com.github.essmehdi.schoolmate.auth.api.dto.LoginResponse
import com.github.essmehdi.schoolmate.users.models.User
import com.github.essmehdi.schoolmate.shared.api.Api
import com.github.essmehdi.schoolmate.shared.api.BaseResponse
import com.onesignal.OneSignal
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class LoginViewModel: ViewModel() {
  val authed: MutableLiveData<BaseResponse<Boolean>> = MutableLiveData()
  val result: MutableLiveData<BaseResponse<LoginResponse>> = MutableLiveData()

  fun checkAuth() {
    authed.value = BaseResponse.Loading()
    viewModelScope.launch {
      Api.authService.me().enqueue(object: Callback<User> {
        override fun onResponse(call: Call<User>, response: Response<User>) {
          authed.value = BaseResponse.Success(response.isSuccessful)
          if (!response.isSuccessful) {
            OneSignal.removeExternalUserId()
            OneSignal.deleteTag("user_role")
          } else {
            OneSignal.setExternalUserId(response.body()!!.email)
            OneSignal.sendTag("user_role", response.body()!!.role.name)
          }
        }

        override fun onFailure(call: Call<User>, t: Throwable) {
          authed.value = BaseResponse.Error(0)
        }

      })
    }
  }

  fun login(loginDto: LoginDto) {
    result.value = BaseResponse.Loading()
    viewModelScope.launch {
      Api.authService.login(loginDto).enqueue(object: Callback<LoginResponse> {
        override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
          if (response.isSuccessful) {
            result.value = BaseResponse.Success(response.body()!!)
          } else {
            result.value = BaseResponse.Error(response.code())
          }
        }

        override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
          result.value = BaseResponse.Error(0)
        }
      })
    }
  }
}