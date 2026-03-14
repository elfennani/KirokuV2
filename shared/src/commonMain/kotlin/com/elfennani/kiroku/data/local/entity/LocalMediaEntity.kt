package com.elfennani.kiroku.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.elfennani.kiroku.domain.model.Media
import com.elfennani.kiroku.domain.model.MediaStatus
import com.elfennani.kiroku.domain.model.MediaType
import com.elfennani.shared.anilist.fragment.MediaFragment
import com.elfennani.shared.anilist.type.MediaListStatus
import com.elfennani.shared.anilist.type.MediaType as AniListMediaType

@Entity("media_v2")
data class LocalMediaEntity(
    @PrimaryKey
    val id: Int,
    val title: String,
    val description: String?,
    val progress: Int?,
    val total: Int?,
    val type: String,
    val status: String?,
    val cover: String?,
    val banner: String?
)

fun LocalMediaEntity.asDomain() = Media(
    id = id,
    title = title,
    description = description,
    progress = progress,
    total = total,
    type = MediaType.valueOf(type),
    status = status?.let { MediaStatus.valueOf(status) },
    cover = cover,
    banner = banner
)

fun MediaFragment.asEntity() = LocalMediaEntity(
    id = this.id,
    title = this.title?.titleFragment.let {
        it?.userPreferred ?: it?.english ?: it?.romaji ?: it?.native!!
    },
    cover = this.coverImage?.large,
    banner = this.bannerImage,
    description = this.description,
    progress = this.mediaListEntry?.progress,
    type = (if (this.type == AniListMediaType.ANIME) MediaType.ANIME else MediaType.MANGA).name,
    total = if (this.type == AniListMediaType.ANIME) this.episodes else this.chapters,
    status = when (this.mediaListEntry?.status) {
        MediaListStatus.COMPLETED -> MediaStatus.COMPLETED
        MediaListStatus.CURRENT -> MediaStatus.IN_PROGRESS
        MediaListStatus.DROPPED -> MediaStatus.DROPPED
        MediaListStatus.PAUSED -> MediaStatus.PAUSED
        MediaListStatus.PLANNING -> MediaStatus.PLANNED
        MediaListStatus.REPEATING -> MediaStatus.REVISITING
        else -> null
    }?.name
)