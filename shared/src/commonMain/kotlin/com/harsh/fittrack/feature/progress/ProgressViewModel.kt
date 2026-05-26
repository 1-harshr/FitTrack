package com.harsh.fittrack.feature.progress

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.harsh.fittrack.data.remote.FitTrackApi
import com.harsh.fittrack.domain.model.ExerciseProgressionResponse
import com.harsh.fittrack.domain.model.MuscleFrequencyPoint
import com.harsh.fittrack.domain.model.WeeklyVolumePoint
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ProgressViewModel(
    private val api: FitTrackApi,
) : ViewModel() {

    private val _state = MutableStateFlow(ProgressState())
    val state: StateFlow<ProgressState> = _state.asStateFlow()

    init {
        loadOverview()
    }

    private fun loadOverview() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            val volumeDeferred = async { runCatching { api.getWeeklyVolume() }.getOrNull() }
            val muscleDeferred = async { runCatching { api.getMuscleFrequency() }.getOrNull() }
            val volumeResp = volumeDeferred.await()
            val muscleResp = muscleDeferred.await()
            _state.value = _state.value.copy(
                weeklyVolume = volumeResp?.weeks?.map { WeeklyVolumePoint(it.weekLabel, it.volumeKg) } ?: emptyList(),
                muscleFrequency = muscleResp?.points?.map { MuscleFrequencyPoint(it.muscle, it.sessionCount) } ?: emptyList(),
                isLoading = false,
            )
        }
    }

    fun selectExercise(exerciseId: String) {
        if (exerciseId == _state.value.selectedExerciseId) return
        _state.value = _state.value.copy(selectedExerciseId = exerciseId, progressionLoading = true)
        viewModelScope.launch {
            val resp = runCatching { api.getExerciseProgression(exerciseId) }.getOrNull()
            _state.value = _state.value.copy(
                exerciseProgression = resp?.let {
                    ExerciseProgressionResponse(
                        exerciseName = it.exerciseName,
                        points = it.points.map { p ->
                            com.harsh.fittrack.domain.model.ExerciseProgressionPoint(p.date, p.maxWeightKg)
                        },
                    )
                },
                progressionLoading = false,
            )
        }
    }
}
