package com.elfennani.kiroku.presentation.screen.media

import android.util.Log
import androidx.compose.animation.Animatable
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.innerShadow
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.fromHtml
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavKey
import coil3.compose.AsyncImage
import com.elfennani.kiroku.R
import com.elfennani.kiroku.domain.model.Episode
import com.elfennani.kiroku.domain.model.MatchStatus
import com.elfennani.kiroku.domain.model.Media
import com.elfennani.kiroku.domain.model.MediaItemList
import com.elfennani.kiroku.domain.model.MediaStatus
import com.elfennani.kiroku.domain.model.MediaType
import com.elfennani.kiroku.domain.model.label
import com.elfennani.kiroku.presentation.component.MediaProgressBar
import com.elfennani.kiroku.presentation.theme.AppTheme
import com.elfennani.kiroku.utils.clean
import kotlinx.coroutines.launch
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

fun Float.normalize(min: Float, max: Float): Float {
    return ((this - min) / (max - min)).coerceIn(0f, 1f)
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
            var bannerHeight by remember { mutableFloatStateOf(0f) }
            var offsetY by remember { mutableFloatStateOf(0f) }
            val normalizedOffset by remember {
                derivedStateOf {
                    offsetY.normalize(0f, bannerHeight * 0.75f)
                }
            }

            LaunchedEffect(offsetY) {
                Log.d("MediaScreen", offsetY.toString())
            }

            val nestedScrollConnection = object : NestedScrollConnection {

                override fun onPostScroll(
                    consumed: Offset,
                    available: Offset,
                    source: NestedScrollSource
                ): Offset {
                    offsetY -= consumed.y

                    return super.onPostScroll(consumed, available, source)
                }
            }
            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                val density = LocalDensity.current

                Box(
                    modifier = Modifier
                        .graphicsLayer {
                            bannerHeight = size.height
                        }
                        .offset(y = with(density) { -(normalizedOffset * (bannerHeight / 2)).toDp() })
                        .alpha(1 - normalizedOffset)
                ) {
                    AsyncImage(
                        model = state.media.banner,
                        contentDescription = state.media.title,
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(3f / 2)
                            .background(MaterialTheme.colorScheme.surface)
                            .scale(normalizedOffset * 0.25f + 1),
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

                Box(
                    modifier = Modifier.nestedScroll(nestedScrollConnection)
                ) {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(
                            bottom = 96.dp + WindowInsets.navigationBars.asPaddingValues()
                                .calculateBottomPadding()
                        )
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
                                        .aspectRatio(0.66f)
                                        .background(MaterialTheme.colorScheme.surface),
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


                                if (state.media.description != null) {
                                    Column(
                                        verticalArrangement = Arrangement.spacedBy(8.dp),
                                        horizontalAlignment = Alignment.End
                                    ) {
                                        Text(
                                            "DESCRIPTION",
                                            modifier = Modifier.fillMaxWidth(),
                                            style = MaterialTheme.typography.labelMedium,
                                            color = MaterialTheme.colorScheme.outlineVariant,
                                        )
                                        Text(
                                            AnnotatedString.fromHtml(state.media.description!!),
                                            maxLines = 3,
                                            overflow = TextOverflow.Ellipsis,
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                        TextButton(
                                            modifier = Modifier.offset(x = 12.dp),
                                            onClick = {},
                                            colors = ButtonDefaults.textButtonColors(
                                                contentColor = MaterialTheme.colorScheme.onBackground
                                            )
                                        ) {
                                            Text("More About")
                                            Spacer(Modifier.width(8.dp))
                                            Icon(
                                                painterResource(R.drawable.outline_keyboard_arrow_right_24),
                                                null
                                            )
                                        }
                                    }
                                }

                                Text(
                                    when (state.media.type) {
                                        MediaType.ANIME -> "EPISODES"
                                        MediaType.MANGA -> "CHAPTERS"
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 8.dp),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.outlineVariant,
                                )
                            }
                        }

                        when (state.items) {
                            MatchStatus.Loading -> item {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(128.dp),
                                    verticalArrangement = Arrangement.Center,
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    CircularProgressIndicator()
                                }
                            }

                            MatchStatus.Unmatched -> item {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 64.dp),
                                    verticalArrangement = Arrangement.Center,
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(painterResource(R.drawable.outline_error_24), "Error")
                                    Spacer(Modifier.height(16.dp))
                                    Text(
                                        "This media is not matched, please match it now",
                                        style = MaterialTheme.typography.titleMedium,
                                        textAlign = TextAlign.Center
                                    )

                                    TextButton(onClick = {}) {
                                        Text("Match Now")
                                    }
                                }
                            }

                            is MatchStatus.Error -> item {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 64.dp),
                                    verticalArrangement = Arrangement.Center,
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(painterResource(R.drawable.outline_error_24), "Error")
                                    Spacer(Modifier.height(16.dp))
                                    Text(
                                        "Failed to fetch episodes",
                                        style = MaterialTheme.typography.titleMedium,
                                        textAlign = TextAlign.Center
                                    )
                                    Text(
                                        state.items.message,
                                        style = MaterialTheme.typography.labelLarge,
                                        textAlign = TextAlign.Center,
                                        color = MaterialTheme.colorScheme.outline
                                    )

                                    TextButton(onClick = {}) {
                                        Text("Match Now")
                                    }
                                }
                            }

                            is MatchStatus.Matched -> {
                                when (state.items.items) {
                                    is MediaItemList.ChapterList -> item { Text("TODO") }
                                    is MediaItemList.EpisodeList -> items((state.items.items as MediaItemList.EpisodeList).episodes) { episode ->
                                        EpisodeItem(
                                            episode = episode
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EpisodeItem(
    modifier: Modifier = Modifier,
    episode: Episode
) {
    val width = LocalView.current.width
    val offsetX = remember { Animatable(0f) }
    val density = LocalDensity.current
    val scope = rememberCoroutineScope()

    Box(
        modifier = modifier.height(IntrinsicSize.Min)
    ) {
        Row(
            modifier = Modifier
                .width(with(density) { offsetX.value.toDp() })
                .fillMaxHeight()
                .background(
                    MaterialTheme.colorScheme.primary.copy(
                        (offsetX.value / (width / 3)).coerceIn(0f, 1f)
                    )
                )
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
        ) {
            AnimatedVisibility(
                visible = offsetX.value > width / 3,
                enter = fadeIn() + slideInVertically(),
                exit = fadeOut() + slideOutVertically()
            ) {
                val hapticFeedback = LocalHapticFeedback.current
                LaunchedEffect(Unit) {
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.GestureThresholdActivate)
                }
                Icon(
                    painterResource(R.drawable.outline_download_24),
                    "Download",
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
                .combinedClickable(
                    onClick = {},
                    onLongClick = {}
                )
                .padding(horizontal = 24.dp, vertical = 6.dp)
                .pointerInput(Unit) {
                    detectHorizontalDragGestures(
                        onDragStart = {},
                        onDragEnd = {
                            scope.launch {
                                offsetX.animateTo(
                                    0f,
                                    animationSpec = spring(
                                        dampingRatio = Spring.DampingRatioMediumBouncy,
                                        stiffness = Spring.StiffnessMedium
                                    ),
                                )
                            }
                        },
                        onDragCancel = {
                            scope.launch {
                                offsetX.animateTo(0f)
                            }
                        },
                        onHorizontalDrag = { _, dragAmount ->
                            scope.launch {
                                offsetX.snapTo(
                                    (offsetX.value + dragAmount).coerceIn(
                                        minimumValue = 0f,
                                        maximumValue = width.toFloat() / 2
                                    )
                                )
                            }
                        }
                    )
                }
                .offset(x = with(density) { offsetX.value.toDp() }),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            AsyncImage(
                model = episode.thumbnail,
                contentDescription = episode.title,
                modifier = Modifier
                    .width(112.dp)
                    .aspectRatio(16f / 9)
                    .background(MaterialTheme.colorScheme.surface)
            )
            Column(
                modifier = Modifier.padding(vertical = 4.dp)
            ) {
                Text(
                    "Episode ${episode.number.clean()}",
                    style = MaterialTheme.typography.labelLarge
                )
                if (episode.title != null)
                    Text(
                        episode.title!!,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outlineVariant,
                        maxLines = 2
                    )
            }
        }
    }
}

@Preview
@Composable
private fun MediaScreenPrev() {
    val fateStrangeFake = Media(
        id = 166617,
        title = "Fate/strange Fake",
        description = """
        In a Holy Grail War, Mages (Masters) and their Heroic Spirits (Servants) fight for the control of the Holy Grail—an omnipotent wish-granting device said to fulfill any desire. Years have passed since the end of the Fifth Holy Grail War in Japan. Now, signs portend the emergence of a new Holy Grail in the western American city of Snowfield. Sure enough, Masters and Servants begin to gather... <br><br>

        A missing Servant class...<br>
        Impossible Servant summonings...<br>
        A nation shrouded in secrecy...<br>
        And a city created as a battleground.<br>
        <br>
        In the face of such irregularities, the Holy Grail War is twisted and driven into the depth of madness. Let the curtain rise on a masquerade of humans and heroes, made to dance upon the stage of a false Holy Grail. <i>This is a Holy Grail War covered in lies.</i>
        <br><br>
        (Source: Official Site, Aniplex USA, edited)
        <br><br>
        <i>Notes:</i><br>
        •  <i>Special premiere of Episode 1 in its English Dub occurred in Los Angeles at the Fate 20th Anniversary Showcase event and as well through Crunchyroll’s YouTube Channel on November 23, 2024 before the Japanese television premiere.</i><br>
        •  <i>The Japanese advanced premiere occurred during the "Fate New Year's Eve TV Special 2024" on December 31, 2024.</i>
    """.trimIndent(),
        progress = 10,
        total = 13,
        type = MediaType.ANIME,
        status = MediaStatus.IN_PROGRESS,
        cover = "https://s4.anilist.co/file/anilistcdn/media/anime/cover/medium/bx166617-34fpC9y47tTx.png",
        banner = "https://s4.anilist.co/file/anilistcdn/media/anime/banner/166617-P4w3p0H4lE1O.jpg"
    )
    AppTheme() {
        MediaScreen(
            state = MediaUiState(
                isLoading = false,
                media = fateStrangeFake
            )
        )
    }
}