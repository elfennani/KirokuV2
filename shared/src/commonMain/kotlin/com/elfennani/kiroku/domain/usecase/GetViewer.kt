package com.elfennani.kiroku.domain.usecase

import com.elfennani.kiroku.domain.repository.UserRepository

class GetViewer(private val userRepository: UserRepository) {
    suspend operator fun invoke() = userRepository.getViewer()
}