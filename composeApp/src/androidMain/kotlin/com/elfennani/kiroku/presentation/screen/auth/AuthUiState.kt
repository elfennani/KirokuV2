package com.elfennani.kiroku.presentation.screen.auth

sealed class AuthUiState {
    object Loading : AuthUiState()
    object Success : AuthUiState()
    data class Error(val message: String) : AuthUiState()
}