package com.elfennani.kiroku.presentation.screen.debug

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.buildAnnotatedString
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.WorkManager
import com.elfennani.kiroku.data.local.dao.DownloadDao
import com.elfennani.kiroku.data.local.entity.asDomain
import com.elfennani.kiroku.domain.model.DownloadStatus
import com.elfennani.kiroku.domain.model.label
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File

@SuppressLint("StaticFieldLeak")
class DebugViewModel(
    private val downloadDao: DownloadDao,
    private val context: Context
) : ViewModel() {
    val state = MutableStateFlow<AnnotatedString?>(null)

    fun deleteDownloads() {
        viewModelScope.launch {
            WorkManager.getInstance(context).cancelAllWork()
            downloadDao.deleteAll()
        }
    }

    fun clearDownloadsFolder() {
        val dir = File(context.filesDir, "downloads")
        dir.deleteRecursively()
    }

    fun loadDownloadErrors() {
        viewModelScope.launch {
            val downloads = downloadDao.getDownloads().map { it.asDomain() }
                .filter { it.status is DownloadStatus.Error }

            state.update {
                buildAnnotatedString {
                    downloads.forEach {
                        append(it.status.label())
                        append("\n")
                    }
                }
            }
        }
    }
}