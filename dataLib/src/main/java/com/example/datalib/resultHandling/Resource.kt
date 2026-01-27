package com.example.datalib.resultHandling

sealed class Resource<out T> {
    data class Success<T>(val data: T) : Resource<T>()
    data class Failure(val error: AppError) : Resource<Nothing>()
}