package com.harsh.fittrack.feature.record

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.harsh.fittrack.domain.model.ExerciseEntry
import com.harsh.fittrack.domain.model.PersonalRecord
import com.harsh.fittrack.domain.model.SetEntry
import com.harsh.fittrack.core.time.Clock
import com.harsh.fittrack.domain.repository.ExerciseRepository
import com.harsh.fittrack.domain.repository.ExerciseWithSets
import com.harsh.fittrack.domain.repository.PersonalRecordRepository
import com.harsh.fittrack.domain.repository.WorkoutRepository
import com.harsh.fittrack.domain.usecase.record.ValidateWorkoutUseCase
import com.harsh.fittrack.domain.usecase.record.WorkoutValidationResult
import kotlinx.coroutines.launch
import kotlin.random.Random
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class RecordViewModel(
    private val workoutRepository: WorkoutRepository,
    private val exerciseRepository: ExerciseRepository,
    private val validateWorkout: ValidateWorkoutUseCase,
    private val clock: Clock,
    private val personalRecordRepository: PersonalRecordRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(RecordState())
    val state: StateFlow<RecordState> = _state.asStateFlow()

    val suggestedTitle: String = defaultWorkoutTitle()

    private var userId: String? = null

    fun startOrResumeWorkout(userId: String) {
        this.userId = userId
        viewModelScope.launch {
            val active = workoutRepository.getActiveWorkout(userId) ?: return@launch
            _state.value = RecordState(
                workoutId = active.workout.id,
                title = active.workout.title,
                hasStarted = true,
                exercises = active.exercises,
            )
        }
    }

    fun startWorkout() {
        val uid = userId ?: return
        val title = _state.value.title.ifBlank { suggestedTitle }
        viewModelScope.launch {
            val workoutId = workoutRepository.createWorkout(userId = uid, title = title)
            _state.value = _state.value.copy(
                workoutId = workoutId,
                title = title,
                hasStarted = true,
            )
        }
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
        val workoutId = _state.value.workoutId ?: return
        viewModelScope.launch { workoutRepository.renameWorkout(workoutId, title) }
    }

    fun addExercise(exerciseId: String) {
        viewModelScope.launch {
            val name = exerciseRepository.byId(exerciseId)?.name ?: exerciseId
            val pr = personalRecordRepository.getForExercise(exerciseId)
            val workoutId = _state.value.workoutId
            if (workoutId != null) {
                val entryId = workoutRepository.addExercise(workoutId, exerciseId, name)
                val setId = workoutRepository.addSet(entryId)
                val entry = ExerciseEntry(
                    id = entryId,
                    workoutId = workoutId,
                    exerciseId = exerciseId,
                    exerciseName = name,
                    orderIndex = _state.value.exercises.size,
                )
                val firstSet = SetEntry(
                    id = setId,
                    exerciseEntryId = entryId,
                    setNumber = 1,
                    reps = 0,
                    weight = 0.0,
                    isCompleted = false,
                )
                _state.value = _state.value.copy(
                    exercises = _state.value.exercises + ExerciseWithSets(entry = entry, sets = listOf(firstSet)),
                    prs = if (pr != null) _state.value.prs + (exerciseId to pr) else _state.value.prs,
                )
            } else {
                val entry = ExerciseEntry(
                    id = newId(),
                    workoutId = "local",
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
                    prs = if (pr != null) _state.value.prs + (exerciseId to pr) else _state.value.prs,
                )
            }
        }
    }

    fun addSet(exerciseEntryId: String) {
        val workoutId = _state.value.workoutId
        if (workoutId != null) {
            viewModelScope.launch {
                val setId = workoutRepository.addSet(exerciseEntryId)
                _state.value = _state.value.copy(
                    exercises = _state.value.exercises.map { ews ->
                        if (ews.entry.id != exerciseEntryId) ews
                        else {
                            val last = ews.sets.lastOrNull()
                            val set = SetEntry(
                                id = setId,
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
        } else {
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
    }

    fun updateSet(set: SetEntry) {
        val exercises = _state.value.exercises.map { ews ->
            if (ews.entry.id != set.exerciseEntryId) ews
            else ews.copy(sets = ews.sets.map { if (it.id == set.id) set else it })
        }
        val exerciseId = exercises.find { it.entry.id == set.exerciseEntryId }?.entry?.exerciseId
        var newPrIds = _state.value.newPrExerciseIds
        var updatedPrs = _state.value.prs
        if (set.isCompleted && exerciseId != null) {
            val currentPr = updatedPrs[exerciseId]
            if (detectNewPr(currentPr, set.weight, set.reps)) {
                newPrIds = newPrIds + exerciseId
                val newPr = PersonalRecord(
                    exerciseId = exerciseId,
                    maxWeightKg = set.weight,
                    maxReps = set.reps,
                    achievedAt = 0L,
                )
                updatedPrs = updatedPrs + (exerciseId to newPr)
                viewModelScope.launch { personalRecordRepository.upsert(newPr) }
            }
        }
        _state.value = _state.value.copy(
            exercises = exercises,
            newPrExerciseIds = newPrIds,
            prs = updatedPrs,
        )
        viewModelScope.launch { workoutRepository.updateSet(set) }
    }

    /** Returns true if the workout passed validation and can proceed to the complete screen. */
    fun finish(durationSeconds: Long): Boolean {
        val result = validateWorkout(_state.value.exercises)
        return when (result) {
            is WorkoutValidationResult.Valid -> {
                _state.value = _state.value.copy(isCompleting = true, validationErrors = emptyList())
                val workoutId = _state.value.workoutId
                if (workoutId != null) {
                    viewModelScope.launch {
                        workoutRepository.finishWorkout(workoutId, durationSeconds)
                    }
                }
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

    fun showSaveTemplateDialog() {
        _state.value = _state.value.copy(showSaveTemplateDialog = true)
    }

    fun dismissSaveTemplateDialog() {
        _state.value = _state.value.copy(showSaveTemplateDialog = false)
    }

    fun startFromTemplate(exerciseIds: List<Pair<String, String>>) {
        exerciseIds.forEach { (exerciseId, _) -> addExercise(exerciseId) }
    }

    fun discard() {
        val workoutId = _state.value.workoutId
        if (workoutId != null) {
            viewModelScope.launch { workoutRepository.discardWorkout(workoutId) }
        }
        _state.value = RecordState()
    }

    private fun newId(): String = Random.nextLong().toString(16)
}
