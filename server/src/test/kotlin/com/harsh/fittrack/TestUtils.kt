package com.harsh.fittrack

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.harsh.fittrack.domain.repository.ExerciseRepository
import com.harsh.fittrack.domain.repository.UserRepository
import com.harsh.fittrack.domain.repository.WorkoutRepository
import com.harsh.fittrack.fakes.FakeServerExerciseRepository
import com.harsh.fittrack.fakes.FakeServerUserRepository
import com.harsh.fittrack.fakes.FakeServerWorkoutRepository
import com.harsh.fittrack.plugins.configureSerialization
import com.harsh.fittrack.plugins.configureStatusPages
import com.harsh.fittrack.plugins.generateToken
import com.harsh.fittrack.feature.auth.authRoutes
import com.harsh.fittrack.feature.exercise.exerciseRoutes
import com.harsh.fittrack.feature.sync.syncRoutes
import com.harsh.fittrack.feature.user.userRoutes
import com.harsh.fittrack.feature.workout.workoutRoutes
import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.install
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.jwt.jwt
import io.ktor.server.routing.routing
import io.ktor.server.testing.ApplicationTestBuilder
import kotlinx.serialization.json.Json
import org.koin.dsl.module
import org.koin.ktor.plugin.Koin

const val TEST_JWT_SECRET = "test-secret-for-unit-tests"

fun testToken(
    userId: String = "user-1",
    email: String = "test@example.com",
): String = generateToken(userId, email, TEST_JWT_SECRET)

fun ApplicationTestBuilder.installTestApp(
    userRepo: UserRepository = FakeServerUserRepository(),
    workoutRepo: WorkoutRepository = FakeServerWorkoutRepository(),
    exerciseRepo: ExerciseRepository = FakeServerExerciseRepository(),
) {
    application {
        install(Koin) {
            modules(module {
                single<UserRepository> { userRepo }
                single<WorkoutRepository> { workoutRepo }
                single<ExerciseRepository> { exerciseRepo }
            })
        }

        // Configure JWT auth using the test secret instead of config/env
        val algorithm = Algorithm.HMAC256(TEST_JWT_SECRET)
        val verifier = JWT.require(algorithm).build()
        install(Authentication) {
            jwt("jwt") {
                realm = "FitTrack API"
                verifier(verifier)
                validate { credential ->
                    val uid = credential.payload.getClaim("userId").asString()
                    val em = credential.payload.getClaim("email").asString()
                    if (!uid.isNullOrBlank() && !em.isNullOrBlank()) JWTPrincipal(credential.payload) else null
                }
            }
        }

        configureSerialization()
        configureStatusPages()

        routing {
            authRoutes(TEST_JWT_SECRET)
            userRoutes()
            exerciseRoutes()
            workoutRoutes()
            syncRoutes()
        }
    }
}

fun ApplicationTestBuilder.jsonClient(): HttpClient = createClient {
    install(ContentNegotiation) {
        json(Json { ignoreUnknownKeys = true })
    }
}
