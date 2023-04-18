package com.github.essmehdi.schoolmate.documents.api.dto

data class UploadDocumentDto(
  val name: String,
  val shared: Boolean,
  val tags: List<Long>
)
