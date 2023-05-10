package com.github.essmehdi.schoolmate.home.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.essmehdi.schoolmate.users.models.User
import com.github.essmehdi.schoolmate.shared.api.Api
import com.github.essmehdi.schoolmate.shared.api.BaseResponse
import com.github.essmehdi.schoolmate.shared.viewmodels.UserComplaintsViewModel
import com.github.essmehdi.schoolmate.shared.api.dto.MessageResponse
import com.github.essmehdi.schoolmate.users.api.dto.ChangePasswordDto
import com.github.essmehdi.schoolmate.users.api.dto.EditUserDto
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class HomeViewModel: UserComplaintsViewModel() {
  val user: MutableLiveData<BaseResponse<User>> = MutableLiveData()
  val changePasswordStatus: MutableLiveData<BaseResponse<MessageResponse>> = MutableLiveData()

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

  fun editUserData(editUserDto: EditUserDto) {
    val id = user.value!!.data!!.id
    user.value = BaseResponse.Loading()
    Api.usersService.editProfileData(id, editUserDto).enqueue(object: Callback<User> {
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

  fun changePassword(changePassword: ChangePasswordDto) {
    changePasswordStatus.value = BaseResponse.Loading()
    Api.usersService.changePassword(changePassword).enqueue(object: Callback<MessageResponse> {
      override fun onResponse(call: Call<MessageResponse>, response: Response<MessageResponse>) {
        if (response.isSuccessful) {
          changePasswordStatus.value = BaseResponse.Success(response.body()!!)
        } else {
          changePasswordStatus.value = BaseResponse.Error(response.code())
        }
      }

      override fun onFailure(call: Call<MessageResponse>, t: Throwable) {
        changePasswordStatus.value = BaseResponse.Error(0)
      }
    })
  }
}