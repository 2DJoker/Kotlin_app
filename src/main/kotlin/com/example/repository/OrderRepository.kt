package com.example.repository

import com.example.db.OrderItemsTable
import com.example.db.OrdersTable
import com.example.domain.Order
import com.example.domain.OrderItem
import com.example.domain.enums.OrderStatus
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import java.time.Instant

interface OrderRepository {
    fun create(userId: Long): Order
    fun addItem(orderId: Long, productId: Long, quantity: Int, priceSnapshot: Double): OrderItem
    fun findByUser(userId: Long): List<Pair<Order, List<OrderItem>>>
    fun findByIdForUser(orderId: Long, userId: Long): Pair<Order, List<OrderItem>>?
    fun cancel(orderId: Long): Boolean
    fun stats(): Pair<Long, Long>
}

class OrderRepositoryImpl : OrderRepository {
    override fun create(userId: Long): Order = transaction {
        val now = Instant.now()
        val id = OrdersTable.insert {
            it[OrdersTable.userId] = userId
            it[status] = OrderStatus.CREATED.name
            it[createdAt] = now
        }[OrdersTable.id].value

        Order(id = id, userId = userId, status = OrderStatus.CREATED, createdAt = now)
    }

    override fun addItem(orderId: Long, productId: Long, quantity: Int, priceSnapshot: Double): OrderItem = transaction {
        val id = OrderItemsTable.insert {
            it[OrderItemsTable.orderId] = orderId
            it[OrderItemsTable.productId] = productId
            it[OrderItemsTable.quantity] = quantity
            it[OrderItemsTable.priceSnapshot] = priceSnapshot
        }[OrderItemsTable.id].value

        OrderItem(id, orderId, productId, quantity, priceSnapshot)
    }

    override fun findByUser(userId: Long): List<Pair<Order, List<OrderItem>>> = transaction {
        OrdersTable.selectAll().where { OrdersTable.userId eq userId }
            .map { it.toOrder() }
            .map { order -> order to findItemsByOrderId(order.id) }
    }

    override fun findByIdForUser(orderId: Long, userId: Long): Pair<Order, List<OrderItem>>? = transaction {
        OrdersTable.selectAll().where { (OrdersTable.id eq orderId) and (OrdersTable.userId eq userId) }
            .singleOrNull()
            ?.toOrder()
            ?.let { order -> order to findItemsByOrderId(order.id) }
    }

    override fun cancel(orderId: Long): Boolean = transaction {
        OrdersTable.update({ OrdersTable.id eq orderId }) {
            it[status] = OrderStatus.CANCELED.name
        } > 0
    }

    override fun stats(): Pair<Long, Long> = transaction {
        val total = OrdersTable.selectAll().count()
        val canceled = OrdersTable.selectAll().where { OrdersTable.status eq OrderStatus.CANCELED.name }.count()
        total to canceled
    }

    private fun findItemsByOrderId(orderId: Long): List<OrderItem> {
        return OrderItemsTable.selectAll().where { OrderItemsTable.orderId eq orderId }
            .map { it.toOrderItem() }
    }

    private fun ResultRow.toOrder(): Order = Order(
        id = this[OrdersTable.id].value,
        userId = this[OrdersTable.userId].value,
        status = OrderStatus.valueOf(this[OrdersTable.status]),
        createdAt = this[OrdersTable.createdAt]
    )

    private fun ResultRow.toOrderItem(): OrderItem = OrderItem(
        id = this[OrderItemsTable.id].value,
        orderId = this[OrderItemsTable.orderId].value,
        productId = this[OrderItemsTable.productId].value,
        quantity = this[OrderItemsTable.quantity],
        priceSnapshot = this[OrderItemsTable.priceSnapshot]
    )
}
