package com.harsh.fittrack.domain.usecase.record

import com.harsh.fittrack.domain.repository.ExerciseWithSets

class ValidateWorkoutUseCase {

    operator fun invoke(
        exercises: List<ExerciseWithSets>,
        exerciseNames: Map<String, String>,
    ): WorkoutValidationResult {
        val errors = mutableListOf<WorkoutValidationError>()

        if (exercises.isEmpty()) {
            return WorkoutValidationResult.Invalid(listOf(WorkoutValidationError.NoExercises))
        }

        for (ews in exercises) {
            val name = exerciseNames[ews.entry.exerciseId] ?: ews.entry.exerciseId

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
