package com.harsh.fittrack.domain.usecase.stats

import com.harsh.fittrack.domain.model.Workout
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus

/**
 * Returns the current workout streak in days.
 *
 * A streak is the longest unbroken chain of calendar days ending on or before [today]
 * where the user logged at least one completed workout. A workout logged today keeps the
 * streak alive; if today has no workout but yesterday does, the streak is still counted
 * (the day isn't over yet).
 */
class CalculateStreakUseCase {
    operator fun invoke(workouts: List<Workout>, today: LocalDate): Int {
        val dates = workouts.filter { it.isCompleted }.map { it.date }.toHashSet()
        var streak = 0
        var check = today
        if (!dates.contains(check)) check = check.minus(DatePeriod(days = 1))
        while (dates.contains(check)) {
            streak++
            check = check.minus(DatePeriod(days = 1))
        }
        return streak
    }
}
