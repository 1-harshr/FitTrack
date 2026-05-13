package com.harsh.fittrack.ui.feature.record

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.harsh.fittrack.resources.Res
import com.harsh.fittrack.resources.complete_cta
import com.harsh.fittrack.resources.complete_headline
import com.harsh.fittrack.resources.complete_sub
import com.harsh.fittrack.ui.theme.FitTrackTheme
import org.jetbrains.compose.resources.stringResource

@Composable
fun WorkoutCompleteScreen(
    onDone: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(FitTrackTheme.colors.surface),
    ) {
        // Background gradient from primary tint
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            FitTrackTheme.colors.primary.copy(alpha = 0.12f),
                            FitTrackTheme.colors.surface,
                        )
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .padding(horizontal = FitTrackTheme.spacing.containerMargin),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            // Trophy / celebration placeholder
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .background(FitTrackTheme.colors.primary.copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .background(FitTrackTheme.colors.primary.copy(alpha = 0.3f), CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    // Star shape placeholder — four boxes rotated
                    Box(Modifier.size(32.dp, 32.dp)) {
                        Box(
                            Modifier
                                .align(Alignment.Center)
                                .size(28.dp, 10.dp)
                                .background(FitTrackTheme.colors.primary, RoundedCornerShape(3.dp))
                        )
                        Box(
                            Modifier
                                .align(Alignment.Center)
                                .size(10.dp, 28.dp)
                                .background(FitTrackTheme.colors.primary, RoundedCornerShape(3.dp))
                        )
                    }
                }
            }

            Box(Modifier.height(32.dp))

            Text(
                text = stringResource(Res.string.complete_headline),
                style = FitTrackTheme.typography.headlineLarge,
                color = FitTrackTheme.colors.onSurface,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center,
            )

            Box(Modifier.height(12.dp))

            Text(
                text = stringResource(Res.string.complete_sub),
                style = FitTrackTheme.typography.bodyMedium,
                color = FitTrackTheme.colors.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )

            Box(Modifier.height(48.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(FitTrackTheme.colors.primary)
                    .clickable(onClick = onDone),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = stringResource(Res.string.complete_cta),
                    style = FitTrackTheme.typography.labelLarge,
                    color = FitTrackTheme.colors.onPrimary,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}
