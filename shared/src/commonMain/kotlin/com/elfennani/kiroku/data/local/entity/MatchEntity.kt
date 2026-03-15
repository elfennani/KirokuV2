package com.elfennani.kiroku.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(
    tableName = "match",
    primaryKeys = ["mediaId", "sourceName"]
)
data class MatchEntity(
    val mediaId: Int,
    val sourceName: String,
    val sourceId: String,
)
