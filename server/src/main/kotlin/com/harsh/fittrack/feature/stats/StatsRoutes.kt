package com.harsh.fittrack.feature.stats

import io.ktor.server.auth.authenticate
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.route
import org.koin.ktor.ext.inject

fun Route.statsRoutes() {
    val statsService: StatsService by inject()

    authenticate("jwt") {
        route("/stats") {
            get("/volume") {
                val userId = call.principal<JWTPrincipal>()!!.payload.getClaim("userId").asString()
                val period = call.request.queryParameters["period"] ?: "8w"
                val weeks = period.removeSuffix("w").toIntOrNull()?.coerceIn(1, 52) ?: 8
                call.respond(statsService.weeklyVolume(userId, weeks))
            }

            get("/exercise/{id}/progression") {
                val userId = call.principal<JWTPrincipal>()!!.payload.getClaim("userId").asString()
                val exerciseId = call.parameters["id"]!!
                call.respond(statsService.exerciseProgression(userId, exerciseId))
            }

            get("/muscle-frequency") {
                val userId = call.principal<JWTPrincipal>()!!.payload.getClaim("userId").asString()
                val period = call.request.queryParameters["period"] ?: "30d"
                val days = period.removeSuffix("d").toIntOrNull()?.coerceIn(1, 365) ?: 30
                call.respond(statsService.muscleFrequency(userId, days))
            }
        }
    }
}
