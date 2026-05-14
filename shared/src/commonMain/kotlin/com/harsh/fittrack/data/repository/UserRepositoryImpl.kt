@file:OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)

package com.harsh.fittrack.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToOneOrNull
import com.harsh.fittrack.data.local.mapper.toDomain
import com.harsh.fittrack.db.FitTrackDatabase
import com.harsh.fittrack.domain.model.Units
import com.harsh.fittrack.domain.model.User
import com.harsh.fittrack.domain.repository.UserRepository
import dev.gitlive.firebase.auth.FirebaseAuth
import dev.gitlive.firebase.auth.FirebaseUser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.withContext

class UserRepositoryImpl(
    private val firebaseAuth: FirebaseAuth,
    private val db: FitTrackDatabase,
) : UserRepository {

    private val io = Dispatchers.Default
    private val userQ get() = db.userQueries

    override fun observeUser(): Flow<User?> =
        firebaseAuth.authStateChanged
            .onEach { firebaseUser ->
                if (firebaseUser != null) upsertFromFirebase(firebaseUser)
            }
            .flatMapLatest { firebaseUser ->
                if (firebaseUser == null) {
                    flowOf(null)
                } else {
                    userQ.selectById(firebaseUser.uid)
                        .asFlow()
                        .mapToOneOrNull(io)
                        .mapLatest { it?.toDomain() }
                }
            }

    override suspend fun setUnits(units: Units) = withContext(io) {
        val uid = firebaseAuth.currentUser?.uid ?: return@withContext
        userQ.setUnits(units = units.name, id = uid)
    }

    private suspend fun upsertFromFirebase(user: FirebaseUser) = withContext(io) {
        val existing = userQ.selectById(user.uid).executeAsOneOrNull()
        userQ.upsert(
            id = user.uid,
            name = user.displayName.orEmpty(),
            email = user.email.orEmpty(),
            photoUrl = user.photoURL,
            units = existing?.units ?: Units.KG.name,
        )
    }
}
