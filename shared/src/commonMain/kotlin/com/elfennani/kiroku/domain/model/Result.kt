package com.elfennani.kiroku.domain.model

sealed class Result<out T> {
    object Loading : Result<Nothing>()
    data class Success<T>(val data: T) : Result<T>()
    data class Error(val throwable: Throwable) : Result<Nothing>()
}

fun <T, R> Result<T>.map(transform: (T) -> R): Result<R> {
    return when (this) {
        is Result.Error -> Result.Error(this.throwable)
        Result.Loading -> Result.Loading
        is Result.Success<T> -> Result.Success(transform(this.data))
    }
}