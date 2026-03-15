package com.elfennani.kiroku.data.local.entity

import androidx.room.Entity
import com.elfennani.kiroku.domain.model.Episode

@Entity(tableName = "episode", primaryKeys = ["id", "source", "mediaId"])
data class EpisodeEntity(
    val id: String,
    val source: String,
    val mediaId: Int,
    val number: Double,
    val thumbnail: String?,
    val title: String?,
    val duration: Long?,
    val size: Long?
)

fun EpisodeEntity.asDomain() = Episode(
    id = id,
    number = number,
    title = title,
    thumbnail = thumbnail,
    duration = duration,
    size = size,
    source = source
)