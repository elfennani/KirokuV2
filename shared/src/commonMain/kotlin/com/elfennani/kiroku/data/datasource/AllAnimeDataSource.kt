package com.elfennani.kiroku.data.datasource

import com.apollographql.apollo.ApolloClient
import com.elfennani.shared.allanime.GetEpisodeByIdQuery
import com.elfennani.shared.allanime.GetEpisodeListQuery
import com.elfennani.shared.allanime.GetShowByIdQuery
import com.elfennani.shared.allanime.SearchShowsQuery
import com.elfennani.shared.allanime.type.SortBy
import com.elfennani.shared.allanime.type.VaildTranslationTypeEnumType
import com.elfennani.kiroku.data.getKtorClient
import com.elfennani.kiroku.domain.datasource.RemoteDataSource
import com.elfennani.kiroku.domain.model.BasicMedia
import com.elfennani.kiroku.domain.model.Episode
import com.elfennani.kiroku.domain.model.Result
import com.elfennani.kiroku.domain.model.VideoType
import com.elfennani.kiroku.utils.clean
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlin.math.roundToLong

@Suppress("UNCHECKED_CAST")
class AllAnimeDataSource(
    private val allAnimeClient: ApolloClient
) : RemoteDataSource {
    override suspend fun searchShows(
        query: String,
        page: Int
    ): Result<List<BasicMedia>> {
        try {
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

            return Result.Success(shows)
        } catch (e: Exception) {
            return Result.Error(e.message ?: "Unexpected Error Occurred")
        }
    }

    override suspend fun getEpisodes(showId: String): Result<List<Episode>> {
        return coroutineScope {
            try {
                val episodeListSuspend = async {
                    allAnimeClient
                        .query(GetEpisodeListQuery(showId))
                        .execute()
                        .dataOrThrow()
                }

                val showSuspend = async {
                    allAnimeClient
                        .query(GetShowByIdQuery(showId))
                        .execute()
                        .dataOrThrow()
                }

                val episodeList = episodeListSuspend.await()
                val show = showSuspend.await()

                /**
                 * A map of which episodes are subbed or dubbed, episode number is a string
                 * and can be a decimal.
                 */
                val episodeNumbers =
                    (show.show?.availableEpisodesDetail as Map<String, List<String>>?)
                        ?.mapValues { entry -> entry.value.map { it.toDouble() } }

//                val episodes = episodeList.episodeInfos?.mapNotNull { episode ->
//                    if (episode == null) return@mapNotNull null
//
//                    val info = episode.vidInforssub as Map<String, Any>
//                    val duration = info["vidDuration"] as Double?
//                    val size = info["vidSize"] as Int?
//                    val resolution = info["vidResolution"] as Int?
//
//                    /**
//                     * Some thumbnails are full URLs but some are only paths, the paths are then
//                     * prepended with a URL gotten from [AllManga](allmanga.to)
//                     */
//                    val thumbnails = episode.thumbnails?.mapNotNull { url ->
//                        if (url == null) return@mapNotNull null
//                        if (url.startsWith("http"))
//                            url
//                        else
//                            "$THUMBNAIL_PREFIX$url"
//                    }
//
//                    Episode(
//                        id = episode._id!!,
//                        name = "Episode ${episode.episodeIdNum?.clean()}",
//                        number = episode.episodeIdNum!!,
//                        thumbnails = thumbnails.orEmpty(),
//                        duration = duration?.roundToLong(),
//                        isSubbed = episodeNumbers?.get("sub")?.contains(episode.episodeIdNum),
//                        isDubbed = episodeNumbers?.get("dub")?.contains(episode.episodeIdNum),
//                        size = size?.toLong()
//                    )
//                }?.sortedBy { it.number }

                Result.Success(emptyList() ?: throw Error("Failed to load episodes."))
            } catch (e: Exception) {
                Result.Error(e.message ?: "Unknown")
            }
        }
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

    override suspend fun getEpisodeSources(
        showId: String,
        episode: Episode
    ): Result<List<Pair<VideoType, String>>> {
        return try {
            val data = allAnimeClient
                .query(
                    GetEpisodeByIdQuery(
                        showId, episode.number.clean(),
                        VaildTranslationTypeEnumType.sub
                    )
                )
                .execute()
                .dataOrThrow()

            val urls = coroutineScope {
                convertToSourceUrls(data.episode?.sourceUrls)
                    .filter { source ->
                        PROVIDERS.contains(source.sourceName)
                    }
                    .mapNotNull {
                        try {
                            val baseUrl = "https://allanime.day"
                            println("Decrypting ${it.sourceUrl}");
                            val path = it.sourceUrl
                                // Removes the filler two dashes `--`
                                .drop(2)
                                // Split the string into an array of two characters
                                .chunked(2)
                                // Decrypt the each chunk then combine them back into a proper string
                                .map { chunk -> chunk.decrypt() }
                                .joinToString("")
                                .replace("clock", "clock.json")

                            val type = if (it.sourceName in M3U8_PROVIDERS)
                                VideoType.HLS
                            else
                                VideoType.MP4

                            type to "$baseUrl$path"
                        } catch (e: Exception) {
                            e.printStackTrace()
                            null
                        }
                    }.map { (type, url) ->
                        async {
                            try {
                                println("Fetching... $type: $url")
                                val ktor = getKtorClient()
                                val response = ktor.get(url)
                                val body = response.bodyAsText()
                                val json = Json {
                                    ignoreUnknownKeys = true

                                }
                                val data = json.decodeFromString<GetEpisodeLinkResponse>(body)
                                val link =
                                    data.links.firstOrNull()?.link ?: data.links.firstOrNull()?.src
                                    ?: return@async null
                                println("Got link: $type: $link")

                                Pair(type, link)
                            } catch (e: Exception) {
                                e.printStackTrace()
                                return@async null
                            }
                        }
                    }
            }.awaitAll().filterNotNull()

            println("Got sources: ${urls.size} Sources")

            Result.Success(urls)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Unknown")
        }
    }

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

        throw Exception("Unsupported character: ${this}")
    }

    companion object {
        const val THUMBNAIL_PREFIX = "https://wp.youtube-anime.com/aln.youtube-anime.com"
        val M3U8_PROVIDERS = listOf("Luf-mp4", "Default", "Yt-mp4");
        val MP4_PROVIDERS = listOf("S-mp4", "Kir", "Sak");
        val PROVIDERS = M3U8_PROVIDERS + MP4_PROVIDERS;
    }
}