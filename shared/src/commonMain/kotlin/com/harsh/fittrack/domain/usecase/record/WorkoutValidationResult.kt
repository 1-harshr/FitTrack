package com.harsh.fittrack.domain.usecase.record

sealed interface WorkoutValidationResult {
    data object Valid : WorkoutValidationResult
    data class Invalid(val errors: List<WorkoutValidationError>) : WorkoutValidationResult
}

sealed interface WorkoutValidationError {
    data object NoExercises : WorkoutValidationError
    data class NoSets(val exerciseName: String) : WorkoutValidationError
    data class EmptySet(val exerciseName: String, val setNumber: Int) : WorkoutValidationError
}
