package com.harsh.fittrack.data.local.mapper

import com.harsh.fittrack.db.ExerciseEntity
import com.harsh.fittrack.db.ExerciseEntryEntity
import com.harsh.fittrack.db.PersonalRecordEntity
import com.harsh.fittrack.db.SetEntryEntity
import com.harsh.fittrack.db.UserEntity
import com.harsh.fittrack.db.WorkoutEntity
import com.harsh.fittrack.domain.model.Equipment
import com.harsh.fittrack.domain.model.Exercise
import com.harsh.fittrack.domain.model.ExerciseEntry
import com.harsh.fittrack.domain.model.MovementType
import com.harsh.fittrack.domain.model.MuscleGroup
import com.harsh.fittrack.domain.model.PersonalRecord
import com.harsh.fittrack.domain.model.SetEntry
import com.harsh.fittrack.domain.model.Units
import com.harsh.fittrack.domain.model.User
import com.harsh.fittrack.domain.model.Workout
import kotlinx.datetime.LocalDate

private const val SEP = "|"

fun ExerciseEntity.toDomain() = Exercise(
    id = id,
    name = name,
    primaryMuscle = runCatching { MuscleGroup.valueOf(primaryMuscle) }.getOrDefault(MuscleGroup.CHEST),
    secondaryMuscles = secondaryMuscles.split(SEP).filter { it.isNotEmpty() }
        .mapNotNull { runCatching { MuscleGroup.valueOf(it) }.getOrNull() },
    equipment = runCatching { Equipment.valueOf(equipment) }.getOrDefault(Equipment.BODYWEIGHT),
    movementType = runCatching { MovementType.valueOf(movementType) }.getOrDefault(MovementType.COMPOUND),
    instructions = instructions.split(SEP).filter { it.isNotEmpty() },
)

fun Exercise.toEntity(isCustom: Boolean = false, catalogVersion: Int = 1) = ExerciseEntity(
    id = id,
    name = name,
    primaryMuscle = primaryMuscle.name,
    secondaryMuscles = secondaryMuscles.joinToString(SEP) { it.name },
    equipment = equipment.name,
    movementType = movementType.name,
    instructions = instructions.joinToString(SEP),
    isCustom = if (isCustom) 1L else 0L,
    catalogVersion = catalogVersion.toLong(),
)

fun WorkoutEntity.toDomain() = Workout(
    id = id,
    userId = userId,
    title = title,
    date = LocalDate.parse(date),
    startedAt = startedAt,
    durationSeconds = durationSeconds,
    totalVolumeKg = totalVolumeKg,
    isCompleted = isCompleted != 0L,
)

fun ExerciseEntryEntity.toDomain() = ExerciseEntry(
    id = id,
    workoutId = workoutId,
    exerciseId = exerciseId,
    exerciseName = exerciseName,
    orderIndex = orderIndex.toInt(),
)

fun SetEntryEntity.toDomain() = SetEntry(
    id = id,
    exerciseEntryId = exerciseEntryId,
    setNumber = setNumber.toInt(),
    reps = reps.toInt(),
    weight = weight,
    isCompleted = isCompleted != 0L,
)

fun PersonalRecordEntity.toDomain() = PersonalRecord(
    exerciseId = exerciseId,
    maxWeightKg = maxWeightKg,
    maxReps = maxReps.toInt(),
    achievedAt = achievedAt,
)

fun UserEntity.toDomain() = User(
    id = id,
    name = name,
    email = email,
    photoUrl = photoUrl,
    units = runCatching { Units.valueOf(units) }.getOrDefault(Units.KG),
)
