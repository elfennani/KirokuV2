package com.elfennani.kiroku.presentation.screen.match

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
data class MatchRoute(
    val mediaId: Int,
    val title: String,
    val sourceName: String
) : NavKey