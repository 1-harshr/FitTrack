package com.harsh.fittrack.feature.template

import com.harsh.fittrack.domain.model.CreateTemplateRequest
import com.harsh.fittrack.plugins.NotFoundException
import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import org.koin.ktor.ext.inject

fun Route.templateRoutes() {
    val templateService: TemplateService by inject()

    authenticate("jwt") {
        route("/templates") {
            get {
                val userId = call.principal<JWTPrincipal>()!!.payload.getClaim("userId").asString()
                call.respond(templateService.listForUser(userId))
            }

            post {
                val userId = call.principal<JWTPrincipal>()!!.payload.getClaim("userId").asString()
                val body = call.receive<CreateTemplateRequest>()
                val created = templateService.create(userId, body)
                call.respond(HttpStatusCode.Created, created)
            }

            route("/{id}") {
                delete {
                    val userId = call.principal<JWTPrincipal>()!!.payload.getClaim("userId").asString()
                    val id = call.parameters["id"]!!
                    val deleted = templateService.delete(userId, id)
                    if (!deleted) throw NotFoundException("Template $id not found.")
                    call.respond(HttpStatusCode.NoContent)
                }
            }
        }
    }
}
