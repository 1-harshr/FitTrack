package com.harsh.fittrack.feature.template

import com.harsh.fittrack.data.repository.query
import com.harsh.fittrack.data.table.TemplateExercisesTable
import com.harsh.fittrack.data.table.WorkoutTemplatesTable
import com.harsh.fittrack.domain.model.CreateTemplateRequest
import com.harsh.fittrack.domain.model.TemplateExercise
import com.harsh.fittrack.domain.model.WorkoutTemplate
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import java.time.OffsetDateTime
import java.util.UUID

class TemplateService {

    suspend fun listForUser(userId: String): List<WorkoutTemplate> = query {
        val templates = WorkoutTemplatesTable.selectAll()
            .where { WorkoutTemplatesTable.userId eq userId }
            .orderBy(WorkoutTemplatesTable.createdAt, org.jetbrains.exposed.sql.SortOrder.DESC)
            .toList()

        templates.map { row ->
            val exercises = TemplateExercisesTable.selectAll()
                .where { TemplateExercisesTable.templateId eq row[WorkoutTemplatesTable.id] }
                .orderBy(TemplateExercisesTable.orderIndex, org.jetbrains.exposed.sql.SortOrder.ASC)
                .map { e ->
                    TemplateExercise(
                        exerciseId = e[TemplateExercisesTable.exerciseId],
                        exerciseName = e[TemplateExercisesTable.exerciseName],
                        orderIndex = e[TemplateExercisesTable.orderIndex],
                    )
                }
            WorkoutTemplate(
                id = row[WorkoutTemplatesTable.id],
                name = row[WorkoutTemplatesTable.name],
                exercises = exercises,
                createdAt = row[WorkoutTemplatesTable.createdAt].toInstant().toEpochMilli(),
            )
        }
    }

    suspend fun create(userId: String, request: CreateTemplateRequest): WorkoutTemplate = query {
        val id = UUID.randomUUID().toString()
        val now = OffsetDateTime.now()
        WorkoutTemplatesTable.insert {
            it[WorkoutTemplatesTable.id] = id
            it[WorkoutTemplatesTable.userId] = userId
            it[WorkoutTemplatesTable.name] = request.name.trim()
            it[WorkoutTemplatesTable.createdAt] = now
        }
        request.exercises.forEach { ex ->
            TemplateExercisesTable.insert {
                it[TemplateExercisesTable.id] = UUID.randomUUID().toString()
                it[templateId] = id
                it[exerciseId] = ex.exerciseId
                it[exerciseName] = ex.exerciseName
                it[orderIndex] = ex.orderIndex
            }
        }
        WorkoutTemplate(
            id = id,
            name = request.name.trim(),
            exercises = request.exercises,
            createdAt = now.toInstant().toEpochMilli(),
        )
    }

    suspend fun delete(userId: String, templateId: String): Boolean = query {
        val count = WorkoutTemplatesTable.deleteWhere {
            (WorkoutTemplatesTable.id eq templateId) and (WorkoutTemplatesTable.userId eq userId)
        }
        count > 0
    }
}
