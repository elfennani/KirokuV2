package com.elfennani.kiroku.data.local.entity


import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.elfennani.kiroku.domain.model.Download
import com.elfennani.kiroku.domain.model.DownloadResource
import com.elfennani.kiroku.domain.model.DownloadStatus
import com.elfennani.kiroku.domain.model.MediaType
import com.elfennani.kiroku.domain.model.Resource
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
@Entity(tableName = "downloads")
data class DownloadEntity(
    @PrimaryKey
    val id: String = Uuid.random().toString(),
    val mediaId: Int,
    /** Number of episode/chapter */
    val number: Double,
    val resources: List<DownloadResource>,
    /**
     * Title of the download, this is what is displayed in
     * the notification and downloads screen
     */
    val title: String,

    val progress: Long?,
    val total: Long?,

    val status: String,
    val path: String?,
    val errorMessage: String?,
)

fun DownloadEntity.asDownloadStatus(): DownloadStatus {
    return when (status) {
        "Complete" -> DownloadStatus.Complete(path = path!!)
        "Downloading" -> DownloadStatus.Downloading
        "Cancelled" -> DownloadStatus.Cancelled
        "Error" -> DownloadStatus.Error(message = errorMessage!!)
        "Idle" -> DownloadStatus.Idle
        "Progress" -> DownloadStatus.Progress(progress = progress, total = total)
        else -> throw Exception("Unknown download status: $status")
    }
}

fun DownloadStatus.asEntityStatus(): String = when (this) {
    is DownloadStatus.Complete -> "Complete"
    is DownloadStatus.Downloading -> "Downloading"
    is DownloadStatus.Error -> "Error"
    is DownloadStatus.Idle -> "Idle"
    is DownloadStatus.Progress -> "Progress"
    is DownloadStatus.Cancelled -> "Cancelled"
}

@OptIn(ExperimentalUuidApi::class)
fun DownloadEntity.asDomain() = Download(
    id = Uuid.parse(id),
    mediaId = mediaId,
    title = title,
    resources = resources,
    status = this.asDownloadStatus(),
    number = number
)

@OptIn(ExperimentalUuidApi::class)
fun Download.asEntity() = DownloadEntity(
    id = id.toString(),
    mediaId = mediaId,
    resources = resources,
    title = title,
    progress = (status as? DownloadStatus.Progress)?.progress,
    total = (status as? DownloadStatus.Progress)?.total,
    status = status.asEntityStatus(),
    path = (status as? DownloadStatus.Complete)?.path,
    errorMessage = (status as? DownloadStatus.Error)?.message,
    number = number,
)