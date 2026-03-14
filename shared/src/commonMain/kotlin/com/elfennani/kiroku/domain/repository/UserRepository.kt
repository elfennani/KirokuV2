package com.elfennani.kiroku.domain.repository

import com.elfennani.kiroku.domain.model.Result
import com.elfennani.kiroku.domain.model.User
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    suspend fun getViewer(): Flow<Result<User>>
    suspend fun fetchViewerByAccessToken(accessToken: String): User
    suspend fun getUserById(id: Int): User
}