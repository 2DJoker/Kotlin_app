package com.example.routing

import com.example.config.AppComponents
import com.example.domain.dto.CreateOrderRequest
import com.example.domain.dto.CreateProductRequest
import com.example.domain.dto.LoginRequest
import com.example.domain.dto.RegisterRequest
import com.example.domain.dto.UpdateProductRequest
import com.example.security.AuthPrincipal
import com.example.service.ForbiddenException
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.principal
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import io.ktor.server.routing.route

fun Route.registerApiRoutes(components: AppComponents) {
    route("/auth") {
        post("/register") {
            val request = call.receive<RegisterRequest>()
            call.respond(HttpStatusCode.Created, components.authService.register(request))
        }

        post("/login") {
            val request = call.receive<LoginRequest>()
            call.respond(components.authService.login(request))
        }
    }

    route("/products") {
        get {
            call.respond(components.productService.listProducts())
        }

        get("/{id}") {
            val id = call.parameters["id"]?.toLongOrNull() ?: return@get call.respond(HttpStatusCode.BadRequest)
            call.respond(components.productService.getProductById(id))
        }
    }

    authenticate("auth-jwt") {
        route("/orders") {
            post {
                val principal = call.principal<AuthPrincipal>() ?: return@post call.respond(HttpStatusCode.Unauthorized)
                val request = call.receive<CreateOrderRequest>()
                call.respond(HttpStatusCode.Created, components.orderService.createOrder(principal.userId, request))
            }

            get {
                val principal = call.principal<AuthPrincipal>() ?: return@get call.respond(HttpStatusCode.Unauthorized)
                call.respond(components.orderService.getOrdersByUser(principal.userId))
            }

            delete("/{id}") {
                val principal = call.principal<AuthPrincipal>() ?: return@delete call.respond(HttpStatusCode.Unauthorized)
                val orderId = call.parameters["id"]?.toLongOrNull() ?: return@delete call.respond(HttpStatusCode.BadRequest)
                components.orderService.cancelOrder(orderId, principal.userId)
                call.respond(HttpStatusCode.NoContent)
            }
        }

        route("/admin") {
            route("/products") {
                post {
                    requireAdmin(call.principal<AuthPrincipal>())
                    val request = call.receive<CreateProductRequest>()
                    call.respond(HttpStatusCode.Created, components.productService.createProduct(request))
                }

                put("/{id}") {
                    requireAdmin(call.principal<AuthPrincipal>())
                    val id = call.parameters["id"]?.toLongOrNull() ?: return@put call.respond(HttpStatusCode.BadRequest)
                    val request = call.receive<UpdateProductRequest>()
                    call.respond(components.productService.updateProduct(id, request))
                }

                delete("/{id}") {
                    requireAdmin(call.principal<AuthPrincipal>())
                    val id = call.parameters["id"]?.toLongOrNull() ?: return@delete call.respond(HttpStatusCode.BadRequest)
                    components.productService.deleteProduct(id)
                    call.respond(HttpStatusCode.NoContent)
                }
            }

            get("/stats/orders") {
                requireAdmin(call.principal<AuthPrincipal>())
                call.respond(components.orderService.getOrderStats())
            }
        }
    }
}

private fun requireAdmin(principal: AuthPrincipal?) {
    if (principal == null || principal.role != "ADMIN") {
        throw ForbiddenException("Admin access required")
    }
}
