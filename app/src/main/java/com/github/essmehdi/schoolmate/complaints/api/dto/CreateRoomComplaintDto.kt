package com.github.essmehdi.schoolmate.complaints.api.dto

import com.github.essmehdi.schoolmate.complaints.enumerations.RoomProb

data class CreateRoomComplaintDto(val room: String,
                                  val roomProb: RoomProb) : CreateComplaintDto()
