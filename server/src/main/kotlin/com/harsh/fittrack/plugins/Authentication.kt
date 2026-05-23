package com.harsh.fittrack.plugins

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.jwt.jwt
import java.util.Date

private const val THIRTY_DAYS_MS = 30L * 24 * 60 * 60 * 1000

fun Application.configureAuthentication() {
    val secret = jwtSecret()
    val algorithm = Algorithm.HMAC256(secret)
    val verifier = JWT.require(algorithm).build()

    install(Authentication) {
        jwt("jwt") {
            realm = "FitTrack API"
            verifier(verifier)
            validate { credential ->
                val userId = credential.payload.getClaim("userId").asString()
                val email = credential.payload.getClaim("email").asString()
                if (!userId.isNullOrBlank() && !email.isNullOrBlank()) JWTPrincipal(credential.payload) else null
            }
        }
    }
}

fun Application.jwtSecret(): String =
    environment.config.propertyOrNull("jwt.secret")?.getString()
        ?: System.getenv("JWT_SECRET")
        ?: "dev-secret-change-in-production"

fun generateToken(userId: String, email: String, secret: String): String =
    JWT.create()
        .withClaim("userId", userId)
        .withClaim("email", email)
        .withExpiresAt(Date(System.currentTimeMillis() + THIRTY_DAYS_MS))
        .sign(Algorithm.HMAC256(secret))
