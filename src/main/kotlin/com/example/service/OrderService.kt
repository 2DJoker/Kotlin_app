package com.example.service

import com.example.cache.CacheService
import com.example.domain.dto.CreateOrderRequest
import com.example.domain.dto.OrderItemResponse
import com.example.domain.dto.OrderResponse
import com.example.domain.dto.OrdersStatsResponse
import com.example.domain.enums.OrderStatus
import com.example.messaging.EventPublisher
import com.example.repository.AuditLogRepository
import com.example.repository.OrderRepository
import com.example.repository.ProductRepository
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class OrderService(
    private val orderRepository: OrderRepository,
    private val productRepository: ProductRepository,
    private val auditLogRepository: AuditLogRepository,
    private val eventPublisher: EventPublisher,
    private val cacheService: CacheService
) {
    private val json = Json { ignoreUnknownKeys = true }

    fun createOrder(userId: Long, request: CreateOrderRequest): OrderResponse {
        if (request.items.isEmpty()) {
            throw ValidationException("Order items cannot be empty")
        }

        request.items.forEach {
            if (it.quantity <= 0) {
                throw ValidationException("Quantity must be positive")
            }
            val product = productRepository.findById(it.productId) ?: throw NotFoundException("Product ${it.productId} not found")
            if (product.stock < it.quantity) {
                throw ValidationException("Not enough stock for product ${it.productId}")
            }
        }

        val order = orderRepository.create(userId)
        val items = request.items.map { item ->
            val product = productRepository.findById(item.productId) ?: throw NotFoundException("Product ${item.productId} not found")
            if (!productRepository.decrementStock(item.productId, item.quantity)) {
                throw ValidationException("Not enough stock for product ${item.productId}")
            }
            orderRepository.addItem(order.id, item.productId, item.quantity, product.price)
        }

        val response = OrderResponse(
            id = order.id,
            userId = userId,
            status = order.status.name,
            createdAt = order.createdAt.toString(),
            items = items.map { OrderItemResponse(it.productId, it.quantity, it.priceSnapshot) }
        )

        auditLogRepository.log(userId, "ORDER_CREATED", "orderId=${order.id}")
        eventPublisher.publishOrderCreated(order.id, userId)
        cacheService.set("order:${order.id}", json.encodeToString(response), 300)

        return response
    }

    fun getOrdersByUser(userId: Long): List<OrderResponse> {
        return orderRepository.findByUser(userId).map { (order, items) ->
            OrderResponse(
                id = order.id,
                userId = order.userId,
                status = order.status.name,
                createdAt = order.createdAt.toString(),
                items = items.map { OrderItemResponse(it.productId, it.quantity, it.priceSnapshot) }
            )
        }
    }

    fun cancelOrder(orderId: Long, userId: Long) {
        val pair = orderRepository.findByIdForUser(orderId, userId) ?: throw NotFoundException("Order not found")
        if (pair.first.status == OrderStatus.CANCELED) {
            throw ValidationException("Order already canceled")
        }

        val canceled = orderRepository.cancel(orderId)
        if (!canceled) throw NotFoundException("Order not found")

        auditLogRepository.log(userId, "ORDER_CANCELED", "orderId=$orderId")
        cacheService.delete("order:$orderId")
    }

    fun getOrderStats(): OrdersStatsResponse {
        val (total, canceled) = orderRepository.stats()
        return OrdersStatsResponse(totalOrders = total, canceledOrders = canceled)
    }
}
