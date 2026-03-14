package com.elfennani.kiroku.presentation.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.elfennani.kiroku.R

@OptIn(ExperimentalTextApi::class)
@Suppress("ComposableNaming")
@Composable
fun InterTypography(): Typography {
    val googleSansCode = FontFamily(
        Font(R.font.google_sans_code),
    )

    val googleSansCodeMedium = FontFamily(
        Font(
            R.font.google_sans_code,
            variationSettings = FontVariation.Settings(
                FontVariation.weight(500)
            )
        ),

        )

    val googleSansCodeSemiBold = FontFamily(
        Font(
            R.font.google_sans_code,
            variationSettings = FontVariation.Settings(
                FontVariation.weight(600)
            )
        ),

        )

    val googleSansCodeBold = FontFamily(
        Font(
            R.font.google_sans_code,
            variationSettings = FontVariation.Settings(
                FontVariation.weight(700)
            )
        ),

        )


    return with(MaterialTheme.typography) {
        copy(
            displayLarge = displayLarge
                .copy(fontFamily = googleSansCode, fontWeight = FontWeight.Bold),
            displayMedium = displayMedium.copy(
                fontFamily = googleSansCode,
                fontWeight = FontWeight.Bold
            ),
            displaySmall = displaySmall.copy(
                fontFamily = googleSansCode,
                fontWeight = FontWeight.Bold
            ),
            headlineLarge = headlineLarge.copy(
                fontFamily = googleSansCode,
                fontWeight = FontWeight.Bold,
            ),
            headlineMedium = headlineMedium.copy(
                fontFamily = googleSansCodeBold,
                letterSpacing = (-2.5).sp,
                fontSize = 40.sp
            ),
            headlineSmall = headlineSmall.copy(
                fontFamily = googleSansCode,
                fontWeight = FontWeight.Bold,
            ),
            titleLarge = titleLarge.copy(
                fontFamily = googleSansCodeMedium,
                fontSize = 18.sp,
                letterSpacing = (-18*0.03).sp,
                lineHeight = (18*1.25).sp
            ),
            titleMedium = titleMedium.copy(
                fontFamily = googleSansCodeMedium,
                fontSize = 15.sp,
                letterSpacing = (-15*0.03).sp,
                lineHeight = (15*1.25).sp
            ),
            titleSmall = titleSmall.copy(
                fontFamily = googleSansCodeMedium,
                fontSize = 14.sp,
                letterSpacing = (-14*0.03).sp,
                lineHeight = (14*1.25).sp
            ),

            labelLarge = labelLarge.copy(
                fontFamily = googleSansCodeSemiBold,
                fontSize = 14.sp,
                lineHeight = 17.5.sp,
                letterSpacing = (-0.5).sp
            ),
            labelMedium = labelMedium.copy(
                fontFamily = googleSansCodeSemiBold,
                fontSize = 12.sp,
                lineHeight = 15.sp,
                letterSpacing = (-0.35).sp
            ),
            labelSmall = labelSmall.copy(
                fontFamily = googleSansCodeSemiBold,
                fontSize = 10.sp,
                lineHeight = 12.5.sp,
                letterSpacing = (-0.3).sp
            ),

            bodyLarge = bodyLarge.copy(fontFamily = googleSansCode, fontWeight = FontWeight.Normal),
            bodyMedium = bodyMedium.copy(
                fontFamily = googleSansCode,
                fontWeight = FontWeight.Normal
            ),
            bodySmall = bodySmall.copy(fontFamily = googleSansCode, fontWeight = FontWeight.Normal),
        )
    }
}

