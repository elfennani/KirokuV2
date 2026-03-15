package com.elfennani.kiroku.presentation.screen.match

import com.elfennani.kiroku.domain.model.BasicMedia

data class MatchUiState(
    val mediaId: Int = -1,
    val items: List<BasicMedia> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
    val query: String = "",
    val page: Int = 1,
)
