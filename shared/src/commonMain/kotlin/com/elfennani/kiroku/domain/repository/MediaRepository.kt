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

    fun getSourcesByType(type: MediaType): List<String>

    suspend fun fetchMedia(mediaId: Int): Resource<Media>
    fun getMediaFlow(mediaId: Int): Flow<Media?>

    suspend fun fetchOngoingMedia(): Resource<List<Media>>
    fun getOngoingMediaFlow(): Flow<List<Media>>

    fun getIsMediaMatched(mediaId: Int, sourceName: String): Flow<Boolean>
    suspend fun matchMedia(mediaId: Int, sourceName: String, sourceId: String)
    suspend fun autoMatchMedia(mediaId: Int, sourceName: String)

    suspend fun fetchMediaItems(mediaId: Int, source: String): Resource<MediaItemList>
    fun getMediaItemsFlow(mediaId: Int, source: String): Flow<MediaItemList>


    suspend fun incrementProgress(mediaId: Int)
    suspend fun markEpisodeAsWatched(mediaId: Int, episodeNumber: Double)
    suspend fun markChapterAsRead(mediaId: Int, chapterNumber: Double)
}