package com.harsh.fittrack.ui.feature.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.toRoute
import com.harsh.fittrack.core.util.DurationFormatter
import com.harsh.fittrack.domain.model.SetEntry
import com.harsh.fittrack.domain.repository.ExerciseWithSets
import com.harsh.fittrack.domain.repository.WorkoutWithDetails
import com.harsh.fittrack.feature.home.WorkoutDetailViewModel
import com.harsh.fittrack.navigation.Route
import com.harsh.fittrack.resources.Res
import com.harsh.fittrack.resources.workout_detail_duration
import com.harsh.fittrack.resources.workout_detail_empty
import com.harsh.fittrack.resources.workout_detail_exercises
import com.harsh.fittrack.resources.workout_detail_reps
import com.harsh.fittrack.resources.workout_detail_set
import com.harsh.fittrack.resources.workout_detail_volume
import com.harsh.fittrack.ui.theme.FitTrackTheme
import com.harsh.fittrack.ui.theme.SurfaceContainerHigh
import com.harsh.fittrack.ui.theme.SurfaceContainerHighest
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun WorkoutDetailScreen(
    backStackEntry: NavBackStackEntry,
    onBack: () -> Unit,
) {
    val route = backStackEntry.toRoute<Route.WorkoutDetail>()
    val vm: WorkoutDetailViewModel = koinViewModel { parametersOf(route.workoutId) }
    val state by vm.state.collectAsStateWithLifecycle()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(FitTrackTheme.colors.surface),
    ) {
        when {
            state.isLoading -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Box(
                        Modifier
                            .size(32.dp)
                            .background(FitTrackTheme.colors.primary.copy(alpha = 0.3f), CircleShape)
                    )
                }
            }
            state.details == null -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Workout not found", color = FitTrackTheme.colors.onSurface)
                }
            }
            else -> {
                WorkoutDetailContent(
                    details = state.details!!,
                    onBack = onBack,
                )
            }
        }
    }
}

@Composable
private fun WorkoutDetailContent(
    details: WorkoutWithDetails,
    onBack: () -> Unit,
) {
    val workout = details.workout

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding(),
        contentPadding = PaddingValues(bottom = 32.dp),
    ) {
        // ── Top bar ───────────────────────────────────────────────────
        item {
            WorkoutDetailTopBar(
                title = workout.title,
                onBack = onBack,
                modifier = Modifier
                    .padding(horizontal = FitTrackTheme.spacing.containerMargin)
                    .padding(top = FitTrackTheme.spacing.md, bottom = FitTrackTheme.spacing.md),
            )
        }

        // ── Stats strip ───────────────────────────────────────────────
        item {
            WorkoutStatsStrip(
                durationSeconds = workout.durationSeconds,
                totalVolumeKg = workout.totalVolumeKg,
                modifier = Modifier
                    .padding(horizontal = FitTrackTheme.spacing.containerMargin)
                    .padding(bottom = FitTrackTheme.spacing.lg),
            )
        }

        // ── Exercises header ──────────────────────────────────────────
        item {
            Text(
                text = stringResource(Res.string.workout_detail_exercises),
                style = FitTrackTheme.typography.labelLarge,
                color = FitTrackTheme.colors.onSurfaceVariant,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier
                    .padding(horizontal = FitTrackTheme.spacing.containerMargin)
                    .padding(bottom = FitTrackTheme.spacing.sm),
            )
        }

        if (details.exercises.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = FitTrackTheme.spacing.containerMargin)
                        .background(SurfaceContainerHigh, RoundedCornerShape(12.dp))
                        .padding(FitTrackTheme.spacing.lg),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = stringResource(Res.string.workout_detail_empty),
                        style = FitTrackTheme.typography.bodyMedium,
                        color = FitTrackTheme.colors.onSurfaceVariant,
                    )
                }
            }
        } else {
            items(details.exercises) { exerciseWithSets ->
                ExerciseBlock(
                    exerciseWithSets = exerciseWithSets,
                    modifier = Modifier
                        .padding(horizontal = FitTrackTheme.spacing.containerMargin)
                        .padding(bottom = FitTrackTheme.spacing.sm),
                )
            }
        }
    }
}

// ── Top bar ───────────────────────────────────────────────────────────────────

@Composable
private fun WorkoutDetailTopBar(
    title: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(FitTrackTheme.spacing.md),
    ) {
        // Back button placeholder
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(SurfaceContainerHigh)
                .clickable(onClick = onBack),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                Modifier
                    .size(8.dp, 14.dp)
                    .background(FitTrackTheme.colors.onSurface, RoundedCornerShape(2.dp))
            )
        }
        Text(
            text = title,
            style = FitTrackTheme.typography.headlineSmall,
            color = FitTrackTheme.colors.onSurface,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f),
        )
    }
}

// ── Stats strip ───────────────────────────────────────────────────────────────

@Composable
private fun WorkoutStatsStrip(
    durationSeconds: Long,
    totalVolumeKg: Double,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(FitTrackTheme.spacing.sm),
    ) {
        StatPill(
            label = stringResource(Res.string.workout_detail_duration),
            value = DurationFormatter.minutes(durationSeconds),
            modifier = Modifier.weight(1f),
        )
        StatPill(
            label = stringResource(Res.string.workout_detail_volume),
            value = "${totalVolumeKg.toInt()} kg",
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun StatPill(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .background(SurfaceContainerHigh, RoundedCornerShape(12.dp))
            .padding(FitTrackTheme.spacing.md),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Text(
            text = label,
            style = FitTrackTheme.typography.labelSmall,
            color = FitTrackTheme.colors.onSurfaceVariant,
        )
        Text(
            text = value,
            style = FitTrackTheme.typography.bodyLarge,
            color = FitTrackTheme.colors.onSurface,
            fontWeight = FontWeight.Bold,
        )
    }
}

// ── Exercise block ────────────────────────────────────────────────────────────

@Composable
private fun ExerciseBlock(
    exerciseWithSets: ExerciseWithSets,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(SurfaceContainerHigh, RoundedCornerShape(12.dp)),
    ) {
        // Exercise header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = FitTrackTheme.spacing.md, vertical = FitTrackTheme.spacing.md),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(FitTrackTheme.spacing.sm),
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(FitTrackTheme.colors.primary.copy(alpha = 0.15f), RoundedCornerShape(6.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Box(
                    Modifier
                        .size(16.dp, 4.dp)
                        .background(FitTrackTheme.colors.primary, RoundedCornerShape(2.dp))
                )
            }
            Text(
                text = exerciseWithSets.entry.exerciseId,
                style = FitTrackTheme.typography.bodyMedium,
                color = FitTrackTheme.colors.onSurface,
                fontWeight = FontWeight.SemiBold,
            )
        }

        if (exerciseWithSets.sets.isNotEmpty()) {
            HorizontalDivider(color = FitTrackTheme.colors.outline.copy(alpha = 0.3f))

            // Set header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = FitTrackTheme.spacing.md, vertical = FitTrackTheme.spacing.sm),
            ) {
                Text(
                    text = stringResource(Res.string.workout_detail_set).uppercase(),
                    style = FitTrackTheme.typography.labelSmall,
                    color = FitTrackTheme.colors.onSurfaceVariant,
                    modifier = Modifier.weight(0.8f),
                )
                Text(
                    text = "KG",
                    style = FitTrackTheme.typography.labelSmall,
                    color = FitTrackTheme.colors.onSurfaceVariant,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    text = stringResource(Res.string.workout_detail_reps).uppercase(),
                    style = FitTrackTheme.typography.labelSmall,
                    color = FitTrackTheme.colors.onSurfaceVariant,
                    modifier = Modifier.weight(1f),
                )
            }

            exerciseWithSets.sets.forEach { set ->
                SetRow(
                    set = set,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = FitTrackTheme.spacing.md, vertical = 6.dp),
                )
            }
        }

        Box(Modifier.height(FitTrackTheme.spacing.sm))
    }
}

@Composable
private fun SetRow(
    set: SetEntry,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Set number
        Box(
            modifier = Modifier
                .size(24.dp)
                .background(SurfaceContainerHighest, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = set.setNumber.toString(),
                style = FitTrackTheme.typography.labelSmall,
                color = FitTrackTheme.colors.onSurfaceVariant,
                fontWeight = FontWeight.Bold,
            )
        }
        Box(Modifier.weight(0.8f))
        Text(
            text = formatWeight(set.weight),
            style = FitTrackTheme.typography.bodySmall,
            color = FitTrackTheme.colors.onSurface,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = set.reps.toString(),
            style = FitTrackTheme.typography.bodySmall,
            color = FitTrackTheme.colors.onSurface,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.weight(1f),
        )
    }
}

private fun formatWeight(kg: Double): String =
    if (kg % 1.0 == 0.0) "${kg.toInt()}" else "$kg"
