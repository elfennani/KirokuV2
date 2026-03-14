package com.elfennani.kiroku.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.elfennani.kiroku.data.local.entity.UserEntity
import com.elfennani.kiroku.data.local.relation.SessionUser
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM UserEntity WHERE id = :id")
    fun getUserById(id: Int): Flow<UserEntity?>

    @Upsert
    suspend fun upsertUser(user: UserEntity): Long

    @Transaction
    @Query("SELECT * FROM SessionEntity ORDER BY created_at DESC LIMIT 1")
    fun getCurrentSessionUser(): Flow<SessionUser>
}