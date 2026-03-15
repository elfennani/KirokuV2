package com.elfennani.kiroku.domain.model

sealed class Result<out T> {
    object Loading : Result<Nothing>()
    data class Success<T>(val data: T) : Result<T>()
    data class Error(val message: String) : Result<Nothing>()
}

fun <T, R> Result<T>.map(transform: (T) -> R): Result<R> {
    return when (this) {
        is Result.Error -> Result.Error(message)
        Result.Loading -> Result.Loading
        is Result.Success<T> -> Result.Success(transform(this.data))
    }
}

fun <T> Resource<T>.toResult(): Result<T> = when (this) {
    is Resource.Error -> Result.Error(message)
    is Resource.Success<T> -> Result.Success(data)
}