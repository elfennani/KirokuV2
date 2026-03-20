package com.elfennani.kiroku.presentation.screen.media.components

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.elfennani.kiroku.R
import com.elfennani.kiroku.domain.model.DownloadStatus
import com.elfennani.kiroku.domain.model.Episode
import com.elfennani.kiroku.domain.model.label
import com.elfennani.kiroku.utils.clean
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

private const val TAG = "EpisodeItem"

private fun Long.secondsToReadable(): String {
    val hrs = this / 3600
    val mins = (this % 3600) / 60
    val secs = this % 60

    return if (hrs > 0) {
        "%02d:%02d:%02d".format(hrs, mins, secs)
    } else {
        "%02d:%02d".format(mins, secs)
    }
}

@Composable
fun EpisodeItem(
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    onDownload: () -> Unit = {},
    episode: Episode,
) {
    val state = rememberDraggableState(key = episode.id, onDownload)
    DraggableItem(key = episode.id, state = state) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
                .combinedClickable(
                    onClick = onClick,
                    onLongClick = {}
                )
                .padding(horizontal = 24.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box {
                AsyncImage(
                    model = episode.thumbnail,
                    contentDescription = episode.title,
                    modifier = Modifier
                        .width(112.dp)
                        .aspectRatio(16f / 9)
                        .background(MaterialTheme.colorScheme.surface)
                )
                Box(
                    Modifier
                        .matchParentSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(Color.Transparent, Color.Black.copy(0.25f)),
                                startY = 0.9f
                            )
                        )
                )
                if (episode.duration != null) {
                    Text(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .offset(6.dp, (-4).dp),
                        text = episode.duration!!.secondsToReadable(),
                        color = Color.White,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFeatureSettings = "tnum"
                        ),
                        lineHeight = MaterialTheme.typography.labelSmall.fontSize
                    )
                }
            }
            Column(
                modifier = Modifier.padding(vertical = 4.dp)
            ) {
                Text(
                    "Episode ${episode.number.clean()}",
                    style = MaterialTheme.typography.labelLarge
                )
                if (episode.title != null)
                    Text(
                        episode.title!!,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outlineVariant,
                        maxLines = if (episode.downloadStatus == null) 2 else 1
                    )
                if (episode.downloadStatus != null) {
                    Spacer(Modifier.height(2.dp))
                    Text(
                        episode.downloadStatus!!.label(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        maxLines = 1
                    )
                }
            }
        }
    }
}