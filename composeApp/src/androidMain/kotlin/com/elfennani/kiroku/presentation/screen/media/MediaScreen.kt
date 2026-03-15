package com.elfennani.kiroku.presentation.screen.media

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavKey
import coil3.compose.AsyncImage
import com.elfennani.kiroku.R
import com.elfennani.kiroku.domain.model.MediaType
import com.elfennani.kiroku.domain.model.label
import com.elfennani.kiroku.presentation.component.MediaProgressBar
import com.elfennani.kiroku.presentation.theme.AppTheme
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun MediaScreen(
    route: MediaRoute,
    onNavigate: (NavKey) -> Unit,
    onNavigateBack: () -> Unit,
) {
    val viewModel = koinViewModel<MediaViewModel>() { parametersOf(route) }
    val state by viewModel.state.collectAsStateWithLifecycle()

    MediaScreen(
        state = state,
        onNavigateBack = onNavigateBack
    )
}

@Composable
private fun MediaScreen(
    state: MediaUiState = MediaUiState(),
    onNavigateBack: () -> Unit = {},
) {
    Scaffold() {
        if (state.isLoading && state.media == null && state.error == null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(it),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // TODO: Add Skeleton Loading elements
                CircularProgressIndicator()
            }
        } else if (state.error != null && state.media == null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(it),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(painterResource(R.drawable.outline_error_24), "Error")
                Spacer(Modifier.height(16.dp))
                Text(
                    "Error fetching media",
                    style = MaterialTheme.typography.titleMedium,
                    textAlign = TextAlign.Center
                )
                Text(
                    state.error,
                    style = MaterialTheme.typography.labelLarge,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        } else if (state.media != null) {
            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                Box {
                    AsyncImage(
                        model = state.media.banner,
                        contentDescription = state.media.title,
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(3f / 2),
                        contentScale = ContentScale.Crop
                    )

                    val brush = Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.background.copy(0f),
                            MaterialTheme.colorScheme.background
                        )
                    )
                    Box(
                        Modifier
                            .matchParentSize()
                            .background(brush)
                    )
                }

                LazyColumn(
                    modifier = Modifier.fillMaxSize()
                ) {
                    item {
                        Column(
                            modifier = Modifier
                                .padding(top = it.calculateTopPadding())
                                .padding(top = 72.dp)
                                .padding(horizontal = 24.dp),
                            verticalArrangement = Arrangement.spacedBy(24.dp)
                        ) {
                            Row() {
                                IconButton(
                                    modifier = Modifier.offset(x = (-12.dp)),
                                    onClick = { onNavigateBack() }
                                ) {
                                    Icon(
                                        modifier = Modifier.size(32.dp),
                                        painter = painterResource(id = R.drawable.baseline_arrow_back_24),
                                        contentDescription = null
                                    )
                                }
                            }

                            AsyncImage(
                                model = state.media.cover,
                                contentDescription = state.media.title,
                                modifier = Modifier
                                    .width(128.dp)
                                    .aspectRatio(0.66f),
                                contentScale = ContentScale.Crop
                            )

                            Column() {
                                Text(
                                    text = state.media.type.name.uppercase(),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.outlineVariant
                                )
                                Text(
                                    state.media.title,
                                    style = MaterialTheme.typography.headlineSmall,
                                    maxLines = 2
                                )
                            }

                            Row(
                                modifier = Modifier.height(IntrinsicSize.Min),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                if (state.media.status != null) {
                                    MediaProgressBar(
                                        modifier = Modifier.weight(1f),
                                        media = state.media,
                                        label = {
                                            Text(
                                                state.media.status!!.label(state.media.type),
                                                style = MaterialTheme.typography.labelMedium
                                            )
                                        }
                                    )
                                    Spacer(Modifier)
                                    VerticalDivider(
                                        modifier = Modifier
                                            .fillMaxHeight()
                                            .padding(vertical = 12.dp),
                                        color = MaterialTheme.colorScheme.outlineVariant
                                    )
                                }
                                TextButton(onClick = {}) {
                                    Text("Edit")
                                }
                                VerticalDivider(
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .padding(vertical = 12.dp),
                                    color = MaterialTheme.colorScheme.outlineVariant
                                )
                                TextButton(onClick = {}) {
                                    Text("+1")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview
@Composable
private fun MediaScreenPrev() {
    AppTheme() {
        MediaScreen(
            state = MediaUiState(
                error = "Failed to load media"
            )
        )
    }
}