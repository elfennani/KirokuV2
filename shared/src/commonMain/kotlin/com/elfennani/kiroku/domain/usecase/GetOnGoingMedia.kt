package com.elfennani.kiroku.domain.usecase

import com.elfennani.kiroku.domain.model.Media
import com.elfennani.kiroku.domain.repository.MediaRepository
import kotlinx.coroutines.flow.Flow

class GetOnGoingMedia(private val mediaRepository: MediaRepository) {
    operator fun invoke(): Flow<List<Media>> {
        return mediaRepository.getOngoingMediaFlow()
    }
}