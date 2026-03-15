package com.elfennani.kiroku.domain.model

/**
 * This a simple data wrapper for shows to be used by
 * different remote services (such as *AllAnime*).
 */
data class BasicMedia(
    /** ID of the show in the service, convert to `String` if it's another type. */
    val id: String,
    val aniListId: Int?,
    val title: String,
    val cover: String?,
    /** Any additional metadata that might be beneficial to display to the user. */
    val metadata: Map<String, String>,
    /** Headers required to display images if needed */
    val headers: Map<String, String> = emptyMap()
)
