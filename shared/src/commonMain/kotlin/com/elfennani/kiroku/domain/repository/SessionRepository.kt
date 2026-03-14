package com.elfennani.kiroku.domain.repository

import com.elfennani.kiroku.domain.model.Session

interface SessionRepository {
    suspend fun saveSession(session: Session)
    suspend fun getSession(): Session?
    suspend fun clearSession()
}