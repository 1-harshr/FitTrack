package com.harsh.fittrack.ui.feature.exercises

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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavBackStackEntry
import androidx.navigation.toRoute
import com.harsh.fittrack.domain.model.Equipment
import com.harsh.fittrack.domain.model.Exercise
import com.harsh.fittrack.domain.model.MovementType
import com.harsh.fittrack.domain.model.MuscleGroup
import com.harsh.fittrack.domain.repository.ExerciseRepository
import com.harsh.fittrack.navigation.ExerciseDetailSource
import com.harsh.fittrack.navigation.Route
import com.harsh.fittrack.resources.Res
import com.harsh.fittrack.resources.exercise_add_to_workout
import com.harsh.fittrack.resources.exercise_equipment
import com.harsh.fittrack.resources.exercise_instructions
import com.harsh.fittrack.resources.exercise_movement
import com.harsh.fittrack.resources.exercise_primary_muscle
import com.harsh.fittrack.resources.exercise_secondary_muscles
import com.harsh.fittrack.ui.theme.FitTrackTheme
import com.harsh.fittrack.ui.theme.SurfaceContainerHigh
import com.harsh.fittrack.ui.theme.SurfaceContainerHighest
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

@Composable
fun ExerciseDetailScreen(
    backStackEntry: NavBackStackEntry,
    onBack: () -> Unit,
    onAddToWorkout: (exerciseId: String) -> Unit = {},
) {
    val route = backStackEntry.toRoute<Route.ExerciseDetail>()
    val exerciseRepository: ExerciseRepository = koinInject()
    val exercise by produceState<Exercise?>(null, route.exerciseId) {
        value = exerciseRepository.byId(route.exerciseId)
    }

    val ex = exercise ?: run {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Exercise not found", color = FitTrackTheme.colors.onSurface)
        }
        return
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(FitTrackTheme.colors.surface),
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                bottom = if (route.source == ExerciseDetailSource.SHEET) 100.dp else 32.dp,
            ),
        ) {
            // ── Hero ──────────────────────────────────────────────────────
            item {
                ExerciseHero(
                    exercise = ex,
                    onBack = onBack,
                )
            }

            // ── Meta chips ────────────────────────────────────────────────
            item {
                ExerciseMetaSection(
                    exercise = ex,
                    modifier = Modifier.padding(
                        horizontal = FitTrackTheme.spacing.containerMargin,
                        vertical = FitTrackTheme.spacing.md,
                    ),
                )
            }

            // ── Instructions header ───────────────────────────────────────
            item {
                Text(
                    text = stringResource(Res.string.exercise_instructions),
                    style = FitTrackTheme.typography.labelLarge,
                    color = FitTrackTheme.colors.onSurfaceVariant,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier
                        .padding(horizontal = FitTrackTheme.spacing.containerMargin)
                        .padding(top = FitTrackTheme.spacing.md, bottom = FitTrackTheme.spacing.sm),
                )
            }

            // ── Instructions steps ────────────────────────────────────────
            itemsIndexed(ex.instructions) { index, step ->
                InstructionStep(
                    number = index + 1,
                    text = step,
                    modifier = Modifier
                        .padding(horizontal = FitTrackTheme.spacing.containerMargin)
                        .padding(bottom = FitTrackTheme.spacing.sm),
                )
            }
        }

        // ── Add to Workout CTA (SHEET context only) ───────────────────────
        if (route.source == ExerciseDetailSource.SHEET) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(FitTrackTheme.colors.surface)
                    .padding(FitTrackTheme.spacing.containerMargin)
                    .padding(bottom = FitTrackTheme.spacing.md),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(FitTrackTheme.colors.primary)
                        .clickable { onAddToWorkout(ex.id) },
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = stringResource(Res.string.exercise_add_to_workout),
                        style = FitTrackTheme.typography.labelLarge,
                        color = FitTrackTheme.colors.onPrimary,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }
    }
}

// ── Hero ──────────────────────────────────────────────────────────────────────

@Composable
private fun ExerciseHero(
    exercise: Exercise,
    onBack: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(260.dp)
            .background(SurfaceContainerHighest),
    ) {
        // Dumbbell illustration placeholder
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Box(
                    Modifier
                        .size(80.dp, 20.dp)
                        .background(
                            FitTrackTheme.colors.primary.copy(alpha = 0.3f),
                            RoundedCornerShape(4.dp),
                        )
                )
                Box(
                    Modifier
                        .size(4.dp, 40.dp)
                        .background(FitTrackTheme.colors.onSurfaceVariant.copy(alpha = 0.3f))
                )
                Box(
                    Modifier
                        .size(80.dp, 20.dp)
                        .background(
                            FitTrackTheme.colors.primary.copy(alpha = 0.3f),
                            RoundedCornerShape(4.dp),
                        )
                )
            }
        }

        // Back button
        Box(
            modifier = Modifier
                .systemBarsPadding()
                .padding(FitTrackTheme.spacing.containerMargin)
                .size(40.dp)
                .clip(CircleShape)
                .background(FitTrackTheme.colors.surface.copy(alpha = 0.85f))
                .clickable(onClick = onBack),
            contentAlignment = Alignment.Center,
        ) {
            // Chevron-left placeholder
            Box(
                Modifier
                    .size(8.dp, 14.dp)
                    .background(FitTrackTheme.colors.onSurface, RoundedCornerShape(2.dp))
            )
        }

        // Exercise name overlay
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .background(
                    androidx.compose.ui.graphics.Brush.verticalGradient(
                        listOf(Color.Transparent, FitTrackTheme.colors.surface.copy(alpha = 0.95f))
                    )
                )
                .padding(FitTrackTheme.spacing.containerMargin),
        ) {
            Text(
                text = exercise.name,
                style = FitTrackTheme.typography.headlineSmall,
                color = FitTrackTheme.colors.onSurface,
                fontWeight = FontWeight.ExtraBold,
            )
            Text(
                text = exercise.primaryMuscle.displayName(),
                style = FitTrackTheme.typography.bodySmall,
                color = FitTrackTheme.colors.primary,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

// ── Meta chips ────────────────────────────────────────────────────────────────

@Composable
private fun ExerciseMetaSection(
    exercise: Exercise,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(FitTrackTheme.spacing.sm)) {
        MetaRow(
            label = stringResource(Res.string.exercise_primary_muscle),
            value = exercise.primaryMuscle.displayName(),
            valueColor = FitTrackTheme.colors.primary,
        )
        if (exercise.secondaryMuscles.isNotEmpty()) {
            MetaRow(
                label = stringResource(Res.string.exercise_secondary_muscles),
                value = exercise.secondaryMuscles.joinToString(", ") { it.displayName() },
            )
        }
        MetaRow(
            label = stringResource(Res.string.exercise_equipment),
            value = exercise.equipment.displayName(),
        )
        MetaRow(
            label = stringResource(Res.string.exercise_movement),
            value = exercise.movementType.displayName(),
        )
    }
}

@Composable
private fun MetaRow(
    label: String,
    value: String,
    valueColor: Color = FitTrackTheme.colors.onSurface,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(SurfaceContainerHigh, RoundedCornerShape(8.dp))
            .padding(horizontal = FitTrackTheme.spacing.md, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = FitTrackTheme.typography.bodySmall,
            color = FitTrackTheme.colors.onSurfaceVariant,
        )
        Text(
            text = value,
            style = FitTrackTheme.typography.bodySmall,
            color = valueColor,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

// ── Instruction step ──────────────────────────────────────────────────────────

@Composable
private fun InstructionStep(
    number: Int,
    text: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(SurfaceContainerHigh, RoundedCornerShape(8.dp))
            .padding(FitTrackTheme.spacing.md),
        horizontalArrangement = Arrangement.spacedBy(FitTrackTheme.spacing.md),
        verticalAlignment = Alignment.Top,
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .background(FitTrackTheme.colors.primary, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = number.toString(),
                style = FitTrackTheme.typography.labelSmall,
                color = FitTrackTheme.colors.onPrimary,
                fontWeight = FontWeight.Bold,
            )
        }
        Text(
            text = text,
            style = FitTrackTheme.typography.bodySmall,
            color = FitTrackTheme.colors.onSurface,
            modifier = Modifier.weight(1f),
        )
    }
}

// ── Display name helpers ──────────────────────────────────────────────────────

private fun MuscleGroup.displayName(): String = when (this) {
    MuscleGroup.CHEST -> "Chest"
    MuscleGroup.BACK -> "Back"
    MuscleGroup.LEGS -> "Legs"
    MuscleGroup.SHOULDERS -> "Shoulders"
    MuscleGroup.ARMS -> "Arms"
    MuscleGroup.CORE -> "Core"
    MuscleGroup.GLUTES -> "Glutes"
    MuscleGroup.CALVES -> "Calves"
}

private fun Equipment.displayName(): String = when (this) {
    Equipment.BARBELL -> "Barbell"
    Equipment.DUMBBELL -> "Dumbbell"
    Equipment.CABLE -> "Cable"
    Equipment.MACHINE -> "Machine"
    Equipment.BODYWEIGHT -> "Bodyweight"
    Equipment.KETTLEBELL -> "Kettlebell"
    Equipment.BAND -> "Band"
}

private fun MovementType.displayName(): String = when (this) {
    MovementType.COMPOUND -> "Compound"
    MovementType.ISOLATION -> "Isolation"
    MovementType.CARDIO -> "Cardio"
}
