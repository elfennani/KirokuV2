package com.elfennani.kiroku.presentation.screen.episode

import android.app.Activity
import android.content.pm.ActivityInfo
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.FrameLayout
import androidx.activity.compose.BackHandler
import androidx.annotation.OptIn
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import com.elfennani.kiroku.R
import com.elfennani.kiroku.presentation.screen.episode.component.Sidebar
import com.elfennani.kiroku.presentation.screen.episode.component.SidebarRoute
import com.elfennani.kiroku.presentation.screen.episode.component.VideoOverlay
import com.elfennani.kiroku.presentation.screen.episode.component.VideoState
import com.elfennani.kiroku.presentation.screen.episode.component.rememberVideoState
import com.elfennani.kiroku.presentation.theme.AppTheme
import com.elfennani.kiroku.presentation.theme.shade4
import com.elfennani.kiroku.utils.clean
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@OptIn(UnstableApi::class)
@Composable
fun EpisodeScreen(route: EpisodeRoute, onNavigateBack: () -> Unit) {
    val viewModel = koinViewModel<EpisodeViewModel>() { parametersOf(route) }
    val state by viewModel.state.collectAsStateWithLifecycle()
    val exoPlayer = viewModel.exoPlayer
    val videoState = rememberVideoState(exoPlayer)

    EpisodeScreen(
        state = state,
        onBack = onNavigateBack,
        videoState = videoState,
        player = {
            AndroidView(
                modifier = Modifier
                    .fillMaxSize(),
                factory = { ctx ->
                    PlayerView(ctx).apply {
                        player = exoPlayer
                        resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
                        layoutParams = FrameLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT,
                        )
                        useController = false
                    }
                },
                update = {
                    it.player = exoPlayer
                }
            )
        }
    )
}

@Composable
private fun EpisodeScreen(
    state: EpisodeUiState = EpisodeUiState(
        episodeNumber = -1.0
    ),
    videoState: VideoState,
    onBack: () -> Unit = {},
    player: @Composable BoxScope.() -> Unit = {}
) {
    val backstack = remember { mutableStateListOf<SidebarRoute>() }
    val context = LocalContext.current

    KeepScreenON()
    HideSystemBars()

    DisposableEffect(Unit) {
        val activity = context as? Activity

        if (activity != null) {
            val previous = activity.requestedOrientation
            activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_USER_LANDSCAPE

            return@DisposableEffect onDispose {
                activity.requestedOrientation = previous
            }
        }

        onDispose {}
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.Black,
        contentColor = shade4
    ) {
        Row(
            modifier = Modifier.fillMaxSize()
        ) {
            VideoOverlay(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                videoState = videoState,
                onBack = onBack,
                title = {
                    Text(
                        state.episode?.title?.let {
                            "${state.episodeNumber.clean()} - $it"
                        }
                            ?: "Episode ${state.episodeNumber.clean()}"
                    )
                },
                subtitle = { Text(state.media?.title ?: "") },
                actions = {
                    IconButton(onClick = {
                        backstack.clear()
                        backstack.add(SidebarRoute.Preferences)
                    }) {
                        Icon(painterResource(R.drawable.sharp_video_settings_24), null)
                    }
                }
            ) {
                player()
            }

            Sidebar(
                backstack = backstack,
                state = state
            )
        }
    }
}

@Preview(
    heightDp = 412,
    widthDp = 915,
)
@Composable
private fun EpisodeScreenPrev() {
    AppTheme {
        val context = LocalContext.current
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

        EpisodeScreen(
            videoState = state
        )
    }
}


@Composable
fun HideSystemBars() {
    val context = LocalContext.current

    DisposableEffect(Unit) {
        val activity = context as? Activity
        val window = activity?.window ?: return@DisposableEffect onDispose {}
        val insetsController = WindowCompat.getInsetsController(window, window.decorView)

        insetsController.apply {
            hide(WindowInsetsCompat.Type.statusBars())
            hide(WindowInsetsCompat.Type.navigationBars())
            systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }

        return@DisposableEffect onDispose {
            insetsController.apply {
                show(WindowInsetsCompat.Type.statusBars())
                show(WindowInsetsCompat.Type.navigationBars())
                systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_DEFAULT
            }
        }
    }
}

@Composable
fun KeepScreenON(modifier: Modifier = Modifier) {
    val context = LocalContext.current

    DisposableEffect(key1 = Unit) {
        val window = (context as? Activity)?.window ?: return@DisposableEffect onDispose { }
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        onDispose {
            window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }
}