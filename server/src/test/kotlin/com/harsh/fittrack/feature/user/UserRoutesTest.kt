package com.harsh.fittrack.feature.user

import com.harsh.fittrack.domain.model.PatchUserRequest
import com.harsh.fittrack.domain.model.User
import com.harsh.fittrack.fakes.FakeServerUserRepository
import com.harsh.fittrack.fakes.testServerUser
import com.harsh.fittrack.installTestApp
import com.harsh.fittrack.jsonClient
import com.harsh.fittrack.testToken
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.patch
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.server.testing.testApplication
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals

class UserRoutesTest {

    private val json = Json { ignoreUnknownKeys = true }

    // ── GET /me ───────────────────────────────────────────────────────────────

    @Test
    fun `GET me success`() = testApplication {
        val fakeUserRepo = FakeServerUserRepository()
        fakeUserRepo.seed(testServerUser)
        installTestApp(userRepo = fakeUserRepo)
        val client = jsonClient()

        val response = client.get("/me") {
            header(HttpHeaders.Authorization, "Bearer ${testToken(userId = testServerUser.id, email = testServerUser.email)}")
        }

        assertEquals(HttpStatusCode.OK, response.status)
        val body = json.decodeFromString<User>(response.bodyAsText())
        assertEquals(testServerUser.id, body.id)
        assertEquals(testServerUser.email, body.email)
        assertEquals(testServerUser.name, body.name)
    }

    @Test
    fun `GET me unauthenticated returns 401`() = testApplication {
        installTestApp(userRepo = FakeServerUserRepository())
        val client = jsonClient()

        val response = client.get("/me")

        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

    @Test
    fun `GET me not found returns 404`() = testApplication {
        // Repo is empty — no user seeded for the token's userId
        installTestApp(userRepo = FakeServerUserRepository())
        val client = jsonClient()

        val response = client.get("/me") {
            header(HttpHeaders.Authorization, "Bearer ${testToken(userId = "user-1", email = "test@example.com")}")
        }

        assertEquals(HttpStatusCode.NotFound, response.status)
    }

    // ── PATCH /me ─────────────────────────────────────────────────────────────

    @Test
    fun `PATCH me update units to KG`() = testApplication {
        val fakeUserRepo = FakeServerUserRepository()
        val userWithLbs = testServerUser.copy(units = "LBS")
        fakeUserRepo.seed(userWithLbs)
        installTestApp(userRepo = fakeUserRepo)
        val client = jsonClient()

        val response = client.patch("/me") {
            header(HttpHeaders.Authorization, "Bearer ${testToken(userId = testServerUser.id, email = testServerUser.email)}")
            contentType(ContentType.Application.Json)
            setBody(Json.encodeToString(PatchUserRequest(units = "KG")))
        }

        assertEquals(HttpStatusCode.OK, response.status)
        val body = json.decodeFromString<User>(response.bodyAsText())
        assertEquals("KG", body.units)
    }

    @Test
    fun `PATCH me update units to LBS`() = testApplication {
        val fakeUserRepo = FakeServerUserRepository()
        fakeUserRepo.seed(testServerUser)  // starts with KG
        installTestApp(userRepo = fakeUserRepo)
        val client = jsonClient()

        val response = client.patch("/me") {
            header(HttpHeaders.Authorization, "Bearer ${testToken(userId = testServerUser.id, email = testServerUser.email)}")
            contentType(ContentType.Application.Json)
            setBody(Json.encodeToString(PatchUserRequest(units = "LBS")))
        }

        assertEquals(HttpStatusCode.OK, response.status)
        val body = json.decodeFromString<User>(response.bodyAsText())
        assertEquals("LBS", body.units)
    }

    @Test
    fun `PATCH me invalid units returns 400`() = testApplication {
        val fakeUserRepo = FakeServerUserRepository()
        fakeUserRepo.seed(testServerUser)
        installTestApp(userRepo = fakeUserRepo)
        val client = jsonClient()

        val response = client.patch("/me") {
            header(HttpHeaders.Authorization, "Bearer ${testToken(userId = testServerUser.id, email = testServerUser.email)}")
            contentType(ContentType.Application.Json)
            setBody(Json.encodeToString(PatchUserRequest(units = "POUNDS")))
        }

        assertEquals(HttpStatusCode.BadRequest, response.status)
    }

    @Test
    fun `PATCH me null units returns current user`() = testApplication {
        val fakeUserRepo = FakeServerUserRepository()
        fakeUserRepo.seed(testServerUser)
        installTestApp(userRepo = fakeUserRepo)
        val client = jsonClient()

        val response = client.patch("/me") {
            header(HttpHeaders.Authorization, "Bearer ${testToken(userId = testServerUser.id, email = testServerUser.email)}")
            contentType(ContentType.Application.Json)
            setBody(Json.encodeToString(PatchUserRequest(units = null)))
        }

        assertEquals(HttpStatusCode.OK, response.status)
        val body = json.decodeFromString<User>(response.bodyAsText())
        assertEquals(testServerUser.id, body.id)
        assertEquals(testServerUser.units, body.units)
    }

    @Test
    fun `PATCH me unauthenticated returns 401`() = testApplication {
        installTestApp(userRepo = FakeServerUserRepository())
        val client = jsonClient()

        val response = client.patch("/me") {
            contentType(ContentType.Application.Json)
            setBody(Json.encodeToString(PatchUserRequest(units = "KG")))
        }

        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }
}
