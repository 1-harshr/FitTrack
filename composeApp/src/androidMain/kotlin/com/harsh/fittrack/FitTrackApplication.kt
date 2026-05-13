package com.harsh.fittrack

import android.app.Application
import com.harsh.fittrack.di.initKoin
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger

class FitTrackApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        initKoin {
            androidContext(this@FitTrackApplication)
            androidLogger()
        }
    }
}
