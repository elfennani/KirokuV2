package com.elfennani.kiroku.domain.model

import kotlin.time.Instant

data class Chapter(
    /** Source ID */
    val id: String,
    val source: String,
    val title: String,
    val chapter: Double,
    val uploaded: Instant?
)
