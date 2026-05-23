package com.harsh.fittrack.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

class FitTrackApiImpl(
    private val tokenStore: TokenStore,
    private val baseUrl: String = "http://localhost:8080",
) : FitTrackApi {

    private val client = HttpClient {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true; encodeDefaults = true })
        }
        expectSuccess = false
    }

    private fun token(): String? = tokenStore.token

    override suspend fun login(email: String, password: String): ApiAuthResponse? {
        val r = client.post("$baseUrl/auth/login") {
            contentType(ContentType.Application.Json)
            setBody(ApiLoginRequest(email = email, password = password))
        }
        if (r.status.isSuccess()) return r.body()
        val body = r.bodyAsText()
        val detail = runCatching {
            Json.parseToJsonElement(body).jsonObject["detail"]?.jsonPrimitive?.content
        }.getOrNull()
        error(detail ?: "Login failed (${r.status.value})")
    }

    override suspend fun register(name: String, email: String, password: String): ApiAuthResponse? {
        val r = client.post("$baseUrl/auth/register") {
            contentType(ContentType.Application.Json)
            setBody(ApiRegisterRequest(name = name, email = email, password = password))
        }
        if (r.status.isSuccess()) return r.body()
        val body = r.bodyAsText()
        val detail = runCatching {
            Json.parseToJsonElement(body).jsonObject["detail"]?.jsonPrimitive?.content
        }.getOrNull()
        error(detail ?: "Registration failed (${r.status.value})")
    }

    override suspend fun getMe(): ApiUser? = runCatching {
        val t = token() ?: return null
        val r = client.get("$baseUrl/me") { header(HttpHeaders.Authorization, "Bearer $t") }
        if (r.status.isSuccess()) r.body() else null
    }.getOrNull()

    override suspend fun patchMe(units: String): ApiUser? = runCatching {
        val t = token() ?: return null
        val r = client.patch("$baseUrl/me") {
            header(HttpHeaders.Authorization, "Bearer $t")
            contentType(ContentType.Application.Json)
            setBody(ApiPatchUserRequest(units = units))
        }
        if (r.status.isSuccess()) r.body() else null
    }.getOrNull()

    override suspend fun getExercises(sinceVersion: Int): ApiExerciseSyncResponse? = runCatching {
        val t = token() ?: return null
        val r = client.get("$baseUrl/exercises?sinceVersion=$sinceVersion") {
            header(HttpHeaders.Authorization, "Bearer $t")
        }
        if (r.status.isSuccess()) r.body() else null
    }.getOrNull()

    override suspend fun getWorkouts(cursor: String?, limit: Int): ApiWorkoutListResponse? = runCatching {
        val t = token() ?: return null
        val query = buildString {
            append("?limit=$limit")
            if (cursor != null) append("&cursor=$cursor")
        }
        val r = client.get("$baseUrl/workouts$query") {
            header(HttpHeaders.Authorization, "Bearer $t")
        }
        if (r.status.isSuccess()) r.body() else null
    }.getOrNull()

    override suspend fun postWorkout(workout: ApiWorkout): ApiWorkout? = runCatching {
        val t = token() ?: return null
        val r = client.post("$baseUrl/workouts") {
            header(HttpHeaders.Authorization, "Bearer $t")
            contentType(ContentType.Application.Json)
            setBody(workout)
        }
        if (r.status.isSuccess()) r.body() else null
    }.getOrNull()

    override suspend fun patchWorkout(id: String, title: String): ApiWorkout? = runCatching {
        val t = token() ?: return null
        val r = client.patch("$baseUrl/workouts/$id") {
            header(HttpHeaders.Authorization, "Bearer $t")
            contentType(ContentType.Application.Json)
            setBody(ApiPatchWorkoutRequest(title = title))
        }
        if (r.status.isSuccess()) r.body() else null
    }.getOrNull()

    override suspend fun deleteWorkout(id: String) {
        val t = token() ?: return
        runCatching {
            client.delete("$baseUrl/workouts/$id") {
                header(HttpHeaders.Authorization, "Bearer $t")
            }
        }
    }

    override suspend fun getSyncStatus(): ApiSyncStatusResponse? = runCatching {
        val t = token() ?: return null
        val r = client.get("$baseUrl/sync/status") {
            header(HttpHeaders.Authorization, "Bearer $t")
        }
        if (r.status.isSuccess()) r.body() else null
    }.getOrNull()
}
