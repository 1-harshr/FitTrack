package com.harsh.fittrack.feature.stats

import kotlinx.serialization.Serializable

@Serializable
data class WeeklyVolumePoint(val weekLabel: String, val volumeKg: Double)

@Serializable
data class WeeklyVolumeResponse(val weeks: List<WeeklyVolumePoint>)

@Serializable
data class ExerciseProgressionPoint(val date: String, val maxWeightKg: Double)

@Serializable
data class ExerciseProgressionResponse(val exerciseName: String, val points: List<ExerciseProgressionPoint>)

@Serializable
data class MuscleFrequencyPoint(val muscle: String, val sessionCount: Int)

@Serializable
data class MuscleFrequencyResponse(val points: List<MuscleFrequencyPoint>)
