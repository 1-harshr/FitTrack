package com.harsh.fittrack.feature.ai

import kotlinx.serialization.Serializable

@Serializable
data class ProgressionSuggestion(
    val exerciseName: String,
    val currentBestKg: Double,
    val currentBestReps: Int,
    val suggestion: String,
)

@Serializable
data class CoachingInsight(
    val targetMuscleGroups: List<String>,
    val progressionSuggestions: List<ProgressionSuggestion>,
    val weaknesses: List<String>,
    val dailyTip: String,
    val generatedAt: Long,
)
