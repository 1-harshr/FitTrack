package com.harsh.fittrack.feature.record

import androidx.lifecycle.ViewModel
import com.harsh.fittrack.domain.model.ExerciseEntry
import com.harsh.fittrack.domain.model.SetEntry
import com.harsh.fittrack.core.time.Clock
import com.harsh.fittrack.domain.repository.ExerciseCatalog
import com.harsh.fittrack.domain.repository.ExerciseWithSets
import com.harsh.fittrack.domain.repository.WorkoutRepository
import com.harsh.fittrack.domain.usecase.record.ValidateWorkoutUseCase
import com.harsh.fittrack.domain.usecase.record.WorkoutValidationResult
import kotlin.random.Random
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class RecordViewModel(
    private val workoutRepository: WorkoutRepository,
    private val catalog: ExerciseCatalog,
    private val validateWorkout: ValidateWorkoutUseCase,
    private val clock: Clock,
) : ViewModel() {

    private val _state = MutableStateFlow(RecordState())
    val state: StateFlow<RecordState> = _state.asStateFlow()

    val suggestedTitle: String = defaultWorkoutTitle()

    fun startOrResumeWorkout(userId: String) { /* DB integration pending */ }

    fun startWorkout() {
        val title = _state.value.title.ifBlank { suggestedTitle }
        _state.value = _state.value.copy(hasStarted = true, title = title)
    }

    private fun defaultWorkoutTitle(): String {
        val dt = clock.nowLocalDateTime()
        val period = when (dt.hour) {
            in 5..11 -> "Morning"
            in 12..16 -> "Afternoon"
            in 17..20 -> "Evening"
            else -> "Night"
        }
        val month = dt.date.month.name.lowercase().replaceFirstChar { it.uppercase() }.take(3)
        return "$period Workout ($month ${dt.date.dayOfMonth})"
    }

    fun renameTitle(title: String) {
        _state.value = _state.value.copy(title = title)
    }

    fun addExercise(exerciseId: String) {
        val name = catalog.byId(exerciseId)?.name ?: exerciseId
        val entry = ExerciseEntry(
            id = newId(),
            workoutId = _state.value.workoutId ?: "local",
            exerciseId = exerciseId,
            exerciseName = name,
            orderIndex = _state.value.exercises.size,
        )
        val firstSet = SetEntry(
            id = newId(),
            exerciseEntryId = entry.id,
            setNumber = 1,
            reps = 0,
            weight = 0.0,
            isCompleted = false,
        )
        _state.value = _state.value.copy(
            exercises = _state.value.exercises + ExerciseWithSets(entry = entry, sets = listOf(firstSet)),
        )
    }

    fun addSet(exerciseEntryId: String) {
        _state.value = _state.value.copy(
            exercises = _state.value.exercises.map { ews ->
                if (ews.entry.id != exerciseEntryId) ews
                else {
                    val last = ews.sets.lastOrNull()
                    val set = SetEntry(
                        id = newId(),
                        exerciseEntryId = exerciseEntryId,
                        setNumber = ews.sets.size + 1,
                        reps = last?.reps ?: 0,
                        weight = last?.weight ?: 0.0,
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

    /** Returns true if the workout passed validation and can proceed to the complete screen. */
    fun finish(): Boolean {
        val result = validateWorkout(_state.value.exercises)
        return when (result) {
            is WorkoutValidationResult.Valid -> {
                _state.value = _state.value.copy(isCompleting = true, validationErrors = emptyList())
                // DB persistence pending
                true
            }
            is WorkoutValidationResult.Invalid -> {
                _state.value = _state.value.copy(validationErrors = result.errors)
                false
            }
        }
    }

    fun clearValidationErrors() {
        _state.value = _state.value.copy(validationErrors = emptyList())
    }

    fun discard() {
        _state.value = RecordState()
    }

    private fun newId(): String = Random.nextLong().toString(16)
}
