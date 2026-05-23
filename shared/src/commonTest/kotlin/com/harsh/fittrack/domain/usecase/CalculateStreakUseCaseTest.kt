package com.harsh.fittrack.domain.usecase

import com.harsh.fittrack.domain.model.Workout
import com.harsh.fittrack.domain.usecase.stats.CalculateStreakUseCase
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals

class CalculateStreakUseCaseTest {

    private val useCase = CalculateStreakUseCase()

    private fun workout(dateStr: String) = Workout(
        id = dateStr,
        userId = "u1",
        title = "Test",
        date = LocalDate.parse(dateStr),
        startedAt = 0L,
        durationSeconds = 3600L,
        totalVolumeKg = 100.0,
        isCompleted = true,
    )

    private fun incompleteWorkout(dateStr: String) = workout(dateStr).copy(isCompleted = false)

    @Test
    fun `returns 0 when no workouts`() {
        val result = useCase(emptyList(), LocalDate.parse("2026-05-22"))
        assertEquals(0, result)
    }

    @Test
    fun `returns 1 when only today`() {
        val today = LocalDate.parse("2026-05-22")
        val result = useCase(listOf(workout("2026-05-22")), today)
        assertEquals(1, result)
    }

    @Test
    fun `returns correct streak for consecutive days ending today`() {
        val today = LocalDate.parse("2026-05-22")
        val workouts = listOf(
            workout("2026-05-20"),
            workout("2026-05-21"),
            workout("2026-05-22"),
        )
        assertEquals(3, useCase(workouts, today))
    }

    @Test
    fun `counts streak ending yesterday when today has no workout`() {
        val today = LocalDate.parse("2026-05-22")
        val workouts = listOf(
            workout("2026-05-20"),
            workout("2026-05-21"),
        )
        // Streak is still alive — the day isn't over yet
        assertEquals(2, useCase(workouts, today))
    }

    @Test
    fun `streak breaks on gap`() {
        val today = LocalDate.parse("2026-05-22")
        val workouts = listOf(
            workout("2026-05-18"),  // old — gap here
            workout("2026-05-21"),
            workout("2026-05-22"),
        )
        assertEquals(2, useCase(workouts, today))
    }

    @Test
    fun `ignores incomplete workouts`() {
        val today = LocalDate.parse("2026-05-22")
        val workouts = listOf(
            incompleteWorkout("2026-05-20"),
            incompleteWorkout("2026-05-21"),
            incompleteWorkout("2026-05-22"),
        )
        assertEquals(0, useCase(workouts, today))
    }

    @Test
    fun `multiple workouts same day count as one day in streak`() {
        val today = LocalDate.parse("2026-05-22")
        val workouts = listOf(
            workout("2026-05-21"),
            workout("2026-05-21"),  // duplicate day
            workout("2026-05-22"),
        )
        assertEquals(2, useCase(workouts, today))
    }
}
