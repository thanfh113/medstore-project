package org.example.project.data.models

import kotlinx.serialization.Serializable

@Serializable
data class Order(
    val id: String,
    val customerId: String,
    val customerName: String,
    val customerPhone: String,
    val customerEmail: String? = null,
    val items: List<OrderItem>,
    val status: OrderStatus,
    val totalAmount: Double,
    val paymentMethod: PaymentMethod,
    val deliveryAddress: Address,
    val notes: String? = null,
    val quotationImageUrl: String? = null, // Ảnh yêu cầu báo giá (for quotation requests)
    val technicianNote: String? = null, // Ghi chú của chuyên viên kỹ thuật
    val deliveryFee: Double = 0.0,
    val discount: Double = 0.0,
    val finalAmount: Double = totalAmount + deliveryFee - discount,
    val estimatedDeliveryTime: String? = null,
    val actualDeliveryTime: String? = null,
    val createdAt: String,
    val updatedAt: String
)

@Serializable
data class OrderItem(
    val id: String,
    val productId: String,
    val productName: String,
    val productImage: String? = null,
    val unitPrice: Double,
    val quantity: Int,
    val totalPrice: Double = unitPrice * quantity,
    val unit: String = "cái",
    val dosageInstructions: String? = null // Hướng dẫn sử dụng
)

@Serializable
enum class OrderStatus(val value: String, val displayName: String, val color: String, val description: String) {
    PENDING("PENDING", "Chờ xử lý", "#FFAB00", "Đơn hàng vừa được tạo, đang chờ xác nhận"),
    CONFIRMED("CONFIRMED", "Đã xác nhận", "#4CAF50", "Đơn hàng đã được xác nhận và chuẩn bị xử lý"),
    PREPARING("PREPARING", "Đang chuẩn bị", "#FFAB00", "Đang chuẩn bị thuốc và đóng gói"),
    SHIPPING("SHIPPING", "Đang giao", "#2196F3", "Đơn hàng đang được vận chuyển"),
    DELIVERED("DELIVERED", "Đã giao", "#4CAF50", "Đã giao hàng thành công"),
    CANCELLED("CANCELLED", "Đã hủy", "#F44336", "Đơn hàng đã bị hủy"),
    RETURNED("RETURNED", "Đã trả", "#9E9E9E", "Đơn hàng đã được trả lại")
}

@Serializable
enum class PaymentMethod(val value: String, val displayName: String, val icon: String) {
    CASH("CASH", "Tiền mặt", "💵"),
    BANK_TRANSFER("BANK_TRANSFER", "Chuyển khoản ngân hàng", "🏦"),
    MOMO("MOMO", "MoMo", "📱"),
    VNPAY("VNPAY", "VNPay", "💰"),
    COD("COD", "Thanh toán COD", "📦")
}

@Serializable
enum class PaymentStatus(val value: String, val displayName: String, val color: String) {
    PENDING("PENDING", "Chờ thanh toán", "#FFAB00"),
    PAID_ELECTRONIC("PAID_ELECTRONIC", "Đã thanh toán điện tử", "#4CAF50"),
    PAID_COD("PAID_COD", "Thanh toán COD", "#2196F3"),
    FAILED("FAILED", "Thanh toán thất bại", "#F44336"),
    REFUNDED("REFUNDED", "Đã hoàn tiền", "#9E9E9E")
}

@Serializable
data class Address(
    val id: String,
    val fullAddress: String,
    val street: String,
    val ward: String,
    val district: String,
    val city: String,
    val postalCode: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val recipientName: String,
    val recipientPhone: String,
    val isDefault: Boolean = false,
    val addressType: AddressType = AddressType.HOME
)

@Serializable
enum class AddressType(val value: String, val displayName: String) {
    HOME("HOME", "Nhà riêng"),
    OFFICE("OFFICE", "Văn phòng"),
    OTHER("OTHER", "Khác")
}

@Serializable
data class Customer(
    val id: String,
    val fullName: String,
    val phone: String,
    val email: String? = null,
    val dateOfBirth: String? = null,
    val gender: Gender? = null,
    val addresses: List<Address> = emptyList(),
    val rewardPoints: Int = 0,
    val membershipLevel: MembershipLevel = MembershipLevel.BRONZE,
    val registrationDate: String,
    val lastOrderDate: String? = null,
    val totalOrders: Int = 0,
    val totalSpent: Double = 0.0,
    val isActive: Boolean = true,
    val notes: String? = null // Ghi chú y tế, dị ứng, v.v.
)

@Serializable
enum class Gender(val value: String, val displayName: String) {
    MALE("MALE", "Nam"),
    FEMALE("FEMALE", "Nữ"),
    OTHER("OTHER", "Khác")
}

@Serializable
enum class MembershipLevel(val value: String, val displayName: String, val discountPercent: Double, val color: String) {
    BRONZE("BRONZE", "Đồng", 0.0, "#CD7F32"),
    SILVER("SILVER", "Bạc", 3.0, "#C0C0C0"),
    GOLD("GOLD", "Vàng", 5.0, "#FFD700"),
    PLATINUM("PLATINUM", "Bạch kim", 10.0, "#E5E4E2"),
    DIAMOND("DIAMOND", "Kim cương", 15.0, "#B9F2FF")
}

// Order tracking history
@Serializable
data class OrderStatusHistory(
    val id: String,
    val orderId: String,
    val status: OrderStatus,
    val timestamp: String,
    val note: String? = null,
    val updatedBy: String, // Staff ID who updated the status
    val location: String? = null // Current location for shipping status
)