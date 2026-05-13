package com.harsh.fittrack.feature.home

import com.harsh.fittrack.domain.repository.WorkoutWithDetails

data class WorkoutDetailState(
    val details: WorkoutWithDetails? = null,
    val isLoading: Boolean = true,
)
