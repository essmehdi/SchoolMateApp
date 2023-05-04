package com.github.essmehdi.schoolmate.schoolnavigation.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import com.github.essmehdi.schoolmate.schoolnavigation.models.SchoolZone
import com.github.essmehdi.schoolmate.shared.api.BaseResponse

class SchoolZonesViewModel: SchoolNavigationViewModel() {
  val schoolZones: LiveData<List<SchoolZone>> = fetchStatus.map {
    if (it is BaseResponse.Success) {
      it.data!!
    } else {
      listOf()
    }
  }
}