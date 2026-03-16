package com.elfennani.kiroku.presentation.screen.media

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.elfennani.kiroku.domain.model.MediaType
import com.elfennani.kiroku.domain.model.Resource
import com.elfennani.kiroku.domain.repository.MediaRepository
import com.elfennani.kiroku.domain.usecase.GetMediaItems
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MediaViewModel(
    private val route: MediaRoute,
    private val mediaRepository: MediaRepository,
    private val getMediaItems: GetMediaItems
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
            _state.map { it.media?.type }.distinctUntilChanged().collect { type ->

                if (type != null) {
                    val sourceName = when (type) {
                        MediaType.ANIME -> mediaRepository.animeSources.first().name
                        MediaType.MANGA -> mediaRepository.mangaSources.first().name
                    }
                    _state.update { it.copy(sourceName = sourceName) }
                    getMediaItems(
                        route.mediaId,
                        sourceName
                    ).collect { matchStatus ->
                        _state.update { it.copy(items = matchStatus) }
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
}