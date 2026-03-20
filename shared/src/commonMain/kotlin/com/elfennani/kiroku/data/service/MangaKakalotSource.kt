package com.elfennani.kiroku.data.service

import com.elfennani.kiroku.data.local.dao.MediaDao
import com.elfennani.kiroku.data.local.entity.MatchEntity
import com.elfennani.kiroku.domain.service.MangaSource
import com.elfennani.kiroku.domain.model.BasicMedia
import com.elfennani.kiroku.domain.model.Chapter
import com.fleeksoft.ksoup.Ksoup
import com.fleeksoft.ksoup.nodes.Document
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.statement.bodyAsText
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.time.Instant

class MangaKakalotSource(
    private val client: HttpClient,
    private val mediaDao: MediaDao
) : MangaSource {
    override val name: String
        get() = "MangaKakalot"

    private val baseUrl = "https://www.mangakakalove.com"

    override suspend fun search(
        query: String,
        page: Int
    ): List<BasicMedia> {
        val html = client.get("$baseUrl/search/story/${query.replace(" ", "_")}") {
            parameter("page", page)
        }.bodyAsText()

        val doc: Document = Ksoup.parse(html = html)
        val items = doc.getElementsByClass("story_item")

        return items.mapNotNull { item ->
            val slug = item.child(0)
                .attribute("href")
                ?.value
                ?.split("/")
                ?.lastOrNull()
                ?: return@mapNotNull null

            val name = item.getElementsByClass("story_name").text()
            val thumbnail = item.getElementsByTag("img").attr("src")
            val latestChapter = item.getElementsByClass("story_chapter")
                .firstOrNull()
                ?.text()
            val updated = item.getElementsByClass("story_item_right")
                .firstOrNull()
                ?.find { node ->
                    node.text().startsWith("Updated")
                }
                .let {
                    it?.text()
                        ?.removePrefix("Updated : ")
                }
            val metadata = mutableMapOf<String, String>()

            if (latestChapter != null) {
                metadata["Latest Chapter"] = latestChapter
            }
            if (updated != null) {
                metadata["Updated"] = updated
            }

            BasicMedia(
                id = slug,
                aniListId = null,
                title = name,
                cover = thumbnail,
                metadata = metadata,
                headers = mapOf(
                    "Referer" to "https://www.mangakakalot.gg/"
                )
            )
        }
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

    override suspend fun deleteMatch(mediaId: Int) {
        mediaDao.deleteMatch(mediaId, name)
    }

    override suspend fun getSourceId(mediaId: Int): String? {
        return mediaDao.getMediaSourceId(mediaId, name)
    }

    @Serializable
    private data class ApiResponse(
        val success: Boolean,
        val data: Data
    )

    override suspend fun getChapters(mediaId: Int): List<Chapter> {
        val sourceId = getSourceId(mediaId) ?: throw Exception("Manga has not been matched yet")

        val res =
            client.get("${baseUrl}/api/manga/${sourceId}/chapters?limit=-1").body<ApiResponse>()

        return res.data.chapters.map { chapter ->
            Chapter(
                id = chapter.slug,
                source = name,
                title = chapter.name,
                number = chapter.number,
                uploaded = Instant.parse(chapter.updatedAt),
                views = chapter.view
            )
        }
    }

    @Serializable
    private data class Data(
        val chapters: List<RemoteChapter>,
        val pagination: Pagination
    )

    @Serializable
    private data class Pagination(
        val total: Int
    )

    /*
        chapter_name	"Chapter 107.8"
        chapter_slug	"chapter-107-8"
        chapter_num	107.8
        updated_at	"2026-03-08T21:20:50.000000Z"
        view	8953
     */
    @Serializable
    private data class RemoteChapter(
        @SerialName("chapter_name") val name: String,
        @SerialName("chapter_slug") val slug: String,
        @SerialName("chapter_num") val number: Double,
        @SerialName("updated_at") val updatedAt: String,
        val view: Int
    )
}