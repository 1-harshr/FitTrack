package com.harsh.fittrack.feature.record

import com.harsh.fittrack.fakes.FakeClock
import com.harsh.fittrack.fakes.FakeExerciseRepository
import com.harsh.fittrack.fakes.FakeWorkoutRepository
import com.harsh.fittrack.fakes.testExercise
import com.harsh.fittrack.fakes.testExerciseEntry
import com.harsh.fittrack.fakes.testExerciseWithSets
import com.harsh.fittrack.fakes.testSetEntry
import com.harsh.fittrack.fakes.testWorkout
import com.harsh.fittrack.domain.model.MuscleGroup
import com.harsh.fittrack.domain.model.SetEntry
import com.harsh.fittrack.domain.repository.ExerciseWithSets
import com.harsh.fittrack.domain.repository.WorkoutWithDetails
import com.harsh.fittrack.domain.usecase.record.ValidateWorkoutUseCase
import com.harsh.fittrack.domain.usecase.record.WorkoutValidationError
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class RecordViewModelTest {

    private val dispatcher = UnconfinedTestDispatcher()

    @BeforeTest
    fun setUp() = Dispatchers.setMain(dispatcher)

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    private fun buildVm(
        workoutRepo: FakeWorkoutRepository = FakeWorkoutRepository(),
        exerciseRepo: FakeExerciseRepository = FakeExerciseRepository(),
        clock: FakeClock = FakeClock(hour = 9),
    ) = RecordViewModel(
        workoutRepository = workoutRepo,
        exerciseRepository = exerciseRepo,
        validateWorkout = ValidateWorkoutUseCase(),
        clock = clock,
    )

    // ── suggestedTitle ─────────────────────────────────────────────────────

    @Test
    fun `suggestedTitle is Morning Workout at 9am`() = runTest {
        val vm = buildVm(clock = FakeClock(hour = 9))
        assertTrue(vm.suggestedTitle.startsWith("Morning Workout"))
    }

    @Test
    fun `suggestedTitle is Afternoon Workout at 13`() = runTest {
        val vm = buildVm(clock = FakeClock(hour = 13))
        assertTrue(vm.suggestedTitle.startsWith("Afternoon Workout"))
    }

    @Test
    fun `suggestedTitle is Evening Workout at 18`() = runTest {
        val vm = buildVm(clock = FakeClock(hour = 18))
        assertTrue(vm.suggestedTitle.startsWith("Evening Workout"))
    }

    @Test
    fun `suggestedTitle is Night Workout at 23`() = runTest {
        val vm = buildVm(clock = FakeClock(hour = 23))
        assertTrue(vm.suggestedTitle.startsWith("Night Workout"))
    }

    // ── startOrResumeWorkout ───────────────────────────────────────────────

    @Test
    fun `startOrResumeWorkout restores active workout from repository`() = runTest {
        val entry = testExerciseEntry()
        val set = testSetEntry()
        val active = WorkoutWithDetails(
            workout = testWorkout("w-active"),
            exercises = listOf(testExerciseWithSets(entry, listOf(set))),
        )
        val workoutRepo = FakeWorkoutRepository().also { it.activeWorkout = active }

        val vm = buildVm(workoutRepo = workoutRepo)
        vm.startOrResumeWorkout("user-1")
        advanceUntilIdle()

        val state = vm.state.value
        assertEquals("w-active", state.workoutId)
        assertTrue(state.hasStarted)
        assertEquals(1, state.exercises.size)
    }

    @Test
    fun `startOrResumeWorkout does nothing when no active workout`() = runTest {
        val workoutRepo = FakeWorkoutRepository().also { it.activeWorkout = null }
        val vm = buildVm(workoutRepo = workoutRepo)
        vm.startOrResumeWorkout("user-1")
        advanceUntilIdle()

        val state = vm.state.value
        assertNull(state.workoutId)
        assertFalse(state.hasStarted)
    }

    // ── startWorkout ───────────────────────────────────────────────────────

    @Test
    fun `startWorkout creates workout and transitions hasStarted`() = runTest {
        val workoutRepo = FakeWorkoutRepository().also { it.createdWorkoutId = "w-new" }
        val vm = buildVm(workoutRepo = workoutRepo)

        vm.startOrResumeWorkout("user-1") // sets userId without active workout
        vm.startWorkout()
        advanceUntilIdle()

        val state = vm.state.value
        assertEquals("w-new", state.workoutId)
        assertTrue(state.hasStarted)
    }

    @Test
    fun `startWorkout uses blank title fallback from suggestedTitle`() = runTest {
        val workoutRepo = FakeWorkoutRepository()
        val vm = buildVm(workoutRepo = workoutRepo, clock = FakeClock(hour = 9))

        vm.startOrResumeWorkout("user-1")
        // leave title blank
        vm.startWorkout()
        advanceUntilIdle()

        assertTrue(vm.state.value.title.isNotBlank())
    }

    @Test
    fun `startWorkout without userId is a no-op`() = runTest {
        val workoutRepo = FakeWorkoutRepository()
        val vm = buildVm(workoutRepo = workoutRepo)

        // No startOrResumeWorkout call — userId not set
        vm.startWorkout()
        advanceUntilIdle()

        assertNull(vm.state.value.workoutId)
    }

    // ── renameTitle ────────────────────────────────────────────────────────

    @Test
    fun `renameTitle updates title in state`() = runTest {
        val vm = buildVm()
        vm.renameTitle("My New Title")
        assertEquals("My New Title", vm.state.value.title)
    }

    @Test
    fun `renameTitle calls repository when workoutId is set`() = runTest {
        val workoutRepo = FakeWorkoutRepository().also { it.createdWorkoutId = "w-1" }
        val vm = buildVm(workoutRepo = workoutRepo)
        vm.startOrResumeWorkout("user-1")
        vm.startWorkout()
        advanceUntilIdle()

        vm.renameTitle("Leg Day")
        advanceUntilIdle()

        assertEquals("w-1", workoutRepo.renamedWorkoutId)
        assertEquals("Leg Day", workoutRepo.renamedTitle)
    }

    // ── addExercise ────────────────────────────────────────────────────────

    @Test
    fun `addExercise appends exercise with initial set to state`() = runTest {
        val exerciseRepo = FakeExerciseRepository(
            listOf(testExercise("ex-1", "Bench Press")),
        )
        val workoutRepo = FakeWorkoutRepository().also {
            it.createdWorkoutId = "w-1"
            it.addedExerciseId = "entry-1"
            it.addedSetId = "set-1"
        }
        val vm = buildVm(workoutRepo = workoutRepo, exerciseRepo = exerciseRepo)
        vm.startOrResumeWorkout("user-1")
        vm.startWorkout()
        advanceUntilIdle()

        vm.addExercise("ex-1")
        advanceUntilIdle()

        val exercises = vm.state.value.exercises
        assertEquals(1, exercises.size)
        assertEquals("Bench Press", exercises.first().entry.exerciseName)
        assertEquals(1, exercises.first().sets.size)
        assertEquals(0, exercises.first().sets.first().reps)
    }

    @Test
    fun `addExercise works without a persisted workout (local mode)`() = runTest {
        val exerciseRepo = FakeExerciseRepository(listOf(testExercise("ex-1", "Squat")))
        val vm = buildVm(exerciseRepo = exerciseRepo)
        // No startWorkout — workoutId is null

        vm.addExercise("ex-1")
        advanceUntilIdle()

        assertEquals(1, vm.state.value.exercises.size)
        assertEquals("Squat", vm.state.value.exercises.first().entry.exerciseName)
    }

    @Test
    fun `addExercise uses exerciseId as name when exercise not found`() = runTest {
        val exerciseRepo = FakeExerciseRepository(emptyList())
        val vm = buildVm(exerciseRepo = exerciseRepo)

        vm.addExercise("unknown-id")
        advanceUntilIdle()

        assertEquals("unknown-id", vm.state.value.exercises.first().entry.exerciseName)
    }

    // ── addSet ─────────────────────────────────────────────────────────────

    @Test
    fun `addSet appends set with incremented setNumber`() = runTest {
        val vm = buildVm()
        vm.addExercise("ex-1") // local mode
        advanceUntilIdle()

        val entryId = vm.state.value.exercises.first().entry.id
        vm.addSet(entryId)

        val sets = vm.state.value.exercises.first().sets
        assertEquals(2, sets.size)
        assertEquals(2, sets.last().setNumber)
    }

    @Test
    fun `addSet copies weight and reps from previous set`() = runTest {
        val vm = buildVm()
        vm.addExercise("ex-1")
        advanceUntilIdle()

        val entryId = vm.state.value.exercises.first().entry.id
        // Update the first set so it has known values
        val firstSet = vm.state.value.exercises.first().sets.first()
        vm.updateSet(firstSet.copy(reps = 8, weight = 100.0))

        vm.addSet(entryId)
        val newSet = vm.state.value.exercises.first().sets.last()
        assertEquals(8, newSet.reps)
        assertEquals(100.0, newSet.weight)
    }

    @Test
    fun `addSet on unknown entryId leaves state unchanged`() = runTest {
        val vm = buildVm()
        vm.addExercise("ex-1")
        advanceUntilIdle()
        val countBefore = vm.state.value.exercises.first().sets.size

        vm.addSet("non-existent-entry")

        assertEquals(countBefore, vm.state.value.exercises.first().sets.size)
    }

    // ── updateSet ──────────────────────────────────────────────────────────

    @Test
    fun `updateSet replaces correct set in state`() = runTest {
        val vm = buildVm()
        vm.addExercise("ex-1")
        advanceUntilIdle()

        val set = vm.state.value.exercises.first().sets.first()
        val updated = set.copy(reps = 12, weight = 75.0)
        vm.updateSet(updated)

        val result = vm.state.value.exercises.first().sets.first()
        assertEquals(12, result.reps)
        assertEquals(75.0, result.weight)
    }

    @Test
    fun `updateSet calls repository`() = runTest {
        val workoutRepo = FakeWorkoutRepository().also { it.createdWorkoutId = "w-1" }
        val vm = buildVm(workoutRepo = workoutRepo)
        vm.startOrResumeWorkout("user-1")
        vm.startWorkout()
        advanceUntilIdle()
        vm.addExercise("ex-1")
        advanceUntilIdle()

        val set = vm.state.value.exercises.first().sets.first()
        val updated = set.copy(reps = 5, weight = 50.0)
        vm.updateSet(updated)
        advanceUntilIdle()

        assertTrue(workoutRepo.updatedSets.isNotEmpty())
        assertEquals(updated, workoutRepo.updatedSets.last())
    }

    // ── finish ─────────────────────────────────────────────────────────────

    @Test
    fun `finish returns false and sets NoExercises error when no exercises`() = runTest {
        val vm = buildVm()
        val result = vm.finish(3600L)

        assertFalse(result)
        assertTrue(vm.state.value.validationErrors.any { it is WorkoutValidationError.NoExercises })
    }

    @Test
    fun `finish returns false when exercise has no sets with reps`() = runTest {
        val vm = buildVm()
        vm.addExercise("ex-1")
        advanceUntilIdle()

        // Default set has reps = 0 → should fail
        val result = vm.finish(3600L)
        assertFalse(result)
        assertTrue(vm.state.value.validationErrors.isNotEmpty())
    }

    @Test
    fun `finish returns true and sets isCompleting when valid`() = runTest {
        val vm = buildVm()
        vm.addExercise("ex-1")
        advanceUntilIdle()

        // Give the set valid reps
        val set = vm.state.value.exercises.first().sets.first()
        vm.updateSet(set.copy(reps = 10))

        val result = vm.finish(3600L)
        assertTrue(result)
        assertTrue(vm.state.value.isCompleting)
    }

    @Test
    fun `finish with valid workout calls finishWorkout on repository`() = runTest {
        val workoutRepo = FakeWorkoutRepository().also { it.createdWorkoutId = "w-1" }
        val vm = buildVm(workoutRepo = workoutRepo)
        vm.startOrResumeWorkout("user-1")
        vm.startWorkout()
        advanceUntilIdle()
        vm.addExercise("ex-1")
        advanceUntilIdle()
        val set = vm.state.value.exercises.first().sets.first()
        vm.updateSet(set.copy(reps = 10))

        vm.finish(1800L)
        advanceUntilIdle()

        assertEquals("w-1", workoutRepo.finishedWorkoutId)
        assertEquals(1800L, workoutRepo.finishedDurationSeconds)
    }

    @Test
    fun `finish clears previous validation errors on valid workout`() = runTest {
        val vm = buildVm()

        // First call fails → errors set
        vm.finish(0L)
        assertTrue(vm.state.value.validationErrors.isNotEmpty())

        // Now add a valid exercise
        vm.addExercise("ex-1")
        advanceUntilIdle()
        val set = vm.state.value.exercises.first().sets.first()
        vm.updateSet(set.copy(reps = 5))

        vm.finish(0L)
        assertTrue(vm.state.value.validationErrors.isEmpty())
    }

    // ── clearValidationErrors ──────────────────────────────────────────────

    @Test
    fun `clearValidationErrors empties error list`() = runTest {
        val vm = buildVm()
        vm.finish(0L) // triggers NoExercises error
        assertTrue(vm.state.value.validationErrors.isNotEmpty())

        vm.clearValidationErrors()
        assertTrue(vm.state.value.validationErrors.isEmpty())
    }

    // ── discard ────────────────────────────────────────────────────────────

    @Test
    fun `discard resets state to defaults`() = runTest {
        val vm = buildVm()
        vm.addExercise("ex-1")
        advanceUntilIdle()

        vm.discard()

        val state = vm.state.value
        assertNull(state.workoutId)
        assertFalse(state.hasStarted)
        assertTrue(state.exercises.isEmpty())
    }

    @Test
    fun `discard with workoutId calls discardWorkout on repository`() = runTest {
        val workoutRepo = FakeWorkoutRepository().also { it.createdWorkoutId = "w-1" }
        val vm = buildVm(workoutRepo = workoutRepo)
        vm.startOrResumeWorkout("user-1")
        vm.startWorkout()
        advanceUntilIdle()

        vm.discard()
        advanceUntilIdle()

        assertEquals("w-1", workoutRepo.discardedWorkoutId)
    }

    @Test
    fun `discard without workoutId does not call repository`() = runTest {
        val workoutRepo = FakeWorkoutRepository()
        val vm = buildVm(workoutRepo = workoutRepo)
        // No workout started

        vm.discard()
        advanceUntilIdle()

        assertNull(workoutRepo.discardedWorkoutId)
    }
}
