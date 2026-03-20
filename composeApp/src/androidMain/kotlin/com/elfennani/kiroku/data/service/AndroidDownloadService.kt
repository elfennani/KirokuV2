package com.elfennani.kiroku.data.service

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.elfennani.kiroku.data.workers.DownloadWorker
import com.elfennani.kiroku.domain.model.Download
import com.elfennani.kiroku.domain.service.DownloadService
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.toJavaUuid

@OptIn(ExperimentalUuidApi::class)
class AndroidDownloadService(
    val context: Context
) : DownloadService {
    override suspend fun enqueueDownload(download: Download) {
        val request = OneTimeWorkRequestBuilder<DownloadWorker>()
            .setId(download.id.toJavaUuid())
            .setInputData(
                workDataOf(
                    DownloadWorker.KEY_INPUT_DOWNLOAD_ID to download.id.toString(),
                )
            )
            .setConstraints(Constraints(requiredNetworkType = NetworkType.CONNECTED))
            .build()
        WorkManager
            .getInstance(context)
            .beginUniqueWork(
                DownloadWorker.WORKER_NAME,
                ExistingWorkPolicy.APPEND_OR_REPLACE,
                request
            )
            .enqueue()
    }

    override suspend fun cancelDownload(download: Download) {
        WorkManager
            .getInstance(context)
            .cancelWorkById(download.id.toJavaUuid())
    }
}