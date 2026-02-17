package com.example.service

import com.example.domain.dto.AuthResponse
import com.example.domain.dto.LoginRequest
import com.example.domain.dto.RegisterRequest
import com.example.domain.enums.UserRole
import com.example.repository.UserRepository
import com.example.security.JwtService
import com.example.security.PasswordHasher

class AuthService(
    private val userRepository: UserRepository,
    private val jwtService: JwtService
) {
    fun register(request: RegisterRequest): AuthResponse {
        if (request.email.isBlank() || request.password.length < 6 || request.fullName.isBlank()) {
            throw ValidationException("Invalid registration data")
        }
        if (userRepository.findByEmail(request.email) != null) {
            throw ConflictException("User already exists")
        }

        val user = userRepository.create(
            email = request.email,
            passwordHash = PasswordHasher.hash(request.password),
            fullName = request.fullName,
            role = UserRole.USER
        )
        return AuthResponse(jwtService.createToken(user), user.role.name)
    }

    fun login(request: LoginRequest): AuthResponse {
        val user = userRepository.findByEmail(request.email) ?: throw UnauthorizedException("Invalid credentials")
        if (!PasswordHasher.verify(request.password, user.passwordHash)) {
            throw UnauthorizedException("Invalid credentials")
        }
        return AuthResponse(jwtService.createToken(user), user.role.name)
    }
}
