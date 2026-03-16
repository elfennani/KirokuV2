package com.elfennani.kiroku.presentation.screen.media.components

import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.elfennani.kiroku.R
import com.elfennani.kiroku.domain.model.Chapter
import com.elfennani.kiroku.utils.clean
import kotlin.time.Clock
import kotlin.time.Instant
import kotlin.time.TimeSource

private fun Instant.readable(): String {
    val diff = Clock.System.now() - this
    val seconds = diff.inWholeSeconds

    return when {
        seconds < 60 -> "${seconds}s ago"
        seconds < 3600 -> "${seconds / 60}m ago"
        seconds < 86400 -> "${seconds / 3600}h ago"
        seconds < 30 * 86400 -> "${seconds / 86400}d ago"
        seconds < 365 * 86400 -> "${seconds / (30 * 86400)}mo ago"
        else -> "${seconds / (365 * 86400)}y ago"
    }
}

private fun Int.readable(): String {
    val absValue = kotlin.math.abs(this)
    return when {
        absValue >= 1_000_000_000 -> "${this / 1_000_000_000}B"
        absValue >= 1_000_000 -> "${this / 1_000_000}M"
        absValue >= 1_000 -> "${this / 1_000}K"
        else -> this.toString()
    }
}


@Composable
fun ChapterItem(
    modifier: Modifier = Modifier,
    chapter: Chapter,
) {
    val properties = remember(chapter.views, chapter.uploaded) {
        buildList {
            chapter.views?.let { add("${it.readable()} Views") }
            chapter.uploaded?.let { add(it.readable()) }
        }
    }


    DraggableItem() {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
                .combinedClickable(
                    onClick = {},
                    onLongClick = {}
                )
                .padding(horizontal = 24.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(vertical = 4.dp)
                    .weight(1f)
            ) {
                Text(
                    "Chapter ${chapter.number.clean()}",
                    style = MaterialTheme.typography.labelLarge
                )
                Text(
                    properties.joinToString(" • "),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outlineVariant,
                    maxLines = 2
                )
            }

            Icon(painterResource(R.drawable.outline_keyboard_arrow_right_24), null)
        }
    }
}