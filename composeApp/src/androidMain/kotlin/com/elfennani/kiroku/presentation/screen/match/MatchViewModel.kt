package com.elfennani.kiroku.presentation.screen.match

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.elfennani.kiroku.domain.model.Resource
import com.elfennani.kiroku.domain.model.resourceOf
import com.elfennani.kiroku.domain.repository.DownloadRepository
import com.elfennani.kiroku.domain.repository.MediaRepository
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.math.max
import kotlin.time.Duration
import kotlin.time.Instant

@OptIn(FlowPreview::class)
class MatchViewModel(
    private val route: MatchRoute,
    private val mediaRepository: MediaRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(MatchUiState(query = route.title, mediaId = route.mediaId))
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            _state.map { Pair(it.query, it.page) }
                .distinctUntilChanged()
                .debounce(300L)
                .collect { (query, page) ->
                    loadPage(query, page)
                }
        }
    }

    private suspend fun loadPage(query: String, page: Int) {
        val animeSource =
            mediaRepository.animeSources.firstOrNull { it.name == route.sourceName }
        val mangaSource =
            mediaRepository.mangaSources.firstOrNull { it.name == route.sourceName }

        _state.update { it.copy(isLoading = true, error = null) }

        val media = resourceOf {
            animeSource?.search(query, page) ?: mangaSource?.search(
                query,
                page
            ) ?: throw Exception("Source not found!")
        }

        when (media) {
            is Resource.Error -> _state.update {
                it.copy(
                    isLoading = false,
                    error = media.message
                )
            }

            is Resource.Success -> _state.update {
                it.copy(
                    isLoading = false,
                    items = media.data
                )
            }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            loadPage(state.value.query, state.value.page)
        }
    }

    fun match(id: String) {
        viewModelScope.launch {
            mediaRepository.matchMedia(route.mediaId, route.sourceName, id)
        }
    }

    fun nextPage() {
        _state.update { it.copy(page = it.page + 1) }
    }

    fun previousPage() {
        _state.update { it.copy(page = max(it.page - 1, 0)) }
    }

    fun setQuery(query: String) {
        _state.update { it.copy(query = query) }
    }
}