package com.harsh.fittrack.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.harsh.fittrack.data.remote.ApiCreateTemplateRequest
import com.harsh.fittrack.data.remote.ApiTemplateExercise
import com.harsh.fittrack.data.remote.FitTrackApi
import com.harsh.fittrack.db.FitTrackDatabase
import com.harsh.fittrack.domain.model.TemplateExercise
import com.harsh.fittrack.domain.model.WorkoutTemplate
import com.harsh.fittrack.domain.repository.TemplateRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.random.Random

class TemplateRepositoryImpl(
    private val db: FitTrackDatabase,
    private val api: FitTrackApi,
) : TemplateRepository {

    private val io = Dispatchers.Default
    private val tQ get() = db.workoutTemplateQueries

    init {
        CoroutineScope(io).launch { syncFromServer() }
    }

    override fun observeTemplates(): Flow<List<WorkoutTemplate>> =
        tQ.selectAllTemplates().asFlow().mapToList(io).map { rows ->
            rows.map { row ->
                val exercises = tQ.selectExercisesForTemplate(row.id).executeAsList().map { e ->
                    TemplateExercise(
                        exerciseId = e.exerciseId,
                        exerciseName = e.exerciseName,
                        orderIndex = e.orderIndex.toInt(),
                    )
                }
                WorkoutTemplate(id = row.id, name = row.name, exercises = exercises, createdAt = row.createdAt)
            }
        }

    override suspend fun createTemplate(name: String, exercises: List<TemplateExercise>): WorkoutTemplate =
        withContext(io) {
            val id = newId()
            val now = System.currentTimeMillis()
            db.transaction {
                tQ.insertTemplate(id = id, name = name, createdAt = now)
                exercises.forEachIndexed { index, ex ->
                    tQ.insertTemplateExercise(
                        id = newId(),
                        templateId = id,
                        exerciseId = ex.exerciseId,
                        exerciseName = ex.exerciseName,
                        orderIndex = index.toLong(),
                    )
                }
            }
            val template = WorkoutTemplate(id = id, name = name, exercises = exercises, createdAt = now)
            // Best-effort sync to server
            CoroutineScope(io).launch {
                runCatching {
                    api.postTemplate(
                        ApiCreateTemplateRequest(
                            name = name,
                            exercises = exercises.mapIndexed { i, e ->
                                ApiTemplateExercise(
                                    exerciseId = e.exerciseId,
                                    exerciseName = e.exerciseName,
                                    orderIndex = i,
                                )
                            },
                        )
                    )
                }
            }
            template
        }

    override suspend fun deleteTemplate(id: String) = withContext(io) {
        tQ.deleteTemplateExercises(id)
        tQ.deleteTemplate(id)
        CoroutineScope(io).launch { runCatching { api.deleteTemplate(id) } }
        Unit
    }

    override suspend fun syncFromServer() = withContext(io) {
        val remote = runCatching { api.getTemplates() }.getOrNull() ?: return@withContext
        db.transaction {
            remote.forEach { t ->
                tQ.insertTemplate(id = t.id, name = t.name, createdAt = t.createdAt)
                tQ.deleteTemplateExercises(t.id)
                t.exercises.forEach { e ->
                    tQ.insertTemplateExercise(
                        id = newId(),
                        templateId = t.id,
                        exerciseId = e.exerciseId,
                        exerciseName = e.exerciseName,
                        orderIndex = e.orderIndex.toLong(),
                    )
                }
            }
        }
    }

    private fun newId(): String = buildString {
        repeat(2) { append(Random.nextLong().toString(16)) }
    }.take(16)
}
