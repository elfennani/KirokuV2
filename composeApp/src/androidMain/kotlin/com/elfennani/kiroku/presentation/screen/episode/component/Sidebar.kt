package com.elfennani.kiroku.presentation.screen.episode.component

import android.graphics.drawable.Icon
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import com.elfennani.kiroku.R
import com.elfennani.kiroku.presentation.screen.episode.EpisodeUiState

sealed class SidebarRoute {
    object None : SidebarRoute()
    object Preferences : SidebarRoute()
    object Sources : SidebarRoute()

    /** For streaming video sources */
    object Resolution : SidebarRoute()

    /** For user uploaded sources (eg. Matroska) */
    object AudioTracks : SidebarRoute()
    object SubtitleTracks : SidebarRoute()
}


@Composable
fun Sidebar(
    modifier: Modifier = Modifier,
    state: EpisodeUiState,
    backstack: SnapshotStateList<SidebarRoute> = remember { mutableStateListOf() }
) {
    AnimatedVisibility(
        visible = backstack.isNotEmpty(),
        enter = expandHorizontally(
            expandFrom = Alignment.Start
        ),
        exit = shrinkHorizontally(
            shrinkTowards = Alignment.Start
        )
    ) {
        NavDisplay(
            modifier = modifier
                .width(384.dp)
                .fillMaxHeight()
                .background(MaterialTheme.colorScheme.background),
            backStack = listOf(SidebarRoute.None) + backstack,
            onBack = { backstack.removeLastOrNull() },
            predictivePopTransitionSpec = {
                ContentTransform(
                    targetContentEnter = fadeIn() + scaleIn(initialScale = 0.9f),
                    initialContentExit = fadeOut() + slideOutHorizontally(
                        targetOffsetX = { it / 2 },
                    ) + scaleOut(targetScale = 0.9f)
                )
            },
            transitionSpec = {
                ContentTransform(
                    targetContentEnter = fadeIn() + slideIntoContainer(
                        towards = AnimatedContentTransitionScope.SlideDirection.Left,
                    ) + scaleIn(initialScale = 0.9f),
                    initialContentExit = fadeOut() + scaleOut(targetScale = 0.9f)
                )
            },
            popTransitionSpec = {
                ContentTransform(
                    targetContentEnter = fadeIn() + scaleIn(initialScale = 0.9f),
                    initialContentExit = fadeOut() + slideOutHorizontally(
                        targetOffsetX = { it / 2 },
                    ) + scaleOut(targetScale = 0.9f)
                )
            },
            entryProvider = entryProvider {
                entry<SidebarRoute.None> {}
                entry<SidebarRoute.Preferences> {
                    SidebarLayout(
                        title = { Text("Preferences") },
                        action = {
                            IconButton(onClick = { backstack.clear() }) {
                                Icon(
                                    painterResource(R.drawable.outline_close_24),
                                    "close"
                                )
                            }
                        }
                    ) {
                        Setting(
                            leadingIcon = {
                                Icon(
                                    painterResource(R.drawable.sharp_hangout_video_24),
                                    "Video Source"
                                )
                            },
                            title = { Text("Video Source") },
                            subtitle = {
                                Text(
                                    state.selectedSource?.let { "${it.name} • ${it.type.name} • ${it.audio.name}" }
                                        ?: state.sourceName
                                        ?: "Loading..."
                                )
                            },
                            onClick = {
                                backstack.add(SidebarRoute.Sources)
                            }
                        )
                    }
                }
                entry<SidebarRoute.Sources> {
                    SidebarLayout(
                        navigationIcon = {
                            IconButton(onClick = { backstack.removeLastOrNull() }) {
                                Icon(
                                    painterResource(R.drawable.baseline_arrow_back_24),
                                    "Back"
                                )
                            }
                        },
                        title = { Text("Sources") },
                    ) {
                        state.sources.forEach {
                            Setting(
                                title = { Text(it.name) },
                                subtitle = { Text("${it.type.name} • ${it.audio.name}") },
                                trailingIcon = {
                                    if (it == state.selectedSource)
                                        Icon(
                                            painterResource(R.drawable.sharp_check_24),
                                            null,
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                },
                                onClick = { backstack.removeLastOrNull() }
                            )
                        }
                    }
                }
            }
        )
    }
}

@Composable
fun SidebarLayout(
    modifier: Modifier = Modifier,
    navigationIcon: @Composable () -> Unit = {},
    title: @Composable () -> Unit = {},
    action: @Composable () -> Unit = {},
    content: @Composable () -> Unit = {},
) {
    Row(
        modifier = modifier
            .fillMaxSize(),
    ) {
        if (isSystemInDarkTheme())
            VerticalDivider(
                color = MaterialTheme.colorScheme.outline.copy(0.5f)
            )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 16.dp),
        ) {
            CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.onBackground) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Box(Modifier.offset(x = (-12).dp)) {
                        navigationIcon()
                    }
                    CompositionLocalProvider(
                        LocalTextStyle provides MaterialTheme.typography.titleMedium
                    ) {
                        title()
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    Box(Modifier.offset(x = 12.dp)) {
                        action()
                    }
                }


                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp),
                    color = MaterialTheme.colorScheme.outline
                )

                content()
            }
        }
    }
}

@Composable
fun Setting(
    modifier: Modifier = Modifier,
    leadingIcon: @Composable () -> Unit = {},
    trailingIcon: @Composable () -> Unit = {
        Icon(
            painterResource(R.drawable.outline_keyboard_arrow_right_24),
            null
        )
    },
    title: @Composable () -> Unit = {},
    subtitle: @Composable () -> Unit = {},
    onClick: () -> Unit = {}
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 24.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        leadingIcon()
        Column(
            modifier = Modifier.weight(1f)
        ) {
            CompositionLocalProvider(
                LocalTextStyle provides MaterialTheme.typography.titleSmall
            ) {
                title()
            }
            CompositionLocalProvider(
                LocalTextStyle provides MaterialTheme.typography.bodySmall,
                LocalContentColor provides MaterialTheme.colorScheme.outlineVariant
            ) {
                subtitle()
            }
        }
        trailingIcon()
    }
}