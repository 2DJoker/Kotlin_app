package com.example.db

import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.javatime.timestamp

object UsersTable : LongIdTable("users") {
    val email = varchar("email", 255).uniqueIndex("idx_users_email")
    val passwordHash = varchar("password_hash", 255)
    val fullName = varchar("full_name", 255)
    val role = varchar("role", 32)
    val createdAt = timestamp("created_at")
}

object ProductsTable : LongIdTable("products") {
    val name = varchar("name", 255)
    val description = text("description")
    val price = double("price")
    val stock = integer("stock")
    val updatedAt = timestamp("updated_at")

    init {
        index("idx_products_name", false, name)
    }
}

object OrdersTable : LongIdTable("orders") {
    val userId = reference("user_id", UsersTable, onDelete = ReferenceOption.CASCADE)
    val status = varchar("status", 32)
    val createdAt = timestamp("created_at")

    init {
        index("idx_orders_user", false, userId)
    }
}

object OrderItemsTable : LongIdTable("order_items") {
    val orderId = reference("order_id", OrdersTable, onDelete = ReferenceOption.CASCADE)
    val productId = reference("product_id", ProductsTable, onDelete = ReferenceOption.RESTRICT)
    val quantity = integer("quantity")
    val priceSnapshot = double("price_snapshot")

    init {
        index("idx_order_items_order", false, orderId)
    }
}

object AuditLogsTable : LongIdTable("audit_logs") {
    val userId = optReference("user_id", UsersTable, onDelete = ReferenceOption.SET_NULL)
    val action = varchar("action", 100)
    val payload = text("payload")
    val createdAt = timestamp("created_at")

    init {
        index("idx_audit_logs_action", false, action)
    }
}
