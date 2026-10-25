package com.localgrok.ui.theme

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

enum class AppTheme {
    SPACE,
    DARK,
}

data class LocalGrokColors(
    val background: Color,
    val surface: Color,
    val darkGrey: Color,
    val mediumGrey: Color,
    val borderGrey: Color,
    val lightGrey: Color,
    val pillActive: Color,
    val pillInactive: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textDim: Color,
    val textMuted: Color,
    val textSubtle: Color,
    val accent: Color,
    val accentSecondary: Color,
    val userBubble: Color,
    val aiBubbleBorder: Color,
    val inputBackground: Color,
    val inputBorder: Color,
    val inputBorderFocused: Color,
    val error: Color,
    val success: Color,
)

val DarkColors =
    LocalGrokColors(
        background = Color(0xFF1C1C1E),
        surface = Color(0xFF242426),
        darkGrey = Color(0xFF2C2C2E),
        mediumGrey = Color(0xFF3A3A3C),
        borderGrey = Color(0xFF48484A),
        lightGrey = Color(0xFF545456),
        pillActive = Color(0xFF636366),
        pillInactive = Color(0xFF3A3A3C),
        textPrimary = Color(0xFFFFFFFF),
        textSecondary = Color(0xFFA1A1A6),
        textDim = Color(0xFF7C7C80),
        textMuted = Color(0xFF545456),
        textSubtle = Color(0xFF8E8E93),
        accent = Color(0xFF0A84FF),
        accentSecondary = Color(0xFF5E5CE6),
        userBubble = Color(0xFF2C2C2E),
        aiBubbleBorder = Color(0xFF48484A),
        inputBackground = Color(0xFF2C2C2E),
        inputBorder = Color(0xFF48484A),
        inputBorderFocused = Color(0xFF636366),
        error = Color(0xFFFF453A),
        success = Color(0xFF30D158),
    )

val SpaceColors =
    LocalGrokColors(
        background = Color(0xFF050505),
        surface = Color(0xFF0C0C0E),
        darkGrey = Color(0xFF1C1C1E),
        mediumGrey = Color(0xFF2C2C2E),
        borderGrey = Color(0xFF3A3A3C),
        lightGrey = Color(0xFF48484A),
        pillActive = Color(0xFF636366),
        pillInactive = Color(0xFF2C2C2E),
        textPrimary = Color(0xFFF5F5F7),
        textSecondary = Color(0xFF98989D),
        textDim = Color(0xFF6E6E73),
        textMuted = Color(0xFF48484A),
        textSubtle = Color(0xFF8E8E93),
        accent = Color(0xFF0A84FF),
        accentSecondary = Color(0xFF5E5CE6),
        userBubble = Color(0xFF1C1C1E),
        aiBubbleBorder = Color(0xFF3A3A3C),
        inputBackground = Color(0xFF1C1C1E),
        inputBorder = Color(0xFF3A3A3C),
        inputBorderFocused = Color(0xFF636366),
        error = Color(0xFFFF453A),
        success = Color(0xFF30D158),
    )

val LocalAppColors = staticCompositionLocalOf { DarkColors }
val LocalAppTheme = compositionLocalOf { AppTheme.DARK }

val TrueBlack = Color(0xFF000000)
val DeepCarbon = Color(0xFF0A0A0A)
val DarkSurface = Color(0xFF0D0D0D)
val CardBackground = Color(0xFF111111)

val GrokDarkGrey = Color(0xFF1A1A1A)
val GrokMediumGrey = Color(0xFF2A2A2A)
val GrokBorderGrey = Color(0xFF333333)
val GrokLightGrey = Color(0xFF3A3A3A)
val GrokPillActive = Color(0xFF4A4A4A)
val GrokPillInactive = Color(0xFF2A2A2A)

val TextWhite = Color(0xFFFFFFFF)
val TextGrey = Color(0xFF808080)
val TextDimGrey = Color(0xFF5A5A5A)
val TextMuted = Color(0xFF3A3A3A)
val TextSubtle = Color(0xFF666666)

val AccentBlue = Color(0xFF1DA1F2)
val MatrixGreen = Color(0xFF00FF41)
val MatrixGreenDark = Color(0xFF008F00)
val MatrixGreenDeep = Color(0xFF006400)
val MatrixGreenGlow = Color(0x2000FF41)

val UserBubble = Color(0xFF1A1A1A)
val AiBubbleBackground = Color(0x00000000)
val AiBubbleBorder = Color(0xFF333333)

val InputBackground = Color(0xFF1A1A1A)
val InputBorder = Color(0xFF333333)
val InputBorderFocused = Color(0xFF4A4A4A)

val ErrorRed = Color(0xFFFF4444)
val ErrorContainer = Color(0xFF93000A)
val OnErrorContainer = Color(0xFFFFDAD6)
val WarningYellow = Color(0xFFFFAA00)
val SuccessGreen = Color(0xFF00FF41)

val GridLineColor = Color(0xFF1A1A1A)
val GridFadeColor = Color(0xFF0A0A0A)
