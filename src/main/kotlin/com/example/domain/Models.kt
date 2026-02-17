package com.example.domain

import com.example.domain.enums.OrderStatus
import com.example.domain.enums.UserRole
import java.time.Instant

data class User(
    val id: Long,
    val email: String,
    val passwordHash: String,
    val fullName: String,
    val role: UserRole,
    val createdAt: Instant
)

data class Product(
    val id: Long,
    val name: String,
    val description: String,
    val price: Double,
    val stock: Int,
    val updatedAt: Instant
)

data class Order(
    val id: Long,
    val userId: Long,
    val status: OrderStatus,
    val createdAt: Instant
)

data class OrderItem(
    val id: Long,
    val orderId: Long,
    val productId: Long,
    val quantity: Int,
    val priceSnapshot: Double
)

data class AuditLog(
    val id: Long,
    val userId: Long?,
    val action: String,
    val payload: String,
    val createdAt: Instant
)
