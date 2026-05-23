package com.harsh.fittrack.data.repository

import app.cash.turbine.test
import com.harsh.fittrack.data.local.DatabaseFactory
import com.harsh.fittrack.data.remote.TokenStore
import com.harsh.fittrack.domain.model.Units
import com.harsh.fittrack.fakes.FakeApi
import com.harsh.fittrack.fakes.fakeApiAuthResponse
import com.harsh.fittrack.fakes.fakeApiUser
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
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class AuthRepositoryImplTest {

    private val dispatcher = UnconfinedTestDispatcher()

    @BeforeTest
    fun setUp() = Dispatchers.setMain(dispatcher)

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    private fun buildRepo(
        api: FakeApi = FakeApi(),
        tokenStore: TokenStore = TokenStore(),
    ): Triple<AuthRepositoryImpl, FakeApi, TokenStore> {
        val db = DatabaseFactory().create()
        return Triple(AuthRepositoryImpl(db, api, tokenStore), api, tokenStore)
    }

    private fun buildRepoWithDb(
        api: FakeApi = FakeApi(),
        tokenStore: TokenStore = TokenStore(),
        configureDb: (com.harsh.fittrack.db.FitTrackDatabase) -> Unit = {},
    ): Triple<AuthRepositoryImpl, FakeApi, TokenStore> {
        val db = DatabaseFactory().create()
        configureDb(db)
        return Triple(AuthRepositoryImpl(db, api, tokenStore), api, tokenStore)
    }

    // ── Init ──────────────────────────────────────────────────────────────

    @Test
    fun `init with no stored token - currentUser is null and tokenStore is empty`() = runTest {
        val (repo, _, tokenStore) = buildRepo()

        repo.currentUser.test {
            assertNull(awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
        assertNull(tokenStore.token)
    }

    @Test
    fun `init with stored token but missing user row - currentUser is null`() = runTest {
        val tokenStore = TokenStore()
        val (repo) = buildRepoWithDb(tokenStore = tokenStore) { db ->
            db.authTokenQueries.upsert(token = "old-token", userId = "ghost-user")
            // Intentionally NOT inserting a user row
        }

        repo.currentUser.test {
            assertNull(awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
        // Token is still restored into the store even without the user row
        assertEquals("old-token", tokenStore.token)
    }

    @Test
    fun `init with stored token and valid user row - currentUser emits user`() = runTest {
        val tokenStore = TokenStore()
        val (repo) = buildRepoWithDb(tokenStore = tokenStore) { db ->
            db.authTokenQueries.upsert(token = "valid-token", userId = "u-1")
            db.userQueries.upsert(id = "u-1", name = "Alice", email = "alice@x.com", photoUrl = null, units = "KG")
        }

        repo.currentUser.test {
            val user = awaitItem()
            assertNotNull(user)
            assertEquals("u-1", user!!.id)
            assertEquals("Alice", user.name)
            assertEquals("alice@x.com", user.email)
            assertEquals(Units.KG, user.units)
            cancelAndIgnoreRemainingEvents()
        }
        assertEquals("valid-token", tokenStore.token)
    }

    @Test
    fun `init with stored token and LBS units restores correct units`() = runTest {
        val (repo) = buildRepoWithDb { db ->
            db.authTokenQueries.upsert(token = "t", userId = "u-1")
            db.userQueries.upsert(id = "u-1", name = "Bob", email = "b@x.com", photoUrl = null, units = "LBS")
        }

        repo.currentUser.test {
            val user = awaitItem()
            assertEquals(Units.LBS, user?.units)
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ── isSignedIn ─────────────────────────────────────────────────────────

    @Test
    fun `isSignedIn returns false when no token`() = runTest {
        val (repo) = buildRepo()
        assertFalse(repo.isSignedIn())
    }

    @Test
    fun `isSignedIn returns true when token is present`() = runTest {
        val tokenStore = TokenStore().also { it.token = "jwt" }
        val (repo) = buildRepo(tokenStore = tokenStore)
        assertTrue(repo.isSignedIn())
    }

    // ── login ──────────────────────────────────────────────────────────────

    @Test
    fun `login success returns user and stores token`() = runTest {
        val api = FakeApi().also {
            it.loginResponse = fakeApiAuthResponse(
                token = "new-jwt",
                user = fakeApiUser(id = "u-2", name = "Dave", email = "dave@x.com"),
            )
        }
        val tokenStore = TokenStore()
        val (repo) = buildRepo(api = api, tokenStore = tokenStore)

        val result = repo.login("dave@x.com", "secret")

        assertTrue(result.isSuccess)
        val user = result.getOrThrow()
        assertEquals("u-2", user.id)
        assertEquals("Dave", user.name)
        assertEquals("new-jwt", tokenStore.token)
    }

    @Test
    fun `login success emits user on currentUser flow`() = runTest {
        val api = FakeApi().also {
            it.loginResponse = fakeApiAuthResponse(user = fakeApiUser(id = "u-3", name = "Eve"))
        }
        val (repo) = buildRepo(api = api)

        repo.currentUser.test {
            awaitItem() // null initial

            repo.login("eve@x.com", "pass")
            val user = awaitItem()
            assertNotNull(user)
            assertEquals("Eve", user?.name)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `login with null API response returns failure`() = runTest {
        val api = FakeApi().also { it.loginResponse = null }
        val (repo) = buildRepo(api = api)

        val result = repo.login("x@x.com", "pass")

        assertTrue(result.isFailure)
        assertNotNull(result.exceptionOrNull()?.message)
    }

    @Test
    fun `login with unknown units string falls back to KG`() = runTest {
        val api = FakeApi().also {
            it.loginResponse = fakeApiAuthResponse(user = fakeApiUser(units = "POUNDS"))
        }
        val (repo) = buildRepo(api = api)

        val result = repo.login("x@x.com", "pass")
        assertEquals(Units.KG, result.getOrThrow().units)
    }

    // ── register ───────────────────────────────────────────────────────────

    @Test
    fun `register success returns user and stores token`() = runTest {
        val api = FakeApi().also {
            it.registerResponse = fakeApiAuthResponse(
                token = "reg-jwt",
                user = fakeApiUser(id = "u-4", name = "Frank"),
            )
        }
        val tokenStore = TokenStore()
        val (repo) = buildRepo(api = api, tokenStore = tokenStore)

        val result = repo.register("Frank", "frank@x.com", "password")

        assertTrue(result.isSuccess)
        assertEquals("u-4", result.getOrThrow().id)
        assertEquals("reg-jwt", tokenStore.token)
    }

    @Test
    fun `register with null API response returns failure`() = runTest {
        val api = FakeApi().also { it.registerResponse = null }
        val (repo) = buildRepo(api = api)

        val result = repo.register("Ghost", "ghost@x.com", "pass")
        assertTrue(result.isFailure)
    }

    // ── signOut ────────────────────────────────────────────────────────────

    @Test
    fun `signOut clears token store and emits null user`() = runTest {
        val api = FakeApi().also {
            it.loginResponse = fakeApiAuthResponse(user = fakeApiUser(id = "u-5"))
        }
        val tokenStore = TokenStore()
        val (repo) = buildRepo(api = api, tokenStore = tokenStore)
        repo.login("x@x.com", "pass")

        repo.currentUser.test {
            awaitItem() // signed-in user

            repo.signOut()
            assertNull(awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
        assertNull(tokenStore.token)
    }

    @Test
    fun `signOut when already signed out is a no-op`() = runTest {
        val (repo) = buildRepo()

        repo.signOut() // should not throw

        assertFalse(repo.isSignedIn())
    }
}
