package com.harsh.fittrack.ui.feature.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.harsh.fittrack.feature.home.CoachingInsight
import com.harsh.fittrack.ui.theme.FitTrackTheme

// Simple vector icons drawn inline since we use placeholder icons elsewhere
private object Icons {
    // No vector icons available, using text symbols
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CoachCard(
    insight: CoachingInsight?,
    isLoading: Boolean,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(FitTrackTheme.colors.primary.copy(alpha = 0.08f))
            .padding(16.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { if (insight != null) expanded = !expanded },
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .background(FitTrackTheme.colors.primary, RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp),
                    ) {
                        Text(
                            text = "AI Coach",
                            style = FitTrackTheme.typography.labelSmall,
                            color = FitTrackTheme.colors.onPrimary,
                        )
                    }
                    if (insight != null) {
                        Text(
                            text = if (expanded) " ▲" else " ▼",
                            style = FitTrackTheme.typography.labelSmall,
                            color = FitTrackTheme.colors.onSurfaceVariant,
                        )
                    }
                }
                Spacer(Modifier.height(6.dp))
                when {
                    isLoading -> ShimmerText()
                    insight != null -> Text(
                        text = insight.dailyTip,
                        style = FitTrackTheme.typography.bodyMedium,
                        color = FitTrackTheme.colors.onSurface,
                    )
                    else -> Text(
                        text = "Tap refresh to get your daily coaching insight.",
                        style = FitTrackTheme.typography.bodyMedium,
                        color = FitTrackTheme.colors.onSurfaceVariant,
                    )
                }
            }

            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = FitTrackTheme.colors.primary,
                    strokeWidth = 2.dp,
                )
            } else {
                Text(
                    text = "↻",
                    style = FitTrackTheme.typography.bodyLarge,
                    color = FitTrackTheme.colors.primary,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onRefresh() }
                        .padding(8.dp),
                )
            }
        }

        AnimatedVisibility(
            visible = expanded && insight != null,
            enter = expandVertically(),
            exit = shrinkVertically(),
        ) {
            insight ?: return@AnimatedVisibility
            Column(modifier = Modifier.padding(top = 12.dp)) {
                if (insight.targetMuscleGroups.isNotEmpty()) {
                    Text(
                        text = "Target Today",
                        style = FitTrackTheme.typography.labelMedium,
                        color = FitTrackTheme.colors.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 4.dp),
                    )
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        insight.targetMuscleGroups.forEach { muscle ->
                            Box(
                                modifier = Modifier
                                    .padding(bottom = 4.dp)
                                    .background(
                                        FitTrackTheme.colors.primary.copy(alpha = 0.15f),
                                        RoundedCornerShape(20.dp),
                                    )
                                    .padding(horizontal = 10.dp, vertical = 4.dp),
                            ) {
                                Text(
                                    text = muscle.replaceFirstChar { it.uppercase() }.lowercase()
                                        .replaceFirstChar { it.uppercase() },
                                    style = FitTrackTheme.typography.labelSmall,
                                    color = FitTrackTheme.colors.primary,
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(10.dp))
                }

                if (insight.progressionSuggestions.isNotEmpty()) {
                    Text(
                        text = "Progression",
                        style = FitTrackTheme.typography.labelMedium,
                        color = FitTrackTheme.colors.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 4.dp),
                    )
                    insight.progressionSuggestions.forEach { s ->
                        Column(modifier = Modifier.padding(bottom = 6.dp)) {
                            Text(
                                text = s.exerciseName,
                                style = FitTrackTheme.typography.bodyMedium,
                                color = FitTrackTheme.colors.onSurface,
                            )
                            Text(
                                text = s.suggestion,
                                style = FitTrackTheme.typography.bodySmall,
                                color = FitTrackTheme.colors.onSurfaceVariant,
                            )
                        }
                    }
                    Spacer(Modifier.height(10.dp))
                }

                if (insight.weaknesses.isNotEmpty()) {
                    Text(
                        text = "Undertrained",
                        style = FitTrackTheme.typography.labelMedium,
                        color = FitTrackTheme.colors.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 4.dp),
                    )
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        insight.weaknesses.forEach { muscle ->
                            Box(
                                modifier = Modifier
                                    .padding(bottom = 4.dp)
                                    .background(
                                        FitTrackTheme.colors.error.copy(alpha = 0.1f),
                                        RoundedCornerShape(20.dp),
                                    )
                                    .padding(horizontal = 10.dp, vertical = 4.dp),
                            ) {
                                Text(
                                    text = muscle.replaceFirstChar { it.uppercase() }.lowercase()
                                        .replaceFirstChar { it.uppercase() },
                                    style = FitTrackTheme.typography.labelSmall,
                                    color = FitTrackTheme.colors.error,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ShimmerText() {
    val alpha by rememberInfiniteTransition(label = "shimmer").animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "shimmerAlpha",
    )
    Box(
        modifier = Modifier
            .fillMaxWidth(0.85f)
            .height(16.dp)
            .alpha(alpha)
            .background(FitTrackTheme.colors.onSurfaceVariant.copy(alpha = 0.2f), RoundedCornerShape(4.dp)),
    )
}
