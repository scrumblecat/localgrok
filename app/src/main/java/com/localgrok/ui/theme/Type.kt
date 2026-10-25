package com.localgrok.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.localgrok.R

val InterFont =
    FontFamily(
        Font(R.font.inter_regular, FontWeight.Normal),
        Font(R.font.inter_medium, FontWeight.Medium),
        Font(R.font.inter_semibold, FontWeight.SemiBold),
        Font(R.font.inter_bold, FontWeight.Bold),
    )

val JetBrainsMono =
    FontFamily(
        Font(R.font.jetbrains_mono_regular, FontWeight.Normal),
        Font(R.font.jetbrains_mono_medium, FontWeight.Medium),
        Font(R.font.jetbrains_mono_bold, FontWeight.Bold),
    )

val LocalGrokTypography =
    Typography(
        displayLarge =
            TextStyle(
                fontFamily = InterFont,
                fontWeight = FontWeight.Bold,
                fontSize = 57.sp,
                lineHeight = 64.sp,
                letterSpacing = (-0.25).sp,
                color = TextWhite,
            ),
        displayMedium =
            TextStyle(
                fontFamily = InterFont,
                fontWeight = FontWeight.Bold,
                fontSize = 45.sp,
                lineHeight = 52.sp,
                letterSpacing = 0.sp,
                color = TextWhite,
            ),
        displaySmall =
            TextStyle(
                fontFamily = InterFont,
                fontWeight = FontWeight.Bold,
                fontSize = 36.sp,
                lineHeight = 44.sp,
                letterSpacing = 0.sp,
                color = TextWhite,
            ),
        headlineLarge =
            TextStyle(
                fontFamily = InterFont,
                fontWeight = FontWeight.Bold,
                fontSize = 32.sp,
                lineHeight = 40.sp,
                letterSpacing = 0.sp,
                color = TextWhite,
            ),
        headlineMedium =
            TextStyle(
                fontFamily = InterFont,
                fontWeight = FontWeight.SemiBold,
                fontSize = 28.sp,
                lineHeight = 36.sp,
                letterSpacing = 0.sp,
                color = TextWhite,
            ),
        headlineSmall =
            TextStyle(
                fontFamily = InterFont,
                fontWeight = FontWeight.SemiBold,
                fontSize = 24.sp,
                lineHeight = 32.sp,
                letterSpacing = 0.sp,
                color = TextWhite,
            ),
        titleLarge =
            TextStyle(
                fontFamily = InterFont,
                fontWeight = FontWeight.SemiBold,
                fontSize = 22.sp,
                lineHeight = 28.sp,
                letterSpacing = 0.sp,
                color = TextWhite,
            ),
        titleMedium =
            TextStyle(
                fontFamily = InterFont,
                fontWeight = FontWeight.Medium,
                fontSize = 16.sp,
                lineHeight = 24.sp,
                letterSpacing = 0.15.sp,
                color = TextWhite,
            ),
        titleSmall =
            TextStyle(
                fontFamily = InterFont,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp,
                lineHeight = 20.sp,
                letterSpacing = 0.1.sp,
                color = TextWhite,
            ),
        bodyLarge =
            TextStyle(
                fontFamily = InterFont,
                fontWeight = FontWeight.Normal,
                fontSize = 16.sp,
                lineHeight = 24.sp,
                letterSpacing = 0.5.sp,
                color = TextWhite,
            ),
        bodyMedium =
            TextStyle(
                fontFamily = InterFont,
                fontWeight = FontWeight.Normal,
                fontSize = 14.sp,
                lineHeight = 20.sp,
                letterSpacing = 0.25.sp,
                color = TextWhite,
            ),
        bodySmall =
            TextStyle(
                fontFamily = InterFont,
                fontWeight = FontWeight.Normal,
                fontSize = 12.sp,
                lineHeight = 16.sp,
                letterSpacing = 0.4.sp,
                color = TextGrey,
            ),
        labelLarge =
            TextStyle(
                fontFamily = InterFont,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp,
                lineHeight = 20.sp,
                letterSpacing = 0.1.sp,
                color = TextWhite,
            ),
        labelMedium =
            TextStyle(
                fontFamily = InterFont,
                fontWeight = FontWeight.Medium,
                fontSize = 12.sp,
                lineHeight = 16.sp,
                letterSpacing = 0.5.sp,
                color = TextGrey,
            ),
        labelSmall =
            TextStyle(
                fontFamily = InterFont,
                fontWeight = FontWeight.Medium,
                fontSize = 11.sp,
                lineHeight = 16.sp,
                letterSpacing = 0.5.sp,
                color = TextDimGrey,
            ),
    )
