package com.elfennani.kiroku.domain.model

data class BasicEpisode(
    val id: String,
    val number: Double,
    val title: String?,
    val thumbnail: String?,
    /** in seconds */
    val duration: Long?
)
