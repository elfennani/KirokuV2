package com.elfennani.kiroku.domain.service

import com.elfennani.kiroku.domain.model.BasicEpisode
import com.elfennani.kiroku.domain.model.BasicMedia
import com.elfennani.kiroku.domain.model.VideoAudio
import com.elfennani.kiroku.domain.model.VideoSource
import com.elfennani.kiroku.domain.model.VideoType

interface AnimeSource {
    /**
     * The name of the source. (eg. "AllAnime")
     */
    val name: String;

    /**
     * Search for shows with the given query to select the matching show.
     *
     * @param query The query to search for.
     * @param page The page to search for.
     */
    suspend fun search(query: String, page: Int): List<BasicMedia>

    /**
     * Match the show with the given source id so that it can be fetched later.
     */
    suspend fun match(mediaId: Int, sourceId: String)

    suspend fun deleteMatch(mediaId: Int)

    /**
     * Get the source id of the show with the given id.
     * If the show has not been matched, return null.
     */
    suspend fun getSourceId(mediaId: Int): String?

    /**
     * Get the episodes of the show with the given id.
     */
    suspend fun getEpisodes(mediaId: Int): List<BasicEpisode>

    /**
     * Get the sources of the episode with the given id.
     * @param mediaId The AniList id of the show.
     * @param episodeNumber The number of the episode.
     */
    suspend fun getSources(
        mediaId: Int,
        episodeNumber: Double,
        audio: VideoAudio? = null,
        type: VideoType? = null
    ): List<VideoSource>
}
