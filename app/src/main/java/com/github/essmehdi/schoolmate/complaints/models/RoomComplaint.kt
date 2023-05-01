package com.github.essmehdi.schoolmate.complaints.models

import com.github.essmehdi.schoolmate.complaints.enumerations.RoomProb

data class RoomComplaint(val room: String,
                         val roomProb: RoomProb) : Complaint(), java.io.Serializable
