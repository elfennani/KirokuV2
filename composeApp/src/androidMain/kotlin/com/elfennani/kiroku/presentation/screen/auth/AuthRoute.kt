package com.elfennani.kiroku.presentation.screen.auth

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
data class AuthRoute(
    val token: String
) : NavKey
