package com.harsh.fittrack.ui.feature.record

import androidx.compose.runtime.Composable

/**
 * Active workout. Live timer, editable title, exercise sections with inline set rows,
 * "+ add exercise" opens [AddExerciseSheet], "Finish workout" navigates to complete.
 */
@Composable
fun RecordWorkoutScreen(
    onOpenAddExercise: () -> Unit,
    onOpenExerciseDetailFromSheet: (exerciseId: String) -> Unit,
    onFinish: () -> Unit,
) {
    // TODO
}
