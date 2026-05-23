package com.harsh.fittrack.domain.repository

import com.harsh.fittrack.domain.model.Exercise
import com.harsh.fittrack.domain.model.User
import com.harsh.fittrack.domain.model.Workout

interface UserRepository {
    suspend fun findById(id: String): User?
    suspend fun findByEmail(email: String): User?
    suspend fun findPasswordHash(email: String): String?
    suspend fun create(id: String, name: String, email: String, passwordHash: String): User
    suspend fun upsert(id: String, name: String, email: String, photoUrl: String?): User
    suspend fun updateUnits(id: String, units: String): User?
}

interface ExerciseRepository {
    suspend fun findSinceVersion(sinceVersion: Int): List<Exercise>
    suspend fun latestVersion(): Int
}

interface WorkoutRepository {
    /** Cursor encodes startedAt epoch ms; null returns the first page. */
    suspend fun listForUser(userId: String, cursor: Long?, limit: Int): List<Workout>
    suspend fun findById(id: String, userId: String): Workout?
    suspend fun save(workout: Workout): Workout
    suspend fun updateTitle(id: String, userId: String, title: String): Workout?
    suspend fun softDelete(id: String, userId: String): Boolean
    suspend fun latestStartedAt(userId: String): Long?
}
