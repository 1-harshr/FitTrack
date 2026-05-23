package com.harsh.fittrack.feature.workout

import com.harsh.fittrack.domain.model.PatchWorkoutRequest
import com.harsh.fittrack.domain.model.Workout
import com.harsh.fittrack.domain.model.WorkoutListResponse
import com.harsh.fittrack.domain.repository.WorkoutRepository
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
import io.ktor.server.routing.patch
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import org.koin.ktor.ext.inject
import java.util.Base64

fun Route.workoutRoutes() {
    val workoutRepo: WorkoutRepository by inject()

    authenticate("jwt") {
        route("/workouts") {
            get {
                val userId = call.principal<JWTPrincipal>()!!.payload.getClaim("userId").asString()
                val limit = call.request.queryParameters["limit"]?.toIntOrNull()?.coerceIn(1, 50) ?: 20
                val cursor = call.request.queryParameters["cursor"]?.decodeCursor()

                val workouts = workoutRepo.listForUser(userId, cursor, limit)
                val nextCursor = if (workouts.size == limit) workouts.last().startedAt.encodeCursor() else null
                call.respond(WorkoutListResponse(nextCursor = nextCursor, workouts = workouts))
            }

            post {
                val userId = call.principal<JWTPrincipal>()!!.payload.getClaim("userId").asString()
                val body = call.receive<Workout>()
                val toSave = body.copy(userId = userId)
                val existing = workoutRepo.findById(toSave.id, userId)

                if (existing != null) {
                    call.respond(HttpStatusCode.OK, existing)
                } else {
                    val saved = workoutRepo.save(toSave)
                    call.respond(HttpStatusCode.Created, saved)
                }
            }

            route("/{id}") {
                get {
                    val userId = call.principal<JWTPrincipal>()!!.payload.getClaim("userId").asString()
                    val id = call.parameters["id"]!!
                    val workout = workoutRepo.findById(id, userId)
                        ?: throw NotFoundException("Workout $id does not exist or has been deleted.")
                    call.respond(workout)
                }

                patch {
                    val userId = call.principal<JWTPrincipal>()!!.payload.getClaim("userId").asString()
                    val id = call.parameters["id"]!!
                    val body = call.receive<PatchWorkoutRequest>()
                    val title = body.title
                    if (title == null) {
                        val workout = workoutRepo.findById(id, userId)
                            ?: throw NotFoundException("Workout $id does not exist or has been deleted.")
                        call.respond(workout)
                        return@patch
                    }
                    val updated = workoutRepo.updateTitle(id, userId, title.trim())
                        ?: throw NotFoundException("Workout $id does not exist or has been deleted.")
                    call.respond(updated)
                }

                delete {
                    val userId = call.principal<JWTPrincipal>()!!.payload.getClaim("userId").asString()
                    val id = call.parameters["id"]!!
                    val deleted = workoutRepo.softDelete(id, userId)
                    if (!deleted) throw NotFoundException("Workout $id does not exist or has been deleted.")
                    call.respond(HttpStatusCode.NoContent)
                }
            }
        }
    }
}

private fun Long.encodeCursor(): String =
    Base64.getEncoder().encodeToString(toString().toByteArray())

private fun String.decodeCursor(): Long? = runCatching {
    String(Base64.getDecoder().decode(this)).toLong()
}.getOrNull()
