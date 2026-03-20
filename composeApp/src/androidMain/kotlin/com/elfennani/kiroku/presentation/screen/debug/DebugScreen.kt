package com.elfennani.kiroku.presentation.screen.debug

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.painterResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.elfennani.kiroku.R
import com.elfennani.kiroku.presentation.screen.episode.component.Setting
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DebugScreen(
    onBack: () -> Unit
) {
    val viewModel = koinViewModel<DebugViewModel>()
    val state by viewModel.state.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Debug Menu") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(painterResource(R.drawable.baseline_arrow_back_24), null)
                    }
                }
            )
        }
    ) {
        LazyColumn(
            contentPadding = it
        ) {
            if (state != null)
                item {
                    Text("Download Errors")
                    Text(state!!)
                    HorizontalDivider()
                }
            item {
                Setting(
                    title = { Text("Delete Downloads") },
                    description = { Text("Delete all downloads") },
                    onClick = { viewModel.deleteDownloads() }
                )
                Setting(
                    title = { Text("Delete Downloads Folder") },
                    description = { Text("Delete `downloads` folder in app files dir") },
                    onClick = { viewModel.clearDownloadsFolder() }
                )
                Setting(
                    title = { Text("Display Download Errors") },
                    onClick = { viewModel.loadDownloadErrors() }
                )
            }
        }
    }
}