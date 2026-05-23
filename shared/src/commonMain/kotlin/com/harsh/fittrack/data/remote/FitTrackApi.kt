package com.harsh.fittrack.data.remote

import kotlinx.serialization.Serializable

interface FitTrackApi {
    suspend fun login(email: String, password: String): ApiAuthResponse?
    suspend fun register(name: String, email: String, password: String): ApiAuthResponse?
    suspend fun getMe(): ApiUser?
    suspend fun patchMe(units: String): ApiUser?
    suspend fun getExercises(sinceVersion: Int = 0): ApiExerciseSyncResponse?
    suspend fun getWorkouts(cursor: String? = null, limit: Int = 20): ApiWorkoutListResponse?
    suspend fun postWorkout(workout: ApiWorkout): ApiWorkout?
    suspend fun patchWorkout(id: String, title: String): ApiWorkout?
    suspend fun deleteWorkout(id: String)
    suspend fun getSyncStatus(): ApiSyncStatusResponse?
}

// ── DTOs (mirror server JSON schema exactly) ─────────────────────────────────

@Serializable
data class ApiUser(
    val id: String,
    val name: String,
    val email: String,
    val photoUrl: String?,
    val units: String,
)

@Serializable
data class ApiSetEntry(
    val id: String,
    val setNumber: Int,
    val reps: Int,
    val weightKg: Double,
    val isCompleted: Boolean,
)

@Serializable
data class ApiExerciseEntry(
    val id: String,
    val exerciseId: String,
    val exerciseName: String,
    val orderIndex: Int,
    val sets: List<ApiSetEntry>,
)

@Serializable
data class ApiWorkout(
    val id: String,
    val userId: String,
    val title: String,
    val date: String,
    val startedAt: Long,
    val durationSeconds: Int,
    val totalVolumeKg: Double,
    val exercises: List<ApiExerciseEntry>,
)

@Serializable
data class ApiWorkoutListResponse(
    val nextCursor: String?,
    val workouts: List<ApiWorkout>,
)

@Serializable
data class ApiExercise(
    val id: String,
    val name: String,
    val primaryMuscle: String,
    val secondaryMuscles: List<String>,
    val equipment: String,
    val movementType: String,
    val instructions: List<String>,
    val catalogVersion: Int,
)

@Serializable
data class ApiExerciseSyncResponse(
    val latestVersion: Int,
    val exercises: List<ApiExercise>,
)

@Serializable
data class ApiSyncStatusResponse(
    val workoutCursor: String?,
    val catalogVersion: Int,
)

@Serializable
data class ApiAuthResponse(
    val token: String,
    val user: ApiUser,
)

@Serializable
internal data class ApiLoginRequest(val email: String, val password: String)

@Serializable
internal data class ApiRegisterRequest(val name: String, val email: String, val password: String)

@Serializable
internal data class ApiPatchUserRequest(val units: String? = null)

@Serializable
internal data class ApiPatchWorkoutRequest(val title: String? = null)
