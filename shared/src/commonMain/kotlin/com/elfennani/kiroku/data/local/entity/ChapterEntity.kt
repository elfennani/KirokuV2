package com.elfennani.kiroku.data.local.entity

import androidx.room.Entity
import com.elfennani.kiroku.domain.model.Chapter
import kotlin.time.Instant

@Entity(tableName = "chapters", primaryKeys = ["id", "source"])
data class ChapterEntity(
    val id: String,
    val source: String,
    val mediaId: Int,
    val title: String,
    val chapter: Double,
    val uploaded: Long?,
    val views: Int?
)

fun ChapterEntity.asDomain(): Chapter {
    return Chapter(
        id = id,
        source = source,
        title = title,
        number = chapter,
        uploaded = uploaded?.let { Instant.fromEpochMilliseconds(uploaded) },
        views = views,
    )
}

fun Chapter.asEntity(mediaId: Int): ChapterEntity {
    return ChapterEntity(
        id = id,
        source = source,
        title = title,
        chapter = number,
        uploaded = uploaded?.toEpochMilliseconds(),
        views = views,
        mediaId = mediaId
    )
}