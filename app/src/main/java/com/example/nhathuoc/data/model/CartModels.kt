package com.example.nhathuoc.data.model

import kotlinx.serialization.Serializable

/**
 * Cart related data transfer objects
 */

@Serializable
data class CartDto(
    val id: Int,
    val userId: Int,
    val items: List<CartItemDto>,
    val totalItems: Int,
    val totalAmount: String, // "123.000đ" format
    val subtotal: String,
    val discount: String,
    val createdAt: String,
    val updatedAt: String
)

@Serializable
data class CartItemDto(
    val id: Int,
    val cartId: Int,
    val productId: Int,
    val product: ProductDto,
    val quantity: Int,
    val unitPrice: String, // "50.000đ"
    val totalPrice: String, // "150.000đ"
    val addedAt: String,
    val updatedAt: String
)

@Serializable
data class AddCartRequest(
    val productId: Int,
    val quantity: Int
)

@Serializable
data class UpdateCartItemRequest(
    val quantity: Int
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