package com.example.config

import io.ktor.server.application.Application

data class AppSettings(
    val dbUrl: String,
    val dbUser: String,
    val dbPassword: String,
    val jwtSecret: String,
    val jwtIssuer: String,
    val jwtAudience: String,
    val jwtRealm: String,
    val redisHost: String,
    val redisPort: Int,
    val redisEnabled: Boolean,
    val rabbitHost: String,
    val rabbitPort: Int,
    val rabbitUser: String,
    val rabbitPassword: String,
    val rabbitQueue: String,
    val rabbitEnabled: Boolean
)

fun Application.loadSettings(): AppSettings {
    val config = environment.config
    fun value(path: String, env: String): String = System.getenv(env) ?: config.property(path).getString()
    return AppSettings(
        dbUrl = value("app.db.url", "DB_URL"),
        dbUser = value("app.db.user", "DB_USER"),
        dbPassword = value("app.db.password", "DB_PASSWORD"),
        jwtSecret = value("app.jwt.secret", "JWT_SECRET"),
        jwtIssuer = value("app.jwt.issuer", "JWT_ISSUER"),
        jwtAudience = value("app.jwt.audience", "JWT_AUDIENCE"),
        jwtRealm = value("app.jwt.realm", "JWT_REALM"),
        redisHost = value("app.redis.host", "REDIS_HOST"),
        redisPort = value("app.redis.port", "REDIS_PORT").toInt(),
        redisEnabled = value("app.redis.enabled", "REDIS_ENABLED").toBoolean(),
        rabbitHost = value("app.rabbit.host", "RABBIT_HOST"),
        rabbitPort = value("app.rabbit.port", "RABBIT_PORT").toInt(),
        rabbitUser = value("app.rabbit.user", "RABBIT_USER"),
        rabbitPassword = value("app.rabbit.password", "RABBIT_PASSWORD"),
        rabbitQueue = value("app.rabbit.queue", "RABBIT_QUEUE"),
        rabbitEnabled = value("app.rabbit.enabled", "RABBIT_ENABLED").toBoolean()
    )
}
