package com.elfennani.kiroku.presentation.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.elfennani.kiroku.R
@Suppress("ComposableNaming")
@Composable
fun InterTypography(): Typography {
    val interFont = FontFamily(
        Font(R.font.inter),
    )

    return with(MaterialTheme.typography) {
        copy(
            displayLarge = displayLarge.copy(fontFamily = interFont, fontWeight = FontWeight.Bold),
            displayMedium = displayMedium.copy(
                fontFamily = interFont,
                fontWeight = FontWeight.Bold
            ),
            displaySmall = displaySmall.copy(fontFamily = interFont, fontWeight = FontWeight.Bold),
            headlineLarge = headlineLarge.copy(
                fontFamily = interFont,
                fontWeight = FontWeight.Bold
            ),
            headlineMedium = headlineMedium.copy(
                fontFamily = interFont,
                fontWeight = FontWeight.Bold
            ),
            headlineSmall = headlineSmall.copy(
                fontFamily = interFont,
                fontWeight = FontWeight.Bold
            ),
            titleLarge = titleLarge.copy(fontFamily = interFont, fontWeight = FontWeight(450)),
            titleMedium = titleMedium.copy(fontFamily = interFont, fontWeight = FontWeight(450)),
            titleSmall = titleSmall.copy(fontFamily = interFont, fontWeight = FontWeight(450)),
            labelLarge = labelLarge.copy(fontFamily = interFont, fontWeight = FontWeight.Normal),
            labelMedium = labelMedium.copy(fontFamily = interFont, fontWeight = FontWeight.Normal),
            labelSmall = labelSmall.copy(fontFamily = interFont, fontWeight = FontWeight.Normal),
            bodyLarge = bodyLarge.copy(fontFamily = interFont, fontWeight = FontWeight.Normal),
            bodyMedium = bodyMedium.copy(fontFamily = interFont, fontWeight = FontWeight.Normal),
            bodySmall = bodySmall.copy(fontFamily = interFont, fontWeight = FontWeight.Normal),
        )
    }
}

