package com.harsh.fittrack.domain.model

/**
 * A single set inside an ExerciseEntry.
 * Weight is always stored in kg — the UI converts to lbs on display.
 */
data class SetEntry(
    val id: String,
    val exerciseEntryId: String,
    val setNumber: Int,
    val reps: Int,
    val weight: Double,
    val isCompleted: Boolean,
)
