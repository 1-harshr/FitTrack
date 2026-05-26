package com.harsh.fittrack.feature.pr

import com.harsh.fittrack.data.table.ExerciseEntriesTable
import com.harsh.fittrack.data.table.PersonalRecordsTable
import com.harsh.fittrack.data.table.SetEntriesTable
import com.harsh.fittrack.data.table.WorkoutsTable
import com.harsh.fittrack.data.repository.query
import com.harsh.fittrack.domain.model.PersonalRecord
import com.harsh.fittrack.domain.model.Workout
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.upsert
import java.time.OffsetDateTime

class PrService {

    suspend fun detectAndUpdatePrs(userId: String, workout: Workout): List<PersonalRecord> = query {
        val newPrs = mutableListOf<PersonalRecord>()

        for (entry in workout.exercises) {
            val completedSets = entry.sets.filter { it.isCompleted && it.reps > 0 && it.weightKg > 0 }
            if (completedSets.isEmpty()) continue

            val bestSet = completedSets.maxByOrNull { estimatedOneRm(it.weightKg, it.reps) } ?: continue
            val candidateOrm = estimatedOneRm(bestSet.weightKg, bestSet.reps)

            val existing = PersonalRecordsTable.selectAll()
                .where { (PersonalRecordsTable.userId eq userId) and (PersonalRecordsTable.exerciseId eq entry.exerciseId) }
                .singleOrNull()

            val existingOrm = if (existing != null) {
                estimatedOneRm(
                    existing[PersonalRecordsTable.maxWeightKg].toDouble(),
                    existing[PersonalRecordsTable.maxReps],
                )
            } else 0.0

            if (candidateOrm > existingOrm) {
                PersonalRecordsTable.upsert {
                    it[PersonalRecordsTable.userId] = userId
                    it[PersonalRecordsTable.exerciseId] = entry.exerciseId
                    it[maxWeightKg] = bestSet.weightKg.toBigDecimal()
                    it[maxReps] = bestSet.reps
                    it[achievedAt] = workout.startedAt
                }
                newPrs += PersonalRecord(
                    exerciseId = entry.exerciseId,
                    maxWeightKg = bestSet.weightKg,
                    maxReps = bestSet.reps,
                    achievedAt = workout.startedAt,
                )
            }
        }

        newPrs
    }

    suspend fun getPrForExercise(userId: String, exerciseId: String): PersonalRecord? = query {
        PersonalRecordsTable.selectAll()
            .where { (PersonalRecordsTable.userId eq userId) and (PersonalRecordsTable.exerciseId eq exerciseId) }
            .singleOrNull()
            ?.let {
                PersonalRecord(
                    exerciseId = it[PersonalRecordsTable.exerciseId],
                    maxWeightKg = it[PersonalRecordsTable.maxWeightKg].toDouble(),
                    maxReps = it[PersonalRecordsTable.maxReps],
                    achievedAt = it[PersonalRecordsTable.achievedAt],
                )
            }
    }
}

private fun estimatedOneRm(weightKg: Double, reps: Int): Double =
    weightKg * (1.0 + reps / 30.0)
