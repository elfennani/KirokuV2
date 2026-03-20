package com.elfennani.kiroku.domain.model

data class Episode(
    /** Source ID */
    val id: String,
    val source: String,
    val title: String?,
    val number: Double,
    val thumbnail: String?,

    /** Duration in seconds */
    val duration: Long?,

    /** Size in bytes */
    val size: Long?,

    val downloadStatus: DownloadStatus? = null
)
