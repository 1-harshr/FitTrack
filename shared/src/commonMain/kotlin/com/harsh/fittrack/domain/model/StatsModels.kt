package com.harsh.fittrack.domain.model

data class WeeklyVolumePoint(val weekLabel: String, val volumeKg: Double)
data class WeeklyVolumeResponse(val weeks: List<WeeklyVolumePoint>)

data class ExerciseProgressionPoint(val date: String, val maxWeightKg: Double)
data class ExerciseProgressionResponse(val exerciseName: String, val points: List<ExerciseProgressionPoint>)

data class MuscleFrequencyPoint(val muscle: String, val sessionCount: Int)
data class MuscleFrequencyResponse(val points: List<MuscleFrequencyPoint>)
