package com.example.integration

import com.example.config.AppSettings
import com.example.db.DatabaseFactory
import com.example.db.ProductsTable
import com.example.db.UsersTable
import com.example.domain.enums.UserRole
import com.example.repository.ProductRepositoryImpl
import com.example.repository.UserRepositoryImpl
import org.jetbrains.exposed.sql.deleteAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.Assumptions.assumeTrue
import org.testcontainers.DockerClientFactory
import org.testcontainers.containers.PostgreSQLContainer

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class PostgresIntegrationTest {
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
            ProductsTable.deleteAll()
            UsersTable.deleteAll()
        }
    }

    @Test
    fun `should create and load user`() {
        val repo = UserRepositoryImpl()
        repo.create("user@test.dev", "hash", "User", UserRole.USER)

        val user = repo.findByEmail("user@test.dev")
        assertNotNull(user)
        assertEquals("user@test.dev", user!!.email)
    }

    @Test
    fun `should create and list product`() {
        val repo = ProductRepositoryImpl()
        repo.create("Phone", "Android", 100.0, 5)

        val products = repo.list()
        assertEquals(1, products.size)
        assertEquals("Phone", products.first().name)
    }
}
