package com.github.orgf.utils.ui

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

object FolderPickerUiTokens {
    val screenTop = Color(0xFF041C2D)
    val screenBottom = Color(0xFF030B14)
    val logoCardColor = Color(0xFF0E2D46)
    val textMuted = Color(0xFF9AACBF)
    val privacyAccent = Color(0xFF1EA8F5)
    val primaryButtonText = Color(0xFF021321)
    val deepShadow = Color(0xFF02080F)
    val surfaceHighlight = Color(0x2E9FD8FF)

    val screenHorizontalPadding = 30.dp
    val topSectionSpacing = 98.dp
    val logoToTitleSpacing = 40.dp
    val titleToSubtitleSpacing = 10.dp
    val headerToButtonSpacing = 102.dp
    val buttonToPrivacySpacing = 64.dp
    val privacyLabelToBodySpacing = 22.dp
    val bottomSectionSpacing = 52.dp
    val privacyBodyMaxWidth = 332.dp

    val boldDepthStyle = FolderPickerDepthStyle(
        logoGlowElevation = 34.dp,
        logoGlowAmbientAlpha = 0.24f,
        logoGlowSpotAlpha = 0.2f,
        logoCardElevation = 16.dp,
        logoCardShadowAlpha = 0.7f,
        badgeElevation = 14.dp,
        badgeAmbientAlpha = 0.65f,
        badgeSpotAlpha = 0.45f,
        buttonElevation = 24.dp,
        buttonAmbientAlpha = 0.78f,
        buttonSpotAlpha = 0.38f,
        buttonBorderAlpha = 0.12f,
        privacyChipElevation = 12.dp,
        privacyChipAmbientAlpha = 0.55f,
        privacyChipSpotAlpha = 0.18f,
        privacyChipBackgroundAlpha = 0.08f,
        privacyChipBorderAlpha = 0.24f
    )

    val brandTitleStyle = TextStyle(
        fontFamily = FontFamily.Default,
        fontSize = 54.sp,
        lineHeight = 54.sp,
        letterSpacing = (-0.8).sp,
        fontWeight = FontWeight.Bold,
        platformStyle = PlatformTextStyle(includeFontPadding = false)
    )

    val brandSubtitleStyle = TextStyle(
        fontFamily = FontFamily.Default,
        fontSize = 18.sp,
        lineHeight = 25.sp,
        letterSpacing = 1.2.sp,
        fontWeight = FontWeight.Normal,
        platformStyle = PlatformTextStyle(includeFontPadding = false)
    )

    val ctaTextStyle = TextStyle(
        fontFamily = FontFamily.Default,
        fontSize = 20.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.sp,
        fontWeight = FontWeight.SemiBold,
        platformStyle = PlatformTextStyle(includeFontPadding = false)
    )

    val privacyLabelStyle = TextStyle(
        fontFamily = FontFamily.Default,
        fontSize = 12.sp,
        lineHeight = 15.sp,
        letterSpacing = 3.8.sp,
        fontWeight = FontWeight.SemiBold,
        platformStyle = PlatformTextStyle(includeFontPadding = false)
    )

    val privacyBodyStyle = TextStyle(
        fontFamily = FontFamily.Default,
        fontSize = 17.sp,
        lineHeight = 23.sp,
        letterSpacing = 0.1.sp,
        fontWeight = FontWeight.Normal,
        platformStyle = PlatformTextStyle(includeFontPadding = false)
    )
}

data class FolderPickerDepthStyle(
    val logoGlowElevation: Dp,
    val logoGlowAmbientAlpha: Float,
    val logoGlowSpotAlpha: Float,
    val logoCardElevation: Dp,
    val logoCardShadowAlpha: Float,
    val badgeElevation: Dp,
    val badgeAmbientAlpha: Float,
    val badgeSpotAlpha: Float,
    val buttonElevation: Dp,
    val buttonAmbientAlpha: Float,
    val buttonSpotAlpha: Float,
    val buttonBorderAlpha: Float,
    val privacyChipElevation: Dp,
    val privacyChipAmbientAlpha: Float,
    val privacyChipSpotAlpha: Float,
    val privacyChipBackgroundAlpha: Float,
    val privacyChipBorderAlpha: Float
)

