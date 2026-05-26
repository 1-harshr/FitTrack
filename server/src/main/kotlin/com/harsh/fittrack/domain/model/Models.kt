package com.harsh.fittrack.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class User(
    val id: String,
    val name: String,
    val email: String,
    val photoUrl: String?,
    val units: String,
)

@Serializable
data class Exercise(
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
data class Workout(
    val id: String,
    val userId: String,
    val title: String,
    val date: String,
    val startedAt: Long,
    val durationSeconds: Int,
    val totalVolumeKg: Double,
    val exercises: List<ExerciseEntry>,
)

@Serializable
data class ExerciseEntry(
    val id: String,
    val exerciseId: String,
    val exerciseName: String,
    val orderIndex: Int,
    val sets: List<SetEntry>,
)

@Serializable
data class SetEntry(
    val id: String,
    val setNumber: Int,
    val reps: Int,
    val weightKg: Double,
    val isCompleted: Boolean,
)

// ── Request / Response DTOs ──────────────────────────────────────────────────

@Serializable
data class RegisterRequest(val name: String, val email: String, val password: String)

@Serializable
data class LoginRequest(val email: String, val password: String)

@Serializable
data class AuthResponse(val token: String, val user: User)

@Serializable
data class PatchUserRequest(val units: String? = null)

@Serializable
data class PatchWorkoutRequest(val title: String? = null)

@Serializable
data class ExerciseSyncResponse(
    val latestVersion: Int,
    val exercises: List<Exercise>,
)

@Serializable
data class WorkoutListResponse(
    val nextCursor: String?,
    val workouts: List<Workout>,
)

@Serializable
data class PersonalRecord(
    val exerciseId: String,
    val maxWeightKg: Double,
    val maxReps: Int,
    val achievedAt: Long,
)

@Serializable
data class WorkoutSaveResponse(
    val workout: Workout,
    val newPrs: List<PersonalRecord> = emptyList(),
)

@Serializable
data class TemplateExercise(
    val exerciseId: String,
    val exerciseName: String,
    val orderIndex: Int,
)

@Serializable
data class WorkoutTemplate(
    val id: String,
    val name: String,
    val exercises: List<TemplateExercise>,
    val createdAt: Long,
)

@Serializable
data class CreateTemplateRequest(
    val name: String,
    val exercises: List<TemplateExercise>,
)

@Serializable
data class SyncStatusResponse(
    val workoutCursor: String?,
    val catalogVersion: Int,
)

@Serializable
data class ProblemDetail(
    val type: String,
    val title: String,
    val status: Int,
    val detail: String,
)
