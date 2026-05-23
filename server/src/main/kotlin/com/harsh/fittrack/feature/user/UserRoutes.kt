package com.harsh.fittrack.feature.user

import com.harsh.fittrack.domain.model.PatchUserRequest
import com.harsh.fittrack.domain.repository.UserRepository
import com.harsh.fittrack.plugins.BadRequestException
import com.harsh.fittrack.plugins.NotFoundException
import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.patch
import io.ktor.server.routing.route
import org.koin.ktor.ext.inject

fun Route.userRoutes() {
    val userRepo: UserRepository by inject()

    authenticate("jwt") {
        route("/me") {
            get {
                val userId = call.principal<JWTPrincipal>()!!.payload.getClaim("userId").asString()
                val user = userRepo.findById(userId)
                    ?: throw NotFoundException("User $userId not found.")
                call.respond(user)
            }

            patch {
                val userId = call.principal<JWTPrincipal>()!!.payload.getClaim("userId").asString()
                val body = call.receive<PatchUserRequest>()
                val units = body.units
                if (units != null && units !in setOf("KG", "LBS")) {
                    throw BadRequestException("units must be 'KG' or 'LBS'.")
                }
                val updated = if (units != null) {
                    userRepo.updateUnits(userId, units)
                        ?: throw NotFoundException("User $userId not found.")
                } else {
                    userRepo.findById(userId)
                        ?: throw NotFoundException("User $userId not found.")
                }
                call.respond(updated)
            }
        }
    }
}
