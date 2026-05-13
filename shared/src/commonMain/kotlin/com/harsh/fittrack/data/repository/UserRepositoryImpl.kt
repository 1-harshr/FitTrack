package com.harsh.fittrack.data.repository

import com.harsh.fittrack.domain.model.Units
import com.harsh.fittrack.domain.model.User
import com.harsh.fittrack.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class UserRepositoryImpl : UserRepository {
    // TODO: wire to AuthRepository + a local settings store for units.
    override fun observeUser(): Flow<User?> = flowOf(null)
    override suspend fun setUnits(units: Units) { /* TODO */ }
}
