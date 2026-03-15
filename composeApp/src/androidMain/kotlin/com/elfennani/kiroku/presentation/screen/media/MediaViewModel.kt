package com.elfennani.kiroku.presentation.screen.media

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.elfennani.kiroku.domain.model.Resource
import com.elfennani.kiroku.domain.repository.MediaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MediaViewModel(
    private val route: MediaRoute,
    private val mediaRepository: MediaRepository
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
    }
}