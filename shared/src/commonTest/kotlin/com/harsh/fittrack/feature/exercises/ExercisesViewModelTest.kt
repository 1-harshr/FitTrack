package com.harsh.fittrack.feature.exercises

import com.harsh.fittrack.domain.model.MovementType
import com.harsh.fittrack.domain.model.MuscleGroup
import com.harsh.fittrack.fakes.FakeExerciseRepository
import com.harsh.fittrack.fakes.testExercise
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class ExercisesViewModelTest {

    private val dispatcher = UnconfinedTestDispatcher()

    @BeforeTest
    fun setUp() = Dispatchers.setMain(dispatcher)

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    private val allExercises = listOf(
        testExercise("e1", "Bench Press", MuscleGroup.CHEST, MovementType.COMPOUND),
        testExercise("e2", "Incline Dumbbell", MuscleGroup.CHEST, MovementType.ISOLATION),
        testExercise("e3", "Squat", MuscleGroup.LEGS, MovementType.COMPOUND),
        testExercise("e4", "Leg Curl", MuscleGroup.LEGS, MovementType.ISOLATION),
        testExercise("e5", "Running", MuscleGroup.LEGS, MovementType.CARDIO),
        testExercise("e6", "Pull-up", MuscleGroup.BACK, MovementType.COMPOUND),
    )

    /** Subscribes to the StateFlow to activate WhileSubscribed and returns the cancellable job. */
    private fun kotlinx.coroutines.CoroutineScope.subscribe(vm: ExercisesViewModel) =
        launch(dispatcher) { vm.state.collect {} }

    @Test
    fun `initial state has empty query and null muscle group`() = runTest {
        val repo = FakeExerciseRepository()
        val vm = ExercisesViewModel(repo)
        val job = subscribe(vm)
        advanceUntilIdle()

        val state = vm.state.value
        assertTrue(state.query.isEmpty())
        assertNull(state.activeMuscleGroup)

        job.cancel()
    }

    @Test
    fun `all exercises returned when no filter applied`() = runTest {
        val repo = FakeExerciseRepository(allExercises)
        val vm = ExercisesViewModel(repo)
        val job = subscribe(vm)
        advanceUntilIdle()

        assertEquals(6, vm.state.value.results.size)
        job.cancel()
    }

    @Test
    fun `setQuery filters results by name`() = runTest {
        val repo = FakeExerciseRepository(allExercises)
        val vm = ExercisesViewModel(repo)
        val job = subscribe(vm)
        advanceUntilIdle()

        vm.setQuery("bench")
        advanceUntilIdle()

        val state = vm.state.value
        assertEquals(1, state.results.size)
        assertEquals("Bench Press", state.results.first().name)
        job.cancel()
    }

    @Test
    fun `setQuery is case-insensitive`() = runTest {
        val repo = FakeExerciseRepository(allExercises)
        val vm = ExercisesViewModel(repo)
        val job = subscribe(vm)
        advanceUntilIdle()

        vm.setQuery("SQUAT")
        advanceUntilIdle()

        assertEquals(1, vm.state.value.results.size)
        assertEquals("Squat", vm.state.value.results.first().name)
        job.cancel()
    }

    @Test
    fun `setQuery with empty string returns all exercises`() = runTest {
        val repo = FakeExerciseRepository(allExercises)
        val vm = ExercisesViewModel(repo)
        val job = subscribe(vm)
        advanceUntilIdle()

        vm.setQuery("bench")
        advanceUntilIdle()
        vm.setQuery("")
        advanceUntilIdle()

        assertEquals(6, vm.state.value.results.size)
        job.cancel()
    }

    @Test
    fun `setMuscleGroup filters by primary muscle`() = runTest {
        val repo = FakeExerciseRepository(allExercises)
        val vm = ExercisesViewModel(repo)
        val job = subscribe(vm)
        advanceUntilIdle()

        vm.setMuscleGroup(MuscleGroup.CHEST)
        advanceUntilIdle()

        val state = vm.state.value
        assertEquals(2, state.results.size)
        assertTrue(state.results.all { it.primaryMuscle == MuscleGroup.CHEST })
        job.cancel()
    }

    @Test
    fun `setMuscleGroup null clears filter and returns all`() = runTest {
        val repo = FakeExerciseRepository(allExercises)
        val vm = ExercisesViewModel(repo)
        val job = subscribe(vm)
        advanceUntilIdle()

        vm.setMuscleGroup(MuscleGroup.BACK)
        advanceUntilIdle()
        vm.setMuscleGroup(null)
        advanceUntilIdle()

        assertEquals(6, vm.state.value.results.size)
        job.cancel()
    }

    @Test
    fun `strengthCount and cardioCount based on full unfiltered list`() = runTest {
        val repo = FakeExerciseRepository(allExercises)
        val vm = ExercisesViewModel(repo)
        val job = subscribe(vm)
        advanceUntilIdle()

        vm.setMuscleGroup(MuscleGroup.CHEST)
        advanceUntilIdle()

        val state = vm.state.value
        assertEquals(2, state.results.size)  // filtered to CHEST only
        assertEquals(5, state.strengthCount) // based on full list (non-cardio)
        assertEquals(1, state.cardioCount)   // based on full list
        job.cancel()
    }

    @Test
    fun `query and muscle group can be combined`() = runTest {
        val repo = FakeExerciseRepository(allExercises)
        val vm = ExercisesViewModel(repo)
        val job = subscribe(vm)
        advanceUntilIdle()

        vm.setMuscleGroup(MuscleGroup.LEGS)
        advanceUntilIdle()
        vm.setQuery("squat")
        advanceUntilIdle()

        val state = vm.state.value
        assertEquals(1, state.results.size)
        assertEquals("Squat", state.results.first().name)
        job.cancel()
    }

    @Test
    fun `empty repository produces zero counts`() = runTest {
        val repo = FakeExerciseRepository(emptyList())
        val vm = ExercisesViewModel(repo)
        val job = subscribe(vm)
        advanceUntilIdle()

        val state = vm.state.value
        assertEquals(0, state.strengthCount)
        assertEquals(0, state.cardioCount)
        assertTrue(state.results.isEmpty())
        job.cancel()
    }

    @Test
    fun `state updates when repository emits new exercises`() = runTest {
        val repo = FakeExerciseRepository(emptyList())
        val vm = ExercisesViewModel(repo)
        val job = subscribe(vm)
        advanceUntilIdle()

        assertEquals(0, vm.state.value.results.size)

        repo.emit(allExercises)
        advanceUntilIdle()

        assertEquals(6, vm.state.value.results.size)
        job.cancel()
    }

    @Test
    fun `active muscle group reflected in state`() = runTest {
        val repo = FakeExerciseRepository(allExercises)
        val vm = ExercisesViewModel(repo)
        val job = subscribe(vm)
        advanceUntilIdle()

        vm.setMuscleGroup(MuscleGroup.SHOULDERS)
        advanceUntilIdle()

        assertEquals(MuscleGroup.SHOULDERS, vm.state.value.activeMuscleGroup)
        job.cancel()
    }

    @Test
    fun `query text reflected in state`() = runTest {
        val repo = FakeExerciseRepository(allExercises)
        val vm = ExercisesViewModel(repo)
        val job = subscribe(vm)
        advanceUntilIdle()

        vm.setQuery("pull")
        advanceUntilIdle()

        assertEquals("pull", vm.state.value.query)
        job.cancel()
    }
}
