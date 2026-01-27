package com.example.composeapp.validation

object ValidationUiMapper {

    fun map(type: ValidationErrorType): String {
        return when (type) {
            ValidationErrorType.EMPTY_EMAIL ->
                "Email can't be empty"

            ValidationErrorType.INVALID_EMAIL ->
                "Enter a valid email address"

            ValidationErrorType.EMPTY_PASSWORD ->
                "Password can't be empty"

            ValidationErrorType.PASSWORD_MISMATCH ->
                "Passwords do not match"
        }
    }
}
