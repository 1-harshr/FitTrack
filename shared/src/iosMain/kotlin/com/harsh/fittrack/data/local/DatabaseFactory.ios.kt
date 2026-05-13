package com.harsh.fittrack.data.local

import app.cash.sqldelight.driver.native.NativeSqliteDriver
import com.harsh.fittrack.db.FitTrackDatabase

actual class DatabaseFactory {
    actual fun create(): FitTrackDatabase =
        FitTrackDatabase(
            NativeSqliteDriver(
                schema = FitTrackDatabase.Schema,
                name = "fittrack.db",
            ),
        )
}
