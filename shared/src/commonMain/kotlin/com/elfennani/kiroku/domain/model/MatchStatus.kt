package com.elfennani.kiroku.domain.model

sealed class MatchStatus {
    data object Unmatched : MatchStatus()
    data object Loading : MatchStatus()
    data class Matched(val items: MediaItemList) : MatchStatus()
    data class Error(val message: String) : MatchStatus()
}
