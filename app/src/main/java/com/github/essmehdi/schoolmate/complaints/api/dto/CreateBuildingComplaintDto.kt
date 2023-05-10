package com.github.essmehdi.schoolmate.complaints.api.dto

import com.github.essmehdi.schoolmate.complaints.enumerations.BuildingProb

data class CreateBuildingComplaintDto(val building: String,
                                      val buildingProb: BuildingProb) : CreateComplaintDto()
