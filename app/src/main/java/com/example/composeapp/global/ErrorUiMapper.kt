package com.example.composeapp.global

import com.example.datalib.resultHandling.AppError

object ErrorUiMapper {

    fun map(error: AppError): String {
        return when (error) {
            AppError.InvalidCredentials ->
                "Invalid username or password"

            AppError.DatabaseError ->
                "Something went wrong. Please try again."

            is AppError.Unknown -> "Unknown error is occured"
            AppError.UserAlreadyExists -> "User already exists"
            AppError.UserNotFound -> "User not found"
        }
    }
}
