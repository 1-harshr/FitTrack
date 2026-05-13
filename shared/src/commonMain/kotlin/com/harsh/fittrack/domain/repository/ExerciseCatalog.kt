package com.harsh.fittrack.domain.repository

import com.harsh.fittrack.domain.model.Exercise
import com.harsh.fittrack.domain.model.MuscleGroup

/** Read-only catalog of the 50+ static exercises that ship with the app. */
interface ExerciseCatalog {
    fun all(): List<Exercise>
    fun byId(id: String): Exercise?
    fun search(query: String, muscleGroup: MuscleGroup? = null): List<Exercise>
}
