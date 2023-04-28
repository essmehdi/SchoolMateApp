package com.github.essmehdi.schoolmate.auth.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.github.essmehdi.schoolmate.auth.api.dto.RegisterDto
import com.github.essmehdi.schoolmate.users.models.User
import com.github.essmehdi.schoolmate.shared.api.Api
import com.github.essmehdi.schoolmate.shared.api.BaseResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RegisterViewModel: ViewModel() {
  val registerStatus: MutableLiveData<BaseResponse<User>> = MutableLiveData()

  fun register(firstName: String, lastName: String, email: String, password: String, confirmation: String) {
    registerStatus.value = BaseResponse.Loading()
    val registerDto = RegisterDto(firstName, lastName, email, password, confirmation)
    Api.authService.register(registerDto).enqueue(object: Callback<User> {
      override fun onResponse(call: Call<User>, response: Response<User>) {
        if (response.isSuccessful) {
          registerStatus.value = BaseResponse.Success(response.body()!!)
        } else {
          registerStatus.value = BaseResponse.Error(response.code())
        }
      }

      override fun onFailure(call: Call<User>, t: Throwable) {
        registerStatus.value = BaseResponse.Error(0)
      }

    })
  }
}