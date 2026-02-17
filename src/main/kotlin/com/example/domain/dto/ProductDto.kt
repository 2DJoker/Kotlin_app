package com.example.domain.dto

import kotlinx.serialization.Serializable

@Serializable
data class ProductResponse(
    val id: Long,
    val name: String,
    val description: String,
    val price: Double,
    val stock: Int
)

@Serializable
data class CreateProductRequest(
    val name: String,
    val description: String,
    val price: Double,
    val stock: Int
)

@Serializable
data class UpdateProductRequest(
    val name: String,
    val description: String,
    val price: Double,
    val stock: Int
)
