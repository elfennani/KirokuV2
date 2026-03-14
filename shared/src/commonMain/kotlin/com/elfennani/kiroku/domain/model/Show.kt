package com.elfennani.kiroku.domain.model

data class Show(
    val id: Int,
    val title: String,
    val cover: AdaptiveImage?,
    val banner: String?,
    val episodes: Int?,
    val progress: Int?,
)
