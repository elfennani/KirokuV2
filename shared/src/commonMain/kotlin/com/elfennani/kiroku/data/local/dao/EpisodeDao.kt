package com.elfennani.kiroku.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.elfennani.kiroku.data.local.entity.EpisodeEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface EpisodeDao {
    @Query("SELECT * FROM episode WHERE mediaId = :mediaId AND source = :source")
    suspend fun getEpisodes(mediaId: Int, source: String): List<EpisodeEntity>

    @Query("SELECT * FROM episode WHERE mediaId = :mediaId AND source = :source")
    fun getEpisodesFlow(mediaId: Int, source: String): Flow<List<EpisodeEntity>>

    @Upsert
    fun upsertEpisodes(episodes: List<EpisodeEntity>)

    @Query("DELETE FROM episode WHERE mediaId = :mediaId AND source = :source")
    suspend fun deleteEpisodes(mediaId: Int, source: String)

    @Transaction
    suspend fun upsertEpisodesTransaction(
        mediaId: Int,
        source: String,
        episodes: List<EpisodeEntity>
    ) {
        deleteEpisodes(mediaId, source)
        upsertEpisodes(episodes)
    }
}