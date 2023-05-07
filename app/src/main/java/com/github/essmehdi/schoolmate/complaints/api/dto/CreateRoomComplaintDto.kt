package com.github.essmehdi.schoolmate.complaints.api.dto

import com.github.essmehdi.schoolmate.complaints.enumerations.RoomProb

data class CreateRoomComplaintDto(val room: String,
                                  val roomProb: RoomProb,
                                  override val description: String,
                                  override val type: String) : CreateComplaintDto(description, type)
