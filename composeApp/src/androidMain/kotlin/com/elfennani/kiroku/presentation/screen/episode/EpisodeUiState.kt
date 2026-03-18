package com.elfennani.kiroku.presentation.screen.episode

import com.elfennani.kiroku.domain.model.Episode
import com.elfennani.kiroku.domain.model.Media
import com.elfennani.kiroku.domain.model.Result
import com.elfennani.kiroku.domain.model.VideoSource

data class EpisodeUiState(
    // Provided by route
    val sourceName: String? = null,
    val episodeNumber: Double,

    // States
    val isLoading: Boolean = true,
    val sources: List<VideoSource> = emptyList(),
    val error: String? = null,
    val selectedSource: VideoSource? = null,
    val episode: Episode? = null,
    val availableEpisodes: List<Double> = emptyList(),
    val media: Media? = null
)