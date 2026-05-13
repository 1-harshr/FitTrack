package com.harsh.fittrack.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable

private val FitTrackDarkColorScheme = darkColorScheme(
    primary = Primary,
    onPrimary = OnPrimary,
    primaryContainer = PrimaryContainer,
    onPrimaryContainer = OnPrimaryContainer,
    inversePrimary = InversePrimary,
    secondary = Secondary,
    onSecondary = OnSecondary,
    secondaryContainer = SecondaryContainer,
    onSecondaryContainer = OnSecondaryContainer,
    tertiary = Tertiary,
    onTertiary = OnTertiary,
    tertiaryContainer = TertiaryContainer,
    onTertiaryContainer = OnTertiaryContainer,
    error = Error,
    onError = OnError,
    errorContainer = ErrorContainer,
    onErrorContainer = OnErrorContainer,
    background = Background,
    onBackground = OnBackground,
    surface = Surface,
    onSurface = OnSurface,
    onSurfaceVariant = OnSurfaceVariant,
    surfaceVariant = SurfaceVariant,
    surfaceTint = SurfaceTint,
    inverseSurface = InverseSurface,
    inverseOnSurface = InverseOnSurface,
    outline = Outline,
    outlineVariant = OutlineVariant,
    scrim = Scrim,
    surfaceBright = SurfaceBright,
    surfaceDim = SurfaceDim,
    surfaceContainer = SurfaceContainer,
    surfaceContainerHigh = SurfaceContainerHigh,
    surfaceContainerHighest = SurfaceContainerHighest,
    surfaceContainerLow = SurfaceContainerLow,
    surfaceContainerLowest = SurfaceContainerLowest,
)

@Composable
fun FitTrackTheme(
    spacing: FitTrackSpacing = FitTrackSpacing(),
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(LocalFitTrackSpacing provides spacing) {
        MaterialTheme(
            colorScheme = FitTrackDarkColorScheme,
            typography = FitTrackTypography,
            shapes = FitTrackShapes,
            content = content,
        )
    }
}

// Convenience accessors — use FitTrackTheme.spacing, .colors, etc. in composables
object FitTrackTheme {
    val colors
        @Composable @ReadOnlyComposable
        get() = MaterialTheme.colorScheme

    val typography
        @Composable @ReadOnlyComposable
        get() = MaterialTheme.typography

    val shapes
        @Composable @ReadOnlyComposable
        get() = MaterialTheme.shapes

    val spacing
        @Composable @ReadOnlyComposable
        get() = LocalFitTrackSpacing.current
}
