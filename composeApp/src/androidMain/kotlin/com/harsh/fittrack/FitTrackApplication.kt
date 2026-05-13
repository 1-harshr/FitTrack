package com.harsh.fittrack

import android.app.Application
import com.harsh.fittrack.di.androidModule
import com.harsh.fittrack.di.initKoin
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger

class FitTrackApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        initKoin {
            androidContext(this@FitTrackApplication)
            androidLogger()
            modules(
                androidModule(
                    context = this@FitTrackApplication,
                    webClientId = getString(R.string.firebase_web_client_id),
                ),
            )
        }
    }
}
