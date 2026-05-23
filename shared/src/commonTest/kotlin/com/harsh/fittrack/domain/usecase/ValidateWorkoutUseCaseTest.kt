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

    @Test
    fun `reps of 1 is the minimum valid value`() {
        val e = entry("Plank")
        val result = useCase(listOf(ExerciseWithSets(entry = e, sets = listOf(set(e.id, 1)))))
        assertIs<WorkoutValidationResult.Valid>(result)
    }

    @Test
    fun `multiple sets in one exercise - only invalid set produces error`() {
        val e = entry("Press")
        val sets = listOf(
            set(e.id, reps = 10).copy(id = "s1", setNumber = 1),
            set(e.id, reps = 0).copy(id = "s2", setNumber = 2),  // invalid
            set(e.id, reps = 8).copy(id = "s3", setNumber = 3),
        )
        val result = useCase(listOf(ExerciseWithSets(entry = e, sets = sets))) as WorkoutValidationResult.Invalid
        assertEquals(1, result.errors.size)
        val error = result.errors.first() as WorkoutValidationError.EmptySet
        assertEquals(2, error.setNumber)
    }

    @Test
    fun `all sets in exercise invalid produces error for each set`() {
        val e = entry("Row")
        val sets = listOf(
            set(e.id, reps = 0).copy(id = "s1", setNumber = 1),
            set(e.id, reps = 0).copy(id = "s2", setNumber = 2),
        )
        val result = useCase(listOf(ExerciseWithSets(entry = e, sets = sets))) as WorkoutValidationResult.Invalid
        assertEquals(2, result.errors.size)
    }

    @Test
    fun `multiple exercises with mixed validity accumulates all errors`() {
        val e1 = entry("Squat", "sq")
        val e2 = entry("Press", "pr")
        val e3 = entry("Pull-up", "pu")
        val exercises = listOf(
            ExerciseWithSets(entry = e1, sets = listOf(set("sq", 5))),   // valid
            ExerciseWithSets(entry = e2, sets = emptyList()),             // NoSets
            ExerciseWithSets(entry = e3, sets = listOf(set("pu", 0))),   // EmptySet
        )
        val result = useCase(exercises) as WorkoutValidationResult.Invalid
        assertEquals(2, result.errors.size)
        assertIs<WorkoutValidationError.NoSets>(result.errors[0])
        assertIs<WorkoutValidationError.EmptySet>(result.errors[1])
    }

    @Test
    fun `valid workout with many exercises and sets returns Valid`() {
        val exercises = (1..5).map { i ->
            val e = entry("Exercise $i", "e$i")
            val sets = (1..4).map { j -> set(e.id, reps = 10).copy(id = "s$i-$j", setNumber = j) }
            ExerciseWithSets(entry = e, sets = sets)
        }
        assertIs<WorkoutValidationResult.Valid>(useCase(exercises))
    }
}
