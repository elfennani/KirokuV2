package com.elfennani.kiroku.domain.datasource

import com.elfennani.kiroku.domain.model.BasicMedia
import com.elfennani.kiroku.domain.model.Episode
import com.elfennani.kiroku.domain.model.Result
import com.elfennani.kiroku.domain.model.VideoType

interface RemoteDataSource {
    suspend fun searchShows(query: String, page: Int): Result<List<BasicMedia>>
    suspend fun getEpisodes(showId: String): Result<List<Episode>>

    /**
     * @return a list of urls along with their type (Streaming or MP4)
     */
    suspend fun getEpisodeSources(
        showId: String,
        episode: Episode
    ): Result<List<Pair<VideoType, String>>>
}