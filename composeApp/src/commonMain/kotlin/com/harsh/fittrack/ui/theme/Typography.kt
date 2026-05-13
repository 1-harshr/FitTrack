package com.harsh.fittrack.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp

// TODO: Replace with loaded font resources (Lexend, Inter, JetBrains Mono)
// val LexendFamily = FontFamily(Font(Res.font.Lexend_Bold, FontWeight.Bold), ...)
val LexendFamily = FontFamily.Default
val InterFamily = FontFamily.Default
val JetBrainsMonoFamily = FontFamily.Monospace

val FitTrackTypography = Typography(
    // display-lg — workout hero numbers, splash screens
    displayLarge = TextStyle(
        fontFamily = LexendFamily,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 48.sp,
        lineHeight = 56.sp,
        letterSpacing = (-0.02).em,
    ),
    // headline-lg — screen titles
    headlineLarge = TextStyle(
        fontFamily = LexendFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp,
        lineHeight = 40.sp,
        letterSpacing = (-0.01).em,
    ),
    // headline-lg-mobile — section headers on mobile
    headlineMedium = TextStyle(
        fontFamily = LexendFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        lineHeight = 36.sp,
    ),
    // headline-md — card titles, sub-section headers
    headlineSmall = TextStyle(
        fontFamily = LexendFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp,
        lineHeight = 32.sp,
    ),
    // body-lg — primary readable content
    bodyLarge = TextStyle(
        fontFamily = InterFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 18.sp,
        lineHeight = 28.sp,
    ),
    // body-md — secondary content, descriptions
    bodyMedium = TextStyle(
        fontFamily = InterFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
    ),
    bodySmall = TextStyle(
        fontFamily = InterFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
    ),
    // label-caps — JetBrains Mono for data readouts, timestamps
    labelLarge = TextStyle(
        fontFamily = JetBrainsMonoFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.1.em,
    ),
    labelMedium = TextStyle(
        fontFamily = JetBrainsMonoFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.08.em,
    ),
    labelSmall = TextStyle(
        fontFamily = JetBrainsMonoFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 10.sp,
        lineHeight = 14.sp,
        letterSpacing = 0.06.em,
    ),
)
