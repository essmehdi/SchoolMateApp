package com.github.essmehdi.schoolmate.complaints.models

import com.github.essmehdi.schoolmate.complaints.enumerations.BuildingProb

data class BuildingComplaint(val building: String,
                             val buildingProb: BuildingProb) : Complaint(), java.io.Serializable
