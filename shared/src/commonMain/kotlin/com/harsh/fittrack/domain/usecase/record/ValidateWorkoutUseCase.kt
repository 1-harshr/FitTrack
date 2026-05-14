package com.harsh.fittrack.domain.usecase.record

import com.harsh.fittrack.domain.repository.ExerciseWithSets

class ValidateWorkoutUseCase {

    operator fun invoke(
        exercises: List<ExerciseWithSets>,
    ): WorkoutValidationResult {
        val errors = mutableListOf<WorkoutValidationError>()

        if (exercises.isEmpty()) {
            return WorkoutValidationResult.Invalid(listOf(WorkoutValidationError.NoExercises))
        }

        for (ews in exercises) {
            val name = ews.entry.exerciseName

            if (ews.sets.isEmpty()) {
                errors += WorkoutValidationError.NoSets(name)
                continue
            }

            for (set in ews.sets) {
                if (set.reps <= 0) {
                    errors += WorkoutValidationError.EmptySet(name, set.setNumber)
                }
            }
        }

        return if (errors.isEmpty()) WorkoutValidationResult.Valid
        else WorkoutValidationResult.Invalid(errors)
    }
}
