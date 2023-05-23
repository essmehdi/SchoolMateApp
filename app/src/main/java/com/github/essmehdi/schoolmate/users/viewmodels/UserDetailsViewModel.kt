package com.github.essmehdi.schoolmate.users.viewmodels

import android.util.Log
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.github.essmehdi.schoolmate.shared.api.Api
import com.github.essmehdi.schoolmate.shared.api.BaseResponse
import com.github.essmehdi.schoolmate.users.api.dto.ChangePrivilegeDto
import com.github.essmehdi.schoolmate.users.models.User
import com.github.essmehdi.schoolmate.users.models.UserRole
import retrofit2.Call
import retrofit2.Response

class UserDetailsViewModel: ViewModel() {
  val user: MutableLiveData<BaseResponse<User>> = MutableLiveData()
  val me: MutableLiveData<BaseResponse<User>> = MutableLiveData()
  val showPrivilegeMenu: MediatorLiveData<Boolean> = MediatorLiveData(false)

  init {
    fetchMe()
    trackShowPrivilegeMenu()
  }

  private fun fetchMe() {
    me.value = BaseResponse.Loading()
    Api.authService.me().enqueue(object: retrofit2.Callback<User> {
      override fun onResponse(call: Call<User>, response: Response<User>) {
        if (response.isSuccessful) {
          me.value = BaseResponse.Success(response.body()!!)
        } else {
          me.value = BaseResponse.Error(response.code())
        }
      }

      override fun onFailure(call: Call<User>, t: Throwable) {
        me.value = BaseResponse.Error(0)
      }
    })
  }

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

  fun changeUserStatus(role: UserRole) {
    val id = user.value!!.data!!.id
    user.value = BaseResponse.Loading()
    Api.usersService.changeUserPrivilege(id, ChangePrivilegeDto(role)).enqueue(object: retrofit2.Callback<User> {
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

  private fun trackShowPrivilegeMenu() {
    showPrivilegeMenu.addSource(me) {
      if (it is BaseResponse.Success) Log.d("UserDetailsViewModel", "me: ${it.data!!}")
      showPrivilegeMenu.value =
        it is BaseResponse.Success && user.value is BaseResponse.Success && it.data!!.role == UserRole.MODERATOR && it.data.id != user.value!!.data!!.id
    }
    showPrivilegeMenu.addSource(user) {
      if (it is BaseResponse.Success) Log.d("UserDetailsViewModel", "user: ${it.data!!}")
      showPrivilegeMenu.value =
        it is BaseResponse.Success && me.value is BaseResponse.Success && me.value!!.data!!.role == UserRole.MODERATOR && it.data!!.id != me.value!!.data!!.id
    }
  }
}