package com.elfennani.kiroku.presentation.screen.episode.component

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class VideoState(
    private val player: Player,
    private val scope: CoroutineScope,
) {
    var isPlaying: Boolean by mutableStateOf(false)
        private set
    var isBuffering: Boolean by mutableStateOf(false)
        private set
    var hasEnded: Boolean by mutableStateOf(false)
        private set

    var duration: Long? by mutableStateOf(null)
        private set
    var currentPosition: Long? by mutableStateOf(null)
        private set

    private var job: Job? = null
    private var listener = object : Player.Listener {
        override fun onIsPlayingChanged(isPlaying: Boolean) {
            super.onIsPlayingChanged(isPlaying)

            this@VideoState.isPlaying = isPlaying
        }

        override fun onPlaybackStateChanged(playbackState: Int) {
            super.onPlaybackStateChanged(playbackState)

            when (playbackState) {
                Player.STATE_BUFFERING -> isBuffering = true
                Player.STATE_READY -> isBuffering = false
                Player.STATE_ENDED -> hasEnded = true
                else -> {}
            }
        }
    }

    init {
        player.addListener(listener)
        startLooper()
    }

    private fun startLooper() {
        duration = player.duration
        currentPosition = player.currentPosition
        isPlaying = player.isPlaying
        isBuffering = player.isLoading
        hasEnded = player.playbackState == Player.STATE_ENDED


        job = scope.launch {
            while (isActive) {
                currentPosition = player.currentPosition
                duration = player.duration
                isPlaying = player.isPlaying
                isBuffering = player.isLoading
                hasEnded = player.playbackState == Player.STATE_ENDED
                delay(1000L)
            }
        }
    }

    fun seekForward() {
        player.seekTo(player.currentPosition + 10000)
    }

    fun seekBackwards() {
        player.seekTo(player.currentPosition - 10000)
    }

    fun togglePlayback() {
        if (player.isPlaying) {
            player.pause()
        } else if (!player.isPlaying || player.playbackState == Player.STATE_ENDED) {
            player.play()
        }
    }

    fun seekTo(positionMs: Long) {
        player.seekTo(positionMs)
    }

    fun onDispose() {
        // Since the player is not initialized here, then we are responsible in disposing of it.
        job?.cancel()
        player.removeListener(listener)
    }
}

@Composable
fun rememberVideoState(
    player: Player,
): VideoState {
    val scope = rememberCoroutineScope()
    val videoState = remember(key1 = player) {
        VideoState(player, scope)
    }

    DisposableEffect(Unit) {
        onDispose(videoState::onDispose)
    }

    return videoState;
}