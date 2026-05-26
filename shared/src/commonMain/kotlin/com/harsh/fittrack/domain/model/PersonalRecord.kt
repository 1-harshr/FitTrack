package com.harsh.fittrack.domain.model

data class PersonalRecord(
    val exerciseId: String,
    val maxWeightKg: Double,
    val maxReps: Int,
    val achievedAt: Long,
)
