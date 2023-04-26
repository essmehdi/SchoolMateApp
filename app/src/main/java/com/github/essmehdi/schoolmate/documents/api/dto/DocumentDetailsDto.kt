package com.github.essmehdi.schoolmate.documents.api.dto

data class DocumentDetailsDto(
  val name: String,
  val shared: Boolean,
  val tags: List<Long>
)
