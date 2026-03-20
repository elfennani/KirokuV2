package com.elfennani.kiroku.domain.model

import kotlin.time.Instant

data class OfflineEpisode(
    val id: Int,
    val media: Media,
    val number: Double,
    val thumbnail: String?,
    val title: String?,
    val size: Long?,
    val status: DownloadStatus,
    val createdAt: Instant
)
