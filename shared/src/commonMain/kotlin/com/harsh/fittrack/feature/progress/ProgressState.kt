package com.harsh.fittrack.feature.progress

import com.harsh.fittrack.domain.model.ExerciseProgressionResponse
import com.harsh.fittrack.domain.model.MuscleFrequencyPoint
import com.harsh.fittrack.domain.model.WeeklyVolumePoint

data class ProgressState(
    val weeklyVolume: List<WeeklyVolumePoint> = emptyList(),
    val muscleFrequency: List<MuscleFrequencyPoint> = emptyList(),
    val exerciseProgression: ExerciseProgressionResponse? = null,
    val selectedExerciseId: String? = null,
    val isLoading: Boolean = true,
    val progressionLoading: Boolean = false,
)
