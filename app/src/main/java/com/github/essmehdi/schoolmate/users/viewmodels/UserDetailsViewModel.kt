package com.github.essmehdi.schoolmate.users.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.github.essmehdi.schoolmate.shared.api.Api
import com.github.essmehdi.schoolmate.shared.api.BaseResponse
import com.github.essmehdi.schoolmate.users.models.User
import retrofit2.Call
import retrofit2.Response

class UserDetailsViewModel: ViewModel() {
  val user: MutableLiveData<BaseResponse<User>> = MutableLiveData()

  fun fetchUser(id: Long) {
    user.value = BaseResponse.Loading()
    Api.usersService.getUserById(id).enqueue(object: retrofit2.Callback<User> {
      override fun onResponse(call: Call<User>, response: Response<User>) {
        if (response.isSuccessful) {
          user.value = BaseResponse.Success(response.body()!!)
        } else {
          user.value = BaseResponse.Error(response.code())
        }
      }

      override fun onFailure(call: Call<User>, t: Throwable) {
        user.value = BaseResponse.Error(0)
      }
    })
  }
}