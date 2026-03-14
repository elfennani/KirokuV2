package com.elfennani.kiroku.domain.model

data class VideoSource(
    val type: VideoType,
    val name: String,
    val url: String,
    val audio: VideoAudio
)
