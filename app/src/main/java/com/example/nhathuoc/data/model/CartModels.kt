package com.example.nhathuoc.data.model

import kotlinx.serialization.Serializable

/**
 * Cart DTOs are aligned with backend CartSummaryResponse:
 * GET /api/v1/cart returns { data: CartDto, message }.
 */
@Serializable
data class CartDto(
    val items: List<CartItemDto> = emptyList(),
    val totalItems: Int = 0,
    val subtotal: Double = 0.0,
    val estimatedShipping: Double = 0.0,
    val total: Double = 0.0,
    val id: String = "",
    val userId: String = "",
    val discount: Double = 0.0,
    val createdAt: String = "",
    val updatedAt: String = ""
) {
    val totalAmount: Double get() = total
}

@Serializable
data class CartItemDto(
    val id: String,
    val userId: String = "",
    val productId: String,
    val productName: String = "",
    val productPrice: Double = 0.0,
    val productImageUrl: String? = null,
    val quantity: Int,
    val unit: String = "Cái",
    val subtotal: Double = 0.0,
    val isAvailable: Boolean = true,
    val stock: Int = 0,
    val createdAt: String = "",
    val product: ProductDto? = null,
    val updatedAt: String? = null
) {
    val displayName: String get() = product?.name ?: productName
    val unitPrice: Double get() = product?.price ?: productPrice
    val totalPrice: Double get() = if (subtotal > 0.0) subtotal else unitPrice * quantity
    val imageUrl: String? get() = product?.imageUrl ?: productImageUrl
}

@Serializable
data class AddCartRequest(
    val productId: String,
    val quantity: Int,
    val unit: String = "Cái"
)

@Serializable
data class UpdateCartItemRequest(
    val quantity: Int,
    val unit: String? = null
)

@Serializable
data class CartResponse(
    val message: String,
    val cart: CartDto? = null
)

@Serializable
data class AddCartResponse(
    val message: String,
    val item: CartItemDto? = null,
    val cart: CartDto? = null
)
