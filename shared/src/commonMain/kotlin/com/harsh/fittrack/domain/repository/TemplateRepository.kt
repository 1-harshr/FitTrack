package com.harsh.fittrack.domain.repository

import com.harsh.fittrack.domain.model.TemplateExercise
import com.harsh.fittrack.domain.model.WorkoutTemplate
import kotlinx.coroutines.flow.Flow

interface TemplateRepository {
    fun observeTemplates(): Flow<List<WorkoutTemplate>>
    suspend fun createTemplate(name: String, exercises: List<TemplateExercise>): WorkoutTemplate
    suspend fun deleteTemplate(id: String)
    suspend fun syncFromServer()
}
