package com.harsh.fittrack.data.repository

import com.harsh.fittrack.data.table.ExerciseEntriesTable
import com.harsh.fittrack.data.table.SetEntriesTable
import com.harsh.fittrack.data.table.WorkoutsTable
import com.harsh.fittrack.domain.model.ExerciseEntry
import com.harsh.fittrack.domain.model.SetEntry
import com.harsh.fittrack.domain.model.Workout
import com.harsh.fittrack.domain.repository.WorkoutRepository
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.update
import java.time.OffsetDateTime

class WorkoutRepositoryImpl : WorkoutRepository {

    override suspend fun listForUser(userId: String, cursor: Long?, limit: Int): List<Workout> = query {
        val rows = WorkoutsTable.selectAll()
            .where {
                (WorkoutsTable.userId eq userId) and
                    WorkoutsTable.deletedAt.isNull() and
                    if (cursor != null) (WorkoutsTable.startedAt less cursor) else org.jetbrains.exposed.sql.Op.TRUE
            }
            .orderBy(WorkoutsTable.startedAt, SortOrder.DESC)
            .limit(limit)
            .toList()
        rows.map { it.toWorkoutWithDetails() }
    }

    override suspend fun findById(id: String, userId: String): Workout? = query {
        WorkoutsTable.selectAll()
            .where { (WorkoutsTable.id eq id) and (WorkoutsTable.userId eq userId) and WorkoutsTable.deletedAt.isNull() }
            .singleOrNull()
            ?.toWorkoutWithDetails()
    }

    override suspend fun save(workout: Workout): Workout = query {
        val exists = WorkoutsTable.selectAll()
            .where { WorkoutsTable.id eq workout.id }
            .singleOrNull()

        if (exists == null) {
            WorkoutsTable.insert {
                it[id] = workout.id
                it[userId] = workout.userId
                it[title] = workout.title
                it[date] = workout.date
                it[startedAt] = workout.startedAt
                it[durationSeconds] = workout.durationSeconds
                it[totalVolumeKg] = workout.totalVolumeKg.toBigDecimal()
                it[createdAt] = OffsetDateTime.now()
            }
            workout.exercises.forEach { entry ->
                ExerciseEntriesTable.insert {
                    it[id] = entry.id
                    it[workoutId] = workout.id
                    it[exerciseId] = entry.exerciseId
                    it[exerciseName] = entry.exerciseName
                    it[orderIndex] = entry.orderIndex
                    it[createdAt] = OffsetDateTime.now()
                }
                entry.sets.forEach { set ->
                    SetEntriesTable.insert {
                        it[id] = set.id
                        it[exerciseEntryId] = entry.id
                        it[setNumber] = set.setNumber
                        it[reps] = set.reps
                        it[weightKg] = set.weightKg.toBigDecimal()
                        it[isCompleted] = set.isCompleted
                        it[createdAt] = OffsetDateTime.now()
                    }
                }
            }
        }
        // If already exists, return the stored version (idempotent)
        WorkoutsTable.selectAll()
            .where { (WorkoutsTable.id eq workout.id) and (WorkoutsTable.userId eq workout.userId) }
            .singleOrNull()
            ?.toWorkoutWithDetails() ?: workout
    }

    override suspend fun updateTitle(id: String, userId: String, title: String): Workout? = query {
        val updated = WorkoutsTable.update({
            (WorkoutsTable.id eq id) and (WorkoutsTable.userId eq userId) and WorkoutsTable.deletedAt.isNull()
        }) {
            it[WorkoutsTable.title] = title
        }
        if (updated == 0) return@query null
        WorkoutsTable.selectAll()
            .where { (WorkoutsTable.id eq id) and (WorkoutsTable.userId eq userId) }
            .singleOrNull()
            ?.toWorkoutWithDetails()
    }

    override suspend fun softDelete(id: String, userId: String): Boolean = query {
        WorkoutsTable.update({
            (WorkoutsTable.id eq id) and (WorkoutsTable.userId eq userId) and WorkoutsTable.deletedAt.isNull()
        }) {
            it[deletedAt] = OffsetDateTime.now()
        } > 0
    }

    override suspend fun latestStartedAt(userId: String): Long? = query {
        WorkoutsTable.selectAll()
            .where { (WorkoutsTable.userId eq userId) and WorkoutsTable.deletedAt.isNull() }
            .orderBy(WorkoutsTable.startedAt, SortOrder.DESC)
            .limit(1)
            .singleOrNull()
            ?.get(WorkoutsTable.startedAt)
    }

    private fun ResultRow.toWorkoutWithDetails(): Workout {
        val workoutId = this[WorkoutsTable.id]
        val entries = ExerciseEntriesTable.selectAll()
            .where { ExerciseEntriesTable.workoutId eq workoutId }
            .orderBy(ExerciseEntriesTable.orderIndex)
            .map { entryRow ->
                val entryId = entryRow[ExerciseEntriesTable.id]
                val sets = SetEntriesTable.selectAll()
                    .where { SetEntriesTable.exerciseEntryId eq entryId }
                    .orderBy(SetEntriesTable.setNumber)
                    .map { setRow ->
                        SetEntry(
                            id = setRow[SetEntriesTable.id],
                            setNumber = setRow[SetEntriesTable.setNumber],
                            reps = setRow[SetEntriesTable.reps],
                            weightKg = setRow[SetEntriesTable.weightKg].toDouble(),
                            isCompleted = setRow[SetEntriesTable.isCompleted],
                        )
                    }
                ExerciseEntry(
                    id = entryId,
                    exerciseId = entryRow[ExerciseEntriesTable.exerciseId],
                    exerciseName = entryRow[ExerciseEntriesTable.exerciseName],
                    orderIndex = entryRow[ExerciseEntriesTable.orderIndex],
                    sets = sets,
                )
            }
        return Workout(
            id = workoutId,
            userId = this[WorkoutsTable.userId],
            title = this[WorkoutsTable.title],
            date = this[WorkoutsTable.date],
            startedAt = this[WorkoutsTable.startedAt],
            durationSeconds = this[WorkoutsTable.durationSeconds],
            totalVolumeKg = this[WorkoutsTable.totalVolumeKg].toDouble(),
            exercises = entries,
        )
    }
}
