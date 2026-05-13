package com.harsh.fittrack.feature.exercises

import com.harsh.fittrack.domain.model.Exercise
import com.harsh.fittrack.domain.model.MuscleGroup

data class ExercisesState(
    val query: String = "",
    val activeMuscleGroup: MuscleGroup? = null,
    val results: List<Exercise> = emptyList(),
)
