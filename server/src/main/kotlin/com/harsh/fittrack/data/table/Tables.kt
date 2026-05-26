package com.harsh.fittrack.data.table

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.kotlin.datetime.timestampWithTimeZone

object UsersTable : Table("users") {
    val id           = text("id")
    val name         = text("name")
    val email        = text("email").uniqueIndex()
    val passwordHash = text("password_hash").nullable()
    val photoUrl     = text("photo_url").nullable()
    val units        = text("units").default("KG")
    val createdAt    = timestampWithTimeZone("created_at")
    val updatedAt    = timestampWithTimeZone("updated_at")
    override val primaryKey = PrimaryKey(id)
}

object ExercisesTable : Table("exercises") {
    val id               = text("id")
    val name             = text("name")
    val primaryMuscle    = text("primary_muscle")
    val secondaryMuscles = array<String>("secondary_muscles")
    val equipment        = text("equipment")
    val movementType     = text("movement_type")
    val instructions     = array<String>("instructions")
    val isCustom         = bool("is_custom").default(false)
    val catalogVersion   = integer("catalog_version").default(1)
    val createdAt        = timestampWithTimeZone("created_at")
    val updatedAt        = timestampWithTimeZone("updated_at")
    override val primaryKey = PrimaryKey(id)
}

object WorkoutsTable : Table("workouts") {
    val id              = text("id")
    val userId          = text("user_id").references(UsersTable.id)
    val title           = text("title")
    val date            = text("date")
    val startedAt       = long("started_at")
    val durationSeconds = integer("duration_seconds").default(0)
    val totalVolumeKg   = decimal("total_volume_kg", precision = 10, scale = 2).default(0.toBigDecimal())
    val deletedAt       = timestampWithTimeZone("deleted_at").nullable()
    val createdAt       = timestampWithTimeZone("created_at")
    override val primaryKey = PrimaryKey(id)
}

object ExerciseEntriesTable : Table("exercise_entries") {
    val id           = text("id")
    val workoutId    = text("workout_id").references(WorkoutsTable.id)
    val exerciseId   = text("exercise_id")
    val exerciseName = text("exercise_name")
    val orderIndex   = integer("order_index")
    val createdAt    = timestampWithTimeZone("created_at")
    override val primaryKey = PrimaryKey(id)
}

object SetEntriesTable : Table("set_entries") {
    val id              = text("id")
    val exerciseEntryId = text("exercise_entry_id").references(ExerciseEntriesTable.id)
    val setNumber       = integer("set_number")
    val reps            = integer("reps")
    val weightKg        = decimal("weight_kg", precision = 6, scale = 2)
    val isCompleted     = bool("is_completed")
    val createdAt       = timestampWithTimeZone("created_at")
    override val primaryKey = PrimaryKey(id)
}

object PersonalRecordsTable : Table("personal_records") {
    val userId      = text("user_id").references(UsersTable.id)
    val exerciseId  = text("exercise_id")
    val maxWeightKg = decimal("max_weight_kg", precision = 6, scale = 2)
    val maxReps     = integer("max_reps")
    val achievedAt  = long("achieved_at")
    override val primaryKey = PrimaryKey(userId, exerciseId)
}

object WorkoutTemplatesTable : Table("workout_templates") {
    val id        = text("id")
    val userId    = text("user_id").references(UsersTable.id)
    val name      = text("name")
    val createdAt = timestampWithTimeZone("created_at")
    override val primaryKey = PrimaryKey(id)
}

object TemplateExercisesTable : Table("template_exercises") {
    val id           = text("id")
    val templateId   = text("template_id").references(WorkoutTemplatesTable.id)
    val exerciseId   = text("exercise_id")
    val exerciseName = text("exercise_name")
    val orderIndex   = integer("order_index")
    override val primaryKey = PrimaryKey(id)
}
