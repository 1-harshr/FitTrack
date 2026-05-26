package com.harsh.fittrack.domain.model

data class TemplateExercise(
    val exerciseId: String,
    val exerciseName: String,
    val orderIndex: Int,
)

data class WorkoutTemplate(
    val id: String,
    val name: String,
    val exercises: List<TemplateExercise>,
    val createdAt: Long,
)
