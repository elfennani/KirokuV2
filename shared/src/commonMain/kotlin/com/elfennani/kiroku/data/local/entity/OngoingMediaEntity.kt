package com.elfennani.kiroku.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ongoing_media")
data class OngoingMediaEntity(
    @PrimaryKey
    val mediaId: Int,
)
