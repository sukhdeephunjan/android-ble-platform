package com.example.composeapp.validation

sealed class ValidationResult {
    object Success : ValidationResult()
    data class Error(val type: ValidationErrorType) : ValidationResult()
}

enum class ValidationErrorType {
    EMPTY_EMAIL,
    INVALID_EMAIL,
    EMPTY_PASSWORD,
    PASSWORD_MISMATCH
}