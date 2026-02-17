package com.example

import com.example.config.buildComponents
import com.example.config.loadSettings
import com.example.db.DatabaseFactory
import com.example.routing.registerApiRoutes
import com.example.security.AuthPrincipal
import com.example.service.AppException
import com.example.service.ConflictException
import com.example.service.ForbiddenException
import com.example.service.NotFoundException
import com.example.service.UnauthorizedException
import com.example.service.ValidationException
import com.example.worker.startOrderEventsWorker
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.application.install
import io.ktor.server.auth.authentication
import io.ktor.server.auth.jwt.jwt
import io.ktor.server.http.content.staticResources
import io.ktor.server.plugins.callloging.CallLogging
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.openapi.openAPI
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.plugins.statuspages.exception
import io.ktor.server.plugins.swagger.swaggerUI
import io.ktor.server.response.respond
import io.ktor.server.response.respondRedirect
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import kotlinx.serialization.Serializable

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    val settings = loadSettings()
    DatabaseFactory.init(settings)
    val components = buildComponents(settings)

    install(CallLogging)
    install(ContentNegotiation) {
        json()
    }
    install(StatusPages) {
        exception<AppException> { call, cause ->
            when (cause) {
                is ValidationException -> call.respond(HttpStatusCode.BadRequest, ErrorResponse(cause.message ?: "Validation error"))
                is UnauthorizedException -> call.respond(HttpStatusCode.Unauthorized, ErrorResponse(cause.message ?: "Unauthorized"))
                is ForbiddenException -> call.respond(HttpStatusCode.Forbidden, ErrorResponse(cause.message ?: "Forbidden"))
                is NotFoundException -> call.respond(HttpStatusCode.NotFound, ErrorResponse(cause.message ?: "Not found"))
                is ConflictException -> call.respond(HttpStatusCode.Conflict, ErrorResponse(cause.message ?: "Conflict"))
                else -> call.respond(HttpStatusCode.InternalServerError, ErrorResponse("Internal error"))
            }
        }
        exception<Throwable> { call, cause ->
            this@module.environment.log.error("Unhandled error", cause)
            call.respond(HttpStatusCode.InternalServerError, ErrorResponse("Internal error"))
        }
    }

    authentication {
        jwt("auth-jwt") {
            realm = settings.jwtRealm
            verifier(components.jwtService.verifier())
            validate { credential ->
                val userId = credential.payload.getClaim("userId").asLong() ?: return@validate null
                val role = credential.payload.getClaim("role").asString() ?: return@validate null
                AuthPrincipal(userId = userId, role = role)
            }
        }
    }

    routing {
        get("/") {
            call.respondRedirect("/ui")
        }

        get("/ui") {
            call.respondRedirect("/ui/index.html")
        }

        get("/health") {
            call.respond(HttpStatusCode.OK, mapOf("status" to "ok"))
        }

        openAPI(path = "openapi", swaggerFile = "openapi/documentation.yaml")
        swaggerUI(path = "swagger", swaggerFile = "openapi/documentation.yaml")
        staticResources("/ui", "static")

        registerApiRoutes(components)
    }

    startOrderEventsWorker(settings)
}

@Serializable
data class ErrorResponse(val message: String)
