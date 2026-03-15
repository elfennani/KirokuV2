package com.elfennani.kiroku.data.repository

import com.apollographql.apollo.ApolloClient
import com.elfennani.kiroku.data.datasource.AllAnimeSource
import com.elfennani.kiroku.data.local.dao.MediaDao
import com.elfennani.kiroku.data.local.entity.LocalMediaEntity
import com.elfennani.kiroku.data.local.entity.asDomain
import com.elfennani.kiroku.data.local.entity.asEntity
import com.elfennani.kiroku.data.local.relation.asDomain
import com.elfennani.kiroku.domain.datasource.AnimeSource
import com.elfennani.kiroku.domain.datasource.MangaSource
import com.elfennani.kiroku.domain.model.Media
import com.elfennani.kiroku.domain.model.MediaItemList
import com.elfennani.kiroku.domain.model.MediaType
import com.elfennani.kiroku.domain.model.Resource
import com.elfennani.kiroku.domain.model.resourceOf
import com.elfennani.kiroku.domain.repository.MediaRepository
import com.elfennani.kiroku.domain.usecase.GetSession
import com.elfennani.shared.anilist.GetCollectionMediaQuery
import com.elfennani.shared.anilist.GetMediaByIdQuery
import com.elfennani.shared.anilist.type.MediaListStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val TAG = "MediaRepository"

class MediaRepositoryImpl(
    private val allAnimeSource: AllAnimeSource,
    private val aniListClient: ApolloClient,
    private val mediaDao: MediaDao,
    private val getSession: GetSession,
) : MediaRepository {
    override val animeSources: List<AnimeSource>
        get() = listOf(allAnimeSource)
    override val mangaSources: List<MangaSource>
        get() = emptyList()

    override fun getAnimeSources(type: MediaType): List<String> {
        TODO("Not yet implemented")
    }

    override suspend fun fetchMedia(mediaId: Int): Resource<Media> {
        return resourceOf {
            val response = aniListClient.query(
                GetMediaByIdQuery(mediaId)
            ).execute().dataOrThrow()

            val media =
                response.Media?.mediaFragment?.asEntity() ?: throw Exception("Media not found")

            mediaDao.insertMedia(media)

            media.asDomain()
        }
    }

    override fun getMediaFlow(mediaId: Int): Flow<Media?> {
        return mediaDao.getMediaFlow(mediaId).map { it?.asDomain() }
    }

    override suspend fun fetchOngoingMedia(): Resource<List<Media>> {
        return resourceOf {
            val session = getSession()!!

            if (session.userID == null) {
                throw Exception("User is not logged in")
            }

            val response = aniListClient.query(
                GetCollectionMediaQuery(
                    userId = session.userID,
                    status = listOf(MediaListStatus.CURRENT, MediaListStatus.REPEATING)
                )
            ).execute().dataOrThrow()

            val media: List<LocalMediaEntity> =
                listOfNotNull(
                    response.anime?.collectionFragment,
                    response.manga?.collectionFragment
                )
                    .flatMap { mediaListCollection ->
                        mediaListCollection.lists?.flatMap { list ->
                            list?.entries?.mapNotNull { it?.media?.mediaFragment?.asEntity() }
                                ?: emptyList()
                        } ?: emptyList()
                    }

            mediaDao.insertOngoingMediaTransaction(media)

            media.map { it.asDomain() }
        }
    }

    override fun getOngoingMediaFlow(): Flow<List<Media>> {
        return mediaDao.getOngoingMediaFlow().map { list -> list.map { it.asDomain() } }
    }

    override suspend fun fetchMediaItems(
        mediaId: Int,
        source: String
    ): Resource<MediaItemList> {
        TODO("Not yet implemented")
    }

    override fun getMediaItemsFlow(
        mediaId: Int,
        source: String
    ): Flow<MediaItemList> {
        TODO("Not yet implemented")
    }

    override suspend fun incrementProgress(mediaId: Int) {
        TODO("Not yet implemented")
    }

    override suspend fun markEpisodeAsWatched(mediaId: Int, episodeNumber: Double) {
        TODO("Not yet implemented")
    }

    override suspend fun markChapterAsRead(mediaId: Int, chapterNumber: Double) {
        TODO("Not yet implemented")
    }
}