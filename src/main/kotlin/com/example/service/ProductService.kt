package com.example.service

import com.example.cache.CacheService
import com.example.domain.dto.CreateProductRequest
import com.example.domain.dto.ProductResponse
import com.example.domain.dto.UpdateProductRequest
import com.example.repository.ProductRepository
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class ProductService(
    private val productRepository: ProductRepository,
    private val cacheService: CacheService
) {
    private val json = Json { ignoreUnknownKeys = true }

    fun listProducts(): List<ProductResponse> = productRepository.list().map { it.toResponse() }

    fun getProductById(id: Long): ProductResponse {
        val key = "product:$id"
        val cached = cacheService.get(key)
        if (cached != null) {
            return json.decodeFromString(ProductResponse.serializer(), cached)
        }

        val product = productRepository.findById(id) ?: throw NotFoundException("Product not found")
        val response = product.toResponse()
        cacheService.set(key, json.encodeToString(response), 120)
        return response
    }

    fun createProduct(request: CreateProductRequest): ProductResponse {
        validate(request.price, request.stock)
        val product = productRepository.create(request.name, request.description, request.price, request.stock)
        return product.toResponse()
    }

    fun updateProduct(id: Long, request: UpdateProductRequest): ProductResponse {
        validate(request.price, request.stock)
        val product = productRepository.update(id, request.name, request.description, request.price, request.stock)
            ?: throw NotFoundException("Product not found")

        cacheService.delete("product:$id")
        return product.toResponse()
    }

    fun deleteProduct(id: Long) {
        val deleted = productRepository.delete(id)
        if (!deleted) throw NotFoundException("Product not found")
        cacheService.delete("product:$id")
    }

    private fun validate(price: Double, stock: Int) {
        if (price <= 0.0 || stock < 0) {
            throw ValidationException("Invalid product data")
        }
    }

    private fun com.example.domain.Product.toResponse() = ProductResponse(
        id = id,
        name = name,
        description = description,
        price = price,
        stock = stock
    )
}
