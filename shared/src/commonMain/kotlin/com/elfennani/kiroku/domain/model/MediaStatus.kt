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