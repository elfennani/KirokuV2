package com.elfennani.kiroku.presentation.screen.media

import com.elfennani.kiroku.domain.model.Episode
import com.elfennani.kiroku.domain.model.MatchStatus
import com.elfennani.kiroku.domain.model.Media
import com.elfennani.kiroku.domain.model.MediaItemList
import com.elfennani.kiroku.domain.model.Result

data class MediaUiState(
    val media: Media? = null,
    val isLoading: Boolean = true,
    val error: String? = null,
    val items: MatchStatus = MatchStatus.Loading,
    val sourceName: String? = null
)
