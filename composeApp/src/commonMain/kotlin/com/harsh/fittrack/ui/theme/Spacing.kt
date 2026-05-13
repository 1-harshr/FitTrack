package com.harsh.fittrack.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Immutable
data class FitTrackSpacing(
    val unit: Dp = 4.dp,
    val xs: Dp = 4.dp,
    val sm: Dp = 8.dp,
    val md: Dp = 16.dp,
    val lg: Dp = 24.dp,
    val xl: Dp = 32.dp,
    val xxl: Dp = 40.dp,
    val containerMargin: Dp = 20.dp,
    val gutter: Dp = 16.dp,
    val sectionGap: Dp = 40.dp,
    val touchTargetMin: Dp = 48.dp,
    val touchTargetPreferred: Dp = 56.dp,
    val cardPadding: Dp = 24.dp,
)

val LocalFitTrackSpacing = staticCompositionLocalOf { FitTrackSpacing() }
