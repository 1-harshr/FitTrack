package com.harsh.fittrack.domain.usecase.stats

import com.harsh.fittrack.domain.model.Workout
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus

/** Returns the number of completed workouts logged in the current calendar week (Mon–Sun). */
class CalculateWeeklyWorkoutsUseCase {
    operator fun invoke(workouts: List<Workout>, today: LocalDate): Int {
        val daysFromMonday = (today.dayOfWeek.ordinal - DayOfWeek.MONDAY.ordinal + 7) % 7
        val weekStart = today.minus(DatePeriod(days = daysFromMonday))
        return workouts.count { it.isCompleted && it.date >= weekStart && it.date <= today }
    }
}
