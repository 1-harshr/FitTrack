package com.harsh.fittrack.core.time

import kotlin.time.Clock as KxClock
import kotlin.time.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/**
 * Thin clock abstraction so streak/greeting logic is testable.
 * Default impl uses the system clock and the device's timezone.
 */
interface Clock {
    fun now(): Instant
    fun timeZone(): TimeZone
    fun today(): LocalDate = now().toLocalDateTime(timeZone()).date
    fun nowLocalDateTime(): LocalDateTime = now().toLocalDateTime(timeZone())
}

class SystemClock : Clock {
    override fun now(): Instant = KxClock.System.now()
    override fun timeZone(): TimeZone = TimeZone.currentSystemDefault()
}
