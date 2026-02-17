package com.example.security

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.example.config.AppSettings
import com.example.domain.User
import java.util.Date

class JwtService(private val settings: AppSettings) {
    private val algorithm = Algorithm.HMAC256(settings.jwtSecret)

    fun createToken(user: User): String {
        val now = System.currentTimeMillis()
        return JWT.create()
            .withIssuer(settings.jwtIssuer)
            .withAudience(settings.jwtAudience)
            .withClaim("userId", user.id)
            .withClaim("role", user.role.name)
            .withExpiresAt(Date(now + 1000L * 60L * 60L * 12L))
            .sign(algorithm)
    }

    fun verifier() = JWT.require(algorithm)
        .withIssuer(settings.jwtIssuer)
        .withAudience(settings.jwtAudience)
        .build()
}
