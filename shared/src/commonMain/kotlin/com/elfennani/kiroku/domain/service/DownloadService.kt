package com.elfennani.kiroku.domain.service

import com.elfennani.kiroku.domain.model.Download

interface DownloadService {
    suspend fun enqueueDownload(download: Download)
    suspend fun cancelDownload(download: Download)
}