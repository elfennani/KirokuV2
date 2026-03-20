package com.elfennani.kiroku.data.service

import com.apollographql.apollo.ApolloClient
import com.elfennani.kiroku.data.local.dao.MediaDao
import com.elfennani.kiroku.data.local.entity.MatchEntity
import com.elfennani.kiroku.domain.service.AnimeSource
import com.elfennani.kiroku.domain.model.BasicEpisode
import com.elfennani.kiroku.domain.model.BasicMedia
import com.elfennani.kiroku.domain.model.VideoAudio
import com.elfennani.kiroku.domain.model.VideoSource
import com.elfennani.kiroku.domain.model.VideoType
import com.elfennani.kiroku.utils.clean
import com.elfennani.shared.allanime.GetEpisodeByIdQuery
import com.elfennani.shared.allanime.GetEpisodeListQuery
import com.elfennani.shared.allanime.GetShowByIdQuery
import com.elfennani.shared.allanime.SearchShowsQuery
import com.elfennani.shared.allanime.type.SortBy
import com.elfennani.shared.allanime.type.VaildTranslationTypeEnumType
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.serialization.Serializable
import kotlin.math.roundToLong

class AllAnimeSource(
    private val allAnimeClient: ApolloClient,
    private val mediaDao: MediaDao,
    private val httpClient: HttpClient
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
        episodeNumber: Double,
        audio: VideoAudio?,
        type: VideoType?
    ): List<VideoSource> {
        return coroutineScope {
            val showId = getSourceId(mediaId) ?: throw Exception("Show has not been matched yet")
            val episode = getEpisodes(mediaId).firstOrNull { it.number == episodeNumber }
                ?: throw Exception("Episode not found")

            val fetch: suspend (VaildTranslationTypeEnumType) -> GetEpisodeByIdQuery.Data = {
                allAnimeClient
                    .query(
                        GetEpisodeByIdQuery(
                            showId = showId,
                            episodeString = episode.number.clean(),
                            audio = it
                        )
                    )
                    .execute()
                    .dataOrThrow()
            }

            val sub = async { fetch(VaildTranslationTypeEnumType.sub) }.await()
            val dub = async { fetch(VaildTranslationTypeEnumType.dub) }.await()

            val urls = coroutineScope {
                val sourceUrls = (

                        if (audio == null || audio == VideoAudio.DUBBED) convertToSourceUrls(dub.episode?.sourceUrls).map {
                            Pair(VideoAudio.DUBBED, it)
                        } else emptyList()

                        ) + (

                        if (audio == null || audio == VideoAudio.SUBBED) convertToSourceUrls(
                            sub.episode?.sourceUrls
                        ).map {
                            Pair(VideoAudio.SUBBED, it)
                        } else emptyList()

                        )


                sourceUrls
                    .filter { (_, source) ->
                        PROVIDERS.contains(source.sourceName)
                    }
                    .mapNotNull { (audio, source) ->
                        try {
                            val baseUrl = "https://allanime.day"
                            println("Decrypting ${source.sourceUrl}");
                            val path = source.sourceUrl
                                // Removes the filler two dashes `--`
                                .drop(2)
                                // Split the string into an array of two characters
                                .chunked(2)
                                // Decrypt the each chunk then combine them back into a proper string
                                .map { chunk -> chunk.decrypt() }
                                .joinToString("")
                                .replace("clock", "clock.json")

                            val type =
                                if (source.sourceName in M3U8_PROVIDERS)
                                    VideoType.HLS
                                else
                                    VideoType.MP4

                            Triple(audio, type, "$baseUrl$path")
                        } catch (e: Exception) {
                            e.printStackTrace()
                            null
                        }
                    }
                    .filter {
                        val itemType = it.second

                        if (type == null) return@filter true
                        else itemType == type
                    }
                    .map { (audio, type, url) ->
                        async {
                            try {
                                println("Fetching... $type: $url")
                                val response = httpClient.get(url)
                                val data = response.body<GetEpisodeLinkResponse>()

                                val link =
                                    data.links.firstOrNull()?.link ?: data.links.firstOrNull()?.src
                                    ?: return@async null
                                println("Got link: $type: $link")

                                Triple(audio, type, link)
                            } catch (e: Exception) {
                                e.printStackTrace()
                                return@async null
                            }
                        }
                    }
            }.awaitAll().filterNotNull()

            urls.map { (audio, type, url) ->
                VideoSource(
                    type = type,
                    name = name,
                    url = url,
                    audio = audio,
                )
            }
        }
    }

    /**
     * Some urls in AllAnime are encrypted, this function decrypts them.
     */
    private fun String.decrypt(): Char {
        if (this == "01") return '9';
        if (this == "08") return '0';
        if (this == "05") return '=';
        if (this == "0a") return '2';
        if (this == "0b") return '3';
        if (this == "0c") return '4';
        if (this == "07") return '?';
        if (this == "00") return '8';
        if (this == "5c") return 'd';
        if (this == "0f") return '7';
        if (this == "5e") return 'f';
        if (this == "17") return '/';
        if (this == "54") return 'l';
        if (this == "09") return '1';
        if (this == "48") return 'p';
        if (this == "4f") return 'w';
        if (this == "0e") return '6';
        if (this == "5b") return 'c';
        if (this == "5d") return 'e';
        if (this == "0d") return '5';
        if (this == "53") return 'k';
        if (this == "1e") return '&';
        if (this == "5a") return 'b';
        if (this == "59") return 'a';
        if (this == "4a") return 'r';
        if (this == "4c") return 't';
        if (this == "4e") return 'v';
        if (this == "57") return 'o';
        if (this == "51") return 'i';
        if (this == "50") return 'h';
        if (this == "4b") return 's';
        if (this == "02") return ':';
        if (this == "55") return 'm';
        if (this == "4d") return 'u';
        if (this == "16") return '.';

        throw Exception("Unsupported character: $this")
    }


    private data class SourceMap(
        val sourceUrl: String,
        val sourceName: String,
    )

    private fun convertToSourceUrls(data: Any?): List<SourceMap> {
        val list = data as List<*>

        return list.map {
            val sourceMap = it as Map<*, *>

            SourceMap(
                sourceUrl = sourceMap["sourceUrl"] as String,
                sourceName = sourceMap["sourceName"] as String,
            )
        }
    }

    @Serializable()
    private data class GetEpisodeLinkResponse(
        val links: List<EpisodeLinkData>
    )

    @Serializable
    private data class EpisodeLinkData(
        val link: String? = null,
        val src: String? = null
    )
}