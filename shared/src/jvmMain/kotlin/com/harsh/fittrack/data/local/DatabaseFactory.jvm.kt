package com.harsh.fittrack.data.local

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.harsh.fittrack.db.FitTrackDatabase

actual class DatabaseFactory {
    actual fun create(): FitTrackDatabase {
        val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        FitTrackDatabase.Schema.create(driver)
        return FitTrackDatabase(driver)
    }
}
