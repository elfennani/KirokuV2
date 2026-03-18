package com.elfennani.kiroku.presentation.screen.episode

import android.annotation.SuppressLint
import android.app.Application
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.elfennani.kiroku.domain.model.MediaItemList
import com.elfennani.kiroku.domain.model.Result
import com.elfennani.kiroku.domain.model.VideoAudio
import com.elfennani.kiroku.domain.model.VideoType
import com.elfennani.kiroku.domain.model.resourceOf
import com.elfennani.kiroku.domain.model.toResult
import com.elfennani.kiroku.domain.repository.MediaRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private const val TAG = "EpisodeViewModel"

@OptIn(ExperimentalCoroutinesApi::class)
@SuppressLint("StaticFieldLeak")
class EpisodeViewModel(
    private val initialRoute: EpisodeRoute,
    private val mediaRepository: MediaRepository,
    private val context: Application
) : ViewModel() {
    private val route = MutableStateFlow(initialRoute)
    private val _uiState = MutableStateFlow(
        EpisodeUiState(
            sourceName = initialRoute.sourceName,
            episodeNumber = initialRoute.episodeNumber
        )
    )
    val state = combine(route, _uiState) { route, state ->
        state.copy(
            sourceName = route.sourceName,
            episodeNumber = route.episodeNumber
        )
    }.stateIn(
        viewModelScope, SharingStarted.Eagerly, EpisodeUiState(
            sourceName = initialRoute.sourceName,
            episodeNumber = initialRoute.episodeNumber
        )
    )
    private val listener = object : Player.Listener {

    }

    val exoPlayer = ExoPlayer.Builder(context)
        .build().apply {
            addListener(listener)
        }


    init {
        // Load media
        viewModelScope.launch {
            mediaRepository.getMediaFlow(initialRoute.mediaId).collect { media ->
                _uiState.update { it.copy(media = media) }
            }
        }

        // Load Episode
        viewModelScope.launch {
            route.flatMapLatest { route ->
                val sourceName = route.sourceName
                if (sourceName != null) {
                    mediaRepository.getMediaItemsFlow(
                        mediaId = route.mediaId,
                        source = sourceName
                    ).map { route to it }
                } else flowOf(route to null)
            }.collect { (route, items) ->
                _uiState.update { state ->
                    state.copy(
                        episode = (items as? MediaItemList.EpisodeList)
                            ?.episodes
                            ?.find { ep -> ep.number == route.episodeNumber },
                        availableEpisodes = (items as? MediaItemList.EpisodeList)
                            ?.episodes
                            ?.map { it.number } ?: emptyList()
                    )
                }
            }
        }

        // Loading Sources
        viewModelScope.launch {
            route.flatMapLatest { route ->
                val episodeNumber = route.episodeNumber
                val sourceName = route.sourceName

                flow {
                    emit(Result.Loading)
                    val result = resourceOf {
                        mediaRepository
                            .fetchEpisodeSources(
                                mediaId = route.mediaId,
                                sourceName = sourceName
                                    ?: mediaRepository.animeSources.first().name,
                                episodeNumber = episodeNumber,
                            )
                    }.toResult()
                    emit(result)
                }
            }.collect { sources ->
                when (sources) {
                    Result.Loading -> {
                        _uiState.update {
                            it.copy(
                                isLoading = true,
                                sources = emptyList(),
                                selectedSource = null
                            )
                        }
                    }

                    is Result.Error -> {
                        Log.d(TAG, "Failed to fetch sources: ${sources.message}")
                    }

                    is Result.Success -> {
                        val source = sources.data.maxByOrNull {
                            var num = 0
                            if (it.audio == VideoAudio.DUBBED)
                                num++
                            if (it.type == VideoType.HLS)
                                num++

                            num
                        }

                        _uiState.update { state ->
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
            _uiState.map { it.selectedSource }.distinctUntilChanged().collect { source ->
                if (source != null) {
                    Log.d(TAG, "SOURCE_SETTING: $source")
                    exoPlayer.setMediaItem(MediaItem.fromUri(source.url))
                    exoPlayer.playWhenReady = true
                    exoPlayer.prepare()
                }
            }
        }
    }

    fun nextEpisode(){
        val episodeNumber = state.value.availableEpisodes.sorted()
            .firstOrNull { it > state.value.episodeNumber }

        if (episodeNumber != null) {
            exoPlayer.removeMediaItem(0)
            route.update { it.copy(episodeNumber = episodeNumber) }
        }
    }

    override fun onCleared() {
        super.onCleared()
        exoPlayer.release()
    }
}