package com.github.essmehdi.schoolmate.complaints.models

import com.github.essmehdi.schoolmate.auth.models.User
import com.github.essmehdi.schoolmate.complaints.enumerations.ComplaintStatus
import java.time.LocalDate

open class Complaint : java.io.Serializable{
    open val id: Long? = null
    open lateinit var description: String
    open lateinit var status: ComplaintStatus
    open lateinit var date: String
    open lateinit var complainant: User
    open val handler: User? = null
}
