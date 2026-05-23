package com.harsh.fittrack.feature.home

import app.cash.turbine.test
import com.harsh.fittrack.fakes.FakeWorkoutRepository
import com.harsh.fittrack.fakes.testExerciseEntry
import com.harsh.fittrack.fakes.testExerciseWithSets
import com.harsh.fittrack.fakes.testSetEntry
import com.harsh.fittrack.fakes.testWorkout
import com.harsh.fittrack.domain.repository.WorkoutWithDetails
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
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
class WorkoutDetailViewModelTest {

    private val dispatcher = UnconfinedTestDispatcher()

    @BeforeTest
    fun setUp() = Dispatchers.setMain(dispatcher)

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun `initial state is loading with null details`() = runTest {
        val repo = FakeWorkoutRepository()
        val vm = WorkoutDetailViewModel("w-1", repo)

        vm.state.test {
            val state = awaitItem()
            assertFalse(state.isLoading)
            assertNull(state.details)
        }
    }

    @Test
    fun `state reflects workout details when available`() = runTest {
        val repo = FakeWorkoutRepository()
        val workout = testWorkout("w-1")
        val entry = testExerciseEntry(workoutId = "w-1")
        val set = testSetEntry()
        val details = WorkoutWithDetails(workout = workout, exercises = listOf(testExerciseWithSets(entry, listOf(set))))
        repo.setWorkoutDetails("w-1", details)

        val vm = WorkoutDetailViewModel("w-1", repo)

        vm.state.test {
            val state = awaitItem()
            assertFalse(state.isLoading)
            assertEquals(details, state.details)
            assertEquals("Test Workout", state.details!!.workout.title)
            assertEquals(1, state.details!!.exercises.size)
            assertEquals(1, state.details!!.exercises.first().sets.size)
        }
    }

    @Test
    fun `state updates when workout details change`() = runTest {
        val repo = FakeWorkoutRepository()
        repo.setWorkoutDetails("w-1", null)

        val vm = WorkoutDetailViewModel("w-1", repo)

        vm.state.test {
            val initial = awaitItem()
            assertNull(initial.details)

            val details = WorkoutWithDetails(
                workout = testWorkout("w-1"),
                exercises = emptyList(),
            )
            repo.setWorkoutDetails("w-1", details)
            val updated = awaitItem()
            assertEquals(details, updated.details)
        }
    }

    @Test
    fun `state clears when workout is deleted`() = runTest {
        val repo = FakeWorkoutRepository()
        val details = WorkoutWithDetails(workout = testWorkout("w-1"), exercises = emptyList())
        repo.setWorkoutDetails("w-1", details)

        val vm = WorkoutDetailViewModel("w-1", repo)

        vm.state.test {
            awaitItem() // initial with details

            repo.setWorkoutDetails("w-1", null)
            val cleared = awaitItem()
            assertNull(cleared.details)
        }
    }

    @Test
    fun `multiple exercises and sets are all present in state`() = runTest {
        val repo = FakeWorkoutRepository()
        val entry1 = testExerciseEntry(id = "e1", name = "Bench Press")
        val entry2 = testExerciseEntry(id = "e2", name = "Squat")
        val sets1 = listOf(testSetEntry("s1", "e1", 1), testSetEntry("s2", "e1", 2))
        val sets2 = listOf(testSetEntry("s3", "e2", 1))
        val details = WorkoutWithDetails(
            workout = testWorkout("w-1"),
            exercises = listOf(
                testExerciseWithSets(entry1, sets1),
                testExerciseWithSets(entry2, sets2),
            ),
        )
        repo.setWorkoutDetails("w-1", details)

        val vm = WorkoutDetailViewModel("w-1", repo)
        vm.state.test {
            val state = awaitItem()
            assertEquals(2, state.details!!.exercises.size)
            assertEquals(2, state.details!!.exercises[0].sets.size)
            assertEquals(1, state.details!!.exercises[1].sets.size)
        }
    }
}
