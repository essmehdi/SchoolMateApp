package com.github.essmehdi.schoolmate.complaints.api.dto

open class CreateComplaintDto {
    private var description: String? = null

    fun getDescription(): String? {
        return description
    }

    fun setDescription(description: String?) {
        this.description = description
    }
}