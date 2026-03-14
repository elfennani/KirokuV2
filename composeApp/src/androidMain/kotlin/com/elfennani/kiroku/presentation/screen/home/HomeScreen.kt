package com.elfennani.kiroku.presentation.screen.home

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavKey
import coil3.compose.AsyncImage
import com.elfennani.kiroku.domain.model.Result
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun HomeScreen(backstack: List<NavKey>) {
    val viewModel = koinViewModel<HomeViewModel>()
    val state by viewModel.state.collectAsState()

    HomeScreen(
        state = state,
    )
}

@Composable
private fun HomeScreen(
    state: HomeUiState = HomeUiState(),
) {
    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(WindowInsets.statusBars.asPaddingValues())
                    .padding(top = 24.dp, bottom = 16.dp)
                    .padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Kiroku", style = MaterialTheme.typography.headlineMedium)
            }
        }
    ) {
        LazyColumn(contentPadding = it) {
            items(state.media) { media ->
                Row() {
                    AsyncImage(
                        modifier = Modifier
                            .width(96.dp)
                            .aspectRatio(0.66f),
                        model = media.cover,
                        contentDescription = media.title,
                        onError = {
                            Log.d("HomeScreen", it.result.throwable.message ?: it.result.toString())
                        }
                    )
                    Column() {
                        Text(media.status?.name ?: "")
                        Text(media.title)
                        Text(media.description ?: "")
                    }
                }
            }
        }
    }
}