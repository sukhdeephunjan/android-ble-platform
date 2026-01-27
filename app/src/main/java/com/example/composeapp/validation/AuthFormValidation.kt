package com.example.composeapp.validation

interface AuthFormValidation {
    fun validateUsername(username: String): ValidationResult
    fun validatePassword(password: String): ValidationResult
}