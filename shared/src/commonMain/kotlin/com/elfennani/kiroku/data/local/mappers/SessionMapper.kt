package com.elfennani.kiroku.data.local.mappers

import com.elfennani.kiroku.data.local.entity.SessionEntity
import com.elfennani.kiroku.domain.model.Session

fun SessionEntity.toDomainModel() = Session(
    token = this.token,
    userID = this.userID
)

fun Session.toEntity(userID: Int? = null) = SessionEntity(
    token = this.token,
    userID = userID ?: this.userID
)