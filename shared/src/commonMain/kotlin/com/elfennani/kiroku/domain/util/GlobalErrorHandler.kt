package com.elfennani.kiroku.domain.util

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow

object GlobalErrorHandler {
    private val _errors = MutableSharedFlow<Exception>()
    val errors: SharedFlow<Exception> = _errors

    suspend fun emitError(message: Exception) {
        _errors.emit(message)
    }
}