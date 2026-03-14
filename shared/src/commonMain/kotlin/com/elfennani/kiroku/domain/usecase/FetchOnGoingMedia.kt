package com.elfennani.kiroku.domain.usecase

import com.elfennani.kiroku.domain.model.Media
import com.elfennani.kiroku.domain.model.Resource
import com.elfennani.kiroku.domain.repository.MediaRepository

class FetchOnGoingMedia(private val mediaRepository: MediaRepository) {
    suspend operator fun invoke(): Resource<List<Media>> {
        return mediaRepository.fetchOngoingMedia()
    }
}