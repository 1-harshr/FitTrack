package com.harsh.fittrack

import androidx.compose.runtime.Composable
import com.harsh.fittrack.ui.theme.FitTrackTheme
import org.koin.compose.KoinContext

@Composable
fun App() {
    KoinContext {
        FitTrackTheme {
            // Navigation host goes here
        }
    }
}