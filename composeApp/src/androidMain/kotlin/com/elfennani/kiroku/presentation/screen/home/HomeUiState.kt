package com.elfennani.kiroku.presentation.screen.home

import com.elfennani.kiroku.domain.model.Media
import com.elfennani.kiroku.domain.model.Result
import com.elfennani.kiroku.domain.model.Show
import com.elfennani.kiroku.domain.model.User

data class HomeUiState(
    val media: List<Media> = emptyList(),
    val error: String? = null,
)
