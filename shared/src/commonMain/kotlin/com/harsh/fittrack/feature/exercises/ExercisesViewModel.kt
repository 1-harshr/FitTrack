package com.harsh.fittrack.feature.exercises

import androidx.lifecycle.ViewModel
import com.harsh.fittrack.domain.model.MuscleGroup
import com.harsh.fittrack.domain.repository.ExerciseCatalog
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ExercisesViewModel(
    private val catalog: ExerciseCatalog,
) : ViewModel() {

    private val _state = MutableStateFlow(ExercisesState(results = catalog.all()))
    val state: StateFlow<ExercisesState> = _state.asStateFlow()

    fun setQuery(query: String) { /* TODO */ }
    fun setMuscleGroup(group: MuscleGroup?) { /* TODO */ }
}
