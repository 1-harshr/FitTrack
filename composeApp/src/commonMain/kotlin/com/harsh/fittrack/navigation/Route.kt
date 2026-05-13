package com.harsh.fittrack.navigation

import kotlinx.serialization.Serializable

/**
 * Type-safe navigation routes. Each tab has its own back stack — tabs do not share one.
 * ExerciseDetail carries its source so the back destination is context-aware.
 */
sealed interface Route {

    // Auth flow
    @Serializable data object Splash : Route
    @Serializable data object Login : Route

    // Tab graph host
    @Serializable data object MainTabs : Route

    // Tab graphs
    @Serializable data object HomeGraph : Route
    @Serializable data object RecordGraph : Route
    @Serializable data object ExercisesGraph : Route
    @Serializable data object ProfileGraph : Route

    // Home tab
    @Serializable data object Home : Route
    @Serializable data class WorkoutDetail(val workoutId: String) : Route

    // Record tab
    @Serializable data object RecordWorkout : Route
    @Serializable data object WorkoutComplete : Route

    // Exercises tab + sheet context
    @Serializable data object ExerciseLibrary : Route
    @Serializable data class ExerciseDetail(
        val exerciseId: String,
        val source: ExerciseDetailSource,
    ) : Route

    // Profile tab
    @Serializable data object Profile : Route
}

@Serializable
enum class ExerciseDetailSource { BROWSE, SHEET }
