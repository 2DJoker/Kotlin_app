package com.example.security

import io.ktor.server.auth.Principal

data class AuthPrincipal(
    val userId: Long,
    val role: String
) : Principal
