package com.harsh.fittrack.feature.sync

import com.harsh.fittrack.fakes.FakeServerExerciseRepository
import com.harsh.fittrack.fakes.FakeServerUserRepository
import com.harsh.fittrack.fakes.FakeServerWorkoutRepository
import com.harsh.fittrack.fakes.testServerWorkout
import com.harsh.fittrack.domain.model.SyncStatusResponse
import com.harsh.fittrack.installTestApp
import com.harsh.fittrack.jsonClient
import com.harsh.fittrack.testToken
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.testApplication
import kotlinx.serialization.json.Json
import java.util.Base64
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

private val json = Json { ignoreUnknownKeys = true }

class SyncRoutesTest {

    @Test
    fun `GET sync status with workouts`() = testApplication {
        val startedAt = 1_716_000_000_000L
        val workoutRepo = FakeServerWorkoutRepository()
        workoutRepo.seed(testServerWorkout(id = "w-1", startedAt = startedAt))
        val exerciseRepo = FakeServerExerciseRepository()
        exerciseRepo.latestVersionValue = 3
        installTestApp(FakeServerUserRepository(), workoutRepo, exerciseRepo)
        val client = jsonClient()

        val response = client.get("/sync/status") {
            header(HttpHeaders.Authorization, "Bearer ${testToken()}")
        }

        assertEquals(HttpStatusCode.OK, response.status)
        val body = json.decodeFromString<SyncStatusResponse>(response.bodyAsText())
        assertNotNull(body.workoutCursor)
        assertEquals(3, body.catalogVersion)

        // Decode the cursor and verify it encodes the startedAt value
        val decodedCursor = String(Base64.getDecoder().decode(body.workoutCursor)).toLong()
        assertEquals(startedAt, decodedCursor)
    }

    @Test
    fun `GET sync status no workouts`() = testApplication {
        val exerciseRepo = FakeServerExerciseRepository()
        exerciseRepo.latestVersionValue = 0
        installTestApp(FakeServerUserRepository(), FakeServerWorkoutRepository(), exerciseRepo)
        val client = jsonClient()

        val response = client.get("/sync/status") {
            header(HttpHeaders.Authorization, "Bearer ${testToken()}")
        }

        assertEquals(HttpStatusCode.OK, response.status)
        val body = json.decodeFromString<SyncStatusResponse>(response.bodyAsText())
        assertNull(body.workoutCursor)
    }

    @Test
    fun `GET sync status unauthenticated`() = testApplication {
        installTestApp(FakeServerUserRepository(), FakeServerWorkoutRepository(), FakeServerExerciseRepository())
        val client = jsonClient()

        val response = client.get("/sync/status")

        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }
}
