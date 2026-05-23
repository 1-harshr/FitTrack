package com.harsh.fittrack

import com.harsh.fittrack.di.serverModule
import com.harsh.fittrack.plugins.configureAuthentication
import com.harsh.fittrack.plugins.configureDatabase
import com.harsh.fittrack.plugins.configureRouting
import com.harsh.fittrack.plugins.configureSerialization
import com.harsh.fittrack.plugins.configureStatusPages
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import org.koin.ktor.plugin.Koin

fun main() {
    embeddedServer(
        Netty,
        port = System.getenv("PORT")?.toIntOrNull() ?: 8080,
        host = "0.0.0.0",
        module = Application::module,
    ).start(wait = true)
}

fun Application.module() {
    install(Koin) { modules(serverModule) }
    configureDatabase()
    configureAuthentication()
    configureSerialization()
    configureStatusPages()
    configureRouting()
}