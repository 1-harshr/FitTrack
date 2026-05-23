package com.harsh.fittrack.domain.usecase

import com.harsh.fittrack.domain.model.ExerciseEntry
import com.harsh.fittrack.domain.model.SetEntry
import com.harsh.fittrack.domain.repository.ExerciseWithSets
import com.harsh.fittrack.domain.usecase.record.ValidateWorkoutUseCase
import com.harsh.fittrack.domain.usecase.record.WorkoutValidationError
import com.harsh.fittrack.domain.usecase.record.WorkoutValidationResult
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class ValidateWorkoutUseCaseTest {

    private val useCase = ValidateWorkoutUseCase()

    private fun entry(name: String, id: String = name) = ExerciseEntry(
        id = id, workoutId = "w1", exerciseId = id,
        exerciseName = name, orderIndex = 0,
    )

    private fun set(
        entryId: String,
        reps: Int,
        weight: Double = 50.0,
        completed: Boolean = true,
    ) = SetEntry(
        id = "$entryId-$reps",
        exerciseEntryId = entryId,
        setNumber = 1,
        reps = reps,
        weight = weight,
        isCompleted = completed,
    )

    private fun validExercise(name: String = "Squat"): ExerciseWithSets {
        val e = entry(name)
        return ExerciseWithSets(entry = e, sets = listOf(set(e.id, reps = 5)))
    }

    @Test
    fun `returns Valid for a well-formed workout`() {
        val result = useCase(listOf(validExercise()))
        assertIs<WorkoutValidationResult.Valid>(result)
    }

    @Test
    fun `returns NoExercises error when list is empty`() {
        val result = useCase(emptyList()) as WorkoutValidationResult.Invalid
        assertEquals(1, result.errors.size)
        assertIs<WorkoutValidationError.NoExercises>(result.errors.first())
    }

    @Test
    fun `returns NoSets error when exercise has empty set list`() {
        val e = entry("Bench Press")
        val result = useCase(listOf(ExerciseWithSets(entry = e, sets = emptyList())))
        assertIs<WorkoutValidationResult.Invalid>(result)
        val errors = (result as WorkoutValidationResult.Invalid).errors
        assertEquals(1, errors.size)
        assertIs<WorkoutValidationError.NoSets>(errors.first())
        assertEquals("Bench Press", (errors.first() as WorkoutValidationError.NoSets).exerciseName)
    }

    @Test
    fun `returns EmptySet error when set has 0 reps`() {
        val e = entry("Deadlift")
        val badSet = set(e.id, reps = 0)
        val result = useCase(listOf(ExerciseWithSets(entry = e, sets = listOf(badSet))))
        assertIs<WorkoutValidationResult.Invalid>(result)
        val errors = (result as WorkoutValidationResult.Invalid).errors
        assertEquals(1, errors.size)
        assertIs<WorkoutValidationError.EmptySet>(errors.first())
    }

    @Test
    fun `accumulates multiple errors across exercises`() {
        val e1 = entry("Squat", "sq")
        val e2 = entry("Pull-Up", "pu")
        val exercises = listOf(
            ExerciseWithSets(entry = e1, sets = emptyList()),         // NoSets
            ExerciseWithSets(entry = e2, sets = listOf(set("pu", 0))), // EmptySet
        )
        val result = useCase(exercises) as WorkoutValidationResult.Invalid
        assertEquals(2, result.errors.size)
    }

    @Test
    fun `set with negative reps is also invalid`() {
        val e = entry("Curl")
        val result = useCase(listOf(ExerciseWithSets(entry = e, sets = listOf(set(e.id, -1)))))
        assertIs<WorkoutValidationResult.Invalid>(result)
    }
}
