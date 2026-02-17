package com.example.integration

import com.example.config.AppSettings
import com.example.db.DatabaseFactory
import com.example.db.OrderItemsTable
import com.example.db.OrdersTable
import com.example.db.ProductsTable
import com.example.db.UsersTable
import com.example.domain.enums.UserRole
import com.example.repository.OrderRepositoryImpl
import com.example.repository.UserRepositoryImpl
import org.jetbrains.exposed.sql.deleteAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assumptions.assumeTrue
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.testcontainers.DockerClientFactory
import org.testcontainers.containers.PostgreSQLContainer

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class OrderStatsIntegrationTest {
    private val postgres = PostgreSQLContainer("postgres:16-alpine")

    @BeforeAll
    fun setup() {
        assumeTrue(DockerClientFactory.instance().isDockerAvailable, "Docker is not available for Testcontainers")
        postgres.start()
        DatabaseFactory.init(
            AppSettings(
                dbUrl = postgres.jdbcUrl,
                dbUser = postgres.username,
                dbPassword = postgres.password,
                jwtSecret = "test",
                jwtIssuer = "issuer",
                jwtAudience = "aud",
                jwtRealm = "realm",
                redisHost = "localhost",
                redisPort = 6379,
                redisEnabled = false,
                rabbitHost = "localhost",
                rabbitPort = 5672,
                rabbitUser = "guest",
                rabbitPassword = "guest",
                rabbitQueue = "events",
                rabbitEnabled = false
            )
        )
    }

    @AfterEach
    fun cleanup() {
        transaction {
            OrderItemsTable.deleteAll()
            OrdersTable.deleteAll()
            ProductsTable.deleteAll()
            UsersTable.deleteAll()
        }
    }

    @Test
    fun `stats should include canceled orders`() {
        val userRepo = UserRepositoryImpl()
        val orderRepo = OrderRepositoryImpl()
        val user = userRepo.create("stats@test.dev", "hash", "Stat User", UserRole.USER)

        val o1 = orderRepo.create(user.id)
        orderRepo.create(user.id)
        orderRepo.cancel(o1.id)

        val stats = orderRepo.stats()
        assertEquals(2L, stats.first)
        assertEquals(1L, stats.second)
    }
}
