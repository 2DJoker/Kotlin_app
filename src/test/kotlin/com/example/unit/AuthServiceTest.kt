package com.example.unit

import com.example.config.AppSettings
import com.example.domain.User
import com.example.domain.dto.RegisterRequest
import com.example.domain.enums.UserRole
import com.example.repository.UserRepository
import com.example.security.JwtService
import com.example.service.AuthService
import com.example.service.ConflictException
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class AuthServiceTest {
    private val users = mutableMapOf<String, User>()

    private val repo = object : UserRepository {
        override fun create(email: String, passwordHash: String, fullName: String, role: UserRole): User {
            val user = User(1, email, passwordHash, fullName, role, Instant.now())
            users[email] = user
            return user
        }

        override fun findByEmail(email: String): User? = users[email]

        override fun findById(id: Long): User? = users.values.firstOrNull { it.id == id }
    }

    private val jwtService = JwtService(
        AppSettings(
            dbUrl = "jdbc:h2:mem:test",
            dbUser = "sa",
            dbPassword = "",
            jwtSecret = "test",
            jwtIssuer = "issuer",
            jwtAudience = "audience",
            jwtRealm = "realm",
            redisHost = "localhost",
            redisPort = 6379,
            redisEnabled = false,
            rabbitHost = "localhost",
            rabbitPort = 5672,
            rabbitUser = "guest",
            rabbitPassword = "guest",
            rabbitQueue = "q",
            rabbitEnabled = false
        )
    )

    @Test
    fun `register should create user token`() {
        val service = AuthService(repo, jwtService)
        val response = service.register(RegisterRequest("test@example.com", "secret123", "Test User"))

        assertEquals("USER", response.role)
    }

    @Test
    fun `register should fail on duplicate email`() {
        val service = AuthService(repo, jwtService)
        service.register(RegisterRequest("dup@example.com", "secret123", "User A"))

        assertFailsWith<ConflictException> {
            service.register(RegisterRequest("dup@example.com", "secret123", "User B"))
        }
    }
}
