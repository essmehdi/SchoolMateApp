package com.github.essmehdi.schoolmate.users.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.github.essmehdi.schoolmate.shared.api.Api
import com.github.essmehdi.schoolmate.shared.api.BaseResponse
import com.github.essmehdi.schoolmate.users.models.User
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class UsersViewModel: ViewModel() {
  val usersFetchStatus: MutableLiveData<BaseResponse<List<User>>> = MutableLiveData()

  fun fetchUsers() {
    usersFetchStatus.value = BaseResponse.Loading()
    Api.usersService.getAllUsers().enqueue(object: Callback<List<User>> {
      override fun onResponse(call: Call<List<User>>, response: Response<List<User>>) {
        if (response.isSuccessful) {
          usersFetchStatus.value = BaseResponse.Success(response.body()!!)
        } else {
          usersFetchStatus.value = BaseResponse.Error(response.code())
        }
      }

      override fun onFailure(call: Call<List<User>>, t: Throwable) {
        usersFetchStatus.value = BaseResponse.Error(0)
      }
    })
  }
}