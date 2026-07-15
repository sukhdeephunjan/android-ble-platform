package com.example.composeapp.validation

import android.util.Patterns

object Validator {

    fun validateEmail(email: String): ValidationResult {
        return when {
            email.isBlank() -> ValidationResult.Error(ValidationErrorType.EMPTY_EMAIL)
            !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> ValidationResult.Error(ValidationErrorType.INVALID_EMAIL)
            else -> ValidationResult.Success
        }
    }

    fun validatePassword(password: String): ValidationResult {
        return when {
            password.isBlank() -> ValidationResult.Error(ValidationErrorType.EMPTY_PASSWORD)
            password.length < 8 -> ValidationResult.Error(ValidationErrorType.PASSWORD_TOO_SHORT)
            else -> ValidationResult.Success
        }
    }
}
