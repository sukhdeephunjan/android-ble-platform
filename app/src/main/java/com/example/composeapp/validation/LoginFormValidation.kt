package com.example.composeapp.validation
import javax.inject.Inject

class LoginFormValidation @Inject constructor()  : AuthFormValidation {
    override fun validateUsername(username: String): ValidationResult {
        return Validator.validateEmail(username)
    }
    override fun validatePassword(password: String): ValidationResult {
        return Validator.validatePassword(password)
    }
}