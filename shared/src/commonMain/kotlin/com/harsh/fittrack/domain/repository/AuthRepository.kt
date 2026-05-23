package com.harsh.fittrack.domain.repository

import com.harsh.fittrack.domain.model.User
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    val currentUser: Flow<User?>

    suspend fun isSignedIn(): Boolean

    suspend fun login(email: String, password: String): Result<User>
    suspend fun register(name: String, email: String, password: String): Result<User>
    suspend fun signOut()
}
