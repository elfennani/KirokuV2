package com.elfennani.kiroku.presentation.screen.episode

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
data class EpisodeRoute(
    val mediaId: Int,
    val sourceName: String?,
    val episodeNumber: Double
): NavKey