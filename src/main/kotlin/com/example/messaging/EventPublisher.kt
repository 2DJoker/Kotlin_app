package com.example.messaging

import com.example.config.AppSettings
import com.rabbitmq.client.ConnectionFactory

interface EventPublisher {
    fun publishOrderCreated(orderId: Long, userId: Long)
}

class RabbitMqEventPublisher(settings: AppSettings) : EventPublisher {
    private val factory = ConnectionFactory().apply {
        host = settings.rabbitHost
        port = settings.rabbitPort
        username = settings.rabbitUser
        password = settings.rabbitPassword
    }
    private val queue = settings.rabbitQueue

    override fun publishOrderCreated(orderId: Long, userId: Long) {
        factory.newConnection().use { connection ->
            connection.createChannel().use { channel ->
                channel.queueDeclare(queue, true, false, false, null)
                val payload = "{\"event\":\"ORDER_CREATED\",\"orderId\":$orderId,\"userId\":$userId}"
                channel.basicPublish("", queue, null, payload.toByteArray())
            }
        }
    }
}

class NoopEventPublisher : EventPublisher {
    override fun publishOrderCreated(orderId: Long, userId: Long) = Unit
}
