package com.elfennani.kiroku.data.datasource

import com.elfennani.kiroku.domain.datasource.AnimeSource
import com.elfennani.kiroku.domain.model.BasicEpisode
import com.elfennani.kiroku.domain.model.BasicMedia
import com.elfennani.kiroku.domain.model.VideoSource

class AllAnimeSource: AnimeSource {
    override val name: String
        get() = "AllAnime"

    override suspend fun search(
        query: String,
        page: Int
    ): List<BasicMedia> {
        TODO("Not yet implemented")
    }

    override suspend fun match(mediaId: Int, sourceId: String) {
        TODO("Not yet implemented")
    }

    override suspend fun getSourceId(mediaId: Int): String? {
        TODO("Not yet implemented")
    }

    override suspend fun getEpisodes(mediaId: Int): List<BasicEpisode> {
        TODO("Not yet implemented")
    }

    override suspend fun getSources(
        mediaId: Int,
        episodeNumber: Double
    ): List<VideoSource> {
        TODO("Not yet implemented")
    }
}