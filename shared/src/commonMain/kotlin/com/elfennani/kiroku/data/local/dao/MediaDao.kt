package com.elfennani.kiroku.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.elfennani.kiroku.data.local.entity.LocalMediaEntity
import com.elfennani.kiroku.data.local.entity.MatchEntity
import com.elfennani.kiroku.data.local.entity.OngoingMediaEntity
import com.elfennani.kiroku.data.local.relation.OngoingWithMedia
import kotlinx.coroutines.flow.Flow

@Dao
interface MediaDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMedia(media: LocalMediaEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMedia(media: List<LocalMediaEntity>)

    @Query("SELECT * FROM media_v2 WHERE id = :id")
    suspend fun getMedia(id: Int): LocalMediaEntity?

    @Query("SELECT * FROM media_v2 WHERE id = :id")
    fun getMediaFlow(id: Int): Flow<LocalMediaEntity?>

    @Query("SELECT * FROM media_v2")
    fun getMediaListFlow(): Flow<List<LocalMediaEntity>>

    @Query("SELECT * FROM ongoing_media")
    fun getOngoingMedia(): List<OngoingWithMedia>

    @Query("SELECT * FROM ongoing_media")
    fun getOngoingMediaFlow(): Flow<List<OngoingWithMedia>>

    @Query("DELETE FROM ongoing_media")
    suspend fun clearOngoingMedia()

    @Insert
    suspend fun insertOngoingMedia(ongoingMedia: List<OngoingMediaEntity>)

    @Upsert
    suspend fun insertMediaMatch(match: MatchEntity)

    @Query("SELECT sourceId FROM `match` WHERE mediaId = :mediaId AND sourceName = :sourceName")
    suspend fun getMediaSourceId(mediaId: Int, sourceName: String): String?

    @Query("SELECT sourceId FROM `match` WHERE mediaId = :mediaId AND sourceName = :sourceName")
    fun getMediaSourceIdFlow(mediaId: Int, sourceName: String): Flow<String?>

    @Transaction
    suspend fun insertOngoingMediaTransaction(mediaList: List<LocalMediaEntity>){
        clearOngoingMedia()
        insertMedia(mediaList)
        insertOngoingMedia(mediaList.map { OngoingMediaEntity(it.id) })
    }
}