package com.github.essmehdi.schoolmate.documents.models

data class Document(val id: Long, val name: String, val shared: Boolean, val uploadedAt: String, val tags: List<DocumentTag>): java.io.Serializable
