package com.harsh.fittrack.data.repository

import com.harsh.fittrack.data.table.ExercisesTable
import com.harsh.fittrack.domain.model.Exercise
import com.harsh.fittrack.domain.repository.ExerciseRepository
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.selectAll

class ExerciseRepositoryImpl : ExerciseRepository {

    override suspend fun findSinceVersion(sinceVersion: Int): List<Exercise> = query {
        ExercisesTable.selectAll()
            .where { ExercisesTable.catalogVersion greater sinceVersion }
            .orderBy(ExercisesTable.catalogVersion)
            .map { it.toExercise() }
    }

    override suspend fun latestVersion(): Int = query {
        ExercisesTable.selectAll()
            .maxOfOrNull { it[ExercisesTable.catalogVersion] } ?: 0
    }

    private fun ResultRow.toExercise() = Exercise(
        id = this[ExercisesTable.id],
        name = this[ExercisesTable.name],
        primaryMuscle = this[ExercisesTable.primaryMuscle],
        secondaryMuscles = this[ExercisesTable.secondaryMuscles].toList(),
        equipment = this[ExercisesTable.equipment],
        movementType = this[ExercisesTable.movementType],
        instructions = this[ExercisesTable.instructions].toList(),
        catalogVersion = this[ExercisesTable.catalogVersion],
    )
}
