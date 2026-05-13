package com.harsh.fittrack.ui.feature.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.harsh.fittrack.core.util.DurationFormatter
import com.harsh.fittrack.core.util.WeightFormatter
import com.harsh.fittrack.domain.model.Units
import com.harsh.fittrack.domain.model.Workout
import com.harsh.fittrack.feature.home.HomeState
import com.harsh.fittrack.feature.home.HomeViewModel
import com.harsh.fittrack.resources.Res
import com.harsh.fittrack.resources.home_empty_subtitle
import com.harsh.fittrack.resources.home_empty_title
import com.harsh.fittrack.resources.home_recent_workouts
import com.harsh.fittrack.resources.home_see_all
import com.harsh.fittrack.resources.home_start_workout
import com.harsh.fittrack.resources.stat_days
import com.harsh.fittrack.resources.stat_streak
import com.harsh.fittrack.resources.stat_this_week
import com.harsh.fittrack.resources.stat_workouts
import com.harsh.fittrack.ui.component.PrimaryButton
import com.harsh.fittrack.ui.theme.FitTrackTheme
import com.harsh.fittrack.ui.theme.OutlineVariant
import com.harsh.fittrack.ui.theme.SurfaceContainerHigh
import com.harsh.fittrack.ui.theme.SurfaceContainerHighest
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun HomeScreen(
    onWorkoutClick: (workoutId: String) -> Unit,
    onStartWorkout: () -> Unit,
) {
    val vm: HomeViewModel = koinViewModel()
    val state by vm.state.collectAsStateWithLifecycle()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(FitTrackTheme.colors.surface),
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding(),
            contentPadding = PaddingValues(bottom = 24.dp),
        ) {
            // ── Header ────────────────────────────────────────────────────
            item {
                HomeHeader(
                    greeting = state.greeting,
                    firstName = state.firstName,
                    modifier = Modifier.padding(
                        horizontal = FitTrackTheme.spacing.containerMargin,
                        vertical = FitTrackTheme.spacing.md,
                    ),
                )
            }

            // ── Stats strip ───────────────────────────────────────────────
            item {
                StatsStrip(
                    state = state,
                    modifier = Modifier
                        .padding(horizontal = FitTrackTheme.spacing.containerMargin)
                        .padding(bottom = FitTrackTheme.spacing.lg),
                )
            }

            // ── Start workout CTA ─────────────────────────────────────────
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = FitTrackTheme.spacing.containerMargin)
                        .padding(bottom = FitTrackTheme.spacing.lg),
                ) {
                    PrimaryButton(
                        text = stringResource(Res.string.home_start_workout),
                        onClick = onStartWorkout,
                    )
                }
            }

            // ── Recent workouts header ────────────────────────────────────
            item {
                SectionHeader(
                    title = stringResource(Res.string.home_recent_workouts),
                    action = stringResource(Res.string.home_see_all),
                    onAction = { /* TODO: navigate to full history */ },
                    modifier = Modifier
                        .padding(horizontal = FitTrackTheme.spacing.containerMargin)
                        .padding(bottom = FitTrackTheme.spacing.sm),
                )
            }

            // ── Workout cards ─────────────────────────────────────────────
            if (state.recentWorkouts.isEmpty() && !state.isLoading) {
                item { EmptyWorkoutsCard() }
            } else {
                items(state.recentWorkouts, key = { it.id }) { workout ->
                    WorkoutCard(
                        workout = workout,
                        today = state.today,
                        onClick = { onWorkoutClick(workout.id) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = FitTrackTheme.spacing.containerMargin)
                            .padding(bottom = FitTrackTheme.spacing.sm),
                    )
                }
            }
        }
    }
}

// ── Header ────────────────────────────────────────────────────────────────────

@Composable
private fun HomeHeader(
    greeting: String,
    firstName: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column {
            if (greeting.isNotEmpty()) {
                Text(
                    text = greeting,
                    style = FitTrackTheme.typography.bodyMedium,
                    color = FitTrackTheme.colors.onSurfaceVariant,
                )
            }
            if (firstName.isNotEmpty()) {
                Text(
                    text = firstName,
                    style = FitTrackTheme.typography.headlineLarge,
                    color = FitTrackTheme.colors.onSurface,
                    fontWeight = FontWeight.Bold,
                )
            } else {
                Text(
                    text = "Welcome back",
                    style = FitTrackTheme.typography.headlineLarge,
                    color = FitTrackTheme.colors.onSurface,
                    fontWeight = FontWeight.Bold,
                )
            }
        }

        // Notification icon placeholder
        NotificationIconPlaceholder()
    }
}

@Composable
private fun NotificationIconPlaceholder() {
    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(SurfaceContainerHigh)
            .clickable { },
        contentAlignment = Alignment.Center,
    ) {
        // Bell shape hint — replace with Icon(painter=...) when asset is ready
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Box(
                Modifier
                    .size(14.dp, 10.dp)
                    .background(
                        FitTrackTheme.colors.onSurfaceVariant,
                        RoundedCornerShape(topStart = 7.dp, topEnd = 7.dp, bottomStart = 2.dp, bottomEnd = 2.dp),
                    )
            )
            Box(
                Modifier
                    .size(6.dp, 3.dp)
                    .background(FitTrackTheme.colors.onSurfaceVariant, RoundedCornerShape(bottomStart = 3.dp, bottomEnd = 3.dp))
            )
        }
    }
}

// ── Stats strip ───────────────────────────────────────────────────────────────

@Composable
private fun StatsStrip(
    state: HomeState,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(FitTrackTheme.spacing.sm),
    ) {
        StatCard(
            value = state.streakDays.toString(),
            label = stringResource(Res.string.stat_streak),
            unit = stringResource(Res.string.stat_days),
            modifier = Modifier.weight(1f),
        )
        StatCard(
            value = state.workoutsThisWeek.toString(),
            label = stringResource(Res.string.stat_this_week),
            unit = stringResource(Res.string.stat_workouts),
            modifier = Modifier.weight(1f),
        )
        StatCard(
            value = state.totalWorkouts.toString(),
            label = "TOTAL",
            unit = stringResource(Res.string.stat_workouts),
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun StatCard(
    value: String,
    label: String,
    unit: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .background(SurfaceContainerHigh, RoundedCornerShape(12.dp))
            .padding(FitTrackTheme.spacing.md),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Text(
            text = value,
            style = FitTrackTheme.typography.headlineMedium,
            color = FitTrackTheme.colors.primary,
            fontWeight = FontWeight.ExtraBold,
        )
        Text(
            text = label,
            style = FitTrackTheme.typography.labelSmall,
            color = FitTrackTheme.colors.onSurfaceVariant,
        )
        Text(
            text = unit,
            style = FitTrackTheme.typography.labelSmall,
            color = FitTrackTheme.colors.onSurfaceVariant.copy(alpha = 0.6f),
        )
    }
}

// ── Section header ────────────────────────────────────────────────────────────

@Composable
private fun SectionHeader(
    title: String,
    action: String,
    onAction: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title,
            style = FitTrackTheme.typography.labelLarge,
            color = FitTrackTheme.colors.onSurfaceVariant,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            text = action,
            style = FitTrackTheme.typography.labelSmall,
            color = FitTrackTheme.colors.primary,
            modifier = Modifier
                .clip(RoundedCornerShape(4.dp))
                .clickable(onClick = onAction)
                .padding(horizontal = 4.dp, vertical = 2.dp),
        )
    }
}

// ── Workout card ──────────────────────────────────────────────────────────────

@Composable
private fun WorkoutCard(
    workout: Workout,
    today: LocalDate?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val dateLabel = today?.let { workout.date.relativeLabel(it) } ?: ""

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(SurfaceContainerHigh)
            .clickable(onClick = onClick),
    ) {
        // Lime accent strip
        Box(
            modifier = Modifier
                .width(4.dp)
                .height(88.dp)
                .background(FitTrackTheme.colors.primaryContainer),
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = FitTrackTheme.spacing.md, vertical = FitTrackTheme.spacing.md),
            verticalArrangement = Arrangement.spacedBy(FitTrackTheme.spacing.xs),
        ) {
            // Title + date
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Text(
                    text = workout.title,
                    style = FitTrackTheme.typography.bodyLarge,
                    color = FitTrackTheme.colors.onSurface,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false),
                )
                Text(
                    text = dateLabel,
                    style = FitTrackTheme.typography.labelSmall,
                    color = FitTrackTheme.colors.onSurfaceVariant,
                    modifier = Modifier.padding(start = FitTrackTheme.spacing.sm),
                )
            }

            // Metrics row
            Row(
                horizontalArrangement = Arrangement.spacedBy(FitTrackTheme.spacing.md),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                MetricChip(value = DurationFormatter.minutes(workout.durationSeconds))

                if (workout.totalVolumeKg > 0.0) {
                    MetricDivider()
                    MetricChip(value = WeightFormatter.format(workout.totalVolumeKg, Units.KG))
                }
            }
        }

        // Chevron placeholder — replace with Icon when asset ready
        Box(
            modifier = Modifier
                .padding(end = FitTrackTheme.spacing.md)
                .align(Alignment.CenterVertically)
                .size(20.dp),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                Modifier
                    .size(6.dp, 10.dp)
                    .background(
                        FitTrackTheme.colors.onSurfaceVariant.copy(alpha = 0.4f),
                        RoundedCornerShape(2.dp),
                    )
            )
        }
    }
}

@Composable
private fun MetricChip(value: String) {
    Text(
        text = value,
        style = FitTrackTheme.typography.labelMedium,
        color = FitTrackTheme.colors.onSurfaceVariant,
    )
}

@Composable
private fun MetricDivider() {
    Box(
        Modifier
            .size(3.dp)
            .background(FitTrackTheme.colors.onSurfaceVariant.copy(alpha = 0.3f), CircleShape)
    )
}

// ── Empty state ───────────────────────────────────────────────────────────────

@Composable
private fun EmptyWorkoutsCard() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = FitTrackTheme.spacing.containerMargin)
            .background(SurfaceContainerHighest, RoundedCornerShape(12.dp))
            .padding(FitTrackTheme.spacing.lg),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(FitTrackTheme.spacing.sm),
    ) {
        // Dumbbell icon placeholder
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(FitTrackTheme.colors.primaryContainer.copy(alpha = 0.2f), CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Box(Modifier.size(24.dp, 6.dp).background(FitTrackTheme.colors.primary, RoundedCornerShape(3.dp)))
        }

        Text(
            text = stringResource(Res.string.home_empty_title),
            style = FitTrackTheme.typography.bodyLarge,
            color = FitTrackTheme.colors.onSurface,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            text = stringResource(Res.string.home_empty_subtitle),
            style = FitTrackTheme.typography.bodyMedium,
            color = FitTrackTheme.colors.onSurfaceVariant,
        )
    }
}

// ── Date helpers ──────────────────────────────────────────────────────────────

private fun LocalDate.relativeLabel(today: LocalDate): String = when (this) {
    today -> "Today"
    today.minus(DatePeriod(days = 1)) -> "Yesterday"
    else -> "${month.name.take(3).lowercase().replaceFirstChar { it.uppercaseChar() }} $dayOfMonth"
}
