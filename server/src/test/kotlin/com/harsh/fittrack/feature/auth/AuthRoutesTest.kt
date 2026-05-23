package com.harsh.fittrack.feature.auth

import com.harsh.fittrack.domain.model.AuthResponse
import com.harsh.fittrack.domain.model.LoginRequest
import com.harsh.fittrack.domain.model.RegisterRequest
import com.harsh.fittrack.fakes.FakeServerUserRepository
import com.harsh.fittrack.fakes.testServerUser
import com.harsh.fittrack.installTestApp
import com.harsh.fittrack.jsonClient
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.server.testing.testApplication
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.mindrot.jbcrypt.BCrypt
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class AuthRoutesTest {

    private val json = Json { ignoreUnknownKeys = true }

    // ── Register ──────────────────────────────────────────────────────────────

    @Test
    fun `register success`() = testApplication {
        val fakeUserRepo = FakeServerUserRepository()
        installTestApp(userRepo = fakeUserRepo)
        val client = jsonClient()

        val response = client.post("/auth/register") {
            contentType(ContentType.Application.Json)
            setBody(Json.encodeToString(RegisterRequest(
                name = "Alice",
                email = "alice@example.com",
                password = "password123",
            )))
        }

        assertEquals(HttpStatusCode.Created, response.status)
        val body = json.decodeFromString<AuthResponse>(response.bodyAsText())
        assertNotNull(body.token)
        assertTrue(body.token.isNotBlank())
        assertEquals("alice@example.com", body.user.email)
        assertEquals("Alice", body.user.name)
        assertTrue(fakeUserRepo.createCalled)
    }

    @Test
    fun `register blank name returns 400`() = testApplication {
        installTestApp(userRepo = FakeServerUserRepository())
        val client = jsonClient()

        val response = client.post("/auth/register") {
            contentType(ContentType.Application.Json)
            setBody(Json.encodeToString(RegisterRequest(
                name = "",
                email = "alice@example.com",
                password = "password123",
            )))
        }

        assertEquals(HttpStatusCode.BadRequest, response.status)
    }

    @Test
    fun `register blank email returns 400`() = testApplication {
        installTestApp(userRepo = FakeServerUserRepository())
        val client = jsonClient()

        val response = client.post("/auth/register") {
            contentType(ContentType.Application.Json)
            setBody(Json.encodeToString(RegisterRequest(
                name = "Alice",
                email = "",
                password = "password123",
            )))
        }

        assertEquals(HttpStatusCode.BadRequest, response.status)
    }

    @Test
    fun `register short password returns 400`() = testApplication {
        installTestApp(userRepo = FakeServerUserRepository())
        val client = jsonClient()

        val response = client.post("/auth/register") {
            contentType(ContentType.Application.Json)
            setBody(Json.encodeToString(RegisterRequest(
                name = "Alice",
                email = "alice@example.com",
                password = "short1",  // 6 chars — less than 8
            )))
        }

        assertEquals(HttpStatusCode.BadRequest, response.status)
    }

    @Test
    fun `register email already exists returns 400`() = testApplication {
        val fakeUserRepo = FakeServerUserRepository()
        fakeUserRepo.seed(testServerUser)
        installTestApp(userRepo = fakeUserRepo)
        val client = jsonClient()

        val response = client.post("/auth/register") {
            contentType(ContentType.Application.Json)
            setBody(Json.encodeToString(RegisterRequest(
                name = "Jane",
                email = testServerUser.email,  // same email as seeded user
                password = "password123",
            )))
        }

        assertEquals(HttpStatusCode.BadRequest, response.status)
    }

    // ── Login ─────────────────────────────────────────────────────────────────

    @Test
    fun `login success`() = testApplication {
        val fakeUserRepo = FakeServerUserRepository()
        val hash = BCrypt.hashpw("correctpassword", BCrypt.gensalt())
        fakeUserRepo.seed(testServerUser, passwordHash = hash)
        installTestApp(userRepo = fakeUserRepo)
        val client = jsonClient()

        val response = client.post("/auth/login") {
            contentType(ContentType.Application.Json)
            setBody(Json.encodeToString(LoginRequest(
                email = testServerUser.email,
                password = "correctpassword",
            )))
        }

        assertEquals(HttpStatusCode.OK, response.status)
        val body = json.decodeFromString<AuthResponse>(response.bodyAsText())
        assertNotNull(body.token)
        assertTrue(body.token.isNotBlank())
        assertEquals(testServerUser.email, body.user.email)
    }

    @Test
    fun `login blank email returns 400`() = testApplication {
        installTestApp(userRepo = FakeServerUserRepository())
        val client = jsonClient()

        val response = client.post("/auth/login") {
            contentType(ContentType.Application.Json)
            setBody(Json.encodeToString(LoginRequest(email = "", password = "password123")))
        }

        assertEquals(HttpStatusCode.BadRequest, response.status)
    }

    @Test
    fun `login blank password returns 400`() = testApplication {
        installTestApp(userRepo = FakeServerUserRepository())
        val client = jsonClient()

        val response = client.post("/auth/login") {
            contentType(ContentType.Application.Json)
            setBody(Json.encodeToString(LoginRequest(email = "alice@example.com", password = "")))
        }

        assertEquals(HttpStatusCode.BadRequest, response.status)
    }

    @Test
    fun `login wrong password returns 401`() = testApplication {
        val fakeUserRepo = FakeServerUserRepository()
        val hash = BCrypt.hashpw("correctpassword", BCrypt.gensalt())
        fakeUserRepo.seed(testServerUser, passwordHash = hash)
        installTestApp(userRepo = fakeUserRepo)
        val client = jsonClient()

        val response = client.post("/auth/login") {
            contentType(ContentType.Application.Json)
            setBody(Json.encodeToString(LoginRequest(
                email = testServerUser.email,
                password = "wrongpassword",
            )))
        }

        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

    @Test
    fun `login unknown email returns 401`() = testApplication {
        installTestApp(userRepo = FakeServerUserRepository())
        val client = jsonClient()

        val response = client.post("/auth/login") {
            contentType(ContentType.Application.Json)
            setBody(Json.encodeToString(LoginRequest(
                email = "nobody@example.com",
                password = "password123",
            )))
        }

        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }
}
