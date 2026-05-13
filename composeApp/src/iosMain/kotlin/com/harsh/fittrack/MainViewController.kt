package com.harsh.fittrack

import androidx.compose.ui.window.ComposeUIViewController
import com.harsh.fittrack.di.initKoin

fun MainViewController() = ComposeUIViewController {
    App(showAppleSignIn = true)
}

fun initKoinIos() {
    initKoin()
}