package com.elfennani.kiroku.presentation.screen.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.elfennani.kiroku.domain.model.Result
import com.elfennani.kiroku.domain.usecase.SaveSession
import com.elfennani.kiroku.domain.util.GlobalErrorHandler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel(
    val token: String,
    val saveSession: SaveSession,
) : ViewModel() {
    private val _state = MutableStateFlow<AuthUiState>(AuthUiState.Loading)
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            try {
                val result = saveSession(token)
                if (result is Result.Success)
                    _state.value = AuthUiState.Success
                else if (result is Result.Error) {
                    _state.value =
                        AuthUiState.Error("Failed to save session: ${result.message}")
                    GlobalErrorHandler.emitError(
                        Exception(
                            "Failed to save session: ${result.message}",
                        )
                    )
                }
            } catch (e: Exception) {
                _state.value = AuthUiState.Error(e.message ?: "Unknown error")
            }
        }
    }
}