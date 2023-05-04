package com.github.essmehdi.schoolmate.schoolnavigation.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import com.github.essmehdi.schoolmate.schoolnavigation.models.SchoolZone
import com.github.essmehdi.schoolmate.shared.api.Api
import com.github.essmehdi.schoolmate.shared.api.BaseResponse
import com.github.essmehdi.schoolmate.users.models.User
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.Polygon
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

open class SchoolNavigationViewModel: ViewModel() {
  val fetchStatus: MutableLiveData<BaseResponse<List<SchoolZone>>> = MutableLiveData()
  val schoolZonesPolygons: LiveData<List<Polygon>> = fetchStatus.map { fetchedZones ->
    if (fetchedZones is BaseResponse.Success) {
      fetchedZones.data!!.map { fetchedZone ->
        val geoPoints = fetchedZone.geometry.points.map { GeoPoint(it.x, it.y) }
        val polygon = Polygon().apply {
          points = geoPoints
          title = fetchedZone.name
          subDescription = fetchedZone.description
        }
        polygon
      }
    } else {
      listOf()
    }
  }
  val user: MutableLiveData<BaseResponse<User>> = MutableLiveData()

  init {
    fetchZones()
  }

  fun fetchZones() {
    fetchStatus.value = BaseResponse.Loading()
    Api.schoolZonesService.getAllSchoolZones().enqueue(object: Callback<List<SchoolZone>> {
      override fun onResponse(call: Call<List<SchoolZone>>, response: Response<List<SchoolZone>>) {
        if (response.isSuccessful) {
          fetchStatus.value = BaseResponse.Success(response.body()!!)
        } else {
          fetchStatus.value = BaseResponse.Error(response.code())
        }
      }

      override fun onFailure(call: Call<List<SchoolZone>>, t: Throwable) {
        fetchStatus.value = BaseResponse.Error(0)
      }
    })
  }

  fun fetchMe() {
    user.value = BaseResponse.Loading()
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