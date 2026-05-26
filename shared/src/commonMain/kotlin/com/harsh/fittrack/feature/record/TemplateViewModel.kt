package com.harsh.fittrack.feature.record

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.harsh.fittrack.domain.model.TemplateExercise
import com.harsh.fittrack.domain.model.WorkoutTemplate
import com.harsh.fittrack.domain.repository.TemplateRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TemplateViewModel(
    private val templateRepository: TemplateRepository,
) : ViewModel() {

    val templates: StateFlow<List<WorkoutTemplate>> = templateRepository
        .observeTemplates()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun createTemplate(name: String, exercises: List<TemplateExercise>) {
        viewModelScope.launch { templateRepository.createTemplate(name, exercises) }
    }

    fun deleteTemplate(id: String) {
        viewModelScope.launch { templateRepository.deleteTemplate(id) }
    }
}
