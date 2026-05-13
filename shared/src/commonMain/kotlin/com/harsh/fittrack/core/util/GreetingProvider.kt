package com.harsh.fittrack.core.util

import com.harsh.fittrack.core.time.Clock

/** "Good morning / afternoon / evening" based on the current local hour. */
class GreetingProvider(private val clock: Clock) {
    fun greeting(): String {
        val hour = clock.nowLocalDateTime().hour
        return when (hour) {
            in 5..11 -> "Good morning"
            in 12..17 -> "Good afternoon"
            else -> "Good evening"
        }
    }
}
