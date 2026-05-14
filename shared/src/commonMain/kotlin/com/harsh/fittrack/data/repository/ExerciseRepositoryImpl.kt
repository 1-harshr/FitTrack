@file:OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)

package com.harsh.fittrack.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.harsh.fittrack.data.local.catalog.ExerciseSeed
import com.harsh.fittrack.data.local.mapper.toDomain
import com.harsh.fittrack.data.local.mapper.toEntity
import com.harsh.fittrack.db.FitTrackDatabase
import com.harsh.fittrack.domain.model.Exercise
import com.harsh.fittrack.domain.model.MuscleGroup
import com.harsh.fittrack.domain.repository.ExerciseRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ExerciseRepositoryImpl(
    private val db: FitTrackDatabase,
) : ExerciseRepository {

    private val io = Dispatchers.Default
    private val exerciseQ get() = db.exerciseQueries

    init {
        CoroutineScope(io).launch { seedIfEmpty() }
    }

    override fun observeExercises(query: String, muscleGroup: MuscleGroup?): Flow<List<Exercise>> =
        exerciseQ.selectAll().asFlow().mapToList(io).mapLatest { all ->
            val q = query.trim().lowercase()
            all.map { it.toDomain() }.filter { exercise ->
                (q.isEmpty() || exercise.name.lowercase().contains(q)) &&
                    (muscleGroup == null || exercise.primaryMuscle == muscleGroup)
            }
        }

    override suspend fun byId(id: String): Exercise? = withContext(io) {
        exerciseQ.selectById(id).executeAsOneOrNull()?.toDomain()
    }

    private suspend fun seedIfEmpty() = withContext(io) {
        val count = exerciseQ.count().executeAsOne()
        if (count > 0L) return@withContext
        db.transaction {
            ExerciseSeed.exercises.forEach { exercise ->
                val entity = exercise.toEntity()
                exerciseQ.upsert(
                    id = entity.id,
                    name = entity.name,
                    primaryMuscle = entity.primaryMuscle,
                    secondaryMuscles = entity.secondaryMuscles,
                    equipment = entity.equipment,
                    movementType = entity.movementType,
                    instructions = entity.instructions,
                    isCustom = entity.isCustom,
                    catalogVersion = entity.catalogVersion,
                )
            }
        }
    }
}
