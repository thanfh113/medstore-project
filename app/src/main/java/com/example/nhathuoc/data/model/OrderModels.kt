package com.example.nhathuoc.data.model

import kotlinx.serialization.Serializable

/**
 * Order related data transfer objects
 */

@Serializable
data class OrderDto(
    val id: Int,
    val userId: Int,
    val orderNumber: String, // "ORD-2024-001"
    val status: String, // "pending", "confirmed", "preparing", "shipping", "delivered", "cancelled"
    val items: List<OrderItemDto>,
    val subtotal: String, // "500.000đ"
    val shippingFee: String, // "30.000đ"
    val discount: String, // "0đ"
    val total: String, // "530.000đ"
    val paymentMethod: String, // "cod", "vnpay", "momo"
    val paymentStatus: String, // "pending", "paid", "failed", "refunded"
    val shippingAddress: UserAddress,
    val notes: String?,
    val rewardPointsEarned: Int,
    val rewardPointsUsed: Int,
    val estimatedDelivery: String?,
    val deliveredAt: String?,
    val cancelledAt: String?,
    val cancelReason: String?,
    val createdAt: String,
    val updatedAt: String
)

@Serializable
data class OrderItemDto(
    val id: Int,
    val orderId: Int,
    val productId: Int,
    val product: ProductDto,
    val quantity: Int,
    val unitPrice: String,
    val totalPrice: String,
    val addedAt: String
)

@Serializable
data class PlaceOrderRequest(
    val items: List<PlaceOrderItem>,
    val paymentMethod: String,
    val shippingAddressId: Int,
    val notes: String? = null,
    val rewardPointsToUse: Int = 0
)

@Serializable
data class PlaceOrderItem(
    val productId: Int,
    val quantity: Int
)

@Serializable
data class PlaceOrderResponse(
    val message: String,
    val order: OrderDto,
    val paymentUrl: String? = null // For online payment methods
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

@Serializable
data class CancelOrderResponse(
    val message: String,
    val order: OrderDto
)

// Order status tracking
enum class OrderStatus(val value: String, val displayName: String) {
    PENDING("pending", "Chờ xác nhận"),
    CONFIRMED("confirmed", "Đã xác nhận"),
    PREPARING("preparing", "Đang chuẩn bị"),
    SHIPPING("shipping", "Đang giao"),
    DELIVERED("delivered", "Đã giao"),
    CANCELLED("cancelled", "Đã hủy");

    companion object {
        fun fromValue(value: String) = values().find { it.value == value } ?: PENDING
    }
}

enum class PaymentMethod(val value: String, val displayName: String) {
    COD("cod", "Thanh toán khi nhận hàng"),
    VNPAY("vnpay", "VNPay"),
    MOMO("momo", "Ví MoMo");

    companion object {
        fun fromValue(value: String) = values().find { it.value == value } ?: COD
    }
}