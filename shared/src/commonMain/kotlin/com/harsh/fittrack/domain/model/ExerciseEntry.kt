package com.harsh.fittrack.domain.model

/** An exercise inside a workout, in a specific position. */
data class ExerciseEntry(
    val id: String,
    val workoutId: String,
    val exerciseId: String,
    val exerciseName: String,
    val orderIndex: Int,
)
