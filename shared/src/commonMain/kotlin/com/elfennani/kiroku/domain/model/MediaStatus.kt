package com.elfennani.kiroku.domain.model

enum class MediaStatus {
    COMPLETED,
    PLANNED,
    IN_PROGRESS,
    /** Rewatching in context of Anime, and Rereading in context of manga */
    REVISITING,
    DROPPED,
    PAUSED
}

fun MediaStatus.label(type: MediaType? = null) = when(this) {
    MediaStatus.COMPLETED -> "Completed"
    MediaStatus.IN_PROGRESS -> when (type) {
        MediaType.ANIME -> "Watching"
        MediaType.MANGA -> "Reading"
        else -> "In progress"
    }
    MediaStatus.REVISITING -> when (type){
        MediaType.ANIME -> "Rewatching"
        MediaType.MANGA -> "Rereading"
        else -> "Revisiting"
    }
    MediaStatus.PLANNED -> "Planned"
    MediaStatus.DROPPED -> "Dropped"
    MediaStatus.PAUSED -> "Paused"
}