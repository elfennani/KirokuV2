package com.elfennani.kiroku.data.workers

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Context.NOTIFICATION_SERVICE
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
import android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.ListenableWorker.Result.*
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.impl.foreground.SystemForegroundService
import com.elfennani.kiroku.R
import com.elfennani.kiroku.data.local.dao.DownloadDao
import com.elfennani.kiroku.data.local.entity.asDomain
import com.elfennani.kiroku.data.local.entity.asEntity
import com.elfennani.kiroku.domain.model.Download
import com.elfennani.kiroku.domain.model.DownloadStatus
import com.elfennani.kiroku.domain.model.isInProgress
import com.elfennani.kiroku.domain.model.label
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.prepareGet
import io.ktor.http.contentLength
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.CancellationException
import io.ktor.utils.io.core.remaining
import io.ktor.utils.io.exhausted
import io.ktor.utils.io.readRemaining
import kotlinx.io.asSink
import java.io.File
import kotlin.math.roundToInt
import kotlin.time.Clock
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant
import kotlin.uuid.ExperimentalUuidApi

private const val TAG = "DownloadWorker"

@OptIn(ExperimentalUuidApi::class)
class DownloadWorker(
    appContext: Context,
    workerParams: WorkerParameters,
    private val downloadDao: DownloadDao,
    private val client: HttpClient,
) :
    CoroutineWorker(appContext, workerParams) {
    override suspend fun doWork(): Result {
        val downloadId = inputData.getString(KEY_INPUT_DOWNLOAD_ID) ?: return failure()
        var download = downloadDao.getDownloadByUUID(downloadId)?.asDomain() ?: return failure()

        if (download.resources.isEmpty()) {
            Log.e(TAG, "Download url(s) not found: $downloadId")
            downloadDao.upsertDownload(
                download.copy(status = DownloadStatus.Error("Download url(s) not found"))
                    .asEntity()
            )
            return failure()
        }

        if (download.resources.size == 1) {
            val resource = download.resources.first()
            val filename =
                "${download.mediaId}-${download.number}.${resource.extension}"
            val cacheFile = File(
                applicationContext.cacheDir,
                filename
            )

            try {
                val stream = cacheFile.outputStream().asSink()
                val buffer = 8 * 1024L

                var progress = 0L;
                var total: Long?;

                var lastEntryUpdate: Instant = Clock.System.now()
                val updateProgress: suspend (progress: Long, total: Long?) -> Unit =
                    { progress, total ->
                        if (lastEntryUpdate + 1.seconds < Clock.System.now()) {
                            println("Received $progress bytes from $total")
                            lastEntryUpdate = Clock.System.now()
                            download = download.copy(
                                status = DownloadStatus.Progress(progress, total)
                            )
                            setForeground(createForegroundInfo(download))
                            downloadDao.upsertDownload(download.asEntity())
                        }
                    }

                download = download.copy(
                    status = DownloadStatus.Downloading
                )
                downloadDao.upsertDownload(download.asEntity())
                setForeground(createForegroundInfo(download))

                client.prepareGet(resource.url).execute { httpResponse ->
                    val channel = httpResponse.body<ByteReadChannel>()

                    stream.use {
                        while (!channel.exhausted()) {
                            val chunk = channel.readRemaining(buffer)
                            progress += chunk.remaining
                            total = httpResponse.contentLength()
                            updateProgress(progress, total)

                            chunk.transferTo(stream)
                        }
                    }
                }

                val file = File(applicationContext.filesDir, "downloads/$filename")
                cacheFile.renameTo(file)

                download = download.copy(status = DownloadStatus.Complete(file.path))
                downloadDao.upsertDownload(download.asEntity())
                notify(download)

                return success()
            } catch (e: CancellationException) {
                Log.e(TAG, "Download cancelled: $downloadId", e)
                if (cacheFile.exists())
                    cacheFile.delete()

                download = download.copy(status = DownloadStatus.Cancelled)
                downloadDao.upsertDownload(download.asEntity())
                notify(download)

                return failure()
            } catch (e: Exception) {
                Log.e(TAG, "Download failed: $downloadId", e)
                if (cacheFile.exists())
                    cacheFile.delete()

                download =
                    download.copy(status = DownloadStatus.Error(e.message ?: "Unknown error"))
                downloadDao.upsertDownload(download.asEntity())
                notify(download)

                return failure()
            }
        } else {
            TODO()
        }

        return success()
    }

    // Creates an instance of ForegroundInfo which can be used to update the
    // ongoing notification.
    private fun createForegroundInfo(download: Download): ForegroundInfo {
        createChannel()
        val notification = download.toNotification()

        return ForegroundInfo(
            getNotificationId(download.mediaId.toDouble(), download.number),
            notification,
            FOREGROUND_SERVICE_TYPE_DATA_SYNC
        )
    }

    private fun notify(download: Download) {
        with(NotificationManagerCompat.from(applicationContext)) {
            if (ActivityCompat.checkSelfPermission(
                    applicationContext,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
            notify(
                getNotificationId(download.mediaId.toDouble(), download.number),
                download.toNotification()
            )
        }
    }

    private fun Download.toNotification() = NotificationCompat.Builder(
        applicationContext,
        applicationContext.getString(R.string.download_notification_channel_id)
    )
        .setContentTitle(title)
        .setTicker(title)
        .setContentText(status.label())
        .setSmallIcon(R.drawable.outline_download_24)
        .setOngoing(status.isInProgress())
        .setSilent(status.isInProgress())
        .addAction(
            NotificationCompat.Action.Builder(
                null,
                "Cancel",
                WorkManager.getInstance(applicationContext)
                    .createCancelPendingIntent(getId())
            )
                .build()
        )
        .apply {
            if (status is DownloadStatus.Progress) {
                val status = status as DownloadStatus.Progress
                if (status.progress != null && status.total != null) {
                    val progress =
                        ((status.progress!!.toFloat() / status.total!!.toFloat()) * 1000).roundToInt()
                    setProgress(
                        1000,
                        progress,
                        false
                    )
                } else {
                    setProgress(100, 0, true)
                }
            } else if (status.isInProgress()) {
                setProgress(100, 0, true)
            }
        }
        .build()

    /**
     * This method allows for creating a unique enough notification ID
     */
    private fun getNotificationId(a: Double, b: Double): Int {
        var result = 17.00
        result = 31 * result + a
        result = 31 * result + b
        return result.roundToInt()
    }

    private fun createChannel() {
        val name = applicationContext.getString(R.string.download_notification_channel_id)

        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val mChannel = NotificationChannel(name, "Download Channel", importance)
        val notificationManager =
            applicationContext.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(mChannel)
    }

    companion object {
        const val KEY_INPUT_DOWNLOAD_ID = "KEY_INPUT_DOWNLOAD_ID"
        const val WORKER_NAME = "media_download_worker"
    }
}