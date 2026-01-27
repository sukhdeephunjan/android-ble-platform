package com.example.composeapp.validation

import android.util.Patterns
import com.example.composeapp.validation.ValidationResult

object Validator {

    fun validateNotBlank(value: String, field: ValidationErrorType): ValidationResult {
        return if (value.isBlank()) ValidationResult.Error(field)
        else ValidationResult.Success
    }

    fun validateEmail(email: String): ValidationResult {
        return when {
            email.isBlank() -> ValidationResult.Error(ValidationErrorType.EMPTY_EMAIL)
            !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> ValidationResult.Error(ValidationErrorType.INVALID_EMAIL)
            else -> ValidationResult.Success
        }
    }

    fun validatePassword(password: String): ValidationResult {
        return if (password.isBlank()) ValidationResult.Error(ValidationErrorType.EMPTY_PASSWORD)
        else ValidationResult.Success
    }
}
