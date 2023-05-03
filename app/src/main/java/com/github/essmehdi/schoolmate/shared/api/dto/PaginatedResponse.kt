package com.github.essmehdi.schoolmate.shared.api.dto

data class PaginatedResponse<T>(
    val results: List<T>,
    val page: Long,
    val count: Int,
    val totalPages: Long,
    val totalItems: Long,
    val last: Boolean)