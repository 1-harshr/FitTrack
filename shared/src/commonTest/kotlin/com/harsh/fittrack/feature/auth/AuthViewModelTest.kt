package com.harsh.fittrack.feature.auth

import app.cash.turbine.test
import com.harsh.fittrack.fakes.FakeAuthRepository
import com.harsh.fittrack.fakes.testUser
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
import kotlin.test.assertIs
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class AuthViewModelTest {

    private val dispatcher = UnconfinedTestDispatcher()

    @BeforeTest
    fun setUp() = Dispatchers.setMain(dispatcher)

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    // ── Initial state ──────────────────────────────────────────────────────

    @Test
    fun `initial state is Loading before user flow emits`() = runTest {
        val repo = FakeAuthRepository(initialUser = null)
        // Intercept before init coroutine runs
        val vm = AuthViewModel(repo)
        // With UnconfinedTestDispatcher the init coroutine runs immediately,
        // so we check the settled value
        assertEquals(AuthState.SignedOut, vm.state.value)
    }

    @Test
    fun `state is SignedOut when repository emits null user`() = runTest {
        val repo = FakeAuthRepository(initialUser = null)
        val vm = AuthViewModel(repo)
        assertEquals(AuthState.SignedOut, vm.state.value)
    }

    @Test
    fun `state is SignedIn when repository has existing user`() = runTest {
        val repo = FakeAuthRepository(initialUser = testUser)
        val vm = AuthViewModel(repo)
        assertEquals(AuthState.SignedIn(testUser), vm.state.value)
    }

    @Test
    fun `state transitions to SignedIn when user emitted after SignedOut`() = runTest {
        val repo = FakeAuthRepository(initialUser = null)
        val vm = AuthViewModel(repo)

        vm.state.test {
            assertEquals(AuthState.SignedOut, awaitItem())
            repo.emit(testUser)
            assertEquals(AuthState.SignedIn(testUser), awaitItem())
        }
    }

    @Test
    fun `state transitions to SignedOut when user revoked`() = runTest {
        val repo = FakeAuthRepository(initialUser = testUser)
        val vm = AuthViewModel(repo)

        vm.state.test {
            assertEquals(AuthState.SignedIn(testUser), awaitItem())
            repo.emit(null)
            assertEquals(AuthState.SignedOut, awaitItem())
        }
    }

    // ── Login ──────────────────────────────────────────────────────────────

    @Test
    fun `login success navigates to SignedIn`() = runTest {
        val repo = FakeAuthRepository(initialUser = null)
        repo.loginResult = Result.success(testUser)
        val vm = AuthViewModel(repo)

        vm.state.test {
            awaitItem() // SignedOut

            vm.login("john@example.com", "secret")

            assertEquals(AuthState.Loading, awaitItem())
            // Repository emits user after successful login
            repo.emit(testUser)
            assertEquals(AuthState.SignedIn(testUser), awaitItem())
        }
    }

    @Test
    fun `login failure sets Error state with message`() = runTest {
        val repo = FakeAuthRepository(initialUser = null)
        repo.loginResult = Result.failure(Exception("Invalid credentials"))
        val vm = AuthViewModel(repo)

        vm.state.test {
            awaitItem() // SignedOut

            vm.login("bad@example.com", "wrong")

            assertEquals(AuthState.Loading, awaitItem())
            val error = awaitItem()
            assertIs<AuthState.Error>(error)
            assertEquals("Invalid credentials", error.message)
        }
    }

    @Test
    fun `login failure uses fallback message when exception has no message`() = runTest {
        val repo = FakeAuthRepository(initialUser = null)
        repo.loginResult = Result.failure(Exception())
        val vm = AuthViewModel(repo)

        vm.state.test {
            awaitItem()
            vm.login("x@x.com", "pass")
            awaitItem() // Loading
            val error = awaitItem()
            assertIs<AuthState.Error>(error)
            assertEquals("Login failed", error.message)
        }
    }

    @Test
    fun `login trims whitespace from email`() = runTest {
        val repo = FakeAuthRepository(initialUser = null)
        repo.loginResult = Result.failure(Exception("stop"))
        val vm = AuthViewModel(repo)
        vm.login("  spaced@email.com  ", "pass")
        advanceUntilIdle()
        assertEquals("spaced@email.com", repo.lastLoginEmail)
    }

    // ── Register ───────────────────────────────────────────────────────────

    @Test
    fun `register success navigates to SignedIn`() = runTest {
        val repo = FakeAuthRepository(initialUser = null)
        repo.registerResult = Result.success(testUser)
        val vm = AuthViewModel(repo)

        vm.state.test {
            awaitItem() // SignedOut
            vm.register("John", "john@example.com", "secret123")
            assertEquals(AuthState.Loading, awaitItem())
            repo.emit(testUser)
            assertEquals(AuthState.SignedIn(testUser), awaitItem())
        }
    }

    @Test
    fun `register failure sets Error state`() = runTest {
        val repo = FakeAuthRepository(initialUser = null)
        repo.registerResult = Result.failure(Exception("Email already in use"))
        val vm = AuthViewModel(repo)

        vm.state.test {
            awaitItem()
            vm.register("Jane", "jane@example.com", "pass")
            assertEquals(AuthState.Loading, awaitItem())
            val error = awaitItem()
            assertIs<AuthState.Error>(error)
            assertTrue("Email already in use" in error.message)
        }
    }

    @Test
    fun `register trims whitespace from name and email`() = runTest {
        val repo = FakeAuthRepository(initialUser = null)
        repo.registerResult = Result.failure(Exception("stop"))
        val vm = AuthViewModel(repo)
        vm.register("  Jane  ", "  jane@example.com  ", "pass")
        advanceUntilIdle()
        assertEquals("Jane", repo.lastRegisterName)
        assertEquals("jane@example.com", repo.lastRegisterEmail)
    }

    // ── clearError ─────────────────────────────────────────────────────────

    @Test
    fun `clearError transitions Error to SignedOut`() = runTest {
        val repo = FakeAuthRepository(initialUser = null)
        repo.loginResult = Result.failure(Exception("oops"))
        val vm = AuthViewModel(repo)

        vm.login("x@x.com", "bad")
        assertIs<AuthState.Error>(vm.state.value)

        vm.clearError()
        assertEquals(AuthState.SignedOut, vm.state.value)
    }

    @Test
    fun `clearError is a no-op when not in Error state`() = runTest {
        val repo = FakeAuthRepository(initialUser = testUser)
        val vm = AuthViewModel(repo)
        assertEquals(AuthState.SignedIn(testUser), vm.state.value)

        vm.clearError()
        assertEquals(AuthState.SignedIn(testUser), vm.state.value)
    }

    // ── Sign out ───────────────────────────────────────────────────────────

    @Test
    fun `signOut delegates to repository`() = runTest {
        val repo = FakeAuthRepository(initialUser = testUser)
        val vm = AuthViewModel(repo)

        vm.signOut()
        assertTrue(repo.signOutCalled)
    }

    @Test
    fun `signOut clears user state`() = runTest {
        val repo = FakeAuthRepository(initialUser = testUser)
        val vm = AuthViewModel(repo)

        vm.state.test {
            assertEquals(AuthState.SignedIn(testUser), awaitItem())
            vm.signOut()
            assertEquals(AuthState.SignedOut, awaitItem())
        }
    }
}
