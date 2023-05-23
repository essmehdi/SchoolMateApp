package com.github.essmehdi.schoolmate.users.viewmodels

import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.essmehdi.schoolmate.shared.api.Api
import com.github.essmehdi.schoolmate.shared.api.BaseResponse
import com.github.essmehdi.schoolmate.shared.api.dto.PaginatedResponse
import com.github.essmehdi.schoolmate.users.models.User
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class UsersViewModel: ViewModel() {
  private val sortField: MutableLiveData<String> = MutableLiveData("lastName")
  private val sortOrder: MutableLiveData<String> = MutableLiveData("desc")
  val users: MutableLiveData<List<User>> = MutableLiveData()
  val currentPageStatus: MutableLiveData<BaseResponse<PaginatedResponse<User>>> = MutableLiveData()
  val currentPage: MutableLiveData<PaginatedResponse<User>?> = MutableLiveData()

  val showEmpty: MediatorLiveData<Boolean> = MediatorLiveData()

  init {
    trackEmpty()
  }

  fun loadUsers() {
    currentPageStatus.value = BaseResponse.Loading()
    if (currentPage.value?.last == true) {
      currentPageStatus.value = BaseResponse.Success(currentPage.value!!)
      return
    }
    viewModelScope.launch {
     Api.usersService.getAllUsers(
        currentPage.value?.page?.plus(1) ?: 0,
        sort = "${sortField.value},${sortOrder.value}",
      ).enqueue(object: Callback<PaginatedResponse<User>> {
          override fun onResponse(
            call: Call<PaginatedResponse<User>>,
            response: Response<PaginatedResponse<User>>
          ) {
            if (response.isSuccessful) {
              currentPageStatus.value = BaseResponse.Success(response.body()!!)
              currentPage.value = response.body()!!
              users.value = (users.value ?: listOf()).plus(response.body()!!.results)
            } else {
              currentPageStatus.value = BaseResponse.Error(response.code())
            }
          }

          override fun onFailure(call: Call<PaginatedResponse<User>>, t: Throwable) {
            currentPageStatus.value = BaseResponse.Error(0)
          }
        })
    }
  }

  fun refresh() {
    users.value = listOf()
    currentPage.value = null
    loadUsers()
  }

  fun changeSortField(field: String) {
    sortField.value = field
    refresh()
  }

  fun changeOrder(order: String) {
    sortOrder.value = order
    refresh()
  }

  private fun trackEmpty() {
    // Check if showEmpty has already been tracked
    if (showEmpty.hasActiveObservers()) return

    showEmpty.addSource(users) {
      showEmpty.value = it.isEmpty() && currentPageStatus.value is BaseResponse.Success
    }
    showEmpty.addSource(currentPageStatus) {
      showEmpty.value = users.value?.isEmpty() == true && it is BaseResponse.Success
    }
  }
}