package com.harsh.fittrack.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavDestination.Companion.hasRoute

private const val DURATION = 300

// -1 means this is a detail/overlay screen, not a root tab
private fun NavBackStackEntry.tabIndex(): Int = when {
    destination.hasRoute<Route.Home>() -> 0
    destination.hasRoute<Route.RecordWorkout>() -> 1
    destination.hasRoute<Route.ExerciseLibrary>() -> 2
    destination.hasRoute<Route.Profile>() -> 3
    else -> -1
}

/**
 * Enter transition for a tab composable.
 * Slides in from left if navigating to a lower-indexed tab, from right otherwise.
 * Falls back to slide-from-right for detail screens entering over a tab.
 */
internal fun AnimatedContentTransitionScope<NavBackStackEntry>.tabEnter(): EnterTransition {
    val from = initialState.tabIndex()
    val to = targetState.tabIndex()
    val fromRight = from == -1 || to == -1 || to > from
    return slideInHorizontally(tween(DURATION)) { if (fromRight) it else -it } +
        fadeIn(tween(DURATION))
}

/**
 * Exit transition for a tab composable.
 * Slides out to right when a higher tab is incoming, to left when a lower tab is incoming.
 */
internal fun AnimatedContentTransitionScope<NavBackStackEntry>.tabExit(): ExitTransition {
    val from = initialState.tabIndex()
    val to = targetState.tabIndex()
    val toRight = to == -1 || from == -1 || to > from
    return slideOutHorizontally(tween(DURATION)) { if (toRight) -it else it } +
        fadeOut(tween(DURATION))
}

// Detail screens always push from the right and pop back to the right.
internal fun slideInFromRight(): EnterTransition =
    slideInHorizontally(tween(DURATION)) { it } + fadeIn(tween(DURATION))

internal fun slideOutToRight(): ExitTransition =
    slideOutHorizontally(tween(DURATION)) { it } + fadeOut(tween(DURATION))

internal fun slideInFromLeft(): EnterTransition =
    slideInHorizontally(tween(DURATION)) { -it } + fadeIn(tween(DURATION))

internal fun slideOutToLeft(): ExitTransition =
    slideOutHorizontally(tween(DURATION)) { -it } + fadeOut(tween(DURATION))
