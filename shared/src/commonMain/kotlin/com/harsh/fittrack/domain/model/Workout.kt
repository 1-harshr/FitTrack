package com.harsh.fittrack.domain.model

import kotlinx.datetime.LocalDate

data class Workout(
    val id: String,
    val userId: String,
    val title: String,
    val date: LocalDate,
    val durationSeconds: Long,
    val isCompleted: Boolean,
)
