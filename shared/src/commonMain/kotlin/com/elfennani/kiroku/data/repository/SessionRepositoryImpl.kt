package com.elfennani.kiroku.data.repository

import com.apollographql.apollo.ApolloClient
import com.elfennani.shared.anilist.GetViewerQuery
import com.elfennani.kiroku.data.local.dao.SessionDao
import com.elfennani.kiroku.data.local.dao.UserDao
import com.elfennani.kiroku.data.local.entity.UserEntity
import com.elfennani.kiroku.data.local.mappers.toEntity
import com.elfennani.kiroku.domain.model.Session
import com.elfennani.kiroku.domain.repository.SessionRepository

class SessionRepositoryImpl(
    private val sessionDao: SessionDao,
    private val userDao: UserDao,
) : SessionRepository {
    override suspend fun saveSession(session: Session) {
        val apolloClientWithAuth = ApolloClient.Builder()
            .serverUrl("https://graphql.anilist.co")
            .addHttpHeader("Authorization", "Bearer ${session.token}")
            .build()
        val data = apolloClientWithAuth.query(GetViewerQuery()).execute().data
            ?: throw Exception("Failed to fetch viewer")
        val viewer = data.Viewer ?: throw Exception("Viewer is null")
        userDao.upsertUser(
            UserEntity(
                id = viewer.id,
                name = viewer.name,
                icon = viewer.avatar?.medium,
                avatar = viewer.avatar?.large
            )
        )
        sessionDao.insertSession(session.toEntity(viewer.id))
    }

    override suspend fun getSession(): Session? {
        val sessionEntity = sessionDao.getSession()
        return sessionEntity?.let {
            Session(token = it.token, userID = sessionEntity.userID)
        }
    }

    override suspend fun clearSession() {
        val sessionEntity = sessionDao.getSession()
        sessionEntity?.let {
            sessionDao.deleteSession(it)
        }
    }
}