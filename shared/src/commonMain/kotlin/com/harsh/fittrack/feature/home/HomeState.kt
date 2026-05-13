package com.harsh.fittrack.feature.home

import com.harsh.fittrack.domain.model.Workout

data class HomeState(
    val greeting: String = "",
    val firstName: String = "",
    val streakDays: Int = 0,
    val workoutsThisWeek: Int = 0,
    val totalWorkouts: Int = 0,
    val workouts: List<Workout> = emptyList(),
    val isLoading: Boolean = true,
)
