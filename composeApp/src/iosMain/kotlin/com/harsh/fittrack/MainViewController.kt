package com.harsh.fittrack

import androidx.compose.ui.window.ComposeUIViewController
import com.harsh.fittrack.di.initKoin
import com.harsh.fittrack.di.iosModule

fun MainViewController() = ComposeUIViewController {
    App(showAppleSignIn = true)
}

fun initKoinIos() {
    initKoin {
        modules(iosModule())
    }
}