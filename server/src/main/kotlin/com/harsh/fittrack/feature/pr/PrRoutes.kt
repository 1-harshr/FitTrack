package com.harsh.fittrack.feature.pr

import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.route
import org.koin.ktor.ext.inject

fun Route.prRoutes() {
    val prService: PrService by inject()

    authenticate("jwt") {
        route("/exercises/{id}/pr") {
            get {
                val userId = call.principal<JWTPrincipal>()!!.payload.getClaim("userId").asString()
                val exerciseId = call.parameters["id"]!!
                val pr = prService.getPrForExercise(userId, exerciseId)
                if (pr != null) call.respond(pr)
                else call.respond(HttpStatusCode.NotFound)
            }
        }
    }
}
