package com.elfennani.kiroku.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.elfennani.kiroku.data.local.entity.SessionEntity

@Dao
interface SessionDao {
    @Insert
    suspend fun insertSession(session: SessionEntity): Long

    @Query("SELECT * FROM SessionEntity WHERE id = :id")
    suspend fun getSessionById(id: Long): SessionEntity?

    @Query("SELECT * FROM SessionEntity LIMIT 1")
    suspend fun getSession(): SessionEntity?

    @Delete
    suspend fun deleteSession(session: SessionEntity)

    @Query("UPDATE SessionEntity SET user_id = :userId WHERE id = :sessionId")
    suspend fun updateSessionUserId(sessionId: Long, userId: Int?)
}