package com.elfennani.kiroku.presentation.screen.match

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import coil3.network.NetworkHeaders
import coil3.network.httpHeaders
import coil3.request.ImageRequest
import com.elfennani.kiroku.R
import com.elfennani.kiroku.presentation.theme.AppTheme
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun MatchScreen(
    route: MatchRoute,
    onNavigateBack: () -> Unit
) {
    val viewModel = koinViewModel<MatchViewModel>() { parametersOf(route) }
    val state by viewModel.state.collectAsStateWithLifecycle()

    MatchScreen(
        state = state,
        onMatch = {
            viewModel.match(it)
            onNavigateBack()
        },
        onNextPage = viewModel::nextPage,
        onPrevPage = viewModel::previousPage,
        onRefresh = viewModel::refresh,
        onQueryChange = viewModel::setQuery,
        onNavigateBack = onNavigateBack
    )
}

@Composable
fun MatchScreen(
    state: MatchUiState = MatchUiState(),
    onRefresh: () -> Unit = {},
    onNextPage: () -> Unit = {},
    onPrevPage: () -> Unit = {},
    onQueryChange: (String) -> Unit = {},
    onMatch: (id: String) -> Unit = {},
    onNavigateBack: () -> Unit = {},
) {
    val context = LocalContext.current
    Scaffold(
        topBar = {
            Box {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.background)
                        .padding(WindowInsets.statusBars.asPaddingValues())
                        .padding(8.dp)
                        .shadow(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onNavigateBack) {
                        Icon(painterResource(R.drawable.baseline_arrow_back_24), "Back")
                    }
                    Column() {
                        BasicTextField(
                            modifier = Modifier.fillMaxWidth(),
                            value = state.query,
                            onValueChange = onQueryChange,
                            textStyle = MaterialTheme.typography.bodyMedium.copy(
                                MaterialTheme.colorScheme.onBackground
                            ),
                            cursorBrush = Brush.verticalGradient(listOf(MaterialTheme.colorScheme.primary)),
                            decorationBox = {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 12.dp, horizontal = 16.dp),
                                ) {
                                    it()
                                    if (state.query.isEmpty()) {
                                        Text(
                                            "Search...",
                                            style = MaterialTheme.typography.bodyMedium,
                                            modifier = Modifier
                                                .matchParentSize()
                                                .alpha(0.5f)
                                        )
                                    }
                                }
                            }
                        )
                    }
                }

                if (isSystemInDarkTheme())
                    HorizontalDivider(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.BottomStart)
                    )
            }
        }
    ) {
        if (state.isLoading) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(it)
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator()
            }
        } else if (state.error != null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(it)
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(painterResource(R.drawable.outline_error_24), "Error")
                Spacer(Modifier.height(16.dp))
                Text(
                    "Failed to fetch media",
                    style = MaterialTheme.typography.titleMedium,
                    textAlign = TextAlign.Center
                )
                Text(
                    state.error,
                    style = MaterialTheme.typography.labelLarge,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.outline
                )

                TextButton(onClick = { onRefresh() }) {
                    Text("Retry")
                }
            }
        } else {
            LazyColumn(
                contentPadding = it
            ) {
                items(state.items) { item ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(IntrinsicSize.Min)
                            .clickable { onMatch(item.id) }
                            .padding(vertical = 12.dp, horizontal = 24.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        AsyncImage(
                            modifier = Modifier
                                .width(72.dp)
                                .aspectRatio(0.66f)
                                .background(MaterialTheme.colorScheme.outline),
                            model = ImageRequest
                                .Builder(context)
                                .data(item.cover)
                                .httpHeaders(
                                    NetworkHeaders.Builder()
                                        .apply {
                                            item.headers.forEach { (name, value) ->
                                                set(name, value)
                                            }
                                        }
                                        .build()
                                )
                                .build(),
                            contentDescription = item.title,
                            contentScale = ContentScale.Crop,
                        )
                        Column(
                            modifier = Modifier
                                .fillMaxHeight()
                                .padding(vertical = 8.dp),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column() {
                                if (item.aniListId != null && item.aniListId == state.mediaId)
                                    Text(
                                        text = "BEST MATCH",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                Text(
                                    item.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            Spacer(Modifier.height(8.dp))
                            item.metadata.forEach { (label, value) ->
                                Text(
                                    text = "$label: $value",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.outlineVariant
                                )
                            }
                        }
                    }
                }
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        IconButton(onClick = { onPrevPage() }) {
                            Icon(
                                painterResource(R.drawable.outline_keyboard_arrow_right_24),
                                null,
                                modifier = Modifier.rotate(180f)
                            )
                        }

                        Text("Page ${state.page}")

                        IconButton(onClick = { onNextPage() }) {
                            Icon(
                                painterResource(R.drawable.outline_keyboard_arrow_right_24),
                                null,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview
@Composable
private fun MatchScreenPrev() {
    AppTheme() {
        MatchScreen()
    }
}