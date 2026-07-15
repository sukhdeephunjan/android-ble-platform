package com.example.datalib.resultHandling

sealed class AppError {
    object InvalidCredentials : AppError()
    object DatabaseError : AppError()
    object UserAlreadyExists : AppError()
    object UserNotFound : AppError()
    data class Unknown(val message: String? = null) : AppError()
}

