package com.harsh.fittrack.feature.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.harsh.fittrack.core.time.Clock
import com.harsh.fittrack.domain.model.Units
import com.harsh.fittrack.domain.repository.AuthRepository
import com.harsh.fittrack.domain.repository.UserRepository
import com.harsh.fittrack.domain.repository.WorkoutRepository
import com.harsh.fittrack.domain.usecase.stats.CalculateStreakUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class ProfileViewModel(
    private val userRepository: UserRepository,
    private val workoutRepository: WorkoutRepository,
    private val authRepository: AuthRepository,
    private val clock: Clock,
    private val calculateStreak: CalculateStreakUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(ProfileState())
    val state: StateFlow<ProfileState> = _state.asStateFlow()

    init {
        val workoutsFlow = userRepository.observeUser().flatMapLatest { user ->
            if (user != null) workoutRepository.observeWorkouts(user.id) else flowOf(emptyList())
        }

        viewModelScope.launch {
            combine(userRepository.observeUser(), workoutsFlow) { user, workouts ->
                val today = clock.nowLocalDateTime().date
                val completed = workouts.filter { it.isCompleted }
                ProfileState(
                    user = user,
                    totalWorkouts = completed.size,
                    streakDays = calculateStreak(workouts, today),
                    totalVolumeKg = completed.sumOf { it.totalVolumeKg },
                    totalVolumeThisMonthKg = completed
                        .filter { it.date.month == today.month && it.date.year == today.year }
                        .sumOf { it.totalVolumeKg },
                    units = user?.units ?: Units.KG,
                    isLoading = false,
                )
            }.collect { _state.value = it }
        }
    }

    fun toggleUnits() {
        viewModelScope.launch {
            val next = if (_state.value.units == Units.KG) Units.LBS else Units.KG
            userRepository.setUnits(next)
            _state.value = _state.value.copy(units = next)
        }
    }

    fun signOut() {
        viewModelScope.launch { authRepository.signOut() }
    }
}
