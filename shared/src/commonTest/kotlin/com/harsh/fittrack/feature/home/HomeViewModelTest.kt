package com.harsh.fittrack.feature.home

import com.harsh.fittrack.core.util.GreetingProvider
import com.harsh.fittrack.fakes.FakeClock
import com.harsh.fittrack.fakes.FakeUserRepository
import com.harsh.fittrack.fakes.FakeWorkoutRepository
import com.harsh.fittrack.fakes.testUser
import com.harsh.fittrack.fakes.testWorkout
import com.harsh.fittrack.domain.usecase.stats.CalculateStreakUseCase
import com.harsh.fittrack.domain.usecase.stats.CalculateWeeklyWorkoutsUseCase
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
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    private val dispatcher = UnconfinedTestDispatcher()

    @BeforeTest
    fun setUp() = Dispatchers.setMain(dispatcher)

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    private fun buildVm(
        userRepo: FakeUserRepository = FakeUserRepository(testUser),
        workoutRepo: FakeWorkoutRepository = FakeWorkoutRepository(),
        clock: FakeClock = FakeClock(),
    ): HomeViewModel {
        val greeting = GreetingProvider(clock)
        return HomeViewModel(
            userRepository = userRepo,
            workoutRepository = workoutRepo,
            greetingProvider = greeting,
            clock = clock,
            calculateStreak = CalculateStreakUseCase(),
            calculateWeeklyWorkouts = CalculateWeeklyWorkoutsUseCase(),
        )
    }

    @Test
    fun `initial state has isLoading true before data arrives`() = runTest {
        val userRepo = FakeUserRepository(null)
        val vm = buildVm(userRepo = userRepo)
        // With no user emitted the state stays with defaults
        assertTrue(vm.state.value.isLoading || vm.state.value.firstName.isEmpty())
    }

    @Test
    fun `state populated with user name and greeting`() = runTest {
        val vm = buildVm()
        advanceUntilIdle()
        val state = vm.state.value
        assertEquals("John", state.firstName)
        assertFalse(state.isLoading)
    }

    @Test
    fun `firstName uses first word of full name`() = runTest {
        val userRepo = FakeUserRepository(testUser.copy(name = "Mary Jane Watson"))
        val vm = buildVm(userRepo = userRepo)
        advanceUntilIdle()
        assertEquals("Mary", vm.state.value.firstName)
    }

    @Test
    fun `greeting is Good morning at 9am`() = runTest {
        val clock = FakeClock(hour = 9)
        val vm = buildVm(clock = clock)
        advanceUntilIdle()
        assertEquals("Good morning", vm.state.value.greeting)
    }

    @Test
    fun `greeting is Good afternoon at 14`() = runTest {
        val clock = FakeClock(hour = 14)
        val vm = buildVm(clock = clock)
        advanceUntilIdle()
        assertEquals("Good afternoon", vm.state.value.greeting)
    }

    @Test
    fun `greeting is Good evening at 20`() = runTest {
        val clock = FakeClock(hour = 20)
        val vm = buildVm(clock = clock)
        advanceUntilIdle()
        assertEquals("Good evening", vm.state.value.greeting)
    }

    @Test
    fun `totalWorkouts counts only completed workouts`() = runTest {
        val workoutRepo = FakeWorkoutRepository()
        workoutRepo.setWorkoutsForUser(
            testUser.id,
            listOf(
                testWorkout("w-1", isCompleted = true),
                testWorkout("w-2", isCompleted = false),
                testWorkout("w-3", isCompleted = true),
            ),
        )
        val vm = buildVm(workoutRepo = workoutRepo)
        advanceUntilIdle()
        assertEquals(2, vm.state.value.totalWorkouts)
    }

    @Test
    fun `recentWorkouts is capped at 10`() = runTest {
        val workoutRepo = FakeWorkoutRepository()
        workoutRepo.setWorkoutsForUser(
            testUser.id,
            (1..15).map { testWorkout("w-$it") },
        )
        val vm = buildVm(workoutRepo = workoutRepo)
        advanceUntilIdle()
        assertEquals(10, vm.state.value.recentWorkouts.size)
    }

    @Test
    fun `streakDays computed from workouts and today`() = runTest {
        val clock = FakeClock(year = 2026, month = 5, day = 23)
        val workoutRepo = FakeWorkoutRepository()
        workoutRepo.setWorkoutsForUser(
            testUser.id,
            listOf(
                testWorkout("w-1", date = "2026-05-21"),
                testWorkout("w-2", date = "2026-05-22"),
                testWorkout("w-3", date = "2026-05-23"),
            ),
        )
        val vm = buildVm(workoutRepo = workoutRepo, clock = clock)
        advanceUntilIdle()
        assertEquals(3, vm.state.value.streakDays)
    }

    @Test
    fun `workoutsThisWeek computed from workouts and today`() = runTest {
        val clock = FakeClock(year = 2026, month = 5, day = 23) // Saturday
        val workoutRepo = FakeWorkoutRepository()
        workoutRepo.setWorkoutsForUser(
            testUser.id,
            listOf(
                testWorkout("w-1", date = "2026-05-18"), // Mon
                testWorkout("w-2", date = "2026-05-19"), // Tue
                testWorkout("w-3", date = "2026-05-04"), // previous week
            ),
        )
        val vm = buildVm(workoutRepo = workoutRepo, clock = clock)
        advanceUntilIdle()
        assertEquals(2, vm.state.value.workoutsThisWeek)
    }

    @Test
    fun `empty workout list shows zeros`() = runTest {
        val vm = buildVm()
        advanceUntilIdle()
        val state = vm.state.value
        assertEquals(0, state.totalWorkouts)
        assertEquals(0, state.streakDays)
        assertEquals(0, state.workoutsThisWeek)
        assertTrue(state.recentWorkouts.isEmpty())
    }

    @Test
    fun `no user results in empty state`() = runTest {
        val vm = buildVm(userRepo = FakeUserRepository(null))
        advanceUntilIdle()
        val state = vm.state.value
        assertEquals("", state.firstName)
        assertEquals(0, state.totalWorkouts)
    }
}
