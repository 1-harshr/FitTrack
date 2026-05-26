package com.harsh.fittrack.feature.ai

import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import org.koin.ktor.ext.inject

fun Route.aiRoutes() {
    val aiService: AiCoachingService by inject()

    authenticate("jwt") {
        get("/ai/coaching-insight") {
            val userId = call.principal<JWTPrincipal>()
                ?.payload?.getClaim("userId")?.asString()
                ?: return@get call.respond(HttpStatusCode.Unauthorized)
            call.respond(aiService.getInsight(userId))
        }
    }
}
