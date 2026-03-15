package com.elfennani.kiroku.data.datasource

import com.apollographql.apollo.ApolloClient
import com.elfennani.kiroku.data.local.dao.MediaDao
import com.elfennani.kiroku.data.local.entity.MatchEntity
import com.elfennani.kiroku.domain.datasource.AnimeSource
import com.elfennani.kiroku.domain.model.BasicEpisode
import com.elfennani.kiroku.domain.model.BasicMedia
import com.elfennani.kiroku.domain.model.Result
import com.elfennani.kiroku.domain.model.VideoSource
import com.elfennani.kiroku.utils.clean
import com.elfennani.shared.allanime.GetEpisodeListQuery
import com.elfennani.shared.allanime.GetShowByIdQuery
import com.elfennani.shared.allanime.SearchShowsQuery
import com.elfennani.shared.allanime.type.SortBy
import kotlinx.coroutines.async
import kotlin.math.roundToLong

class AllAnimeSource(
    private val allAnimeClient: ApolloClient,
    private val mediaDao: MediaDao
) : AnimeSource {
    override val name: String
        get() = "AllAnime"

    private val THUMBNAIL_PREFIX = "https://wp.youtube-anime.com/aln.youtube-anime.com"
    private val M3U8_PROVIDERS = listOf("Luf-mp4", "Default", "Yt-mp4");
    private val MP4_PROVIDERS = listOf("S-mp4", "Kir", "Sak");
    private val PROVIDERS = M3U8_PROVIDERS + MP4_PROVIDERS;

    override suspend fun search(
        query: String,
        page: Int
    ): List<BasicMedia> {
        val data = allAnimeClient
            .query(SearchShowsQuery(query, SortBy.Top, page))
            .execute()
            .dataOrThrow()
        val shows = data.shows.edges?.map { show ->
            val episodes = show.availableEpisodes as Map<String, Int>
            val isSUB = (episodes["sub"] ?: 0) > 0
            val isDUB = (episodes["dub"] ?: 0) > 0
            val max = episodes.values.maxOrNull()
            val audio = buildString {
                if (isSUB)
                    append("SUB")
                if (isSUB && isDUB)
                    append("/")
                if (isDUB)
                    append("DUB")
            }

            BasicMedia(
                id = show._id!!,
                aniListId = (show.aniListId as String?)?.toInt(),
                title = show.name!!,
                cover = show.thumbnail,
                metadata = mapOf(
                    "Audio" to audio,
                    "Episodes" to (max?.toString() ?: "N/A")
                )
            )
        } ?: throw Exception("No shows found")

        return shows
    }

    override suspend fun match(mediaId: Int, sourceId: String) {
        mediaDao.insertMediaMatch(
            MatchEntity(
                mediaId = mediaId,
                sourceName = name,
                sourceId = sourceId
            )
        )
    }

    override suspend fun getSourceId(mediaId: Int): String? {
        return mediaDao.getMediaSourceId(mediaId, name)
    }

    override suspend fun deleteMatch(mediaId: Int) {
        mediaDao.deleteMatch(mediaId, name)
    }

    override suspend fun getEpisodes(mediaId: Int): List<BasicEpisode> {
        val showId = getSourceId(mediaId) ?: throw Exception("Show has not been matched yet")

        val episodeList = allAnimeClient
            .query(GetEpisodeListQuery(showId))
            .execute()
            .dataOrThrow()
        val show = allAnimeClient
            .query(GetShowByIdQuery(showId))
            .execute()
            .dataOrThrow()

        /**
         * A map of which episodes are subbed or dubbed, episode number is a string
         * and can be a decimal.
         */
        val episodeNumbers =
            (show.show?.availableEpisodesDetail as Map<String, List<String>>?)
                ?.mapValues { entry -> entry.value.map { it.toDouble() } }

        val episodes = episodeList.episodeInfos?.mapNotNull { episode ->
            if (episode == null) return@mapNotNull null

            val info = episode.vidInforssub as Map<String, Any>
            val duration = info["vidDuration"] as Double?
            val size = info["vidSize"] as Int?
            val resolution = info["vidResolution"] as Int?

            /**
             * Some thumbnails are full URLs but some are only paths, the paths are then
             * prepended with a URL gotten from [AllManga](allmanga.to)
             */
            val thumbnails = episode.thumbnails?.mapNotNull { url ->
                if (url == null) return@mapNotNull null
                if (url.startsWith("http"))
                    url
                else
                    "$THUMBNAIL_PREFIX$url"
            }

            BasicEpisode(
                id = episode._id!!,
                number = episode.episodeIdNum ?: return@mapNotNull null,
                title = null,
                thumbnail = thumbnails.orEmpty().firstOrNull(),
                duration = duration?.roundToLong()
            )
        }?.sortedBy { it.number }

        if (episodes.isNullOrEmpty())
            throw Exception("Failed to load episodes.")

        return episodes
    }

    override suspend fun getSources(
        mediaId: Int,
        episodeNumber: Double
    ): List<VideoSource> {
        TODO("Not yet implemented")
    }
}