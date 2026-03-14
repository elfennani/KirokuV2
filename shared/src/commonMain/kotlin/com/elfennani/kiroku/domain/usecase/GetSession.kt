package com.elfennani.kiroku.domain.usecase

import com.elfennani.kiroku.domain.repository.SessionRepository

class GetSession(private val sessionRepository: SessionRepository) {
    suspend operator fun invoke() = sessionRepository.getSession()
}