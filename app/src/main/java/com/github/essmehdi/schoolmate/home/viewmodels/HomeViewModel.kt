package com.github.essmehdi.schoolmate.home.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.essmehdi.schoolmate.auth.models.User
import com.github.essmehdi.schoolmate.shared.api.Api
import com.github.essmehdi.schoolmate.shared.api.BaseResponse
import com.github.essmehdi.schoolmate.shared.viewmodels.UserComplaintsViewModel
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class HomeViewModel: UserComplaintsViewModel() {
  var user: MutableLiveData<BaseResponse<User>> = MutableLiveData()

  fun fetchUserData() {
    user.value = BaseResponse.Loading()
    viewModelScope.launch {
      Api.authService.me().enqueue(object: Callback<User> {
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
}