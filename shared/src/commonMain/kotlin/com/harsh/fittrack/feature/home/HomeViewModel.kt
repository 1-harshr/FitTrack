package com.harsh.fittrack.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.harsh.fittrack.core.time.Clock
import com.harsh.fittrack.core.util.GreetingProvider
import com.harsh.fittrack.data.remote.FitTrackApi
import com.harsh.fittrack.domain.repository.UserRepository
import com.harsh.fittrack.domain.repository.WorkoutRepository
import com.harsh.fittrack.domain.usecase.stats.CalculateStreakUseCase
import com.harsh.fittrack.domain.usecase.stats.CalculateWeeklyWorkoutsUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModel(
    private val userRepository: UserRepository,
    private val workoutRepository: WorkoutRepository,
    private val greetingProvider: GreetingProvider,
    private val clock: Clock,
    private val calculateStreak: CalculateStreakUseCase,
    private val calculateWeeklyWorkouts: CalculateWeeklyWorkoutsUseCase,
    private val api: FitTrackApi,
) : ViewModel() {

    private val _state = MutableStateFlow(HomeState())
    val state: StateFlow<HomeState> = _state.asStateFlow()

    init {
        val workoutsFlow = userRepository.observeUser().flatMapLatest { user ->
            if (user != null) workoutRepository.observeWorkouts(user.id) else flowOf(emptyList())
        }

        viewModelScope.launch {
            combine(userRepository.observeUser(), workoutsFlow) { user, workouts ->
                val today = clock.nowLocalDateTime().date
                HomeState(
                    greeting = greetingProvider.greeting(),
                    firstName = user?.name?.firstWord() ?: "",
                    streakDays = calculateStreak(workouts, today),
                    workoutsThisWeek = calculateWeeklyWorkouts(workouts, today),
                    totalWorkouts = workouts.count { it.isCompleted },
                    recentWorkouts = workouts.take(10),
                    today = today,
                    isLoading = false,
                )
            }.collect { _state.value = it }
        }

        refreshCoachingInsight()
    }

    fun refreshCoachingInsight() {
        viewModelScope.launch {
            _state.value = _state.value.copy(coachIsLoading = true)
            val resp = runCatching { api.getCoachingInsight() }.getOrNull()
            _state.value = _state.value.copy(
                coachIsLoading = false,
                coachInsight = resp?.let { r ->
                    CoachingInsight(
                        targetMuscleGroups = r.targetMuscleGroups,
                        progressionSuggestions = r.progressionSuggestions.map {
                            ProgressionSuggestion(it.exerciseName, it.currentBestKg, it.currentBestReps, it.suggestion)
                        },
                        weaknesses = r.weaknesses,
                        dailyTip = r.dailyTip,
                        generatedAt = r.generatedAt,
                    )
                },
            )
        }
    }

    private fun String.firstWord(): String = trim().split(" ").firstOrNull() ?: this
}
