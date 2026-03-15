package com.elfennani.kiroku.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.elfennani.kiroku.data.local.entity.ChapterEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ChapterDao {
    @Query("SELECT * FROM chapters WHERE mediaId = :mediaId")
    fun getAllFlow(mediaId: Int): Flow<List<ChapterEntity>>

    @Query("DELETE FROM chapters WHERE mediaId = :mediaId AND source = :source")
    suspend fun deleteAll(mediaId: Int, source: String)

    @Upsert
    suspend fun upsertAll(chapters: List<ChapterEntity>)

    @Transaction
    suspend fun upsertAllTransaction(mediaId: Int, chapters: List<ChapterEntity>) {
        deleteAll(mediaId, chapters.first().source)
        upsertAll(chapters)
    }
}