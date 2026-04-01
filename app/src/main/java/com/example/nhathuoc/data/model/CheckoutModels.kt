package com.example.nhathuoc.data.model

// User address model
data class UserAddressDto(
    val id: String,
    val fullName: String,
    val phone: String,
    val address: String,
    val district: String,
    val city: String,
    val type: AddressType = AddressType.HOME,
    val isDefault: Boolean = false
)

// Order item model
data class OrderItemDto(
    val id: String,
    val name: String,
    val quantity: Int,
    val price: Double
)

// Checkout response model
data class CheckoutPreviewDto(
    val orderId: String,
    val items: List<OrderItemDto>,
    val pricing: PricingBreakdown
)

data class PricingBreakdown(
    val subtotal: Double,
    val discount5percent: Double,
    val shipping: Double,
    val tax: Double,
    val rewardPointsDiscount: Double,
    val finalTotal: Double
)

// Address type enum
enum class AddressType {
    HOME,
    OFFICE,
    OTHER
}

// Pickup type enum
enum class PickupType {
    DELIVERY,
    PICKUP
}

// Payment progress enum
enum class PaymentProgress {
    PROCESSING,
    SUCCESS,
    FAILED
}

// Cart item DTO
data class CartItemDto(
    val id: String,
    val productId: String,
    val productName: String? = null,
    val quantity: Int = 1,
    val price: Double? = null,
    val subtotal: Double? = null,
    val createdAt: String? = null
)

// Cart DTO
data class CartDto(
    val id: String,
    val userId: String,
    val items: List<CartItemDto>,
    val totalItems: Int = 0,
    val totalPrice: Double = 0.0,
    val createdAt: String? = null,
    val updatedAt: String? = null
)

// Product with detailed info
data class ProductDetailDto(
    val id: String,
    val name: String,
    val description: String? = null,
    val price: Double,
    val originalPrice: Double? = null,
    val image: String? = null,
    val images: List<String>? = emptyList(),
    val brand: String? = null,
    val category: String? = null,
    val stock: Int = 0,
    val rating: Double? = null,
    val reviewCount: Int = 0,
    val ingredients: String? = null,
    val indications: String? = null,
    val contraindications: String? = null,
    val dosage: String? = null,
    val sideEffects: String? = null,
    val storage: String? = null,
    val certificate: String? = null,
    val createdAt: String? = null
)

// Payment status response
data class PaymentStatusDto(
    val orderId: String,
    val status: String, // SUCCESS, FAILED, PENDING
    val amount: Double,
    val method: PaymentMethod,
    val transactionId: String? = null,
    val message: String? = null,
    val updatedAt: String? = null
)

// Order response
data class OrderDto(
    val id: String,
    val userId: String,
    val items: List<OrderItemDto>,
    val totalAmount: Double,
    val status: OrderStatus,
    val paymentStatus: PaymentStatus,
    val paymentMethod: PaymentMethod,
    val shippingAddress: UserAddressDto? = null,
    val shippingType: PickupType,
    val notes: String? = null,
    val rewardPointsUsed: Int = 0,
    val createdAt: String? = null,
    val updatedAt: String? = null
)

// Order status enum
enum class OrderStatus {
    PENDING,
    CONFIRMED,
    PROCESSING,
    SHIPPING,
    DELIVERED,
    CANCELLED
}

// Payment status enum
enum class PaymentStatus {
    UNPAID,
    COMPLETED,
    FAILED,
    REFUNDED
}
