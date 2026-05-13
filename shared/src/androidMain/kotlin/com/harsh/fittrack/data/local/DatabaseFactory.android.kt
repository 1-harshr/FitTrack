package com.harsh.fittrack.data.local

import android.content.Context
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import com.harsh.fittrack.db.FitTrackDatabase

actual class DatabaseFactory(private val context: Context) {
    actual fun create(): FitTrackDatabase =
        FitTrackDatabase(
            AndroidSqliteDriver(
                schema = FitTrackDatabase.Schema,
                context = context,
                name = "fittrack.db",
            ),
        )
}
