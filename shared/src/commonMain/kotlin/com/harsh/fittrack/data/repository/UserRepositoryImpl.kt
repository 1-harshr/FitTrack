@file:OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)

package com.harsh.fittrack.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToOneOrNull
import com.harsh.fittrack.data.local.mapper.toDomain
import com.harsh.fittrack.data.remote.FitTrackApi
import com.harsh.fittrack.db.FitTrackDatabase
import com.harsh.fittrack.domain.model.Units
import com.harsh.fittrack.domain.model.User
import com.harsh.fittrack.domain.repository.AuthRepository
import com.harsh.fittrack.domain.repository.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.withContext

class UserRepositoryImpl(
    private val authRepository: AuthRepository,
    private val db: FitTrackDatabase,
    private val api: FitTrackApi,
) : UserRepository {

    private val io = Dispatchers.Default
    private val userQ get() = db.userQueries

    override fun observeUser(): Flow<User?> =
        authRepository.currentUser.flatMapLatest { authUser ->
            if (authUser == null) {
                flowOf(null)
            } else {
                userQ.selectById(authUser.id)
                    .asFlow()
                    .mapToOneOrNull(io)
                    .mapLatest { it?.toDomain() }
            }
        }

    override suspend fun setUnits(units: Units): Unit = withContext(io) {
        userQ.setUnitsForAll(units = units.name)
        try { api.patchMe(units.name) } catch (_: Throwable) { }
    }
}
