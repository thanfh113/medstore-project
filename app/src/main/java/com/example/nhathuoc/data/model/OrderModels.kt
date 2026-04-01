package com.example.nhathuoc.data.model

import kotlinx.serialization.Serializable
import com.example.nhathuoc.data.model.OrderStatus
import com.example.nhathuoc.data.model.PaymentMethod
import com.example.nhathuoc.data.model.PickupType

/**
 * Order related data transfer objects
 */

@Serializable
data class OrderDto(
    val id: String, // UUID from backend
    val orderCode: String, // Changed from orderNumber to match schema
    val userId: String, // UUID from backend
    val shopId: String, // UUID from backend - Added for shop relationship
    val status: String, // "PENDING", "CONFIRMED", "PREPARING", "SHIPPING", "DELIVERED", "CANCELLED"
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
    val paymentMethod: String, // "COD", "VNPAY", "MOMO"
    val paymentStatus: String, // "UNPAID", "PAID", "FAILED", "REFUNDED"
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
    val id: String, // UUID from backend
    val orderId: String, // UUID from backend
    val productId: String, // UUID from backend
    val name: String, // Added: Product name at time of order
    val product: ProductDto? = null, // Made optional for flexibility
    val quantity: Int,
    val unit: String, // Added to match order_items schema
    val price: Double, // Renamed from unitPrice to match schema
    val totalPrice: Double? = null, // Keep for calculated field
    val createdAt: String? = null // Keep for backward compatibility
)

@Serializable
data class PlaceOrderRequest(
    val items: List<PlaceOrderItem>,
    val paymentMethod: String,
    val pickupType: String = "DELIVERY", // Added: "DELIVERY" or "PICKUP"
    val shippingAddressId: String?, // UUID from backend - Optional for pickup orders
    val branchId: String? = null, // UUID from backend - Required for pickup orders
    val note: String? = null, // Changed from notes to note
    val pointsToUse: Int = 0 // Renamed from rewardPointsToUse
)

@Serializable
data class PlaceOrderItem(
    val productId: String, // UUID from backend
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

