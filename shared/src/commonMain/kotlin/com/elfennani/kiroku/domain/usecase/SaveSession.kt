package com.elfennani.kiroku.domain.usecase

import com.elfennani.kiroku.domain.model.Result
import com.elfennani.kiroku.domain.model.Session
import com.elfennani.kiroku.domain.repository.SessionRepository

class SaveSession(
    private val sessionRepository: SessionRepository,
) {
    suspend operator fun invoke(token: String): Result<Unit> {
        try {
            val session = Session(token, null)
            sessionRepository.saveSession(session)
            return Result.Success(Unit)
        } catch (e: Exception) {
            return Result.Error(e.message ?: "Failed to save session")
        }
    }
}