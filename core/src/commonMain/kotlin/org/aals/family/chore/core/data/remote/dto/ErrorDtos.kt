package org.aals.family.chore.core.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class ErrorResponse(
    val message: String,
    val code: String? = null,
    val validationErrors: List<ValidationErrorDto>? = null
)

@Serializable
data class ValidationErrorDto(
    val field: String,
    val errorCode: String,
    val message: String
)
