package com.harsh.fittrack.feature.exercises

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.harsh.fittrack.domain.model.MovementType
import com.harsh.fittrack.domain.model.MuscleGroup
import com.harsh.fittrack.domain.repository.ExerciseRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

@OptIn(ExperimentalCoroutinesApi::class)
class ExercisesViewModel(
    private val exerciseRepository: ExerciseRepository,
) : ViewModel() {

    private val _query = MutableStateFlow("")
    private val _muscleGroup = MutableStateFlow<MuscleGroup?>(null)

    val state: StateFlow<ExercisesState> = combine(
        _query,
        _muscleGroup,
        exerciseRepository.observeExercises(),
    ) { query, group, all ->
        val q = query.trim().lowercase()
        val filtered = all.filter { exercise ->
            (q.isEmpty() || exercise.name.lowercase().contains(q)) &&
                (group == null || exercise.primaryMuscle == group)
        }
        ExercisesState(
            query = query,
            activeMuscleGroup = group,
            results = filtered,
            strengthCount = all.count { it.movementType != MovementType.CARDIO },
            cardioCount = all.count { it.movementType == MovementType.CARDIO },
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ExercisesState())

    fun setQuery(query: String) { _query.value = query }
    fun setMuscleGroup(group: MuscleGroup?) { _muscleGroup.value = group }
}
