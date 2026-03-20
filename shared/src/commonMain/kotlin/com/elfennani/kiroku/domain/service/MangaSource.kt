package com.elfennani.kiroku.domain.service

import com.elfennani.kiroku.domain.model.BasicMedia
import com.elfennani.kiroku.domain.model.Chapter

interface MangaSource {
    val name: String

    /**
     * Search for manga with the given query to select the matching manga.
     *
     * @param query The query to search for.
     * @param page The page to search for.
     */
    suspend fun search(query: String, page: Int): List<BasicMedia>

    /**
     * Match the manga with the given source id so that it can be fetched later.
     */
    suspend fun match(mediaId: Int, sourceId: String)

    suspend fun deleteMatch(mediaId: Int)

    /**
     * Get the source id of the manga with the given id.
     * If the manga has not been matched, return null.
     */
    suspend fun getSourceId(mediaId: Int): String?

    suspend fun getChapters(mediaId: Int): List<Chapter>
}