package com.elfennani.kiroku.presentation.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb

private val lightScheme = lightColorScheme(
    primary = primary,
    onPrimary = Color.White,
    background = shade4,
    onBackground = shade1,
    outline = shade3,
    outlineVariant = shade2,

    surface = shade3,

    secondary = primaryLight2,
    onSecondary = primaryDark2
)

private val darkScheme = darkColorScheme(
    primary = primary,
    onPrimary = Color.White,
    background = Color.Black,
    onBackground = shade4,
    outline = shade2,
    outlineVariant = shade3,

    secondary = primaryDark2,
    onSecondary = primaryLight2
)

@Composable
fun AppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable() () -> Unit
) {
    val colorScheme = when {
        darkTheme -> darkScheme
        else -> lightScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = InterTypography(),
        content = content
    )
}

