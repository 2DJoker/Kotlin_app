package com.example.domain.dto

import kotlinx.serialization.Serializable

@Serializable
data class OrderItemRequest(
    val productId: Long,
    val quantity: Int
)

@Serializable
data class CreateOrderRequest(
    val items: List<OrderItemRequest>
)

@Serializable
data class OrderItemResponse(
    val productId: Long,
    val quantity: Int,
    val priceSnapshot: Double
)

@Serializable
data class OrderResponse(
    val id: Long,
    val userId: Long,
    val status: String,
    val createdAt: String,
    val items: List<OrderItemResponse>
)

@Serializable
data class OrdersStatsResponse(
    val totalOrders: Long,
    val canceledOrders: Long
)
