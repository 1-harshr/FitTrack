package com.harsh.fittrack.domain.repository

import com.harsh.fittrack.domain.model.Exercise
import com.harsh.fittrack.domain.model.MuscleGroup
import kotlinx.coroutines.flow.Flow

interface ExerciseRepository {
    /** Live-updating list filtered by name query and/or muscle group. */
    fun observeExercises(query: String = "", muscleGroup: MuscleGroup? = null): Flow<List<Exercise>>
    suspend fun byId(id: String): Exercise?
}
