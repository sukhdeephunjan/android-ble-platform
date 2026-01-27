package com.example.composeapp.validation

import javax.inject.Inject

class SignupFormValidation  @Inject constructor() : AuthFormValidation {

    override fun validateUsername(username: String): ValidationResult {
        return Validator.validateEmail(username)
    }

    override fun validatePassword(password: String): ValidationResult {
        return Validator.validatePassword(password)
    }

    fun validateConfirmPassword(
        password: String,
        confirmPassword: String
    ): ValidationResult {
        return if (password != confirmPassword) {
            ValidationResult.Error(ValidationErrorType.PASSWORD_MISMATCH)
        } else {
            ValidationResult.Success
        }
    }
}