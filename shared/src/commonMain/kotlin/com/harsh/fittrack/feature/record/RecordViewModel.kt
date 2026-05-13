package com.harsh.fittrack.feature.record

import androidx.lifecycle.ViewModel
import com.harsh.fittrack.domain.model.SetEntry
import com.harsh.fittrack.domain.repository.WorkoutRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class RecordViewModel(
    private val workoutRepository: WorkoutRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(RecordState())
    val state: StateFlow<RecordState> = _state.asStateFlow()

    fun startOrResumeWorkout(userId: String) { /* TODO */ }
    fun renameTitle(title: String) { /* TODO */ }
    fun addExercise(exerciseId: String) { /* TODO */ }
    fun addSet(exerciseEntryId: String) { /* TODO */ }
    fun updateSet(set: SetEntry) { /* TODO */ }
    fun finish() { /* TODO */ }
    fun discard() { /* TODO */ }
}
