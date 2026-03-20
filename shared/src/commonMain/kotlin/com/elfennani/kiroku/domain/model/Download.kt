package com.elfennani.kiroku.domain.model

import kotlinx.serialization.Serializable
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
data class Download(
    val id: Uuid,
    val mediaId: Int,
    /** Number of episode/chapter */
    val number: Double,
    val title: String,
    val resources: List<DownloadResource>,
    val status: DownloadStatus,
)

@Serializable
data class DownloadResource(
    val url: String,
    val extension: String
)