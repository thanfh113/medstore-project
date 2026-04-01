package com.example.nhathuoc.data.model

import kotlinx.serialization.Serializable

/**
 * Enum definitions to replace string literals throughout the application
 * Improves type safety and reduces string-based errors
 */

@Serializable
enum class ProductType(val value: String) {
    MEDICINE("MEDICINE"),
    SUPPLEMENT("SUPPLEMENT"),
    DEVICE("DEVICE"),
    COSMETIC("COSMETIC"),
    PERSONAL_CARE("PERSONAL_CARE")
}

@Serializable
enum class OrderStatus(val value: String, val displayName: String) {
    PENDING("PENDING", "Đang chờ"),
    CONFIRMED("CONFIRMED", "Đã xác nhận"),
    PREPARING("PREPARING", "Đang chuẩn bị"),
    SHIPPING("SHIPPING", "Đang giao hàng"),
    DELIVERED("DELIVERED", "Đã giao"),
    CANCELLED("CANCELLED", "Đã hủy"),
    RETURNED("RETURNED", "Đã trả lại");

    companion object {
        fun fromValue(value: String) = entries.find { it.value == value } ?: PENDING
    }
}

@Serializable
enum class PaymentMethod(val value: String, val displayName: String) {
    COD("COD", "Thanh toán khi nhận hàng"),
    VNPAY("VNPAY", "VNPay"),
    MOMO("MOMO", "MoMo"),
    BANK_TRANSFER("BANK_TRANSFER", "Chuyển khoản ngân hàng"),
    CREDIT_CARD("CREDIT_CARD", "Thẻ tín dụng");

    companion object {
        fun fromValue(value: String) = entries.find { it.value == value } ?: COD
    }
}

@Serializable
enum class PaymentStatus(val value: String, val displayName: String) {
    UNPAID("UNPAID", "Chưa thanh toán"),
    PAID("PAID", "Đã thanh toán"),
    PENDING("PENDING", "Đang xử lý"),
    FAILED("FAILED", "Thất bại"),
    REFUNDED("REFUNDED", "Đã hoàn tiền")
}

@Serializable
enum class PickupType(val value: String, val displayName: String) {
    DELIVERY("DELIVERY", "Giao hàng tận nơi"),
    STORE_PICKUP("STORE_PICKUP", "Lấy tại cửa hàng");

    companion object {
        fun fromValue(value: String) = entries.find { it.value == value } ?: DELIVERY
    }
}

@Serializable
enum class RewardTier(val value: String, val displayName: String, val minSpent: Double) {
    BRONZE("BRONZE", "Đồng", 0.0),
    SILVER("SILVER", "Bạc", 1000000.0),
    GOLD("GOLD", "Vàng", 5000000.0),
    PLATINUM("PLATINUM", "Bạch kim", 20000000.0)
}

@Serializable
enum class Gender(val value: Int, val displayName: String) {
    MALE(1, "Nam"),
    FEMALE(2, "Nữ"),
    OTHER(3, "Khác")
}

@Serializable
enum class NotificationType(val value: String, val displayName: String) {
    ORDER("ORDER", "Đơn hàng"),
    PROMOTION("PROMOTION", "Khuyến mãi"),
    SYSTEM("SYSTEM", "Hệ thống"),
    REWARD("REWARD", "Điểm thưởng")
}

@Serializable
enum class AddressType(val value: String, val displayName: String) {
    HOME("HOME", "Nhà riêng"),
    OFFICE("OFFICE", "Văn phòng"),
    OTHER("OTHER", "Khác")
}

@Serializable
enum class ProductSortOrder(val value: String, val displayName: String) {
    PRICE_ASC("price_asc", "Giá tăng dần"),
    PRICE_DESC("price_desc", "Giá giảm dần"),
    NAME_ASC("name_asc", "Tên A-Z"),
    NAME_DESC("name_desc", "Tên Z-A"),
    CREATED_ASC("created_asc", "Mới nhất"),
    CREATED_DESC("created_desc", "Cũ nhất"),
    BEST_SELLING("best_selling", "Bán chạy"),
    FEATURED("featured", "Nổi bật")
}