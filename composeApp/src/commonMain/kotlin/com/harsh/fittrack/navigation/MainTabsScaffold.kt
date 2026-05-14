package com.harsh.fittrack.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.harsh.fittrack.domain.repository.UserRepository
import com.harsh.fittrack.ui.component.FitBottomNav
import com.harsh.fittrack.ui.feature.exercises.ExerciseDetailScreen
import com.harsh.fittrack.ui.feature.exercises.ExerciseLibraryScreen
import com.harsh.fittrack.ui.feature.home.HomeScreen
import com.harsh.fittrack.ui.feature.home.WorkoutDetailScreen
import com.harsh.fittrack.ui.feature.profile.ProfileScreen
import com.harsh.fittrack.ui.feature.record.RecordWorkoutScreen
import com.harsh.fittrack.ui.feature.record.WorkoutCompleteScreen
import org.koin.compose.koinInject

@Composable
fun MainTabsScaffold(onSignedOut: () -> Unit = {}) {
    val userRepository: UserRepository = koinInject()
    val user by userRepository.observeUser().collectAsStateWithLifecycle(null)
    val userId = user?.id ?: return

    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()

    val currentTab = when {
        backStackEntry?.destination?.hasRoute<Route.Home>() == true ||
        backStackEntry?.destination?.hasRoute<Route.WorkoutDetail>() == true -> NavTab.Home
        backStackEntry?.destination?.hasRoute<Route.RecordWorkout>() == true ||
        backStackEntry?.destination?.hasRoute<Route.WorkoutComplete>() == true -> NavTab.Record
        backStackEntry?.destination?.hasRoute<Route.ExerciseLibrary>() == true ||
        backStackEntry?.destination?.hasRoute<Route.ExerciseDetail>() == true -> NavTab.Exercises
        backStackEntry?.destination?.hasRoute<Route.Profile>() == true -> NavTab.Profile
        else -> NavTab.Home
    }

    Scaffold(
        bottomBar = {
            FitBottomNav(
                currentTab = currentTab,
                onSelectTab = { tab ->
                    navController.navigate(tab.startRoute) {
                        popUpTo(Route.Home) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
            )
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Route.Home,
            modifier = Modifier.padding(innerPadding),
            // Default transitions for detail screens pushed on top of a tab
            enterTransition = { slideInFromRight() },
            exitTransition = { slideOutToLeft() },
            popEnterTransition = { slideInFromLeft() },
            popExitTransition = { slideOutToRight() },
        ) {
            composable<Route.Home>(
                enterTransition = { tabEnter() },
                exitTransition = { tabExit() },
                popEnterTransition = { tabEnter() },
                popExitTransition = { tabExit() },
            ) {
                HomeScreen(
                    onWorkoutClick = { workoutId ->
                        navController.navigate(Route.WorkoutDetail(workoutId))
                    },
                    onStartWorkout = {
                        navController.navigate(Route.RecordWorkout)
                    },
                )
            }

            composable<Route.WorkoutDetail> { backStackEntry ->
                WorkoutDetailScreen(
                    backStackEntry = backStackEntry,
                    onBack = { navController.popBackStack() },
                )
            }

            composable<Route.RecordWorkout>(
                enterTransition = { tabEnter() },
                exitTransition = { tabExit() },
                popEnterTransition = { tabEnter() },
                popExitTransition = { tabExit() },
            ) { backStackEntry ->
                val addedExerciseId by backStackEntry.savedStateHandle
                    .getStateFlow<String?>("added_exercise_id", null)
                    .collectAsStateWithLifecycle()

                RecordWorkoutScreen(
                    userId = userId,
                    onFinished = {
                        navController.navigate(Route.WorkoutComplete) {
                            popUpTo(Route.RecordWorkout) { inclusive = true }
                        }
                    },
                    onDiscard = { navController.popBackStack() },
                    onOpenExerciseDetail = { exerciseId ->
                        navController.navigate(Route.ExerciseDetail(exerciseId, ExerciseDetailSource.SHEET))
                    },
                    addedExerciseId = addedExerciseId,
                    onExerciseConsumed = {
                        backStackEntry.savedStateHandle.remove<String>("added_exercise_id")
                    },
                )
            }

            composable<Route.WorkoutComplete> {
                WorkoutCompleteScreen(
                    onDone = {
                        navController.navigate(Route.Home) {
                            popUpTo(Route.Home) { inclusive = true }
                        }
                    },
                )
            }

            composable<Route.ExerciseLibrary>(
                enterTransition = { tabEnter() },
                exitTransition = { tabExit() },
                popEnterTransition = { tabEnter() },
                popExitTransition = { tabExit() },
            ) {
                ExerciseLibraryScreen(
                    onExerciseClick = { exerciseId ->
                        navController.navigate(
                            Route.ExerciseDetail(exerciseId, ExerciseDetailSource.BROWSE)
                        )
                    },
                )
            }

            composable<Route.ExerciseDetail> { backStackEntry ->
                ExerciseDetailScreen(
                    backStackEntry = backStackEntry,
                    onBack = { navController.popBackStack() },
                    onAddToWorkout = { exerciseId ->
                        navController.previousBackStackEntry
                            ?.savedStateHandle
                            ?.set("added_exercise_id", exerciseId)
                        navController.popBackStack()
                    },
                )
            }

            composable<Route.Profile>(
                enterTransition = { tabEnter() },
                exitTransition = { tabExit() },
                popEnterTransition = { tabEnter() },
                popExitTransition = { tabExit() },
            ) {
                ProfileScreen(onSignedOut = onSignedOut)
            }
        }
    }
}
