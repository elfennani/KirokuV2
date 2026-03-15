package com.elfennani.kiroku.presentation.screen.media

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
data class MediaRoute(
    val mediaId: Int
): NavKey