package com.harsh.fittrack

import androidx.compose.runtime.Composable
import com.harsh.fittrack.navigation.AppNav
import com.harsh.fittrack.ui.theme.FitTrackTheme
import org.koin.compose.KoinContext

@Composable
fun App(showAppleSignIn: Boolean = false) {
    KoinContext {
        FitTrackTheme {
            AppNav(showAppleSignIn = showAppleSignIn)
        }
    }
}
