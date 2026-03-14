package com.elfennani.kiroku.domain.model

data class Media(
    val id: Int,
    val title: String,
    val description: String?,
    val progress: Int?,
    val total: Int?,
    val type: MediaType,
    val status: MediaStatus?,
    val cover: String?,
    val banner: String?
)
