package com.elfennani.kiroku.data.repository

import com.apollographql.apollo.ApolloClient
import com.elfennani.kiroku.data.datasource.AllAnimeSource
import com.elfennani.kiroku.data.datasource.MangaKakalotSource
import com.elfennani.kiroku.data.local.dao.ChapterDao
import com.elfennani.kiroku.data.local.dao.EpisodeDao
import com.elfennani.kiroku.data.local.dao.MediaDao
import com.elfennani.kiroku.data.local.entity.EpisodeEntity
import com.elfennani.kiroku.data.local.entity.LocalMediaEntity
import com.elfennani.kiroku.data.local.entity.asDomain
import com.elfennani.kiroku.data.local.entity.asEntity
import com.elfennani.kiroku.data.local.relation.asDomain
import com.elfennani.kiroku.domain.datasource.AnimeSource
import com.elfennani.kiroku.domain.datasource.MangaSource
import com.elfennani.kiroku.domain.model.Episode
import com.elfennani.kiroku.domain.model.MatchStatus
import com.elfennani.kiroku.domain.model.Media
import com.elfennani.kiroku.domain.model.MediaItemList
import com.elfennani.kiroku.domain.model.MediaType
import com.elfennani.kiroku.domain.model.Resource
import com.elfennani.kiroku.domain.model.resourceOf
import com.elfennani.kiroku.domain.repository.MediaRepository
import com.elfennani.kiroku.domain.usecase.GetSession
import com.elfennani.kiroku.utils.clean
import com.elfennani.shared.anilist.GetAnimeEpisodesQuery
import com.elfennani.shared.anilist.GetCollectionMediaQuery
import com.elfennani.shared.anilist.GetMediaByIdQuery
import com.elfennani.shared.anilist.type.MediaListStatus
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map

private val TAG = "MediaRepository"

class MediaRepositoryImpl(
    private val allAnimeSource: AllAnimeSource,
    private val mangaKakalot: MangaKakalotSource,
    private val aniListClient: ApolloClient,
    private val mediaDao: MediaDao,
    private val episodeDao: EpisodeDao,
    private val chapterDao: ChapterDao,
    private val getSession: GetSession,
) : MediaRepository {
    override val animeSources: List<AnimeSource>
        get() = listOf(allAnimeSource)
    override val mangaSources: List<MangaSource>
        get() = listOf(mangaKakalot)

    override fun getSourcesByType(type: MediaType): List<String> {
        return when (type) {
            MediaType.ANIME -> animeSources.map { it.name }
            MediaType.MANGA -> mangaSources.map { it.name }
        }
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

    override fun getIsMediaMatched(
        mediaId: Int,
        sourceName: String
    ): Flow<Boolean> {
        return mediaDao.getMediaSourceIdFlow(mediaId, sourceName)
            .map { it != null }
    }

    override suspend fun matchMedia(
        mediaId: Int,
        sourceName: String,
        sourceId: String
    ) {
        val isAnimeSource =
            animeSources.any { it.name == sourceName }
        if (isAnimeSource)
            animeSources.firstOrNull { it.name == sourceName }?.match(mediaId, sourceId)
                ?: throw Exception(
                    "Source not found"
                )
        else
            mangaSources.firstOrNull { it.name == sourceName }?.match(mediaId, sourceId)
    }

    override suspend fun autoMatchMedia(mediaId: Int, sourceName: String) {
        val isAnimeSource =
            animeSources.any { it.name == sourceName }

        if (isAnimeSource) {
            val source =
                animeSources.firstOrNull { it.name == sourceName }
                    ?: throw Exception("Source not found")

            val media = getMediaFlow(mediaId).firstOrNull()
            if (media != null) {
                val res = source.search(media.title, 1)
                val show = res.firstOrNull { it.aniListId == mediaId }

                if (show == null) {
                    throw Exception("Media not found")
                }

                matchMedia(mediaId, sourceName, show.id)
            } else {
                throw Exception("Media not found")
            }
        } else {
            throw Exception("Auto matching manga is not supported")
        }
    }

    override suspend fun fetchMediaItems(
        mediaId: Int,
        source: String
    ): Resource<MediaItemList> {
        val isAnimeSource = animeSources.any { it.name == source }
        if (isAnimeSource) {
            val source = (animeSources).firstOrNull { it.name == source }
                ?: throw Exception("Source not found")

            val sourceId = source.getSourceId(mediaId) ?: return Resource.Success(
                MediaItemList.EpisodeList(emptyList())
            )

            return resourceOf {
                val aniListEpisodes = aniListClient.query(
                    GetAnimeEpisodesQuery(mediaId)
                ).execute().dataOrThrow()

                val episodes = source.getEpisodes(mediaId).map { episode ->
                    val aniListEpisode = aniListEpisodes.Media?.streamingEpisodes?.firstOrNull {
                        it?.title?.startsWith("Episode ${episode.number.clean()} -") ?: false
                    }
                    val title =
                        aniListEpisode?.title?.removePrefix("Episode ${episode.number.clean()} - ")
                    val thumbnail = aniListEpisode?.thumbnail


                    EpisodeEntity(
                        id = episode.id,
                        number = episode.number,
                        title = title ?: episode.title,
                        thumbnail = thumbnail ?: episode.thumbnail,
                        source = source.name,
                        duration = episode.duration,
                        size = null,
                        mediaId = mediaId,
                    )
                }

                episodeDao.upsertEpisodesTransaction(mediaId, source.name, episodes)

                MediaItemList.EpisodeList(episodes.map { it.asDomain() })
            }
        } else {
            val source = (mangaSources).firstOrNull { it.name == source }
                ?: throw Exception("Source not found")
            return resourceOf {
                val chapters = source.getChapters(mediaId)

                chapterDao.upsertAllTransaction(
                    mediaId = mediaId,
                    chapters = chapters.map { it.asEntity(mediaId) }
                )

                MediaItemList.ChapterList(chapters = chapters)
            }
        }
    }

    override fun getMediaItemsFlow(
        mediaId: Int,
        source: String
    ): Flow<MediaItemList> {
        val isAnimeSource = animeSources.any { it.name == source }
        if (isAnimeSource) {
            val source = (animeSources).firstOrNull { it.name == source }
                ?: throw Exception("Source not found")

            return episodeDao.getEpisodesFlow(mediaId, source.name).map { episodes ->
                MediaItemList.EpisodeList(episodes.map { it.asDomain() })
            }
        } else {
            val source = (mangaSources).firstOrNull { it.name == source }
                ?: throw Exception("Source not found")

            return chapterDao.getAllFlow(mediaId).map { chapters ->
                MediaItemList.ChapterList(chapters = chapters.map { it.asDomain() })
            }
        }
    }

    override suspend fun deleteMatch(mediaId: Int, sourceName: String) {
        val deleteMatch =
            animeSources.firstOrNull { it.name == sourceName }?.let { it::deleteMatch }
                ?: mangaSources.firstOrNull { it.name == sourceName }?.let { it::deleteMatch }
                ?: throw Exception("Source not found")

        deleteMatch(mediaId)
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