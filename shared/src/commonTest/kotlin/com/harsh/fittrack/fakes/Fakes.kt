package com.harsh.fittrack.fakes

import com.harsh.fittrack.core.time.Clock
import com.harsh.fittrack.data.remote.ApiAuthResponse
import com.harsh.fittrack.data.remote.ApiExerciseSyncResponse
import com.harsh.fittrack.data.remote.ApiSyncStatusResponse
import com.harsh.fittrack.data.remote.ApiUser
import com.harsh.fittrack.data.remote.ApiWorkout
import com.harsh.fittrack.data.remote.ApiWorkoutListResponse
import com.harsh.fittrack.data.remote.FitTrackApi
import com.harsh.fittrack.domain.model.Equipment
import com.harsh.fittrack.domain.model.Exercise
import com.harsh.fittrack.domain.model.ExerciseEntry
import com.harsh.fittrack.domain.model.MovementType
import com.harsh.fittrack.domain.model.MuscleGroup
import com.harsh.fittrack.domain.model.SetEntry
import com.harsh.fittrack.domain.model.Units
import com.harsh.fittrack.domain.model.User
import com.harsh.fittrack.domain.model.Workout
import com.harsh.fittrack.domain.repository.AuthRepository
import com.harsh.fittrack.domain.repository.ExerciseRepository
import com.harsh.fittrack.domain.repository.ExerciseWithSets
import com.harsh.fittrack.domain.repository.UserRepository
import com.harsh.fittrack.domain.repository.WorkoutRepository
import com.harsh.fittrack.domain.repository.WorkoutWithDetails
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone

// ── Domain fixtures ──────────────────────────────────────────────────────────

val testUser = User(
    id = "user-1",
    name = "John Doe",
    email = "john@example.com",
    units = Units.KG,
)

fun testWorkout(
    id: String = "w-1",
    userId: String = testUser.id,
    date: String = "2026-05-23",
    volumeKg: Double = 1000.0,
    isCompleted: Boolean = true,
) = Workout(
    id = id,
    userId = userId,
    title = "Test Workout",
    date = LocalDate.parse(date),
    startedAt = 0L,
    durationSeconds = 3600L,
    totalVolumeKg = volumeKg,
    isCompleted = isCompleted,
)

fun testExercise(
    id: String = "ex-1",
    name: String = "Bench Press",
    muscle: MuscleGroup = MuscleGroup.CHEST,
    movement: MovementType = MovementType.COMPOUND,
) = Exercise(
    id = id,
    name = name,
    primaryMuscle = muscle,
    secondaryMuscles = emptyList(),
    equipment = Equipment.BARBELL,
    movementType = movement,
    instructions = listOf("Do the thing"),
)

fun testExerciseEntry(
    id: String = "entry-1",
    workoutId: String = "w-1",
    exerciseId: String = "ex-1",
    name: String = "Bench Press",
    orderIndex: Int = 0,
) = ExerciseEntry(
    id = id,
    workoutId = workoutId,
    exerciseId = exerciseId,
    exerciseName = name,
    orderIndex = orderIndex,
)

fun testSetEntry(
    id: String = "set-1",
    entryId: String = "entry-1",
    setNumber: Int = 1,
    reps: Int = 10,
    weight: Double = 80.0,
    isCompleted: Boolean = false,
) = SetEntry(
    id = id,
    exerciseEntryId = entryId,
    setNumber = setNumber,
    reps = reps,
    weight = weight,
    isCompleted = isCompleted,
)

fun testExerciseWithSets(
    entry: ExerciseEntry = testExerciseEntry(),
    sets: List<SetEntry> = listOf(testSetEntry()),
) = ExerciseWithSets(entry = entry, sets = sets)

// ── Fake repositories ────────────────────────────────────────────────────────

class FakeAuthRepository(initialUser: User? = null) : AuthRepository {
    private val _currentUser = MutableStateFlow(initialUser)
    override val currentUser: Flow<User?> = _currentUser

    var loginResult: Result<User> = Result.success(testUser)
    var registerResult: Result<User> = Result.success(testUser)
    var signOutCalled = false

    var lastLoginEmail: String? = null
    var lastLoginPassword: String? = null
    var lastRegisterName: String? = null
    var lastRegisterEmail: String? = null
    var lastRegisterPassword: String? = null

    override suspend fun isSignedIn(): Boolean = _currentUser.value != null

    override suspend fun login(email: String, password: String): Result<User> {
        lastLoginEmail = email
        lastLoginPassword = password
        return loginResult
    }

    override suspend fun register(name: String, email: String, password: String): Result<User> {
        lastRegisterName = name
        lastRegisterEmail = email
        lastRegisterPassword = password
        return registerResult
    }

    override suspend fun signOut() {
        signOutCalled = true
        _currentUser.value = null
    }

    fun emit(user: User?) { _currentUser.value = user }
}

class FakeUserRepository(initialUser: User? = null) : UserRepository {
    private val _user = MutableStateFlow(initialUser)
    var setUnitsCalls = mutableListOf<Units>()

    override fun observeUser(): Flow<User?> = _user

    override suspend fun setUnits(units: Units) {
        setUnitsCalls += units
        _user.value = _user.value?.copy(units = units)
    }

    fun emit(user: User?) { _user.value = user }
}

class FakeWorkoutRepository : WorkoutRepository {
    private val workoutsByUser = mutableMapOf<String, MutableStateFlow<List<Workout>>>()
    private val workoutsById = mutableMapOf<String, MutableStateFlow<WorkoutWithDetails?>>()

    var activeWorkout: WorkoutWithDetails? = null
    var createdWorkoutId = "workout-new"
    var addedExerciseId = "entry-new"
    var addedSetId = "set-new"

    var renamedWorkoutId: String? = null
    var renamedTitle: String? = null
    var finishedWorkoutId: String? = null
    var finishedDurationSeconds: Long? = null
    var discardedWorkoutId: String? = null
    var updatedSets = mutableListOf<SetEntry>()

    fun setWorkoutsForUser(userId: String, workouts: List<Workout>) {
        workoutsByUser.getOrPut(userId) { MutableStateFlow(emptyList()) }.value = workouts
    }

    fun setWorkoutDetails(workoutId: String, details: WorkoutWithDetails?) {
        workoutsById.getOrPut(workoutId) { MutableStateFlow(null) }.value = details
    }

    override fun observeWorkouts(userId: String): Flow<List<Workout>> =
        workoutsByUser.getOrPut(userId) { MutableStateFlow(emptyList()) }

    override fun observeWorkout(workoutId: String): Flow<WorkoutWithDetails?> =
        workoutsById.getOrPut(workoutId) { MutableStateFlow(null) }

    override suspend fun getActiveWorkout(userId: String): WorkoutWithDetails? = activeWorkout
    override suspend fun createWorkout(userId: String, title: String): String = createdWorkoutId
    override suspend fun renameWorkout(workoutId: String, title: String) {
        renamedWorkoutId = workoutId; renamedTitle = title
    }
    override suspend fun finishWorkout(workoutId: String, durationSeconds: Long) {
        finishedWorkoutId = workoutId; finishedDurationSeconds = durationSeconds
    }
    override suspend fun discardWorkout(workoutId: String) { discardedWorkoutId = workoutId }
    override suspend fun addExercise(workoutId: String, exerciseId: String, exerciseName: String): String = addedExerciseId
    override suspend fun removeExercise(exerciseEntryId: String) {}
    override suspend fun addSet(exerciseEntryId: String): String = addedSetId
    override suspend fun updateSet(setEntry: SetEntry) { updatedSets += setEntry }
    override suspend fun removeSet(setId: String) {}
}

class FakeExerciseRepository(initial: List<Exercise> = emptyList()) : ExerciseRepository {
    private val _exercises = MutableStateFlow(initial)

    override fun observeExercises(query: String, muscleGroup: MuscleGroup?): Flow<List<Exercise>> =
        _exercises.map { list ->
            list.filter { ex ->
                (query.isBlank() || ex.name.contains(query, ignoreCase = true)) &&
                    (muscleGroup == null || ex.primaryMuscle == muscleGroup)
            }
        }

    override suspend fun byId(id: String): Exercise? = _exercises.value.find { it.id == id }

    fun emit(exercises: List<Exercise>) { _exercises.value = exercises }
}

// ── Fake API ─────────────────────────────────────────────────────────────────

class FakeApi : FitTrackApi {
    var loginResponse: ApiAuthResponse? = null
    var registerResponse: ApiAuthResponse? = null
    var getMeResponse: ApiUser? = null
    var patchMeResponse: ApiUser? = null
    var getExercisesResponse: ApiExerciseSyncResponse? = null
    var getWorkoutsResponse: ApiWorkoutListResponse? = null
    var postWorkoutResponse: ApiWorkout? = null
    var patchWorkoutResponse: ApiWorkout? = null
    var getSyncStatusResponse: ApiSyncStatusResponse? = null

    var patchMeThrows = false
    var patchMeCalledWith: String? = null
    var deleteWorkoutCalledWith: String? = null

    override suspend fun login(email: String, password: String) = loginResponse
    override suspend fun register(name: String, email: String, password: String) = registerResponse
    override suspend fun getMe() = getMeResponse
    override suspend fun patchMe(units: String): ApiUser? {
        patchMeCalledWith = units
        if (patchMeThrows) throw RuntimeException("Network error")
        return patchMeResponse
    }
    override suspend fun getExercises(sinceVersion: Int) = getExercisesResponse
    override suspend fun getWorkouts(cursor: String?, limit: Int) = getWorkoutsResponse
    override suspend fun postWorkout(workout: ApiWorkout) = postWorkoutResponse
    override suspend fun patchWorkout(id: String, title: String) = patchWorkoutResponse
    override suspend fun deleteWorkout(id: String) { deleteWorkoutCalledWith = id }
    override suspend fun getSyncStatus() = getSyncStatusResponse
}

fun fakeApiUser(
    id: String = "u-1",
    name: String = "Test User",
    email: String = "test@example.com",
    units: String = "KG",
) = ApiUser(id = id, name = name, email = email, photoUrl = null, units = units)

fun fakeApiAuthResponse(
    token: String = "jwt-token",
    user: ApiUser = fakeApiUser(),
) = ApiAuthResponse(token = token, user = user)

// ── Fake clock ───────────────────────────────────────────────────────────────

class FakeClock(
    private val year: Int = 2026,
    private val month: Int = 5,
    private val day: Int = 23,
    private var hour: Int = 10,
) : Clock {
    override fun now(): Instant = Instant.fromEpochMilliseconds(0)
    override fun timeZone(): TimeZone = TimeZone.UTC
    override fun nowLocalDateTime(): LocalDateTime = LocalDateTime(year, month, day, hour, 0)

    fun setHour(h: Int) { hour = h }
}
