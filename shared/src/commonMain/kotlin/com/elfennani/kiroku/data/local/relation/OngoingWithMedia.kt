package com.elfennani.kiroku.data.local.relation

import androidx.room.Embedded
import androidx.room.Relation
import com.elfennani.kiroku.data.local.entity.LocalMediaEntity
import com.elfennani.kiroku.data.local.entity.OngoingMediaEntity
import com.elfennani.kiroku.data.local.entity.asDomain

data class OngoingWithMedia(
    @Embedded
    val ongoing: OngoingMediaEntity,
    @Relation(
        parentColumn = "mediaId",
        entityColumn = "id"
    )
    val media: LocalMediaEntity
)

fun OngoingWithMedia.asDomain() = media.asDomain()