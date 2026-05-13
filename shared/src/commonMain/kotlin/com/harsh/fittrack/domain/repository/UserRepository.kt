package com.harsh.fittrack.domain.repository

import com.harsh.fittrack.domain.model.Units
import com.harsh.fittrack.domain.model.User
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    fun observeUser(): Flow<User?>
    suspend fun setUnits(units: Units)
}
