package com.harsh.fittrack.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToOneOrNull
import com.harsh.fittrack.data.local.mapper.toDomain
import com.harsh.fittrack.data.remote.FitTrackApi
import com.harsh.fittrack.db.FitTrackDatabase
import com.harsh.fittrack.domain.model.PersonalRecord
import com.harsh.fittrack.domain.repository.PersonalRecordRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class PersonalRecordRepositoryImpl(
    private val db: FitTrackDatabase,
    private val api: FitTrackApi,
) : PersonalRecordRepository {

    private val io = Dispatchers.Default
    private val prQ get() = db.personalRecordQueries

    override fun observeForExercise(exerciseId: String): Flow<PersonalRecord?> =
        prQ.selectByExercise(exerciseId).asFlow().mapToOneOrNull(io).map { it?.toDomain() }

    override suspend fun getForExercise(exerciseId: String): PersonalRecord? = withContext(io) {
        prQ.selectByExercise(exerciseId).executeAsOneOrNull()?.toDomain()
    }

    override suspend fun upsert(record: PersonalRecord) = withContext(io) {
        prQ.upsert(
            exerciseId = record.exerciseId,
            maxWeightKg = record.maxWeightKg,
            maxReps = record.maxReps.toLong(),
            achievedAt = record.achievedAt,
        )
    }

    override suspend fun saveAll(records: List<PersonalRecord>) = withContext(io) {
        db.transaction {
            records.forEach { record ->
                prQ.upsert(
                    exerciseId = record.exerciseId,
                    maxWeightKg = record.maxWeightKg,
                    maxReps = record.maxReps.toLong(),
                    achievedAt = record.achievedAt,
                )
            }
        }
    }

    override suspend fun syncFromServer(exerciseId: String) = withContext(io) {
        val apiPr = runCatching { api.getExercisePr(exerciseId) }.getOrNull() ?: return@withContext
        prQ.upsert(
            exerciseId = apiPr.exerciseId,
            maxWeightKg = apiPr.maxWeightKg,
            maxReps = apiPr.maxReps.toLong(),
            achievedAt = apiPr.achievedAt,
        )
    }
}
