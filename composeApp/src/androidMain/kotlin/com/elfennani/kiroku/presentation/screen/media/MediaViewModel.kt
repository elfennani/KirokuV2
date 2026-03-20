package com.elfennani.kiroku.presentation.screen.media

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.elfennani.kiroku.domain.model.Episode
import com.elfennani.kiroku.domain.model.MatchStatus
import com.elfennani.kiroku.domain.model.MediaItemList
import com.elfennani.kiroku.domain.model.MediaType
import com.elfennani.kiroku.domain.model.Resource
import com.elfennani.kiroku.domain.repository.DownloadRepository
import com.elfennani.kiroku.domain.repository.MediaRepository
import com.elfennani.kiroku.domain.usecase.GetMediaItems
import io.ktor.util.Hash.combine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MediaViewModel(
    private val route: MediaRoute,
    private val mediaRepository: MediaRepository,
    private val getMediaItems: GetMediaItems,
    private val downloadRepository: DownloadRepository
) : ViewModel() {
    private val _state = MutableStateFlow(MediaUiState())
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            mediaRepository.getMediaFlow(route.mediaId).collect { media ->
                if (media != null)
                    _state.update { it.copy(isLoading = false, media = media) }
            }
        }

        viewModelScope.launch {
            mediaRepository.fetchMedia(route.mediaId).let { res ->
                when (res) {
                    is Resource.Error -> {
                        _state.update {
                            it.copy(error = res.message)
                        }
                    }

                    else -> {}
                }
            }
        }

        viewModelScope.launch {
            combine(
                _state.map { it.media?.type }.distinctUntilChanged(),
                downloadRepository.getDownloadsByMediaId(route.mediaId)
            ) { type, downloads ->
                type to downloads
            }.flatMapLatest { (type, downloads) ->
                if (type == null) return@flatMapLatest flowOf(null to downloads)

                val sourceName = when (type) {
                    MediaType.ANIME -> mediaRepository.animeSources.first().name
                    MediaType.MANGA -> mediaRepository.mangaSources.first().name
                }
                _state.update { it.copy(sourceName = sourceName) }
                getMediaItems(
                    route.mediaId,
                    sourceName
                ).map { it to downloads }
            }.collect { (matchStatus, downloads) ->
                if (matchStatus != null) {
                    _state.update { it.copy(items = matchStatus) }
                    if (matchStatus is MatchStatus.Matched && matchStatus.items is MediaItemList.EpisodeList) {
                        _state.update { state ->
                            val episodes =
                                ((state.items as MatchStatus.Matched).items as MediaItemList.EpisodeList).episodes

                            state.copy(
                                items = MatchStatus.Matched(
                                    items = MediaItemList.EpisodeList(
                                        episodes = episodes.map { episode ->
                                            episode.copy(
                                                downloadStatus = downloads.find { download ->
                                                    download.number == episode.number
                                                }?.status
                                            )
                                        }
                                    )
                                )
                            )
                        }
                    }
                }
            }
        }
    }

    fun unmatch() {
        viewModelScope.launch {
            if (state.value.sourceName != null)
                mediaRepository.deleteMatch(
                    mediaId = route.mediaId,
                    sourceName = state.value.sourceName!!
                )
        }
    }

    fun download(episode: Episode) {
        viewModelScope.launch {
            downloadRepository.enqueueDownload(route.mediaId, episode)
        }
    }
}