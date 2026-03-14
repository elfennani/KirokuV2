package com.elfennani.kiroku.data.local.mappers

import com.elfennani.kiroku.data.local.entity.UserEntity
import com.elfennani.kiroku.data.local.relation.SessionUser
import com.elfennani.kiroku.domain.model.User

fun UserEntity.toDomainModel(): User {
    return User(
        id = this.id,
        name = this.name,
        icon = this.icon,
        avatar = this.avatar
    )
}

fun SessionUser.toDomainModel(): User? {
    return this.user?.toDomainModel()
}

fun User.toEntity(): UserEntity {
    return UserEntity(
        id = this.id,
        name = this.name,
        icon = this.icon,
        avatar = this.avatar
    )
}