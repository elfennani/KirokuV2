package com.elfennani.kiroku.presentation.screen.episode

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.elfennani.kiroku.domain.model.MediaItemList
import com.elfennani.kiroku.domain.model.Resource
import com.elfennani.kiroku.domain.model.VideoAudio
import com.elfennani.kiroku.domain.model.VideoType
import com.elfennani.kiroku.domain.model.resourceOf
import com.elfennani.kiroku.domain.repository.MediaRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.time.delay
import kotlin.time.Duration
import kotlin.time.DurationUnit

private const val TAG = "EpisodeViewModel"

@SuppressLint("StaticFieldLeak")
class EpisodeViewModel(
    private val route: EpisodeRoute,
    private val mediaRepository: MediaRepository,
    private val context: Context
) : ViewModel() {
    private val _state = MutableStateFlow(
        EpisodeUiState(
            sourceName = route.sourceName,
            episodeNumber = route.episodeNumber
        )
    )
    val state = _state.asStateFlow()
    val buffered = MutableStateFlow<Long>(0L)

    private val listener = object : Player.Listener {

    }
    val exoPlayer = ExoPlayer.Builder(context)
        .build().apply {
            addListener(listener)
        }


    init {
        viewModelScope.launch {
            _state.collect {
                Log.d(TAG, "State: $it")
            }
        }
        // Load media
        viewModelScope.launch {
            mediaRepository.getMediaFlow(route.mediaId).collect { media ->
                _state.update { it.copy(media = media) }
            }
        }

        // Load Episode
        viewModelScope.launch {
            _state.map { it.sourceName }.distinctUntilChanged().collect { sourceName ->
                if (sourceName != null) {
                    _state.update { state -> state.copy(episode = null) }
                    mediaRepository.getMediaItemsFlow(
                        mediaId = route.mediaId,
                        source = sourceName
                    ).collect { items ->
                        _state.update { state ->
                            state.copy(
                                episode = (items as MediaItemList.EpisodeList).episodes.find { it.number == route.episodeNumber }
                            )
                        }
                    }
                }
            }
        }

        // Loading Sources
        viewModelScope.launch {
            _state
                .map { it.sourceName to it.episodeNumber }
                .distinctUntilChanged()
                .collect { (sourceName, episodeNumber) ->
                    _state.update {
                        it.copy(
                            isLoading = true,
                            sources = emptyList(),
                            selectedSource = null
                        )
                    }
                    val sources = resourceOf {
                        mediaRepository
                            .fetchEpisodeSources(
                                mediaId = route.mediaId,
                                sourceName = sourceName
                                    ?: mediaRepository.animeSources.first().name,
                                episodeNumber = episodeNumber,
                            )
                    }
                    when (sources) {
                        is Resource.Error -> {
                            Log.d(TAG, "Failed to fetch sources: ${sources.message}")
                        }

                        is Resource.Success -> {
                            val source = sources.data.maxByOrNull {
                                var num = 0
                                if (it.audio == VideoAudio.DUBBED)
                                    num++
                                if (it.type == VideoType.HLS)
                                    num++

                                num
                            }

                            _state.update { state ->
                                state.copy(
                                    sources = sources.data,
                                    selectedSource = source,
                                    isLoading = false
                                )
                            }
                        }
                    }
                }
        }

        // Setting source to exoPlayer
        viewModelScope.launch {
            _state.map { it.selectedSource }.distinctUntilChanged().collect { source ->
                if (source != null && exoPlayer != null) {
                    Log.d(TAG, "SOURCE_SETTING: $source")
                    exoPlayer.setMediaItem(MediaItem.fromUri(source.url))
                    exoPlayer.playWhenReady = true
                    exoPlayer.prepare()
                }
            }
        }

        viewModelScope.launch {
            while (true) {
                delay(1000)
                buffered.value = exoPlayer.bufferedPosition ?: 0
            }
        }

        viewModelScope.launch {
            buffered.distinctUntilChangedBy { it }.collect {
                Log.d(TAG, "Buffered: $it")
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        exoPlayer.release()
    }
}