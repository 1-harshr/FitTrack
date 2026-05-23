package com.harsh.fittrack.feature.auth

import com.harsh.fittrack.domain.model.AuthResponse
import com.harsh.fittrack.domain.model.LoginRequest
import com.harsh.fittrack.domain.model.RegisterRequest
import com.harsh.fittrack.domain.repository.UserRepository
import com.harsh.fittrack.plugins.BadRequestException
import com.harsh.fittrack.plugins.generateToken
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import org.koin.ktor.ext.inject
import org.mindrot.jbcrypt.BCrypt
import java.util.UUID

fun Route.authRoutes(jwtSecret: String) {
    val userRepo: UserRepository by inject()

    route("/auth") {
        post("/register") {
            val body = call.receive<RegisterRequest>()
            if (body.name.isBlank()) throw BadRequestException("name is required.")
            if (body.email.isBlank()) throw BadRequestException("email is required.")
            if (body.password.length < 8) throw BadRequestException("password must be at least 8 characters.")

            if (userRepo.findByEmail(body.email.lowercase()) != null) {
                throw BadRequestException("An account with that email already exists.")
            }

            val hash = BCrypt.hashpw(body.password, BCrypt.gensalt())
            val user = userRepo.create(
                id = UUID.randomUUID().toString(),
                name = body.name.trim(),
                email = body.email.lowercase().trim(),
                passwordHash = hash,
            )

            val token = generateToken(user.id, user.email, jwtSecret)
            call.respond(HttpStatusCode.Created, AuthResponse(token = token, user = user))
        }

        post("/login") {
            val body = call.receive<LoginRequest>()
            if (body.email.isBlank()) throw BadRequestException("email is required.")
            if (body.password.isBlank()) throw BadRequestException("password is required.")

            val storedHash = userRepo.findPasswordHash(body.email.lowercase())
            if (storedHash == null || !BCrypt.checkpw(body.password, storedHash)) {
                call.respond(HttpStatusCode.Unauthorized, mapOf("message" to "Invalid email or password."))
                return@post
            }

            val user = userRepo.findByEmail(body.email.lowercase())!!
            val token = generateToken(user.id, user.email, jwtSecret)
            call.respond(AuthResponse(token = token, user = user))
        }
    }
}
