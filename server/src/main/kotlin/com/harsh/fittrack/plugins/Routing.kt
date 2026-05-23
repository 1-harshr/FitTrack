package com.harsh.fittrack.plugins

import com.harsh.fittrack.feature.auth.authRoutes
import com.harsh.fittrack.feature.exercise.exerciseRoutes
import com.harsh.fittrack.feature.sync.syncRoutes
import com.harsh.fittrack.feature.user.userRoutes
import com.harsh.fittrack.feature.workout.workoutRoutes
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.calllogging.CallLogging
import io.ktor.server.plugins.cors.routing.CORS
import io.ktor.server.routing.routing
import org.slf4j.event.Level

fun Application.configureRouting() {
    install(CallLogging) { level = Level.INFO }

    install(CORS) {
        anyHost()
        allowNonSimpleContentTypes = true
        allowHeaders { true }
    }

    val secret = jwtSecret()
    routing {
        authRoutes(secret)
        userRoutes()
        exerciseRoutes()
        workoutRoutes()
        syncRoutes()
    }
}
