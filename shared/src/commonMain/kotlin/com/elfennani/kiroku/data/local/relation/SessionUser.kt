package com.elfennani.kiroku.data.local.relation

import androidx.room.Embedded
import androidx.room.Relation
import com.elfennani.kiroku.data.local.entity.SessionEntity
import com.elfennani.kiroku.data.local.entity.UserEntity

data class SessionUser(
    @Embedded
    val session: SessionEntity,

    @Relation(
        parentColumn = "user_id",
        entityColumn = "id"
    )
    val user: UserEntity?
)