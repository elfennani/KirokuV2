package com.elfennani.kiroku.data.repository

import com.elfennani.kiroku.data.local.dao.DownloadDao
import com.elfennani.kiroku.data.local.entity.asDomain
import com.elfennani.kiroku.data.local.entity.asEntity
import com.elfennani.kiroku.domain.model.Chapter
import com.elfennani.kiroku.domain.model.Download
import com.elfennani.kiroku.domain.model.DownloadResource
import com.elfennani.kiroku.domain.model.DownloadStatus
import com.elfennani.kiroku.domain.model.Episode
import com.elfennani.kiroku.domain.model.VideoAudio
import com.elfennani.kiroku.domain.model.VideoType
import com.elfennani.kiroku.domain.repository.DownloadRepository
import com.elfennani.kiroku.domain.repository.MediaRepository
import com.elfennani.kiroku.domain.service.DownloadService
import com.elfennani.kiroku.utils.clean
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
class DownloadRepositoryImpl(
    private val downloadService: DownloadService,
    private val downloadDao: DownloadDao,
    private val mediaRepository: MediaRepository
) : DownloadRepository {
    override suspend fun enqueueDownload(mediaId: Int, episode: Episode) {
        val downloads = downloadDao.getDownloadsByMediaId(mediaId).map { it.asDomain() }
        val exists = downloads.firstOrNull { dl -> dl.number == episode.number }
        val inProgress = downloads.firstOrNull { dl ->
            dl.number == episode.number && (dl.status is DownloadStatus.Downloading || dl.status is DownloadStatus.Progress)
        }
        val isDownloaded = downloads.any { dl ->
            dl.number == episode.number && (dl.status is DownloadStatus.Complete)
        }

        if (isDownloaded) {
            return;
        }

        val sourceService = mediaRepository.animeSources.first { it.name == episode.source }
        val media = mediaRepository.getMediaFlow(mediaId).firstOrNull() ?: return

        var download = Download(
            id = Uuid.random(),
            mediaId = mediaId,
            title = "${media.title} • Episode ${episode.number.clean()}",
            resources = emptyList(),
            status = DownloadStatus.Idle,
            number = episode.number
        )

        if (inProgress != null) {
            download = inProgress
        }

        if(exists != null){
            downloadDao.deleteDownload(mediaId, episode.number)
        }

        downloadDao.upsertDownload(download.asEntity())

        val sources = sourceService.getSources(
            mediaId = mediaId,
            episodeNumber = episode.number,
            type = VideoType.MP4
        )

        val source = sources.maxByOrNull {
            if (it.audio == VideoAudio.DUBBED) 1
            else 0
        }

        download = if (source == null) {
            download.copy(
                status = DownloadStatus.Error("No sources found")
            )
        } else {
            download.copy(
                resources = listOf(DownloadResource(url = source.url, extension = "mp4")),
                status = DownloadStatus.Downloading
            )
        }

        downloadDao.upsertDownload(download.asEntity())

        if (download.status !is DownloadStatus.Error)
            downloadService.enqueueDownload(download)
    }

    override suspend fun enqueueDownload(chapter: Chapter) {
        TODO("Not yet implemented")
    }

    override suspend fun cancelDownload(id: Uuid) {
        TODO("Not yet implemented")
    }

    override suspend fun getDownloadByUUID(uuid: Uuid): Download? {
        TODO("Not yet implemented")
    }

    override fun getDownloadsByMediaId(mediaId: Int): Flow<List<Download>> {
        return downloadDao.getDownloadsByMediaIdFlow(mediaId)
            .map { list -> list.map { it.asDomain() } }
    }
}