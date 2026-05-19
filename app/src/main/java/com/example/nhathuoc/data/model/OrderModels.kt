package com.example.nhathuoc.data.model

import kotlinx.serialization.Serializable

/**
 * Order related data transfer objects
 */

@Serializable
data class OrderDto(
    val id: String, // UUID from backend
    val orderCode: String, // Changed from orderNumber to match schema
    val userId: String, // UUID from backend
    val status: String, // "PENDING", "PROCESSING", "SHIPPING", "DELIVERED", "CANCELLED", "RETURNED"
    val pickupType: String = "DELIVERY", // Added: "DELIVERY" or "PICKUP"
    val branchId: String? = null, // UUID from backend - Added for pickup orders
    val addressId: String? = null, // UUID from backend - Added address reference
    val items: List<OrderItemDto>,
    val subtotal: Double, // Changed from String to numeric
    val shippingFee: Double, // Changed from String to numeric
    val discount: Double, // Changed from String to numeric
    val pointsUsed: Int = 0, // Renamed from rewardPointsUsed
    val pointsEarned: Int = 0, // Renamed from rewardPointsEarned
    val total: Double, // Changed from String to numeric
    val paymentMethod: String, // "COD", "MOMO", "ZALOPAY"
    val paymentStatus: String, // "UNPAID", "PENDING", "COMPLETED", "FAILED", "REFUNDED"
    val shippingAddress: UserAddress? = null, // Keep for backward compatibility
    val note: String? = null, // Changed from notes to note (singular)
    val estimatedDelivery: String?,
    val deliveredAt: String?,
    val cancelledAt: String?,
    val cancelReason: String?,
    val createdAt: String,
    val updatedAt: String
)

@Serializable
data class OrderItemDto(
    val id: String,
    val orderId: String,
    val productId: String,
    val name: String,
    val product: ProductDto? = null,
    val imageUrl: String? = null,
    val quantity: Int,
    val unit: String,
    val price: Double,
    val totalPrice: Double? = null,
    val createdAt: String? = null,
    val hasReviewed: Boolean = false
)

@Serializable
data class OrderListResponse(
    val orders: List<OrderDto>,
    val pagination: PaginationInfo
)

@Serializable
data class CancelOrderRequest(
    val reason: String
)
