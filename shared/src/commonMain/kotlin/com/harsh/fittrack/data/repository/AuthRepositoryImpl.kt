package com.harsh.fittrack.data.repository

import com.harsh.fittrack.data.remote.FitTrackApi
import com.harsh.fittrack.data.remote.TokenStore
import com.harsh.fittrack.db.FitTrackDatabase
import com.harsh.fittrack.domain.model.Units
import com.harsh.fittrack.domain.model.User
import com.harsh.fittrack.domain.repository.AuthRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext

class AuthRepositoryImpl(
    private val db: FitTrackDatabase,
    private val api: FitTrackApi,
    private val tokenStore: TokenStore,
) : AuthRepository {

    private val io = Dispatchers.Default

    private val _currentUser = MutableStateFlow<User?>(null)
    override val currentUser: Flow<User?> = _currentUser.asStateFlow()

    init {
        val stored = db.authTokenQueries.selectOne().executeAsOneOrNull()
        if (stored != null) {
            tokenStore.token = stored.token
            val userRow = db.userQueries.selectById(stored.userId).executeAsOneOrNull()
            if (userRow != null) {
                _currentUser.value = User(
                    id = userRow.id,
                    name = userRow.name,
                    email = userRow.email,
                    photoUrl = userRow.photoUrl,
                    units = Units.valueOf(userRow.units),
                )
            }
        }
    }

    override suspend fun isSignedIn(): Boolean = tokenStore.token != null

    override suspend fun login(email: String, password: String): Result<User> = withContext(io) {
        runCatching {
            val response = api.login(email, password)
                ?: error("Login failed — check your email and password.")
            storeSession(response.token, response.user.id, response.user.name, response.user.email, response.user.units)
        }
    }

    override suspend fun register(name: String, email: String, password: String): Result<User> = withContext(io) {
        runCatching {
            val response = api.register(name, email, password)
                ?: error("Registration failed — the email may already be in use.")
            storeSession(response.token, response.user.id, response.user.name, response.user.email, response.user.units)
        }
    }

    override suspend fun signOut() = withContext(io) {
        tokenStore.token = null
        db.authTokenQueries.deleteAll()
        db.userQueries.deleteAll()
        _currentUser.value = null
    }

    private fun storeSession(token: String, id: String, name: String, email: String, units: String): User {
        tokenStore.token = token
        val safeUnits = runCatching { Units.valueOf(units) }.getOrElse { Units.KG }
        db.authTokenQueries.upsert(token = token, userId = id)
        db.userQueries.upsert(id = id, name = name, email = email, photoUrl = null, units = safeUnits.name)
        val user = User(id = id, name = name, email = email, photoUrl = null, units = safeUnits)
        _currentUser.value = user
        return user
    }
}
