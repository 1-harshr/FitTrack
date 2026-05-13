package com.harsh.fittrack.ui.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.harsh.fittrack.ui.theme.FitTrackTheme
import com.harsh.fittrack.ui.theme.OnSurface
import com.harsh.fittrack.ui.theme.OutlineVariant

/** Lime-green filled CTA — primary actions (Start Workout, Save, etc.). */
@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false,
) {
    Button(
        onClick = onClick,
        enabled = enabled && !isLoading,
        modifier = modifier
            .fillMaxWidth()
            .height(FitTrackTheme.spacing.touchTargetPreferred),
        shape = FitTrackTheme.shapes.small,
        colors = ButtonDefaults.buttonColors(
            containerColor = FitTrackTheme.colors.primaryContainer,
            contentColor = FitTrackTheme.colors.onPrimaryContainer,
            disabledContainerColor = FitTrackTheme.colors.primaryContainer.copy(alpha = 0.38f),
            disabledContentColor = FitTrackTheme.colors.onPrimaryContainer.copy(alpha = 0.38f),
        ),
        contentPadding = PaddingValues(horizontal = FitTrackTheme.spacing.lg),
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                color = FitTrackTheme.colors.onPrimaryContainer,
                strokeWidth = 2.dp,
            )
        } else {
            Text(
                text = text,
                style = FitTrackTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

/** Transparent outlined button — secondary / destructive actions. */
@Composable
fun GhostButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    contentColor: Color = FitTrackTheme.colors.onSurface,
) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .fillMaxWidth()
            .height(FitTrackTheme.spacing.touchTargetPreferred),
        shape = FitTrackTheme.shapes.small,
        border = BorderStroke(1.dp, OutlineVariant),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = contentColor,
            disabledContentColor = contentColor.copy(alpha = 0.38f),
        ),
        contentPadding = PaddingValues(horizontal = FitTrackTheme.spacing.lg),
    ) {
        Text(
            text = text,
            style = FitTrackTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
        )
    }
}

/**
 * OAuth provider button (Google / Apple).
 * Badge on the left keeps the provider recognisable; label centred.
 */
@Composable
fun SocialButton(
    text: String,
    providerLabel: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    filled: Boolean = false,
) {
    if (filled) {
        Button(
            onClick = onClick,
            enabled = enabled && !isLoading,
            modifier = modifier
                .fillMaxWidth()
                .height(FitTrackTheme.spacing.touchTargetPreferred),
            shape = FitTrackTheme.shapes.small,
            colors = ButtonDefaults.buttonColors(
                containerColor = OnSurface,
                contentColor = FitTrackTheme.colors.surface,
            ),
            contentPadding = PaddingValues(horizontal = FitTrackTheme.spacing.lg),
        ) {
            SocialButtonContent(text, providerLabel, isLoading, FitTrackTheme.colors.surface)
        }
    } else {
        OutlinedButton(
            onClick = onClick,
            enabled = enabled && !isLoading,
            modifier = modifier
                .fillMaxWidth()
                .height(FitTrackTheme.spacing.touchTargetPreferred),
            shape = FitTrackTheme.shapes.small,
            border = BorderStroke(1.dp, OutlineVariant),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = FitTrackTheme.colors.onSurface,
            ),
            contentPadding = PaddingValues(horizontal = FitTrackTheme.spacing.lg),
        ) {
            SocialButtonContent(text, providerLabel, isLoading, FitTrackTheme.colors.onSurface)
        }
    }
}

@Composable
private fun SocialButtonContent(text: String, providerLabel: String, isLoading: Boolean, tint: Color) {
    Box(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = providerLabel,
            modifier = Modifier.align(Alignment.CenterStart),
            style = FitTrackTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = tint,
        )
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp).align(Alignment.Center),
                color = tint,
                strokeWidth = 2.dp,
            )
        } else {
            Text(
                text = text,
                modifier = Modifier.align(Alignment.Center),
                style = FitTrackTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = tint,
            )
        }
    }
}
