package com.elfennani.kiroku.domain.repository

import com.elfennani.kiroku.domain.datasource.AnimeSource
import com.elfennani.kiroku.domain.datasource.MangaSource
import com.elfennani.kiroku.domain.model.Media
import com.elfennani.kiroku.domain.model.MediaItemList
import com.elfennani.kiroku.domain.model.MediaType
import com.elfennani.kiroku.domain.model.Resource
import kotlinx.coroutines.flow.Flow

interface MediaRepository {
    val animeSources: List<AnimeSource>
    val mangaSources: List<MangaSource>

    fun getAnimeSources(type: MediaType): List<String>

    suspend fun fetchMedia(mediaId: Int): Media
    fun getMediaFlow(mediaId: Int): Flow<Media>

    suspend fun fetchOngoingMedia(): Resource<List<Media>>
    fun getOngoingMediaFlow(): Flow<List<Media>>

    suspend fun fetchMediaItems(mediaId: Int, source: String): Resource<MediaItemList>
    fun getMediaItemsFlow(mediaId: Int, source: String): Flow<MediaItemList>

    suspend fun incrementProgress(mediaId: Int)
    suspend fun markEpisodeAsWatched(mediaId: Int, episodeNumber: Double)
    suspend fun markChapterAsRead(mediaId: Int, chapterNumber: Double)
}