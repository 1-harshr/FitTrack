package com.harsh.fittrack.feature.exercise

import com.harsh.fittrack.domain.model.ExerciseSyncResponse
import com.harsh.fittrack.domain.repository.ExerciseRepository
import io.ktor.server.auth.authenticate
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import org.koin.ktor.ext.inject

fun Route.exerciseRoutes() {
    val exerciseRepo: ExerciseRepository by inject()

    authenticate("jwt") {
        get("/exercises") {
            val sinceVersion = call.request.queryParameters["sinceVersion"]?.toIntOrNull() ?: 0
            val exercises = exerciseRepo.findSinceVersion(sinceVersion)
            val latestVersion = exerciseRepo.latestVersion()
            call.respond(ExerciseSyncResponse(latestVersion = latestVersion, exercises = exercises))
        }
    }
}
