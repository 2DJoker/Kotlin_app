package com.example.e2e

import com.example.module
import com.example.db.ProductsTable
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.config.MapApplicationConfig
import io.ktor.server.testing.testApplication
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ApiE2ETest {
    @Test
    fun `register and login should return JWT`() = testApplication {
        environment {
            config = testConfig("auth_db")
        }

        application { module() }

        val client = createClient {
            install(ContentNegotiation) { json() }
        }

        val registerResp = client.post("/auth/register") {
            contentType(ContentType.Application.Json)
            setBody("""{"email":"e2e@test.dev","password":"secret123","fullName":"E2E User"}""")
        }
        assertEquals(HttpStatusCode.Created, registerResp.status)

        val loginResp = client.post("/auth/login") {
            contentType(ContentType.Application.Json)
            setBody("""{"email":"e2e@test.dev","password":"secret123"}""")
        }
        assertEquals(HttpStatusCode.OK, loginResp.status)

        val body = loginResp.body<AuthResponseTest>()
        assertTrue(body.token.isNotBlank())
    }

    @Test
    fun `create and cancel order should work`() = testApplication {
        environment {
            config = testConfig("order_db")
        }
        application { module() }

        transaction {
            ProductsTable.insert {
                it[name] = "Seed Product"
                it[description] = "Seed"
                it[price] = 10.0
                it[stock] = 20
                it[updatedAt] = java.time.Instant.now()
            }
        }

        val client = createClient {
            install(ContentNegotiation) { json() }
        }

        client.post("/auth/register") {
            contentType(ContentType.Application.Json)
            setBody("""{"email":"buyer@test.dev","password":"secret123","fullName":"Buyer"}""")
        }

        val login = client.post("/auth/login") {
            contentType(ContentType.Application.Json)
            setBody("""{"email":"buyer@test.dev","password":"secret123"}""")
        }.body<AuthResponseTest>()

        val createOrder = client.post("/orders") {
            contentType(ContentType.Application.Json)
            header(HttpHeaders.Authorization, "Bearer ${login.token}")
            setBody("""{"items":[{"productId":1,"quantity":2}]}""")
        }
        assertEquals(HttpStatusCode.Created, createOrder.status)

        val list = client.get("/orders") {
            header(HttpHeaders.Authorization, "Bearer ${login.token}")
        }
        assertEquals(HttpStatusCode.OK, list.status)

        val orders = Json.decodeFromString<List<OrderResponseTest>>(list.bodyAsText())
        val orderId = orders.first().id

        val cancel = client.delete("/orders/$orderId") {
            header(HttpHeaders.Authorization, "Bearer ${login.token}")
        }
        assertEquals(HttpStatusCode.NoContent, cancel.status)
    }

    private fun testConfig(dbName: String): MapApplicationConfig {
        return MapApplicationConfig(
            "app.db.url" to "jdbc:h2:mem:$dbName;MODE=PostgreSQL;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false",
            "app.db.user" to "sa",
            "app.db.password" to "",
            "app.jwt.secret" to "test-secret",
            "app.jwt.issuer" to "test-issuer",
            "app.jwt.audience" to "test-audience",
            "app.jwt.realm" to "test-realm",
            "app.redis.host" to "localhost",
            "app.redis.port" to "6379",
            "app.redis.enabled" to "false",
            "app.rabbit.host" to "localhost",
            "app.rabbit.port" to "5672",
            "app.rabbit.user" to "guest",
            "app.rabbit.password" to "guest",
            "app.rabbit.queue" to "test-queue",
            "app.rabbit.enabled" to "false"
        )
    }
}

@Serializable
private data class AuthResponseTest(
    val token: String,
    val role: String
)

@Serializable
private data class OrderResponseTest(
    val id: Long,
    @SerialName("userId") val userId: Long,
    val status: String,
    val createdAt: String
)
