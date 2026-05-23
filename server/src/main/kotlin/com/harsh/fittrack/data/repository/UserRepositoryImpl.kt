package com.harsh.fittrack.data.repository

import com.harsh.fittrack.data.table.UsersTable
import com.harsh.fittrack.domain.model.User
import com.harsh.fittrack.domain.repository.UserRepository
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.update
import org.jetbrains.exposed.sql.upsert
import java.time.OffsetDateTime

class UserRepositoryImpl : UserRepository {

    override suspend fun findById(id: String): User? = query {
        UsersTable.selectAll().where { UsersTable.id eq id }.singleOrNull()?.toUser()
    }

    override suspend fun findByEmail(email: String): User? = query {
        UsersTable.selectAll().where { UsersTable.email eq email }.singleOrNull()?.toUser()
    }

    override suspend fun findPasswordHash(email: String): String? = query {
        UsersTable.selectAll().where { UsersTable.email eq email }
            .singleOrNull()?.get(UsersTable.passwordHash)
    }

    override suspend fun create(id: String, name: String, email: String, passwordHash: String): User = query {
        UsersTable.insert {
            it[UsersTable.id] = id
            it[UsersTable.name] = name
            it[UsersTable.email] = email
            it[UsersTable.passwordHash] = passwordHash
            it[UsersTable.createdAt] = OffsetDateTime.now()
            it[UsersTable.updatedAt] = OffsetDateTime.now()
        }
        UsersTable.selectAll().where { UsersTable.id eq id }.single().toUser()
    }

    override suspend fun upsert(id: String, name: String, email: String, photoUrl: String?): User = query {
        UsersTable.upsert(UsersTable.id) {
            it[UsersTable.id] = id
            it[UsersTable.name] = name
            it[UsersTable.email] = email
            it[UsersTable.photoUrl] = photoUrl
            it[UsersTable.createdAt] = OffsetDateTime.now()
            it[UsersTable.updatedAt] = OffsetDateTime.now()
        }
        UsersTable.selectAll().where { UsersTable.id eq id }.single().toUser()
    }

    override suspend fun updateUnits(id: String, units: String): User? = query {
        UsersTable.update({ UsersTable.id eq id }) {
            it[UsersTable.units] = units
            it[updatedAt] = OffsetDateTime.now()
        }
        UsersTable.selectAll().where { UsersTable.id eq id }.singleOrNull()?.toUser()
    }

    private fun ResultRow.toUser() = User(
        id = this[UsersTable.id],
        name = this[UsersTable.name],
        email = this[UsersTable.email],
        photoUrl = this[UsersTable.photoUrl],
        units = this[UsersTable.units],
    )
}

internal suspend fun <T> query(block: () -> T): T =
    newSuspendedTransaction(Dispatchers.IO) { block() }
