package com.harsh.fittrack

import androidx.compose.ui.window.ComposeUIViewController
import com.harsh.fittrack.di.initKoin
import com.harsh.fittrack.di.iosModule

fun MainViewController() = ComposeUIViewController {
    App(showAppleSignIn = true)
}

/**
 * Called from iOSApp.swift before the first Compose frame.
 *
 * [apiBaseUrl] — optional override (e.g. from a build config or environment plist).
 * Leave empty in Swift to use the default (http://localhost:8080 for the simulator).
 */
fun startKoinIos(apiBaseUrl: String = "http://localhost:8080") {
    initKoin {
        modules(iosModule(apiBaseUrl = apiBaseUrl))
    }
}