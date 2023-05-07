package com.github.essmehdi.schoolmate.complaints.api.dto

import com.github.essmehdi.schoolmate.complaints.enumerations.FacilityType

data class CreateFacilityComplaintDto(val facilityType: FacilityType,
                                      val className: String,
                                      override val description: String,
                                      override val type: String) : CreateComplaintDto(description, type)