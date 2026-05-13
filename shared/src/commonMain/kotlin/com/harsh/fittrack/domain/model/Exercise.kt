package com.harsh.fittrack.domain.model

/** Entry in the static exercise catalog (50+ exercises shipped with the app). */
data class Exercise(
    val id: String,
    val name: String,
    val primaryMuscle: MuscleGroup,
    val secondaryMuscles: List<MuscleGroup>,
    val equipment: Equipment,
    val movementType: MovementType,
    val instructions: List<String>,
)

enum class MuscleGroup {
    CHEST, BACK, LEGS, SHOULDERS, ARMS, CORE, GLUTES, CALVES,
}

enum class Equipment {
    BARBELL, DUMBBELL, CABLE, MACHINE, BODYWEIGHT, KETTLEBELL, BAND,
}

enum class MovementType {
    COMPOUND, ISOLATION, CARDIO,
}
