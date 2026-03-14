package com.elfennani.kiroku.presentation.screen.home

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation3.runtime.NavKey
import coil3.compose.AsyncImage
import com.elfennani.kiroku.R
import com.elfennani.kiroku.domain.model.Media
import com.elfennani.kiroku.domain.model.MediaType
import com.elfennani.kiroku.domain.model.Result
import com.elfennani.kiroku.domain.model.label
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
                    .padding(top = 64.dp, bottom = 16.dp)
                    .padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Kiroku", style = MaterialTheme.typography.headlineMedium)
                IconButton(onClick = {}) {
                    Icon(
                        modifier = Modifier.size(32.dp),
                        painter = painterResource(R.drawable.outline_search_24),
                        contentDescription = null
                    )
                }
            }
        }
    ) {
        LazyColumn(contentPadding = it) {
            item {
                Text(
                    "Anime",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.outlineVariant,
                    modifier = Modifier.padding(horizontal = 24.dp)
                )
                Spacer(Modifier.height(8.dp))
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.padding(horizontal = 24.dp)
                )
            }
            items(state.media.filter { it.type == MediaType.ANIME }) { media ->
                MediaItemView(media)
            }

            item {
                Spacer(Modifier.height(24.dp))
                Text(
                    "Manga",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.outlineVariant,
                    modifier = Modifier.padding(horizontal = 24.dp)
                )
                Spacer(Modifier.height(8.dp))
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.padding(horizontal = 24.dp)
                )
            }
            items(state.media.filter { it.type == MediaType.MANGA }) { media ->
                MediaItemView(media)
            }
        }
    }
}

@Composable
private fun MediaItemView(media: Media) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .clickable {}
            .padding(vertical = 12.dp, horizontal = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        AsyncImage(
            modifier = Modifier
                .width(72.dp)
                .aspectRatio(0.66f)
                .background(MaterialTheme.colorScheme.outline),
            model = media.cover,
            contentDescription = media.title,
            contentScale = ContentScale.Crop,
            onError = {
                Log.d("HomeScreen", it.result.throwable.message ?: it.result.toString())
            }
        )
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .padding(vertical = 8.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column() {
                if (media.status != null)
                    Text(
                        text = media.status!!.label(media.type).uppercase(),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.outlineVariant
                    )
                Text(
                    media.title,
                    style = MaterialTheme.typography.titleLarge,
                    maxLines = 1
                )
            }
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Text(
                        text = "Progress",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outlineVariant
                    )

                    Text(
                        text = buildAnnotatedString {
                            withStyle(
                                style = MaterialTheme.typography.labelLarge
                                    .toSpanStyle()
                                    .copy(color = MaterialTheme.colorScheme.onBackground)
                            ) {
                                append(media.progress?.toString() ?: "N/A")
                            }
                            withStyle(
                                style = MaterialTheme.typography.labelSmall
                                    .toSpanStyle()
                                    .copy(color = MaterialTheme.colorScheme.outlineVariant)
                            ) {
                                if (media.total != null)
                                    append("/${media.total}")
                                else if (media.type == MediaType.MANGA)
                                    append(" Chaps.")
                                else
                                    append(" Eps.")
                            }
                        },
                        style = MaterialTheme.typography.labelSmall,
                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(2.dp)
                        .background(MaterialTheme.colorScheme.secondary),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(
                                if (media.progress != null && media.total != null)
                                    media.progress!!.toFloat() / media.total!!
                                else
                                    1f
                            )
                            .blur(2.dp, edgeTreatment = BlurredEdgeTreatment.Unbounded)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(2.dp)
                                .background(MaterialTheme.colorScheme.primary)
                        )
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(
                                if (media.progress != null && media.total != null)
                                    media.progress!!.toFloat() / media.total!!
                                else
                                    1f
                            )
                            .height(2.dp)
                            .background(MaterialTheme.colorScheme.primary)
                    )
                }
            }
        }
    }
}

operator fun PaddingValues.plus(other: PaddingValues): PaddingValues {
    return PaddingValues(
        top = this.calculateTopPadding() + other.calculateTopPadding(),
        start = this.calculateStartPadding(LayoutDirection.Ltr) + other.calculateStartPadding(
            LayoutDirection.Ltr
        ),
        end = this.calculateEndPadding(LayoutDirection.Ltr) + other.calculateEndPadding(
            LayoutDirection.Ltr
        ),
        bottom = this.calculateBottomPadding() + other.calculateBottomPadding()
    )
}