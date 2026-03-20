package com.elfennani.kiroku.domain.model

sealed class MediaItemList {
    data class EpisodeList(val episodes: List<Episode>) : MediaItemList()
    data class ChapterList(val chapters: List<Chapter>) : MediaItemList()
}

fun MediaItemList.isEmpty() = when(this){
    is MediaItemList.ChapterList -> this.chapters.isEmpty()
    is MediaItemList.EpisodeList -> this.episodes.isEmpty()
}

