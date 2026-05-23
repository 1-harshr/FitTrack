package com.harsh.fittrack.domain.usecase

import com.harsh.fittrack.domain.model.Workout
import com.harsh.fittrack.domain.usecase.stats.CalculateWeeklyWorkoutsUseCase
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals

class CalculateWeeklyWorkoutsUseCaseTest {

    private val useCase = CalculateWeeklyWorkoutsUseCase()

    private fun workout(dateStr: String, completed: Boolean = true) = Workout(
        id = dateStr,
        userId = "u1",
        title = "Test",
        date = LocalDate.parse(dateStr),
        startedAt = 0L,
        durationSeconds = 3600L,
        totalVolumeKg = 100.0,
        isCompleted = completed,
    )

    @Test
    fun `returns 0 when no workouts`() {
        assertEquals(0, useCase(emptyList(), LocalDate.parse("2026-05-22")))
    }

    @Test
    fun `counts workouts within Mon-Sun week`() {
        // 2026-05-18 = Monday, 2026-05-22 = Friday (today)
        val today = LocalDate.parse("2026-05-22")
        val workouts = listOf(
            workout("2026-05-18"),  // Monday — in week
            workout("2026-05-20"),  // Wednesday — in week
            workout("2026-05-22"),  // Friday — in week (today)
            workout("2026-05-17"),  // Sunday last week — NOT in week
        )
        assertEquals(3, useCase(workouts, today))
    }

    @Test
    fun `excludes incomplete workouts`() {
        val today = LocalDate.parse("2026-05-22")
        val workouts = listOf(
            workout("2026-05-18", completed = false),
            workout("2026-05-22", completed = true),
        )
        assertEquals(1, useCase(workouts, today))
    }

    @Test
    fun `returns 0 when all workouts are in past weeks`() {
        val today = LocalDate.parse("2026-05-22")
        val workouts = listOf(
            workout("2026-05-10"),
            workout("2026-05-11"),
        )
        assertEquals(0, useCase(workouts, today))
    }

    @Test
    fun `returns 7 when workout every day of week`() {
        val today = LocalDate.parse("2026-05-24") // Sunday
        val workouts = (18..24).map { day -> workout("2026-05-$day") }
        assertEquals(7, useCase(workouts, today))
    }
}
