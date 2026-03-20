package com.elfennani.kiroku.domain.model

import com.elfennani.kiroku.domain.model.DownloadStatus.Cancelled
import com.elfennani.kiroku.domain.model.DownloadStatus.Complete
import com.elfennani.kiroku.domain.model.DownloadStatus.Downloading
import com.elfennani.kiroku.domain.model.DownloadStatus.Progress
import kotlin.math.roundToInt
import kotlin.text.toFloat

sealed class DownloadStatus {
    object Idle : DownloadStatus()
    object Downloading : DownloadStatus()
    data class Progress(val progress: Long?, val total: Long?) : DownloadStatus()
    data class Complete(val path: String) : DownloadStatus()
    data class Error(val message: String) : DownloadStatus()
    data object Cancelled : DownloadStatus()
}

fun DownloadStatus.label() = when (this) {
    is Downloading -> "Downloading..."
    is Progress -> {
        if (progress != null && total != null) {
            val percentage = ((progress.toFloat() / total) * 100).roundToInt().toString()
            "Downloading... $percentage%"
        } else {
            "Downloading..."
        }
    }

    is Complete -> "Downloaded"
    is DownloadStatus.Error -> "Error: $message"
    is Cancelled -> "Cancelled"
    else -> "Fetching Data..."
}

fun DownloadStatus.isInProgress(): Boolean {
    return this is DownloadStatus.Progress || this is DownloadStatus.Downloading || this is DownloadStatus.Idle
}