package com.harsh.fittrack.plugins

import com.harsh.fittrack.data.seed.ExerciseSeeder
import com.harsh.fittrack.data.table.ExerciseEntriesTable
import com.harsh.fittrack.data.table.ExercisesTable
import com.harsh.fittrack.data.table.PersonalRecordsTable
import com.harsh.fittrack.data.table.SetEntriesTable
import com.harsh.fittrack.data.table.TemplateExercisesTable
import com.harsh.fittrack.data.table.UsersTable
import com.harsh.fittrack.data.table.WorkoutTemplatesTable
import com.harsh.fittrack.data.table.WorkoutsTable
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.server.application.Application
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

fun Application.configureDatabase() {
    val url      = System.getenv("DB_URL")      ?: "jdbc:postgresql://localhost:5432/fittrack"
    val user     = System.getenv("DB_USER")     ?: "fittrack"
    val password = System.getenv("DB_PASSWORD") ?: "secret"

    val dataSource = HikariDataSource(HikariConfig().apply {
        jdbcUrl         = url
        username        = user
        this.password   = password
        driverClassName = "org.postgresql.Driver"
        maximumPoolSize = 10
        isAutoCommit    = false
        transactionIsolation = "TRANSACTION_REPEATABLE_READ"
        validate()
    })

    Database.connect(dataSource)

    transaction {
        SchemaUtils.createMissingTablesAndColumns(
            UsersTable,
            ExercisesTable,
            WorkoutsTable,
            ExerciseEntriesTable,
            SetEntriesTable,
            PersonalRecordsTable,
            WorkoutTemplatesTable,
            TemplateExercisesTable,
        )
    }

    // Populate built-in exercise catalog on every startup (idempotent — skips if already seeded).
    ExerciseSeeder.seed()
    environment.log.info("Exercise catalog ready (${ExerciseSeeder::class.simpleName}).")
}
