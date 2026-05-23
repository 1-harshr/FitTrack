package com.harsh.fittrack.feature.profile

import com.harsh.fittrack.domain.model.Units
import com.harsh.fittrack.domain.usecase.stats.CalculateStreakUseCase
import com.harsh.fittrack.fakes.FakeAuthRepository
import com.harsh.fittrack.fakes.FakeClock
import com.harsh.fittrack.fakes.FakeUserRepository
import com.harsh.fittrack.fakes.FakeWorkoutRepository
import com.harsh.fittrack.fakes.testUser
import com.harsh.fittrack.fakes.testWorkout
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
class ProfileViewModelTest {

    private val dispatcher = UnconfinedTestDispatcher()

    @BeforeTest
    fun setUp() = Dispatchers.setMain(dispatcher)

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    private fun buildVm(
        userRepo: FakeUserRepository = FakeUserRepository(testUser),
        workoutRepo: FakeWorkoutRepository = FakeWorkoutRepository(),
        authRepo: FakeAuthRepository = FakeAuthRepository(testUser),
        clock: FakeClock = FakeClock(year = 2026, month = 5, day = 23),
    ) = ProfileViewModel(
        userRepository = userRepo,
        workoutRepository = workoutRepo,
        authRepository = authRepo,
        clock = clock,
        calculateStreak = CalculateStreakUseCase(),
    )

    // ── State population ───────────────────────────────────────────────────

    @Test
    fun `state has user when signed in`() = runTest {
        val vm = buildVm()
        advanceUntilIdle()
        assertEquals(testUser, vm.state.value.user)
    }

    @Test
    fun `state has null user when signed out`() = runTest {
        val vm = buildVm(userRepo = FakeUserRepository(null))
        advanceUntilIdle()
        assertNull(vm.state.value.user)
    }

    @Test
    fun `isLoading is false after data loads`() = runTest {
        val vm = buildVm()
        advanceUntilIdle()
        assertFalse(vm.state.value.isLoading)
    }

    @Test
    fun `totalWorkouts counts only completed workouts`() = runTest {
        val workoutRepo = FakeWorkoutRepository()
        workoutRepo.setWorkoutsForUser(
            testUser.id,
            listOf(
                testWorkout("w1", isCompleted = true),
                testWorkout("w2", isCompleted = true),
                testWorkout("w3", isCompleted = false),
            ),
        )
        val vm = buildVm(workoutRepo = workoutRepo)
        advanceUntilIdle()
        assertEquals(2, vm.state.value.totalWorkouts)
    }

    @Test
    fun `totalVolumeKg sums volume from completed workouts only`() = runTest {
        val workoutRepo = FakeWorkoutRepository()
        workoutRepo.setWorkoutsForUser(
            testUser.id,
            listOf(
                testWorkout("w1", isCompleted = true, volumeKg = 500.0),
                testWorkout("w2", isCompleted = true, volumeKg = 300.0),
                testWorkout("w3", isCompleted = false, volumeKg = 999.0), // should be excluded
            ),
        )
        val vm = buildVm(workoutRepo = workoutRepo)
        advanceUntilIdle()
        assertEquals(800.0, vm.state.value.totalVolumeKg)
    }

    @Test
    fun `totalVolumeThisMonthKg sums only workouts in current month`() = runTest {
        val clock = FakeClock(year = 2026, month = 5, day = 23)
        val workoutRepo = FakeWorkoutRepository()
        workoutRepo.setWorkoutsForUser(
            testUser.id,
            listOf(
                testWorkout("w1", date = "2026-05-10", volumeKg = 400.0), // this month
                testWorkout("w2", date = "2026-05-15", volumeKg = 200.0), // this month
                testWorkout("w3", date = "2026-04-20", volumeKg = 600.0), // last month
            ),
        )
        val vm = buildVm(workoutRepo = workoutRepo, clock = clock)
        advanceUntilIdle()
        assertEquals(600.0, vm.state.value.totalVolumeThisMonthKg)
    }

    @Test
    fun `streakDays calculated from workout history`() = runTest {
        val clock = FakeClock(year = 2026, month = 5, day = 23)
        val workoutRepo = FakeWorkoutRepository()
        workoutRepo.setWorkoutsForUser(
            testUser.id,
            listOf(
                testWorkout("w1", date = "2026-05-21"),
                testWorkout("w2", date = "2026-05-22"),
                testWorkout("w3", date = "2026-05-23"),
            ),
        )
        val vm = buildVm(workoutRepo = workoutRepo, clock = clock)
        advanceUntilIdle()
        assertEquals(3, vm.state.value.streakDays)
    }

    @Test
    fun `default units comes from user profile`() = runTest {
        val lbsUser = testUser.copy(units = Units.LBS)
        val vm = buildVm(userRepo = FakeUserRepository(lbsUser))
        advanceUntilIdle()
        assertEquals(Units.LBS, vm.state.value.units)
    }

    // ── toggleUnits ────────────────────────────────────────────────────────

    @Test
    fun `toggleUnits switches KG to LBS`() = runTest {
        val userRepo = FakeUserRepository(testUser.copy(units = Units.KG))
        val vm = buildVm(userRepo = userRepo)
        advanceUntilIdle()

        vm.toggleUnits()
        advanceUntilIdle()

        assertEquals(Units.LBS, vm.state.value.units)
    }

    @Test
    fun `toggleUnits switches LBS to KG`() = runTest {
        val userRepo = FakeUserRepository(testUser.copy(units = Units.LBS))
        val vm = buildVm(userRepo = userRepo)
        advanceUntilIdle()

        vm.toggleUnits()
        advanceUntilIdle()

        assertEquals(Units.KG, vm.state.value.units)
    }

    @Test
    fun `toggleUnits calls setUnits on user repository`() = runTest {
        val userRepo = FakeUserRepository(testUser.copy(units = Units.KG))
        val vm = buildVm(userRepo = userRepo)
        advanceUntilIdle()

        vm.toggleUnits()
        advanceUntilIdle()

        assertEquals(listOf(Units.LBS), userRepo.setUnitsCalls)
    }

    @Test
    fun `toggleUnits can be called multiple times`() = runTest {
        val userRepo = FakeUserRepository(testUser.copy(units = Units.KG))
        val vm = buildVm(userRepo = userRepo)
        advanceUntilIdle()

        vm.toggleUnits()
        advanceUntilIdle()
        vm.toggleUnits()
        advanceUntilIdle()

        assertEquals(Units.KG, vm.state.value.units)
        assertEquals(2, userRepo.setUnitsCalls.size)
    }

    // ── signOut ────────────────────────────────────────────────────────────

    @Test
    fun `signOut calls authRepository signOut`() = runTest {
        val authRepo = FakeAuthRepository(testUser)
        val vm = buildVm(authRepo = authRepo)

        vm.signOut()
        advanceUntilIdle()

        assertTrue(authRepo.signOutCalled)
    }
}
