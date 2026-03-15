package com.elfennani.kiroku.domain.usecase

import com.elfennani.kiroku.domain.model.MatchStatus
import com.elfennani.kiroku.domain.model.Resource
import com.elfennani.kiroku.domain.model.isEmpty
import com.elfennani.kiroku.domain.repository.MediaRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map

class GetMediaItems(private val mediaRepository: MediaRepository) {
    @OptIn(ExperimentalCoroutinesApi::class)
    operator fun invoke(mediaId: Int, sourceName: String): Flow<MatchStatus> {
        return mediaRepository.getIsMediaMatched(mediaId, sourceName)
            .distinctUntilChanged()
            .flatMapLatest { isMatched ->
                if (!isMatched) {
                    flow<MatchStatus> {
                        try {
                            emit(MatchStatus.Loading)
                            mediaRepository.autoMatchMedia(mediaId, sourceName)
                        } catch (e: Exception) {
                            print("Error matching media: ${e.message}")
                            emit(MatchStatus.Unmatched)
                        }
                    }
                } else {
                    flow {
                        val dataFlow = mediaRepository.getMediaItemsFlow(mediaId, sourceName)
                        val cached = dataFlow.first()

                        if (cached.isEmpty()) {
                            emit(MatchStatus.Loading)
                        } else {
                            emit(MatchStatus.Matched(cached))
                        }

                        try {
                            mediaRepository.fetchMediaItems(mediaId, sourceName)
                        } catch (e: Throwable) {
                            print("Error fetching media items: ${e.message}")

                            if (cached.isEmpty()) {
                                emit(MatchStatus.Error(e.message ?: "Unexpected Error"))
                                return@flow
                            }
                        }

                        emitAll(dataFlow.map { MatchStatus.Matched(it) })
                    }
                }
            }
    }
}