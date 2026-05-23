package com.harsh.fittrack.feature.workout

import com.harsh.fittrack.TEST_JWT_SECRET
import com.harsh.fittrack.fakes.FakeServerExerciseRepository
import com.harsh.fittrack.fakes.FakeServerUserRepository
import com.harsh.fittrack.fakes.FakeServerWorkoutRepository
import com.harsh.fittrack.fakes.testServerWorkout
import com.harsh.fittrack.domain.model.Workout
import com.harsh.fittrack.domain.model.WorkoutListResponse
import com.harsh.fittrack.installTestApp
import com.harsh.fittrack.jsonClient
import com.harsh.fittrack.testToken
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.testApplication
import kotlinx.serialization.json.Json
import java.util.Base64
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

private val json = Json { ignoreUnknownKeys = true }

class WorkoutRoutesTest {

    // ── GET /workouts ─────────────────────────────────────────────────────────

    @Test
    fun `GET workouts returns list for authenticated user`() = testApplication {
        val workoutRepo = FakeServerWorkoutRepository()
        workoutRepo.seed(
            testServerWorkout(id = "w-1", startedAt = 2000L),
            testServerWorkout(id = "w-2", startedAt = 1000L),
        )
        installTestApp(FakeServerUserRepository(), workoutRepo, FakeServerExerciseRepository())
        val client = jsonClient()

        val response = client.get("/workouts") {
            header(HttpHeaders.Authorization, "Bearer ${testToken()}")
        }

        assertEquals(HttpStatusCode.OK, response.status)
        val body = json.decodeFromString<WorkoutListResponse>(response.bodyAsText())
        assertEquals(2, body.workouts.size)
        assertTrue(body.workouts.any { it.id == "w-1" })
        assertTrue(body.workouts.any { it.id == "w-2" })
    }

    @Test
    fun `GET workouts pagination limit`() = testApplication {
        val workoutRepo = FakeServerWorkoutRepository()
        workoutRepo.seed(
            testServerWorkout(id = "w-1", startedAt = 3000L),
            testServerWorkout(id = "w-2", startedAt = 2000L),
            testServerWorkout(id = "w-3", startedAt = 1000L),
        )
        installTestApp(FakeServerUserRepository(), workoutRepo, FakeServerExerciseRepository())
        val client = jsonClient()

        val response = client.get("/workouts?limit=2") {
            header(HttpHeaders.Authorization, "Bearer ${testToken()}")
        }

        assertEquals(HttpStatusCode.OK, response.status)
        val body = json.decodeFromString<WorkoutListResponse>(response.bodyAsText())
        assertEquals(2, body.workouts.size)
        assertNotNull(body.nextCursor)
    }

    @Test
    fun `GET workouts unauthenticated`() = testApplication {
        installTestApp(FakeServerUserRepository(), FakeServerWorkoutRepository(), FakeServerExerciseRepository())
        val client = jsonClient()

        val response = client.get("/workouts")

        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

    // ── POST /workouts ────────────────────────────────────────────────────────

    @Test
    fun `POST workouts creates new workout`() = testApplication {
        val workoutRepo = FakeServerWorkoutRepository()
        installTestApp(FakeServerUserRepository(), workoutRepo, FakeServerExerciseRepository())
        val client = jsonClient()

        val newWorkout = testServerWorkout(id = "w-new")
        val response = client.post("/workouts") {
            header(HttpHeaders.Authorization, "Bearer ${testToken()}")
            header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            setBody(json.encodeToString(Workout.serializer(), newWorkout))
        }

        assertEquals(HttpStatusCode.Created, response.status)
        val saved = json.decodeFromString<Workout>(response.bodyAsText())
        assertEquals("w-new", saved.id)
        assertTrue(workoutRepo.saveCalled)
    }

    @Test
    fun `POST workouts idempotent if already exists`() = testApplication {
        val workoutRepo = FakeServerWorkoutRepository()
        val existing = testServerWorkout(id = "w-1")
        workoutRepo.seed(existing)
        installTestApp(FakeServerUserRepository(), workoutRepo, FakeServerExerciseRepository())
        val client = jsonClient()

        val response = client.post("/workouts") {
            header(HttpHeaders.Authorization, "Bearer ${testToken()}")
            header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            setBody(json.encodeToString(Workout.serializer(), existing))
        }

        assertEquals(HttpStatusCode.OK, response.status)
        val returned = json.decodeFromString<Workout>(response.bodyAsText())
        assertEquals("w-1", returned.id)
    }

    // ── GET /workouts/{id} ────────────────────────────────────────────────────

    @Test
    fun `GET workout by id found`() = testApplication {
        val workoutRepo = FakeServerWorkoutRepository()
        workoutRepo.seed(testServerWorkout(id = "w-1"))
        installTestApp(FakeServerUserRepository(), workoutRepo, FakeServerExerciseRepository())
        val client = jsonClient()

        val response = client.get("/workouts/w-1") {
            header(HttpHeaders.Authorization, "Bearer ${testToken()}")
        }

        assertEquals(HttpStatusCode.OK, response.status)
        val body = json.decodeFromString<Workout>(response.bodyAsText())
        assertEquals("w-1", body.id)
    }

    @Test
    fun `GET workout by id not found`() = testApplication {
        installTestApp(FakeServerUserRepository(), FakeServerWorkoutRepository(), FakeServerExerciseRepository())
        val client = jsonClient()

        val response = client.get("/workouts/missing") {
            header(HttpHeaders.Authorization, "Bearer ${testToken()}")
        }

        assertEquals(HttpStatusCode.NotFound, response.status)
    }

    // ── PATCH /workouts/{id} ──────────────────────────────────────────────────

    @Test
    fun `PATCH workout updates title`() = testApplication {
        val workoutRepo = FakeServerWorkoutRepository()
        workoutRepo.seed(testServerWorkout(id = "w-1", title = "Old Title"))
        installTestApp(FakeServerUserRepository(), workoutRepo, FakeServerExerciseRepository())
        val client = jsonClient()

        val response = client.patch("/workouts/w-1") {
            header(HttpHeaders.Authorization, "Bearer ${testToken()}")
            header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            setBody("""{"title":"New Title"}""")
        }

        assertEquals(HttpStatusCode.OK, response.status)
        val body = json.decodeFromString<Workout>(response.bodyAsText())
        assertEquals("New Title", body.title)
    }

    @Test
    fun `PATCH workout null title returns existing`() = testApplication {
        val workoutRepo = FakeServerWorkoutRepository()
        workoutRepo.seed(testServerWorkout(id = "w-1", title = "Morning Session"))
        installTestApp(FakeServerUserRepository(), workoutRepo, FakeServerExerciseRepository())
        val client = jsonClient()

        val response = client.patch("/workouts/w-1") {
            header(HttpHeaders.Authorization, "Bearer ${testToken()}")
            header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            setBody("""{}""")
        }

        assertEquals(HttpStatusCode.OK, response.status)
        val body = json.decodeFromString<Workout>(response.bodyAsText())
        assertEquals("Morning Session", body.title)
    }

    @Test
    fun `PATCH workout not found`() = testApplication {
        installTestApp(FakeServerUserRepository(), FakeServerWorkoutRepository(), FakeServerExerciseRepository())
        val client = jsonClient()

        val response = client.patch("/workouts/missing") {
            header(HttpHeaders.Authorization, "Bearer ${testToken()}")
            header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            setBody("""{"title":"X"}""")
        }

        assertEquals(HttpStatusCode.NotFound, response.status)
    }

    // ── DELETE /workouts/{id} ─────────────────────────────────────────────────

    @Test
    fun `DELETE workout success`() = testApplication {
        val workoutRepo = FakeServerWorkoutRepository()
        workoutRepo.seed(testServerWorkout(id = "w-1"))
        installTestApp(FakeServerUserRepository(), workoutRepo, FakeServerExerciseRepository())
        val client = jsonClient()

        val response = client.delete("/workouts/w-1") {
            header(HttpHeaders.Authorization, "Bearer ${testToken()}")
        }

        assertEquals(HttpStatusCode.NoContent, response.status)
    }

    @Test
    fun `DELETE workout not found`() = testApplication {
        installTestApp(FakeServerUserRepository(), FakeServerWorkoutRepository(), FakeServerExerciseRepository())
        val client = jsonClient()

        val response = client.delete("/workouts/missing") {
            header(HttpHeaders.Authorization, "Bearer ${testToken()}")
        }

        assertEquals(HttpStatusCode.NotFound, response.status)
    }
}
