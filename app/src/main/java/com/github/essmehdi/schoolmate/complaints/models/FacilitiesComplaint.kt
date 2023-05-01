package com.github.essmehdi.schoolmate.complaints.models

import com.github.essmehdi.schoolmate.complaints.enumerations.FacilityType

data class FacilitiesComplaint(val facilityType: FacilityType,
                               val className: String) : Complaint(), java.io.Serializable
