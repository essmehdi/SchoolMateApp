package com.github.essmehdi.schoolmate.complaints.api.dto

data class CreateRoomComplaintDto(val room: String,
                                  val roomProb: String) : CreateComplaintDto()
