package com.harsh.fittrack.data.local.catalog

import com.harsh.fittrack.domain.model.Exercise
import com.harsh.fittrack.domain.model.MuscleGroup
import com.harsh.fittrack.domain.repository.ExerciseCatalog

/**
 * In-memory implementation of the static exercise catalog.
 *
 * TODO: seed with the 50+ exercises required by the PRD covering CHEST, BACK, LEGS,
 * SHOULDERS, ARMS, CORE, GLUTES, CALVES across BARBELL/DUMBBELL/CABLE/MACHINE/
 * BODYWEIGHT/KETTLEBELL/BAND equipment.
 */
class StaticExerciseCatalog : ExerciseCatalog {
    private val exercises: List<Exercise> = emptyList() // TODO: populate

    override fun all(): List<Exercise> = exercises

    override fun byId(id: String): Exercise? = exercises.firstOrNull { it.id == id }

    override fun search(query: String, muscleGroup: MuscleGroup?): List<Exercise> {
        val q = query.trim().lowercase()
        return exercises.filter {
            (q.isEmpty() || it.name.lowercase().contains(q)) &&
                (muscleGroup == null || it.primaryMuscle == muscleGroup)
        }
    }
}
