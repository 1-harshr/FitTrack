package com.harsh.fittrack.fakes

import com.harsh.fittrack.domain.model.Exercise
import com.harsh.fittrack.domain.model.ExerciseEntry
import com.harsh.fittrack.domain.model.SetEntry
import com.harsh.fittrack.domain.model.User
import com.harsh.fittrack.domain.model.Workout
import com.harsh.fittrack.domain.repository.ExerciseRepository
import com.harsh.fittrack.domain.repository.UserRepository
import com.harsh.fittrack.domain.repository.WorkoutRepository

// ── Fixtures ─────────────────────────────────────────────────────────────────

val testServerUser = User(
    id = "user-1",
    name = "John Doe",
    email = "john@example.com",
    photoUrl = null,
    units = "KG",
)

fun testServerWorkout(
    id: String = "workout-1",
    userId: String = "user-1",
    title: String = "Morning Session",
    startedAt: Long = 1_700_000_000_000L,
    date: String = "2023-11-14",
    durationSeconds: Int = 3600,
    totalVolumeKg: Double = 1000.0,
    exercises: List<ExerciseEntry> = emptyList(),
): Workout = Workout(
    id = id,
    userId = userId,
    title = title,
    date = date,
    startedAt = startedAt,
    durationSeconds = durationSeconds,
    totalVolumeKg = totalVolumeKg,
    exercises = exercises,
)

fun testServerExercise(
    id: String = "exercise-1",
    name: String = "Barbell Squat",
    catalogVersion: Int = 1,
): Exercise = Exercise(
    id = id,
    name = name,
    primaryMuscle = "Quadriceps",
    secondaryMuscles = listOf("Glutes", "Hamstrings"),
    equipment = "Barbell",
    movementType = "Compound",
    instructions = listOf("Stand with feet shoulder-width apart", "Lower until thighs are parallel to floor"),
    catalogVersion = catalogVersion,
)

// ── FakeServerUserRepository ──────────────────────────────────────────────────

class FakeServerUserRepository : UserRepository {

    private val byId = mutableMapOf<String, User>()
    private val byEmail = mutableMapOf<String, User>()
    private val hashByEmail = mutableMapOf<String, String>()

    var createCalled = false

    fun seed(user: User, passwordHash: String = "") {
        byId[user.id] = user
        byEmail[user.email] = user
        if (passwordHash.isNotEmpty()) {
            hashByEmail[user.email] = passwordHash
        }
    }

    override suspend fun findById(id: String): User? = byId[id]

    override suspend fun findByEmail(email: String): User? = byEmail[email]

    override suspend fun findPasswordHash(email: String): String? = hashByEmail[email]

    override suspend fun create(id: String, name: String, email: String, passwordHash: String): User {
        createCalled = true
        val user = User(id = id, name = name, email = email, photoUrl = null, units = "KG")
        byId[id] = user
        byEmail[email] = user
        hashByEmail[email] = passwordHash
        return user
    }

    override suspend fun upsert(id: String, name: String, email: String, photoUrl: String?): User {
        val existing = byId[id]
        val user = User(
            id = id,
            name = name,
            email = email,
            photoUrl = photoUrl,
            units = existing?.units ?: "KG",
        )
        byId[id] = user
        byEmail[email] = user
        return user
    }

    override suspend fun updateUnits(id: String, units: String): User? {
        val user = byId[id] ?: return null
        val updated = user.copy(units = units)
        byId[id] = updated
        byEmail[updated.email] = updated
        return updated
    }
}

// ── FakeServerExerciseRepository ─────────────────────────────────────────────

class FakeServerExerciseRepository : ExerciseRepository {

    val exercises = mutableListOf<Exercise>()
    var latestVersionValue: Int = 0

    override suspend fun findSinceVersion(sinceVersion: Int): List<Exercise> =
        exercises.filter { it.catalogVersion > sinceVersion }

    override suspend fun latestVersion(): Int = latestVersionValue
}

// ── FakeServerWorkoutRepository ──────────────────────────────────────────────

class FakeServerWorkoutRepository : WorkoutRepository {

    private val workouts = mutableListOf<Workout>()

    var saveCalled = false
    var lastSaved: Workout? = null

    fun seed(vararg workout: Workout) {
        workouts.addAll(workout)
    }

    override suspend fun listForUser(userId: String, cursor: Long?, limit: Int): List<Workout> =
        workouts
            .filter { it.userId == userId }
            .filter { w -> cursor == null || w.startedAt < cursor }
            .sortedByDescending { it.startedAt }
            .take(limit)

    override suspend fun findById(id: String, userId: String): Workout? =
        workouts.find { it.id == id && it.userId == userId }

    override suspend fun save(workout: Workout): Workout {
        saveCalled = true
        lastSaved = workout
        val index = workouts.indexOfFirst { it.id == workout.id }
        if (index >= 0) {
            workouts[index] = workout
        } else {
            workouts.add(workout)
        }
        return workout
    }

    override suspend fun updateTitle(id: String, userId: String, title: String): Workout? {
        val index = workouts.indexOfFirst { it.id == id && it.userId == userId }
        if (index < 0) return null
        val updated = workouts[index].copy(title = title)
        workouts[index] = updated
        return updated
    }

    override suspend fun softDelete(id: String, userId: String): Boolean {
        val index = workouts.indexOfFirst { it.id == id && it.userId == userId }
        if (index < 0) return false
        workouts.removeAt(index)
        return true
    }

    override suspend fun latestStartedAt(userId: String): Long? =
        workouts.filter { it.userId == userId }.maxOfOrNull { it.startedAt }
}
