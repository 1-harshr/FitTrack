package com.harsh.fittrack.feature.profile

import com.harsh.fittrack.domain.model.Units
import com.harsh.fittrack.domain.model.User

data class ProfileState(
    val user: User? = null,
    val totalWorkouts: Int = 0,
    val streakDays: Int = 0,
    val totalVolumeKg: Double = 0.0,
    val units: Units = Units.KG,
)
