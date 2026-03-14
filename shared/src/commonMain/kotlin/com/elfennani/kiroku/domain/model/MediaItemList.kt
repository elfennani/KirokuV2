package com.elfennani.kiroku.domain.model

sealed class MediaItemList {
    data class EpisodeList(val episodes: List<Episode>) : MediaItemList()
    data class ChapterList(val mangas: List<Chapter>) : MediaItemList()
}