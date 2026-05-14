package com.harsh.fittrack.feature.record

import com.harsh.fittrack.domain.repository.ExerciseWithSets

data class RecordState(
    val workoutId: String? = null,
    val title: String = "",
    val elapsedSeconds: Long = 0,
    val exercises: List<ExerciseWithSets> = emptyList(),
    val exerciseNames: Map<String, String> = emptyMap(),
    val isCompleting: Boolean = false,
)
