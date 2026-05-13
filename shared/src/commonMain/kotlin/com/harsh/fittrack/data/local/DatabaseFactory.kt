package com.harsh.fittrack.data.local

import com.harsh.fittrack.db.FitTrackDatabase

/** Provides a platform-specific SqlDriver-backed FitTrackDatabase. */
expect class DatabaseFactory {
    fun create(): FitTrackDatabase
}
