@file:OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)

package com.harsh.fittrack.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOneOrNull
import com.harsh.fittrack.data.local.mapper.toDomain
import com.harsh.fittrack.data.remote.ApiExerciseEntry
import com.harsh.fittrack.data.remote.ApiSetEntry
import com.harsh.fittrack.data.remote.ApiWorkout
import com.harsh.fittrack.data.remote.FitTrackApi
import com.harsh.fittrack.db.FitTrackDatabase
import com.harsh.fittrack.domain.model.SetEntry
import com.harsh.fittrack.domain.model.Workout
import com.harsh.fittrack.domain.repository.ExerciseWithSets
import com.harsh.fittrack.domain.repository.WorkoutRepository
import com.harsh.fittrack.domain.repository.WorkoutWithDetails
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.time.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.random.Random

class WorkoutRepositoryImpl(
    private val db: FitTrackDatabase,
    private val api: FitTrackApi,
) : WorkoutRepository {

    private val workoutQ get() = db.workoutQueries
    private val exerciseQ get() = db.exerciseEntryQueries
    private val setQ get() = db.setEntryQueries
    private val io = Dispatchers.Default

    init {
        // Pull completed workouts from the server on startup so a fresh install
        // or re-login restores the user's history from the server.
        CoroutineScope(io).launch { pullRemoteWorkouts() }
    }

    /**
     * Fetches all pages of completed workouts from the server and merges them
     * into the local SQLDelight database (insert-or-ignore to avoid overwriting
     * any locally-recorded in-progress workouts).
     */
    private suspend fun pullRemoteWorkouts() {
        runCatching {
            var cursor: String? = null
            do {
                val page = api.getWorkouts(cursor = cursor, limit = 50) ?: break
                page.workouts.forEach { remote ->
                    // Only insert if not already present locally
                    val alreadyExists = workoutQ.selectById(remote.id).executeAsOneOrNull() != null
                    if (!alreadyExists) {
                        workoutQ.insert(
                            id = remote.id,
                            userId = remote.userId,
                            title = remote.title,
                            date = remote.date,
                            startedAt = remote.startedAt,
                        )
                        // Mark it completed with the server's authoritative values
                        workoutQ.markCompleted(
                            durationSeconds = remote.durationSeconds.toLong(),
                            totalVolumeKg = remote.totalVolumeKg,
                            id = remote.id,
                        )
                        // Restore exercises + sets
                        remote.exercises.forEach { entry ->
                            exerciseQ.insert(
                                id = entry.id,
                                workoutId = remote.id,
                                exerciseId = entry.exerciseId,
                                exerciseName = entry.exerciseName,
                                orderIndex = entry.orderIndex.toLong(),
                            )
                            entry.sets.forEach { set ->
                                setQ.insert(
                                    id = set.id,
                                    exerciseEntryId = entry.id,
                                    setNumber = set.setNumber.toLong(),
                                    reps = set.reps.toLong(),
                                    weight = set.weightKg,
                                    isCompleted = if (set.isCompleted) 1L else 0L,
                                )
                            }
                        }
                    }
                }
                cursor = page.nextCursor
            } while (cursor != null)
        }
    }

    override fun observeWorkouts(userId: String): Flow<List<Workout>> =
        workoutQ.selectAllForUser(userId)
            .asFlow()
            .mapToList(io)
            .mapLatest { list -> list.map { it.toDomain() } }

    override fun observeWorkout(workoutId: String): Flow<WorkoutWithDetails?> = combine(
        workoutQ.selectById(workoutId).asFlow().mapToOneOrNull(io),
        exerciseQ.selectForWorkout(workoutId).asFlow().mapToList(io),
        setQ.selectForWorkout(workoutId).asFlow().mapToList(io),
    ) { workout, exercises, sets ->
        workout ?: return@combine null
        val setsByExercise = sets.groupBy { it.exerciseEntryId }
        WorkoutWithDetails(
            workout = workout.toDomain(),
            exercises = exercises.map { e ->
                ExerciseWithSets(
                    entry = e.toDomain(),
                    sets = setsByExercise[e.id]?.map { it.toDomain() } ?: emptyList(),
                )
            },
        )
    }

    override suspend fun getActiveWorkout(userId: String): WorkoutWithDetails? = withContext(io) {
        val workout = workoutQ.selectActiveForUser(userId).executeAsOneOrNull()?.toDomain()
            ?: return@withContext null
        val exercises = exerciseQ.selectForWorkout(workout.id).executeAsList()
        val sets = setQ.selectForWorkout(workout.id).executeAsList().groupBy { it.exerciseEntryId }
        WorkoutWithDetails(
            workout = workout,
            exercises = exercises.map { e ->
                ExerciseWithSets(
                    entry = e.toDomain(),
                    sets = sets[e.id]?.map { it.toDomain() } ?: emptyList(),
                )
            },
        )
    }

    override suspend fun createWorkout(userId: String, title: String): String =
        withContext(io) {
            val id = newId()
            val now = Clock.System.now()
            val date = now.toLocalDateTime(TimeZone.currentSystemDefault()).date
            workoutQ.insert(
                id = id,
                userId = userId,
                title = title,
                date = date.toString(),
                startedAt = now.toEpochMilliseconds(),
            )
            id
        }

    override suspend fun renameWorkout(workoutId: String, title: String) {
        withContext(io) {
            workoutQ.updateTitle(title = title, id = workoutId)
            // Best-effort server sync so the server title stays in sync when online
            runCatching { api.patchWorkout(workoutId, title) }
        }
    }

    override suspend fun finishWorkout(workoutId: String, durationSeconds: Long) =
        withContext(io) {
            val totalVolumeKg = db.transactionWithResult {
                val exercises = exerciseQ.selectForWorkout(workoutId).executeAsList()
                exercises.sumOf { e ->
                    setQ.selectForExerciseEntry(e.id).executeAsList()
                        .filter { it.isCompleted != 0L }
                        .sumOf { it.weight * it.reps }
                }
            }
            workoutQ.markCompleted(
                durationSeconds = durationSeconds,
                totalVolumeKg = totalVolumeKg,
                id = workoutId,
            )
            syncToServer(workoutId, durationSeconds, totalVolumeKg)
        }

    private suspend fun syncToServer(workoutId: String, durationSeconds: Long, totalVolumeKg: Double) {
        runCatching {
            val workout = workoutQ.selectById(workoutId).executeAsOneOrNull() ?: return
            val exercises = exerciseQ.selectForWorkout(workoutId).executeAsList()
            val apiWorkout = ApiWorkout(
                id = workout.id,
                userId = workout.userId,
                title = workout.title,
                date = workout.date,
                startedAt = workout.startedAt,
                durationSeconds = durationSeconds.toInt(),
                totalVolumeKg = totalVolumeKg,
                exercises = exercises.map { e ->
                    ApiExerciseEntry(
                        id = e.id,
                        exerciseId = e.exerciseId,
                        exerciseName = e.exerciseName,
                        orderIndex = e.orderIndex.toInt(),
                        sets = setQ.selectForExerciseEntry(e.id).executeAsList().map { s ->
                            ApiSetEntry(
                                id = s.id,
                                setNumber = s.setNumber.toInt(),
                                reps = s.reps.toInt(),
                                weightKg = s.weight,
                                isCompleted = s.isCompleted != 0L,
                            )
                        },
                    )
                },
            )
            api.postWorkout(apiWorkout)
        }
    }

    override suspend fun discardWorkout(workoutId: String) =
        withContext(io) { workoutQ.delete(workoutId) }

    override suspend fun addExercise(
        workoutId: String,
        exerciseId: String,
        exerciseName: String,
    ): String = withContext(io) {
        val id = newId()
        val orderIndex = exerciseQ.nextOrderIndex(workoutId).executeAsOne()
        exerciseQ.insert(
            id = id,
            workoutId = workoutId,
            exerciseId = exerciseId,
            exerciseName = exerciseName,
            orderIndex = orderIndex,
        )
        id
    }

    override suspend fun removeExercise(exerciseEntryId: String) =
        withContext(io) { exerciseQ.delete(exerciseEntryId) }

    override suspend fun addSet(exerciseEntryId: String): String = withContext(io) {
        val setNumber = setQ.nextSetNumber(exerciseEntryId).executeAsOne()
        val last = setQ.selectForExerciseEntry(exerciseEntryId).executeAsList().lastOrNull()
        val id = newId()
        setQ.insert(
            id = id,
            exerciseEntryId = exerciseEntryId,
            setNumber = setNumber,
            reps = last?.reps ?: 0L,
            weight = last?.weight ?: 0.0,
            isCompleted = 0L,
        )
        id
    }

    override suspend fun updateSet(setEntry: SetEntry) = withContext(io) {
        setQ.update(
            reps = setEntry.reps.toLong(),
            weight = setEntry.weight,
            isCompleted = if (setEntry.isCompleted) 1L else 0L,
            id = setEntry.id,
        )
    }

    override suspend fun removeSet(setId: String) =
        withContext(io) { setQ.delete(setId) }

    private fun newId(): String = buildString {
        repeat(4) { append(Random.nextLong().toString(16)) }
    }.take(32)
}
