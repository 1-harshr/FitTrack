package com.harsh.fittrack.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.harsh.fittrack.core.time.Clock
import com.harsh.fittrack.core.util.GreetingProvider
import com.harsh.fittrack.domain.model.Workout
import com.harsh.fittrack.domain.repository.UserRepository
import com.harsh.fittrack.domain.repository.WorkoutRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModel(
    private val userRepository: UserRepository,
    private val workoutRepository: WorkoutRepository,
    private val greetingProvider: GreetingProvider,
    private val clock: Clock,
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
                val completed = workouts.filter { it.isCompleted }
                HomeState(
                    greeting = greetingProvider.greeting(),
                    firstName = user?.name?.firstWord() ?: "",
                    streakDays = computeStreak(completed, today),
                    workoutsThisWeek = countThisWeek(completed, today),
                    totalWorkouts = completed.size,
                    recentWorkouts = workouts.take(10),
                    today = today,
                    isLoading = false,
                )
            }.collect { _state.value = it }
        }
    }

    private fun computeStreak(completed: List<Workout>, today: LocalDate): Int {
        val dates = completed.map { it.date }.toHashSet()
        var streak = 0
        var check = today
        // Accept streak that may start from today or yesterday
        if (!dates.contains(check)) check = check.minus(DatePeriod(days = 1))
        while (dates.contains(check)) {
            streak++
            check = check.minus(DatePeriod(days = 1))
        }
        return streak
    }

    private fun countThisWeek(completed: List<Workout>, today: LocalDate): Int {
        val daysFromMonday = (today.dayOfWeek.ordinal - DayOfWeek.MONDAY.ordinal + 7) % 7
        val weekStart = today.minus(DatePeriod(days = daysFromMonday))
        return completed.count { it.date >= weekStart && it.date <= today }
    }

    private fun String.firstWord(): String = trim().split(" ").firstOrNull() ?: this
}
