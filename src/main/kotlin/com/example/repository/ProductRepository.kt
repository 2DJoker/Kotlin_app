package com.example.repository

import com.example.db.ProductsTable
import com.example.domain.Product
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import java.time.Instant

interface ProductRepository {
    fun create(name: String, description: String, price: Double, stock: Int): Product
    fun list(): List<Product>
    fun findById(id: Long): Product?
    fun update(id: Long, name: String, description: String, price: Double, stock: Int): Product?
    fun delete(id: Long): Boolean
    fun decrementStock(productId: Long, quantity: Int): Boolean
}

class ProductRepositoryImpl : ProductRepository {
    override fun create(name: String, description: String, price: Double, stock: Int): Product = transaction {
        val now = Instant.now()
        val id = ProductsTable.insert {
            it[ProductsTable.name] = name
            it[ProductsTable.description] = description
            it[ProductsTable.price] = price
            it[ProductsTable.stock] = stock
            it[ProductsTable.updatedAt] = now
        }[ProductsTable.id].value

        Product(id, name, description, price, stock, now)
    }

    override fun list(): List<Product> = transaction {
        ProductsTable.selectAll().map { it.toProduct() }
    }

    override fun findById(id: Long): Product? = transaction {
        ProductsTable.selectAll().where { ProductsTable.id eq id }.singleOrNull()?.toProduct()
    }

    override fun update(id: Long, name: String, description: String, price: Double, stock: Int): Product? = transaction {
        val now = Instant.now()
        val updated = ProductsTable.update({ ProductsTable.id eq id }) {
            it[ProductsTable.name] = name
            it[ProductsTable.description] = description
            it[ProductsTable.price] = price
            it[ProductsTable.stock] = stock
            it[updatedAt] = now
        }

        if (updated == 0) null else Product(id, name, description, price, stock, now)
    }

    override fun delete(id: Long): Boolean = transaction {
        val entityId = ProductsTable.selectAll().where { ProductsTable.id eq id }.singleOrNull()?.get(ProductsTable.id)
            ?: return@transaction false
        ProductsTable.deleteWhere { ProductsTable.id eq entityId } > 0
    }

    override fun decrementStock(productId: Long, quantity: Int): Boolean = transaction {
        val product = ProductsTable.selectAll().where { ProductsTable.id eq productId }.singleOrNull()
            ?: return@transaction false
        val currentStock = product[ProductsTable.stock]
        if (currentStock < quantity) {
            return@transaction false
        }
        val updated = ProductsTable.update({ ProductsTable.id eq productId }) {
            it[ProductsTable.stock] = currentStock - quantity
            it[ProductsTable.updatedAt] = Instant.now()
        }
        updated > 0
    }

    private fun ResultRow.toProduct(): Product = Product(
        id = this[ProductsTable.id].value,
        name = this[ProductsTable.name],
        description = this[ProductsTable.description],
        price = this[ProductsTable.price],
        stock = this[ProductsTable.stock],
        updatedAt = this[ProductsTable.updatedAt]
    )
}
