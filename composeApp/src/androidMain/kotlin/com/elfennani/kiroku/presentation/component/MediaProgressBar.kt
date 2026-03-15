package com.elfennani.kiroku.presentation.component


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.blur
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.elfennani.kiroku.domain.model.Media
import com.elfennani.kiroku.domain.model.MediaType

@Composable
fun MediaProgressBar(
    modifier: Modifier = Modifier,
    media: Media,
    label: @Composable () -> Unit = {Text("Progress")}
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            CompositionLocalProvider(
                LocalTextStyle provides MaterialTheme.typography.labelSmall,
                LocalContentColor provides MaterialTheme.colorScheme.outlineVariant
            ) {
                label()
            }
            
            Text(
                text = buildAnnotatedString {
                    withStyle(
                        style = MaterialTheme.typography.labelLarge
                            .toSpanStyle()
                            .copy(color = MaterialTheme.colorScheme.onBackground)
                    ) {
                        append(media.progress?.toString() ?: "N/A")
                    }
                    withStyle(
                        style = MaterialTheme.typography.labelSmall
                            .toSpanStyle()
                            .copy(color = MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        if (media.total != null)
                            append("/${media.total}")
                        else if (media.type == MediaType.MANGA)
                            append(" Chaps.")
                        else
                            append(" Eps.")
                    }
                },
                style = MaterialTheme.typography.labelSmall,
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(2.dp)
                .background(MaterialTheme.colorScheme.secondary),
            contentAlignment = Alignment.CenterStart
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(
                        if (media.progress != null && media.total != null)
                            media.progress!!.toFloat() / media.total!!
                        else
                            1f
                    )
                    .blur(2.dp, edgeTreatment = BlurredEdgeTreatment.Unbounded)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(2.dp)
                        .background(MaterialTheme.colorScheme.primary)
                )
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth(
                        if (media.progress != null && media.total != null)
                            media.progress!!.toFloat() / media.total!!
                        else
                            1f
                    )
                    .height(2.dp)
                    .background(MaterialTheme.colorScheme.primary)
            )
        }
    }
}
