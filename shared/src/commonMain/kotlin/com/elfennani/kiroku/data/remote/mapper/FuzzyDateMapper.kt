package com.elfennani.kiroku.data.remote.mapper

import com.elfennani.shared.anilist.fragment.DateFragment
import com.elfennani.kiroku.domain.model.Date
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

fun DateFragment.toFormattedString(): String? {
    val year = this.year ?: return null
    val month = this.month ?: return null
    val day = this.day ?: return null

    return "$year-${month.toString().padStart(2, '0')}-${day.toString().padStart(2, '0')}"
}

@OptIn(ExperimentalTime::class)
fun DateFragment.toInstant(): Instant? {
    val year = this.year ?: return null
    val month = this.month ?: return null
    val day = this.day ?: return null

    return Instant.parse(
        "$year-${month.toString().padStart(2, '0')}-${
            day.toString().padStart(2, '0')
        }T00:00:00Z"
    )
}

fun DateFragment.toDomainModel(): Date? {
    return Date(
        year = year ?: return null,
        month = month ?: return null,
        day = day ?: return null
    )
}