package com.harsh.fittrack.feature.stats

import com.harsh.fittrack.data.repository.query
import com.harsh.fittrack.data.table.ExerciseEntriesTable
import com.harsh.fittrack.data.table.ExercisesTable
import com.harsh.fittrack.data.table.SetEntriesTable
import com.harsh.fittrack.data.table.WorkoutsTable
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.selectAll
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.temporal.WeekFields
import java.util.Locale

class StatsService {

    suspend fun weeklyVolume(userId: String, weeks: Int = 8): WeeklyVolumeResponse = query {
        val cutoff = Instant.now().minusSeconds(weeks * 7L * 86400L).toEpochMilli()

        val workouts = WorkoutsTable.selectAll()
            .where {
                (WorkoutsTable.userId eq userId) and
                    WorkoutsTable.deletedAt.isNull() and
                    (WorkoutsTable.durationSeconds greater 0) and
                    (WorkoutsTable.startedAt greaterEq cutoff)
            }
            .toList()

        // Group volume by ISO week
        val weeklyMap = mutableMapOf<String, Double>()
        for (row in workouts) {
            val instant = Instant.ofEpochMilli(row[WorkoutsTable.startedAt])
            val ldt = instant.atZone(ZoneOffset.UTC).toLocalDate()
            val wf = WeekFields.of(Locale.getDefault())
            val weekNum = ldt.get(wf.weekOfWeekBasedYear())
            val label = "${ldt.month.name.take(3).replaceFirstChar { it.uppercase() }} $weekNum"
            weeklyMap[label] = (weeklyMap[label] ?: 0.0) + row[WorkoutsTable.totalVolumeKg].toDouble()
        }

        val result = weeklyMap.entries
            .sortedByDescending { it.key }
            .take(weeks)
            .reversed()
            .map { WeeklyVolumePoint(weekLabel = it.key, volumeKg = it.value) }

        WeeklyVolumeResponse(weeks = result)
    }

    suspend fun exerciseProgression(userId: String, exerciseId: String): ExerciseProgressionResponse = query {
        val exerciseName = ExercisesTable.selectAll()
            .where { ExercisesTable.id eq exerciseId }
            .singleOrNull()
            ?.get(ExercisesTable.name) ?: exerciseId

        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

        // Join workouts → exercise_entries → set_entries for this exercise
        val rows = (WorkoutsTable innerJoin ExerciseEntriesTable innerJoin SetEntriesTable)
            .selectAll()
            .where {
                (WorkoutsTable.userId eq userId) and
                    WorkoutsTable.deletedAt.isNull() and
                    (WorkoutsTable.durationSeconds greater 0) and
                    (ExerciseEntriesTable.exerciseId eq exerciseId) and
                    SetEntriesTable.isCompleted
            }
            .orderBy(WorkoutsTable.startedAt, SortOrder.ASC)
            .toList()

        // Group by workout date, keep max weight per date
        val byDate = mutableMapOf<String, Double>()
        for (row in rows) {
            val date = Instant.ofEpochMilli(row[WorkoutsTable.startedAt])
                .atZone(ZoneOffset.UTC)
                .toLocalDate()
                .format(formatter)
            val w = row[SetEntriesTable.weightKg].toDouble()
            byDate[date] = maxOf(byDate[date] ?: 0.0, w)
        }

        val points = byDate.entries.sortedBy { it.key }
            .map { ExerciseProgressionPoint(date = it.key, maxWeightKg = it.value) }

        ExerciseProgressionResponse(exerciseName = exerciseName, points = points)
    }

    suspend fun muscleFrequency(userId: String, days: Int = 30): MuscleFrequencyResponse = query {
        val cutoff = Instant.now().minusSeconds(days * 86400L).toEpochMilli()

        val rows = (WorkoutsTable innerJoin ExerciseEntriesTable innerJoin ExercisesTable)
            .selectAll()
            .where {
                (WorkoutsTable.userId eq userId) and
                    WorkoutsTable.deletedAt.isNull() and
                    (WorkoutsTable.durationSeconds greater 0) and
                    (WorkoutsTable.startedAt greaterEq cutoff)
            }
            .toList()

        // Count distinct workout sessions per primary muscle
        val muscleWorkouts = mutableMapOf<String, MutableSet<String>>()
        for (row in rows) {
            val muscle = row[ExercisesTable.primaryMuscle]
            val workoutId = row[WorkoutsTable.id]
            muscleWorkouts.getOrPut(muscle) { mutableSetOf() }.add(workoutId)
        }

        val points = muscleWorkouts.entries
            .sortedByDescending { it.value.size }
            .map { MuscleFrequencyPoint(muscle = it.key, sessionCount = it.value.size) }

        MuscleFrequencyResponse(points = points)
    }
}
