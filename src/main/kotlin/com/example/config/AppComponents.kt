package com.example.config

import com.example.cache.CacheService
import com.example.cache.NoopCacheService
import com.example.cache.RedisCacheService
import com.example.messaging.EventPublisher
import com.example.messaging.NoopEventPublisher
import com.example.messaging.RabbitMqEventPublisher
import com.example.repository.AuditLogRepository
import com.example.repository.AuditLogRepositoryImpl
import com.example.repository.OrderRepository
import com.example.repository.OrderRepositoryImpl
import com.example.repository.ProductRepository
import com.example.repository.ProductRepositoryImpl
import com.example.repository.UserRepository
import com.example.repository.UserRepositoryImpl
import com.example.security.JwtService
import com.example.service.AuthService
import com.example.service.OrderService
import com.example.service.ProductService

data class AppComponents(
    val authService: AuthService,
    val productService: ProductService,
    val orderService: OrderService,
    val jwtService: JwtService
)

fun buildComponents(settings: AppSettings): AppComponents {
    val userRepository: UserRepository = UserRepositoryImpl()
    val productRepository: ProductRepository = ProductRepositoryImpl()
    val orderRepository: OrderRepository = OrderRepositoryImpl()
    val auditLogRepository: AuditLogRepository = AuditLogRepositoryImpl()

    val cacheService: CacheService = if (settings.redisEnabled) RedisCacheService(settings) else NoopCacheService()
    val eventPublisher: EventPublisher = if (settings.rabbitEnabled) RabbitMqEventPublisher(settings) else NoopEventPublisher()

    val jwtService = JwtService(settings)

    return AppComponents(
        authService = AuthService(userRepository, jwtService),
        productService = ProductService(productRepository, cacheService),
        orderService = OrderService(orderRepository, productRepository, auditLogRepository, eventPublisher, cacheService),
        jwtService = jwtService
    )
}
