package com.elfennani.kiroku.domain.repository

import com.elfennani.kiroku.domain.model.Chapter
import com.elfennani.kiroku.domain.model.Download
import com.elfennani.kiroku.domain.model.DownloadStatus
import com.elfennani.kiroku.domain.model.Episode
import com.elfennani.kiroku.domain.model.OfflineEpisode
import com.elfennani.kiroku.domain.service.AnimeSource
import kotlinx.coroutines.flow.Flow
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
interface DownloadRepository {
    suspend fun enqueueDownload(mediaId: Int, episode: Episode)
    suspend fun enqueueDownload(chapter: Chapter)
    suspend fun cancelDownload(id: Uuid = Uuid.Companion.random())

    suspend fun getDownloadByUUID(uuid: Uuid): Download?

    fun getDownloadsByMediaId(mediaId: Int): Flow<List<Download>>
}