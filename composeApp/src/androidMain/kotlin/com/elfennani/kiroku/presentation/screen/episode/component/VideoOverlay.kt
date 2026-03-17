package com.elfennani.kiroku.presentation.screen.episode.component

import android.annotation.SuppressLint
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.OptIn
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import com.elfennani.kiroku.R
import com.elfennani.kiroku.presentation.theme.AppTheme
import com.elfennani.kiroku.presentation.theme.shade4
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


@SuppressLint("DefaultLocale")
private fun Long?.toReadable(forceHours: Boolean = false): String {
    if (this == null || this < 0) {
        return if (forceHours) "--:--:--" else "--:--"
    }

    val totalSeconds = this / 1000
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60

    return if (forceHours)
        String.format("%02d:%02d:%02d", hours, minutes, seconds)
    else
        String.format("%02d:%02d", minutes, seconds)
}


@OptIn(UnstableApi::class)
@Composable
fun VideoOverlay(
    modifier: Modifier = Modifier,
    videoState: VideoState,
    onBack: () -> Unit = {},
    title: @Composable () -> Unit = {},
    subtitle: @Composable () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {},
    content: @Composable BoxScope.() -> Unit,
) {
    val container = LocalWindowInfo.current.containerSize
    val view = LocalView.current
    val density = LocalDensity.current
    val width = with(density) { ((container.height * 16) / 9).toDp() }
    var visible by remember { mutableStateOf(true) }
    var lastInteractionTime by remember { mutableStateOf(System.currentTimeMillis()) }

    LaunchedEffect(lastInteractionTime) {
        delay(3000)
        if (System.currentTimeMillis() - lastInteractionTime >= 3000 && visible && videoState.isPlaying) {
            visible = false
        }
    }


    Box(
        modifier = modifier
            .clickable(null, null) {
                visible = !visible
                lastInteractionTime = System.currentTimeMillis()
            }
    ) {
        content()

        AnimatedVisibility(
            modifier = Modifier.matchParentSize(),
            visible = visible,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(0.33f)),
            )
        }

        CompositionLocalProvider(LocalContentColor provides shade4) {
            AnimatedVisibility(
                modifier = Modifier
                    .widthIn(max = width)
                    .align(Alignment.TopCenter),
                visible = visible,
                enter = fadeIn() + slideInVertically(),
                exit = fadeOut() + slideOutVertically()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 22.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = onBack) {
                            Icon(painterResource(R.drawable.baseline_arrow_back_24), "Back")
                        }

                        Row(
                            modifier = Modifier
                                .weight(1f)
                                .widthIn(max = width / 2)
                                .padding(4.dp)
                                .clickable {}
                        ) {
                            Column {
                                CompositionLocalProvider(
                                    LocalTextStyle provides MaterialTheme.typography.labelLarge
                                ) {
                                    title()
                                }
                                CompositionLocalProvider(
                                    LocalTextStyle provides MaterialTheme.typography.bodySmall,
                                    LocalContentColor provides shade4.copy(alpha = 0.85f)
                                ) {
                                    subtitle()
                                }
                            }
                        }
                    }

                    actions()
                }
            }

            AnimatedVisibility(
                modifier = Modifier
                    .align(Alignment.Center),
                visible = visible,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Row(
                    modifier = Modifier
                        .align(Alignment.Center),
                    horizontalArrangement = Arrangement.spacedBy(32.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { videoState.seekBackwards() }) {
                        Icon(painterResource(R.drawable.sharp_replay_10_24), "Rewind 10 seconds")
                    }
                    IconButton(
                        modifier = Modifier.size(72.dp),
                        onClick = { videoState.togglePlayback() }
                    ) {
                        Icon(
                            modifier = Modifier.size(56.dp),
                            painter = painterResource(
                                id = if (!videoState.isPlaying)
                                    R.drawable.baseline_play_arrow_24
                                else R.drawable.baseline_pause_24
                            ),
                            contentDescription = "Play/Pause"
                        )
                    }
                    IconButton(onClick = { videoState.seekForward() }) {
                        Icon(painterResource(R.drawable.sharp_forward_10_24), "Forward 10 seconds")
                    }
                }
            }

            AnimatedVisibility(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .widthIn(max = width),
                visible = visible,
                enter = fadeIn() + slideInVertically(initialOffsetY = { it / 2 }),
                exit = fadeOut() + slideOutVertically(targetOffsetY = { it / 2 })
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            CompositionLocalProvider(
                                LocalTextStyle provides MaterialTheme.typography.bodySmall,
                                LocalContentColor provides shade4.copy(alpha = 0.85f)
                            ) {
                                Text(videoState.currentPosition.toReadable())
                                Text(videoState.duration.toReadable())
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(UnstableApi::class)
@Preview(
    heightDp = 412,
    widthDp = 915,
)
@Composable
private fun VideoOverlayPreview() {
    val context = LocalContext.current;
    val exoPlayer = remember {
        ExoPlayer
            .Builder(context)
            .build()
            .apply {
                playWhenReady = false
                addMediaItem(MediaItem.fromUri("https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4"))
                prepare()
                seekTo(2_000)
            }
    }
    val state = rememberVideoState(exoPlayer)

    AppTheme {
        VideoOverlay(
            modifier = Modifier.fillMaxSize(),
            videoState = state,
            title = { Text("Episode XX - Title") },
            subtitle = { Text("Show title") },
            actions = {
                IconButton(onClick = {}) {
                    Icon(painterResource(R.drawable.sharp_video_settings_24), null)
                }
                IconButton(onClick = {}) {
                    Icon(painterResource(R.drawable.outline_download_24), null)
                }
            }
        ) {
            AndroidView(
                factory = { ctx ->
                    PlayerView(ctx).apply {
                        player = exoPlayer
                        resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                        layoutParams = FrameLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT,
                        )
                        useController = false
                    }
                },
                modifier = Modifier
                    .background(Color.Black)
                    .fillMaxSize()
            )
        }
    }
}