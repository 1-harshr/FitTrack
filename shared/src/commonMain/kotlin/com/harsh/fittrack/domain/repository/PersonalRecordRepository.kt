package com.harsh.fittrack.domain.repository

import com.harsh.fittrack.domain.model.PersonalRecord
import kotlinx.coroutines.flow.Flow

interface PersonalRecordRepository {
    fun observeForExercise(exerciseId: String): Flow<PersonalRecord?>
    suspend fun getForExercise(exerciseId: String): PersonalRecord?
    suspend fun saveAll(records: List<PersonalRecord>)
    suspend fun upsert(record: PersonalRecord)
    suspend fun syncFromServer(exerciseId: String)
}
