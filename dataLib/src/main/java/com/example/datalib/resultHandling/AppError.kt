package com.example.datalib.resultHandling

sealed class AppError {
    object UserNotFound : AppError()
    object InvalidCredentials : AppError()
    object UserAlreadyExists : AppError()
    object DatabaseError : AppError()
    data class Unknown(val message: String? = null) : AppError()
}