package com.elfennani.kiroku.data.repository

import com.apollographql.apollo.ApolloClient
import com.elfennani.shared.anilist.GetViewerQuery
import com.elfennani.kiroku.data.local.dao.SessionDao
import com.elfennani.kiroku.data.local.dao.UserDao
import com.elfennani.kiroku.data.local.mappers.toDomainModel
import com.elfennani.kiroku.data.local.mappers.toEntity
import com.elfennani.kiroku.domain.model.Result
import com.elfennani.kiroku.domain.model.User
import com.elfennani.kiroku.domain.repository.UserRepository
import com.elfennani.kiroku.domain.util.GlobalErrorHandler
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.take

class UserRepositoryImpl(
    private val aniListClient: ApolloClient,
    private val userDao: UserDao,
    private val sessionDao: SessionDao,
) : UserRepository {
    private suspend fun fetchViewerData(): User {
        val data = aniListClient.query(GetViewerQuery()).execute().data
            ?: throw Exception("Failed to fetch viewer")
        val viewer = data.Viewer ?: throw Exception("Viewer is null")

        return User(
            id = viewer.id,
            name = viewer.name,
            icon = viewer.avatar?.medium,
            avatar = viewer.avatar?.large
        )
    }

    override suspend fun getViewer(): Flow<Result<User>> = flow {
        emit(Result.Loading)

        // Emit the cached user if available
        val cachedSession = userDao.getCurrentSessionUser()
        emitAll(
            cachedSession
                .take(1)
                .mapNotNull { it.toDomainModel() }
                .map { Result.Success(it) }
        )

        try {
            val viewer = fetchViewerData()
            userDao.upsertUser(viewer.toEntity())
            sessionDao.updateSessionUserId(cachedSession.first().session.id, viewer.id)
        } catch (e: Exception) {
            e.printStackTrace()
            GlobalErrorHandler.emitError(e)
        }

        emitAll(
            userDao.getCurrentSessionUser().map {
                it.user?.toDomainModel()
                    ?.let { user -> Result.Success(user) }
                    ?: Result.Loading
            }
        )
    }

    override suspend fun fetchViewerByAccessToken(accessToken: String): User {
        val apolloClientWithAuth = aniListClient.newBuilder()
            .interceptors(emptyList())
            .addHttpHeader("Authorization", "Bearer $accessToken")
            .build()
        val data = apolloClientWithAuth.query(GetViewerQuery()).execute().data
            ?: throw Exception("Failed to fetch viewer")
        val viewer = data.Viewer ?: throw Exception("Viewer is null")

        return User(
            id = viewer.id,
            name = viewer.name,
            icon = viewer.avatar?.medium,
            avatar = viewer.avatar?.large
        )
    }

    override suspend fun getUserById(id: Int): User {
        TODO("Not yet implemented")
    }
}