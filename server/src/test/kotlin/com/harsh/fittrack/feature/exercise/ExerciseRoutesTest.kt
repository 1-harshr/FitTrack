package com.harsh.fittrack.feature.exercise

import com.harsh.fittrack.fakes.FakeServerExerciseRepository
import com.harsh.fittrack.fakes.FakeServerUserRepository
import com.harsh.fittrack.fakes.FakeServerWorkoutRepository
import com.harsh.fittrack.fakes.testServerExercise
import com.harsh.fittrack.domain.model.ExerciseSyncResponse
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
import kotlin.test.Test
import kotlin.test.assertEquals

private val json = Json { ignoreUnknownKeys = true }

class ExerciseRoutesTest {

    @Test
    fun `GET exercises returns all since version 0`() = testApplication {
        val exerciseRepo = FakeServerExerciseRepository()
        exerciseRepo.exercises.addAll(
            listOf(
                testServerExercise(id = "e-1", name = "Squat", catalogVersion = 1),
                testServerExercise(id = "e-2", name = "Bench Press", catalogVersion = 2),
                testServerExercise(id = "e-3", name = "Deadlift", catalogVersion = 3),
            )
        )
        exerciseRepo.latestVersionValue = 3
        installTestApp(FakeServerUserRepository(), FakeServerWorkoutRepository(), exerciseRepo)
        val client = jsonClient()

        val response = client.get("/exercises") {
            header(HttpHeaders.Authorization, "Bearer ${testToken()}")
        }

        assertEquals(HttpStatusCode.OK, response.status)
        val body = json.decodeFromString<ExerciseSyncResponse>(response.bodyAsText())
        assertEquals(3, body.exercises.size)
        assertEquals(3, body.latestVersion)
    }

    @Test
    fun `GET exercises filters by sinceVersion`() = testApplication {
        val exerciseRepo = FakeServerExerciseRepository()
        exerciseRepo.exercises.addAll(
            listOf(
                testServerExercise(id = "e-1", name = "Squat", catalogVersion = 1),
                testServerExercise(id = "e-2", name = "Bench Press", catalogVersion = 2),
                testServerExercise(id = "e-3", name = "Deadlift", catalogVersion = 3),
            )
        )
        exerciseRepo.latestVersionValue = 3
        installTestApp(FakeServerUserRepository(), FakeServerWorkoutRepository(), exerciseRepo)
        val client = jsonClient()

        val response = client.get("/exercises?sinceVersion=1") {
            header(HttpHeaders.Authorization, "Bearer ${testToken()}")
        }

        assertEquals(HttpStatusCode.OK, response.status)
        val body = json.decodeFromString<ExerciseSyncResponse>(response.bodyAsText())
        assertEquals(2, body.exercises.size)
        assertEquals(3, body.latestVersion)
        assertEquals(setOf(2, 3), body.exercises.map { it.catalogVersion }.toSet())
    }

    @Test
    fun `GET exercises unauthenticated`() = testApplication {
        installTestApp(FakeServerUserRepository(), FakeServerWorkoutRepository(), FakeServerExerciseRepository())
        val client = jsonClient()

        val response = client.get("/exercises")

        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }
}
