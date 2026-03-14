package com.elfennani.kiroku.presentation.screen.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.elfennani.kiroku.domain.model.Resource
import com.elfennani.kiroku.domain.model.Show
import com.elfennani.kiroku.domain.model.map
import com.elfennani.kiroku.domain.usecase.FetchOnGoingMedia
import com.elfennani.kiroku.domain.usecase.GetOnGoingMedia
import com.elfennani.kiroku.domain.usecase.GetViewer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

const val TAG = "HomeViewModel"


class HomeViewModel(
    private val getOnGoingMedia: GetOnGoingMedia,
    private val fetchOnGoingMedia: FetchOnGoingMedia,
) : ViewModel() {
    private val _state = MutableStateFlow<HomeUiState>(HomeUiState())

    val state = combine(_state, getOnGoingMedia()) { state, media ->
        Log.d(TAG, media.toString())
        state.copy(media = media)
    }.stateIn(viewModelScope, SharingStarted.Lazily, HomeUiState())

    init {
        viewModelScope.launch {
            when (val res = fetchOnGoingMedia()) {
                is Resource.Error -> {
                    Log.d(TAG, "Error: ${res.message}")
                    _state.update { it.copy(error = res.message) }
                }
                else -> {
                    Log.d(TAG, "Success")
                }
            }
        }
    }
}