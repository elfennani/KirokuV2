package com.elfennani.kiroku.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.elfennani.kiroku.data.local.entity.DownloadEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DownloadDao {
    @Query("SELECT * FROM downloads WHERE id = :uuid")
    suspend fun getDownloadByUUID(uuid: String): DownloadEntity?

    @Query("SELECT * FROM downloads WHERE mediaId = :mediaId")
    suspend fun getDownloadsByMediaId(mediaId: Int): List<DownloadEntity>

    @Query("SELECT * FROM downloads WHERE mediaId = :mediaId")
    fun getDownloadsByMediaIdFlow(mediaId: Int): Flow<List<DownloadEntity>>

    @Query("SELECT * FROM downloads")
    suspend fun getDownloads(): List<DownloadEntity>

    @Query("SELECT * FROM downloads")
    fun getDownloadsFlow(): Flow<List<DownloadEntity>>

    @Upsert
    suspend fun upsertDownload(download: DownloadEntity)

    @Query("UPDATE downloads SET status = :status WHERE id = :uuid")
    suspend fun updateDownloadStatus(uuid: String, status: String)

    @Query("UPDATE downloads SET progress = :progress WHERE id = :uuid")
    suspend fun updateDownloadProgress(uuid: String, progress: Long)

    @Query("DELETE FROM downloads")
    suspend fun deleteAll()

    @Query("DELETE FROM downloads WHERE id = :uuid")
    suspend fun deleteDownload(uuid: String)

    @Query("DELETE FROM downloads WHERE mediaId = :mediaId AND number = :number")
    suspend fun deleteDownload(mediaId: Int, number: Double)
}