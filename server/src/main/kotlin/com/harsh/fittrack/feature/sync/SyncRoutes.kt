package com.harsh.fittrack.feature.sync

import com.harsh.fittrack.domain.model.SyncStatusResponse
import com.harsh.fittrack.domain.repository.ExerciseRepository
import com.harsh.fittrack.domain.repository.WorkoutRepository
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import org.koin.ktor.ext.inject
import java.util.Base64

fun Route.syncRoutes() {
    val workoutRepo: WorkoutRepository by inject()
    val exerciseRepo: ExerciseRepository by inject()

    authenticate("jwt") {
        get("/sync/status") {
            val userId = call.principal<JWTPrincipal>()!!.payload.getClaim("userId").asString()
            val latestStartedAt = workoutRepo.latestStartedAt(userId)
            val cursor = latestStartedAt?.let {
                Base64.getEncoder().encodeToString(it.toString().toByteArray())
            }
            val catalogVersion = exerciseRepo.latestVersion()
            call.respond(SyncStatusResponse(workoutCursor = cursor, catalogVersion = catalogVersion))
        }
    }
}
