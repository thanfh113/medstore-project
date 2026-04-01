package com.example.nhathuoc.data.model

import kotlinx.serialization.Serializable

/**
 * Cart related data transfer objects
 */

@Serializable
data class CartDto(
    val id: String, // UUID from backend
    val userId: String, // UUID from backend
    val items: List<CartItemDto>,
    val totalItems: Int,
    val totalAmount: Double, // Changed from String to numeric
    val subtotal: Double,
    val discount: Double,
    val createdAt: String,
    val updatedAt: String
)

@Serializable
data class CartItemDto(
    val id: String, // UUID from backend
    val userId: String, // UUID from backend - Match cart_items table
    val productId: String, // UUID from backend
    val product: ProductDto,
    val quantity: Int,
    val unit: String = "Hộp", // Added to match cart_items table
    val unitPrice: Double, // Changed from String to numeric
    val totalPrice: Double, // Changed from String to numeric
    val createdAt: String, // Match cart_items table (no updatedAt in schema)
    val updatedAt: String? = null // Keep for backward compatibility
)

@Serializable
data class AddCartRequest(
    val productId: String, // UUID from backend
    val quantity: Int,
    val unit: String = "Hộp" // Added to match cart_items table
)

@Serializable
data class UpdateCartItemRequest(
    val quantity: Int,
    val unit: String? = null // Added to allow unit updates
)

@Serializable
data class CartResponse(
    val message: String,
    val cart: CartDto
)

@Serializable
data class AddCartResponse(
    val message: String,
    val item: CartItemDto,
    val cart: CartDto
)