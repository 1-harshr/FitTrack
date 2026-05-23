package com.harsh.fittrack.plugins

import com.harsh.fittrack.domain.model.ProblemDetail
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.response.respond

private const val ERROR_BASE = "https://api.fittrack.app/errors"

class NotFoundException(message: String) : Exception(message)
class ForbiddenException(message: String) : Exception(message)
class BadRequestException(message: String) : Exception(message)

fun Application.configureStatusPages() {
    install(StatusPages) {
        exception<NotFoundException> { call, cause ->
            call.respond(HttpStatusCode.NotFound, ProblemDetail(
                type = "$ERROR_BASE/not-found",
                title = "Not Found",
                status = 404,
                detail = cause.message ?: "Resource not found.",
            ))
        }
        exception<ForbiddenException> { call, cause ->
            call.respond(HttpStatusCode.Forbidden, ProblemDetail(
                type = "$ERROR_BASE/forbidden",
                title = "Forbidden",
                status = 403,
                detail = cause.message ?: "Access denied.",
            ))
        }
        exception<BadRequestException> { call, cause ->
            call.respond(HttpStatusCode.BadRequest, ProblemDetail(
                type = "$ERROR_BASE/bad-request",
                title = "Bad Request",
                status = 400,
                detail = cause.message ?: "Invalid request.",
            ))
        }
        exception<Throwable> { call, cause ->
            call.application.environment.log.error("Unhandled exception", cause)
            call.respond(HttpStatusCode.InternalServerError, ProblemDetail(
                type = "$ERROR_BASE/internal-error",
                title = "Internal Server Error",
                status = 500,
                detail = "An unexpected error occurred.",
            ))
        }
    }
}
