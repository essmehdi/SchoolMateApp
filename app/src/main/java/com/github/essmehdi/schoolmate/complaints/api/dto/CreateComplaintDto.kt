package com.github.essmehdi.schoolmate.complaints.api.dto

open class CreateComplaintDto(){
    open lateinit var description: String
    open lateinit var type: String
}