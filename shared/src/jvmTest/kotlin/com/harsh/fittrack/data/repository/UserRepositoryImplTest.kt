package com.harsh.fittrack.data.repository

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import app.cash.turbine.test
import com.harsh.fittrack.db.FitTrackDatabase
import com.harsh.fittrack.domain.model.Units
import com.harsh.fittrack.fakes.FakeApi
import com.harsh.fittrack.fakes.FakeAuthRepository
import com.harsh.fittrack.fakes.testUser
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
import kotlin.test.assertNotNull
import kotlin.test.assertNull

@OptIn(ExperimentalCoroutinesApi::class)
class UserRepositoryImplTest {

    private val dispatcher = UnconfinedTestDispatcher()

    @BeforeTest
    fun setUp() = Dispatchers.setMain(dispatcher)

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    private fun createTestDatabase(): FitTrackDatabase {
        val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        FitTrackDatabase.Schema.create(driver)
        return FitTrackDatabase(driver)
    }

    private fun buildRepo(
        authRepo: FakeAuthRepository = FakeAuthRepository(null),
        api: FakeApi = FakeApi(),
        configureDb: (FitTrackDatabase) -> Unit = {},
    ): Pair<UserRepositoryImpl, FitTrackDatabase> {
        val db = createTestDatabase()
        configureDb(db)
        return Pair(UserRepositoryImpl(authRepo, db, api), db)
    }

    // ── observeUser ────────────────────────────────────────────────────────

    @Test
    fun `observeUser emits null when auth user is null`() = runTest {
        val (repo) = buildRepo(authRepo = FakeAuthRepository(null))

        repo.observeUser().test {
            assertNull(awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `observeUser emits user from DB when auth user exists`() = runTest {
        val authRepo = FakeAuthRepository(testUser)
        val (repo) = buildRepo(authRepo = authRepo) { db ->
            db.userQueries.upsert(
                id = testUser.id,
                name = testUser.name,
                email = testUser.email,
                photoUrl = null,
                units = "KG",
            )
        }

        repo.observeUser().test {
            val user = awaitItem()
            assertNotNull(user)
            assertEquals(testUser.id, user!!.id)
            assertEquals(testUser.name, user.name)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `observeUser emits null when auth user exists but DB row is absent`() = runTest {
        val authRepo = FakeAuthRepository(testUser)
        val (repo) = buildRepo(authRepo = authRepo)

        repo.observeUser().test {
            assertNull(awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `observeUser switches to null when user signs out`() = runTest {
        val authRepo = FakeAuthRepository(testUser)
        val (repo) = buildRepo(authRepo = authRepo) { db ->
            db.userQueries.upsert(testUser.id, testUser.name, testUser.email, null, "KG")
        }

        repo.observeUser().test {
            assertNotNull(awaitItem())

            authRepo.emit(null)
            assertNull(awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ── setUnits ───────────────────────────────────────────────────────────

    @Test
    fun `setUnits updates the units column in DB`() = runTest {
        val authRepo = FakeAuthRepository(testUser)
        val (repo, db) = buildRepo(authRepo = authRepo) { db ->
            db.userQueries.upsert(testUser.id, testUser.name, testUser.email, null, "KG")
        }

        repo.setUnits(Units.LBS)

        val row = db.userQueries.selectById(testUser.id).executeAsOneOrNull()
        assertEquals("LBS", row?.units)
    }

    @Test
    fun `setUnits calls API patchMe with correct units string`() = runTest {
        val api = FakeApi()
        val (repo) = buildRepo(api = api)

        repo.setUnits(Units.LBS)

        assertEquals("LBS", api.patchMeCalledWith)
    }

    @Test
    fun `setUnits KG calls API with KG string`() = runTest {
        val api = FakeApi()
        val (repo) = buildRepo(api = api)

        repo.setUnits(Units.KG)

        assertEquals("KG", api.patchMeCalledWith)
    }

    @Test
    fun `setUnits does not throw when API fails`() = runTest {
        val api = FakeApi().also { it.patchMeThrows = true }
        val (repo) = buildRepo(api = api)

        repo.setUnits(Units.LBS)
    }
}
