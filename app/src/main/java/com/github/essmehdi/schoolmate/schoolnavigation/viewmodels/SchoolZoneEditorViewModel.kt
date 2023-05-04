package com.github.essmehdi.schoolmate.schoolnavigation.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.github.essmehdi.schoolmate.schoolnavigation.api.dto.CreateSchoolZoneDto
import com.github.essmehdi.schoolmate.schoolnavigation.api.dto.EditSchoolZoneDto
import com.github.essmehdi.schoolmate.schoolnavigation.models.Point
import com.github.essmehdi.schoolmate.schoolnavigation.models.SchoolZone
import com.github.essmehdi.schoolmate.shared.api.Api
import com.github.essmehdi.schoolmate.shared.api.BaseResponse
import com.github.essmehdi.schoolmate.shared.api.dto.MessageResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SchoolZoneEditorViewModel: ViewModel() {
  val chosenPoints: MutableLiveData<List<Point>> = MutableLiveData()
  val status: MutableLiveData<BaseResponse<SchoolZone>> = MutableLiveData()
  val deleteStatus: MutableLiveData<BaseResponse<MessageResponse>> = MutableLiveData()


  fun editZone(id: Long, editSchoolZoneDto: EditSchoolZoneDto) {
    status.value = BaseResponse.Loading()
    Api.schoolZonesService.editSchoolZone(id, editSchoolZoneDto).enqueue(object : Callback<SchoolZone> {
      override fun onResponse(call: Call<SchoolZone>, response: Response<SchoolZone>) {
        if (response.isSuccessful) {
          status.value = BaseResponse.Success(response.body()!!)
        } else {
          status.value = BaseResponse.Error(response.code())
        }
      }

      override fun onFailure(call: Call<SchoolZone>, t: Throwable) {
        status.value = BaseResponse.Error(0)
      }
    })
  }

  fun addZone(createSchoolZoneDto: CreateSchoolZoneDto) {
    status.value = BaseResponse.Loading()
    Api.schoolZonesService.createSchoolZone(createSchoolZoneDto).enqueue(object : Callback<SchoolZone> {
      override fun onResponse(call: Call<SchoolZone>, response: Response<SchoolZone>) {
        if (response.isSuccessful) {
          status.value = BaseResponse.Success(response.body()!!)
        } else {
          status.value = BaseResponse.Error(response.code())
        }
      }

      override fun onFailure(call: Call<SchoolZone>, t: Throwable) {
        status.value = BaseResponse.Error(0)
      }
    })
  }

  fun deleteZone(id: Long) {
    deleteStatus.value = BaseResponse.Loading()
    Api.schoolZonesService.deleteSchoolZone(id).enqueue(object : Callback<MessageResponse> {
      override fun onResponse(call: Call<MessageResponse>, response: Response<MessageResponse>) {
        if (response.isSuccessful) {
          deleteStatus.value = BaseResponse.Success(response.body()!!)
        } else {
          deleteStatus.value = BaseResponse.Error(response.code())
        }
      }

      override fun onFailure(call: Call<MessageResponse>, t: Throwable) {
        deleteStatus.value = BaseResponse.Error(0)
      }
    })
  }
}