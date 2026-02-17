package com.example.unit

import com.example.cache.NoopCacheService
import com.example.domain.Product
import com.example.domain.dto.CreateProductRequest
import com.example.repository.ProductRepository
import com.example.service.ProductService
import com.example.service.ValidationException
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertFailsWith

class ProductServiceTest {
    private val repository = object : ProductRepository {
        override fun create(name: String, description: String, price: Double, stock: Int): Product {
            return Product(1, name, description, price, stock, Instant.now())
        }

        override fun list(): List<Product> = emptyList()

        override fun findById(id: Long): Product? = null

        override fun update(id: Long, name: String, description: String, price: Double, stock: Int): Product? = null

        override fun delete(id: Long): Boolean = false

        override fun decrementStock(productId: Long, quantity: Int): Boolean = false
    }

    @Test
    fun `create product should validate price`() {
        val service = ProductService(repository, NoopCacheService())

        assertFailsWith<ValidationException> {
            service.createProduct(CreateProductRequest("A", "B", 0.0, 10))
        }
    }
}
