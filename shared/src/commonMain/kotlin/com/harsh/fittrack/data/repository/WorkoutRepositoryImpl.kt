package com.harsh.fittrack.data.repository

import com.harsh.fittrack.db.FitTrackDatabase
import com.harsh.fittrack.domain.model.SetEntry
import com.harsh.fittrack.domain.model.Workout
import com.harsh.fittrack.domain.repository.WorkoutRepository
import com.harsh.fittrack.domain.repository.WorkoutWithDetails
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class WorkoutRepositoryImpl(
    private val db: FitTrackDatabase,
) : WorkoutRepository {

    override fun observeWorkouts(userId: String): Flow<List<Workout>> = flowOf(emptyList()) // TODO
    override fun observeWorkout(workoutId: String): Flow<WorkoutWithDetails?> = flowOf(null) // TODO

    override suspend fun createWorkout(userId: String, title: String): String = TODO()
    override suspend fun renameWorkout(workoutId: String, title: String) { TODO() }
    override suspend fun finishWorkout(workoutId: String, durationSeconds: Long) { TODO() }
    override suspend fun discardWorkout(workoutId: String) { TODO() }

    override suspend fun addExercise(workoutId: String, exerciseId: String): String = TODO()
    override suspend fun removeExercise(exerciseEntryId: String) { TODO() }

    override suspend fun addSet(exerciseEntryId: String): String = TODO()
    override suspend fun updateSet(setEntry: SetEntry) { TODO() }
    override suspend fun removeSet(setId: String) { TODO() }
}
