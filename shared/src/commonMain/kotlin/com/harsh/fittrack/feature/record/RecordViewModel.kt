package com.harsh.fittrack.feature.record

import androidx.lifecycle.ViewModel
import com.harsh.fittrack.domain.model.ExerciseEntry
import com.harsh.fittrack.domain.model.SetEntry
import com.harsh.fittrack.domain.repository.ExerciseCatalog
import com.harsh.fittrack.domain.repository.ExerciseWithSets
import com.harsh.fittrack.domain.repository.WorkoutRepository
import kotlin.random.Random
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class RecordViewModel(
    private val workoutRepository: WorkoutRepository,
    private val catalog: ExerciseCatalog,
) : ViewModel() {

    private val _state = MutableStateFlow(RecordState())
    val state: StateFlow<RecordState> = _state.asStateFlow()

    fun startOrResumeWorkout(userId: String) { /* DB integration pending */ }

    fun renameTitle(title: String) {
        _state.value = _state.value.copy(title = title)
    }

    fun addExercise(exerciseId: String) {
        val entry = ExerciseEntry(
            id = newId(),
            workoutId = _state.value.workoutId ?: "local",
            exerciseId = exerciseId,
            orderIndex = _state.value.exercises.size,
        )
        val name = catalog.byId(exerciseId)?.name ?: exerciseId
        _state.value = _state.value.copy(
            exercises = _state.value.exercises + ExerciseWithSets(entry = entry, sets = emptyList()),
            exerciseNames = _state.value.exerciseNames + (exerciseId to name),
        )
    }

    fun addSet(exerciseEntryId: String) {
        _state.value = _state.value.copy(
            exercises = _state.value.exercises.map { ews ->
                if (ews.entry.id != exerciseEntryId) ews
                else {
                    val nextNumber = ews.sets.size + 1
                    val set = SetEntry(
                        id = newId(),
                        exerciseEntryId = exerciseEntryId,
                        setNumber = nextNumber,
                        reps = 0,
                        weight = 0.0,
                        isCompleted = false,
                    )
                    ews.copy(sets = ews.sets + set)
                }
            },
        )
    }

    fun updateSet(set: SetEntry) {
        _state.value = _state.value.copy(
            exercises = _state.value.exercises.map { ews ->
                if (ews.entry.id != set.exerciseEntryId) ews
                else ews.copy(sets = ews.sets.map { if (it.id == set.id) set else it })
            },
        )
    }

    fun finish() {
        _state.value = _state.value.copy(isCompleting = true)
        // DB persistence pending
    }

    fun discard() {
        _state.value = RecordState()
    }

    private fun newId(): String = Random.nextLong().toString(16)
}
