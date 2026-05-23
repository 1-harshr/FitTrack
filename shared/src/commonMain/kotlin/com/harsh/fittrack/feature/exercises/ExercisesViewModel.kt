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
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn

@OptIn(ExperimentalCoroutinesApi::class)
class ExercisesViewModel(
    private val exerciseRepository: ExerciseRepository,
) : ViewModel() {

    private val _query = MutableStateFlow("")
    private val _muscleGroup = MutableStateFlow<MuscleGroup?>(null)

    // Delegate filtering to the repository — it does the DB-level pass.
    // The combine here only assembles the counts and wraps into state.
    private val filteredFlow = combine(_query, _muscleGroup) { q, group -> q to group }
        .flatMapLatest { (q, group) ->
            exerciseRepository.observeExercises(query = q, muscleGroup = group)
        }

    // Always observe the full unfiltered list for counts so toggling a filter
    // doesn't zero out the strength/cardio totals shown in the UI.
    private val allFlow = exerciseRepository.observeExercises()

    val state: StateFlow<ExercisesState> = combine(
        _query,
        _muscleGroup,
        filteredFlow,
        allFlow,
    ) { query, group, filtered, all ->
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
