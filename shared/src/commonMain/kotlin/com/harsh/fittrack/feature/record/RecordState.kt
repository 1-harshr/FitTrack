package com.harsh.fittrack.feature.record

import com.harsh.fittrack.domain.model.PersonalRecord
import com.harsh.fittrack.domain.repository.ExerciseWithSets
import com.harsh.fittrack.domain.usecase.record.WorkoutValidationError

data class RecordState(
    val workoutId: String? = null,
    val title: String = "",
    val hasStarted: Boolean = false,
    val elapsedSeconds: Long = 0,
    val exercises: List<ExerciseWithSets> = emptyList(),
    val validationErrors: List<WorkoutValidationError> = emptyList(),
    val isCompleting: Boolean = false,
    val newPrExerciseIds: Set<String> = emptySet(),
    val prs: Map<String, PersonalRecord> = emptyMap(),
    val showSaveTemplateDialog: Boolean = false,
)
