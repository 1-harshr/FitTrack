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

    val strengthCount: Int = catalog.all().count { it.movementType.name != "CARDIO" }
    val cardioCount: Int = catalog.all().count { it.movementType.name == "CARDIO" }

    fun setQuery(query: String) {
        val group = _state.value.activeMuscleGroup
        _state.value = _state.value.copy(
            query = query,
            results = catalog.search(query, group),
        )
    }

    fun setMuscleGroup(group: MuscleGroup?) {
        val query = _state.value.query
        _state.value = _state.value.copy(
            activeMuscleGroup = group,
            results = catalog.search(query, group),
        )
    }
}
