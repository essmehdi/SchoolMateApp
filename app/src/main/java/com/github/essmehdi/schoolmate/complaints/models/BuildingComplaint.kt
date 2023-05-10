package com.github.essmehdi.schoolmate.complaints.models

import com.github.essmehdi.schoolmate.auth.models.User
import com.github.essmehdi.schoolmate.complaints.enumerations.BuildingProb
import com.github.essmehdi.schoolmate.complaints.enumerations.ComplaintStatus

data class BuildingComplaint(val building: String,
                             val buildingProb: BuildingProb) : Complaint() , java.io.Serializable

