package com.github.essmehdi.schoolmate.complaints.models

import com.github.essmehdi.schoolmate.auth.models.User
import com.github.essmehdi.schoolmate.complaints.enumerations.ComplaintStatus
import java.time.LocalDate

open class Complaint : java.io.Serializable{
    private var id: Long? = null
    private lateinit var description: String
    private lateinit var status: ComplaintStatus
    private lateinit var date: String
    private lateinit var complainant: User
    private lateinit var handler: User

    fun getId(): Long? {
        return id
    }

    fun setId(id: Long) {
        this.id = id
    }

    fun getDescription(): String {
        return description
    }

    fun setDescription(description: String) {
        this.description = description
    }

    fun getStatus(): ComplaintStatus {
        return status
    }

    fun setStatus(status: ComplaintStatus) {
        this.status = status
    }

    fun getDate(): String {
        return date
    }

    fun setDate(date: String) {
        this.date = date
    }

    fun getComplainant(): User {
        return complainant
    }

    fun setComplainant(complainant: User) {
        this.complainant = complainant
    }

    fun getHandler(): User {
        return handler
    }

    fun setHandler(handler: User) {
        this.handler = handler
    }

}
