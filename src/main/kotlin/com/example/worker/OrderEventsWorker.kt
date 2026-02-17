package com.example.worker

import com.example.config.AppSettings
import com.rabbitmq.client.ConnectionFactory
import io.ktor.server.application.Application
import kotlin.concurrent.thread

fun Application.startOrderEventsWorker(settings: AppSettings) {
    val logger = this.environment.log
    if (!settings.rabbitEnabled) {
        logger.info("RabbitMQ worker disabled")
        return
    }

    thread(start = true, isDaemon = true, name = "order-events-worker") {
        runCatching {
            val factory = ConnectionFactory().apply {
                host = settings.rabbitHost
                port = settings.rabbitPort
                username = settings.rabbitUser
                password = settings.rabbitPassword
            }

            factory.newConnection().use { connection ->
                connection.createChannel().use { channel ->
                    channel.queueDeclare(settings.rabbitQueue, true, false, false, null)
                    val consumer = com.rabbitmq.client.DeliverCallback { _, message ->
                        val body = String(message.body)
                        logger.info("Order event received: $body")
                        logger.info("Fake email sent for event: $body")
                    }
                    channel.basicConsume(settings.rabbitQueue, true, consumer) { _ -> }

                    while (true) {
                        Thread.sleep(1000)
                    }
                }
            }
        }.onFailure { logger.error("Order events worker failed", it) }
    }
}
