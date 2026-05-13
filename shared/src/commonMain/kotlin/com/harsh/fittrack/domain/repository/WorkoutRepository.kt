package com.harsh.fittrack.domain.repository

import com.harsh.fittrack.domain.model.ExerciseEntry
import com.harsh.fittrack.domain.model.SetEntry
import com.harsh.fittrack.domain.model.Workout
import kotlinx.coroutines.flow.Flow

interface WorkoutRepository {
    /** Most-recent-first feed for the Home screen. */
    fun observeWorkouts(userId: String): Flow<List<Workout>>

    fun observeWorkout(workoutId: String): Flow<WorkoutWithDetails?>

    suspend fun createWorkout(userId: String, title: String): String
    suspend fun renameWorkout(workoutId: String, title: String)
    suspend fun finishWorkout(workoutId: String, durationSeconds: Long)
    suspend fun discardWorkout(workoutId: String)

    suspend fun addExercise(workoutId: String, exerciseId: String): String
    suspend fun removeExercise(exerciseEntryId: String)

    suspend fun addSet(exerciseEntryId: String): String
    suspend fun updateSet(setEntry: SetEntry)
    suspend fun removeSet(setId: String)
}

/** Aggregate read model returned for the Workout Detail / active workout views. */
data class WorkoutWithDetails(
    val workout: Workout,
    val exercises: List<ExerciseWithSets>,
)

data class ExerciseWithSets(
    val entry: ExerciseEntry,
    val sets: List<SetEntry>,
)
