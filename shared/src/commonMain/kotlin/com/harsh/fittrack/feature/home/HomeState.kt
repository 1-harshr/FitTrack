package com.harsh.fittrack.feature.home

import com.harsh.fittrack.domain.model.Workout
import kotlinx.datetime.LocalDate

data class HomeState(
    val greeting: String = "",
    val firstName: String = "",
    val streakDays: Int = 0,
    val workoutsThisWeek: Int = 0,
    val totalWorkouts: Int = 0,
    val recentWorkouts: List<Workout> = emptyList(),
    val today: LocalDate? = null,
    val isLoading: Boolean = true,
    val coachInsight: CoachingInsight? = null,
    val coachIsLoading: Boolean = false,
)
