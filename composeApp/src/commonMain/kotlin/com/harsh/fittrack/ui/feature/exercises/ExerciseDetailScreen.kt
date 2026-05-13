package com.harsh.fittrack.ui.feature.exercises

import androidx.compose.runtime.Composable
import com.harsh.fittrack.navigation.ExerciseDetailSource

/**
 * Single screen used in two contexts (BROWSE / SHEET). The "Add to workout" CTA
 * only renders in SHEET context.
 */
@Composable
fun ExerciseDetailScreen(
    exerciseId: String,
    source: ExerciseDetailSource,
    onBack: () -> Unit,
    onAddToWorkout: () -> Unit,
) {
    // TODO
}
