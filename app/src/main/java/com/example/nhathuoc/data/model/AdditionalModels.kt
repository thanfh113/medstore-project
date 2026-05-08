package com.example.nhathuoc.data.model

import kotlinx.serialization.Serializable

/**
 * Additional models for completing the API service
 * Updated to match MySQL schema
 */

// Shops model based on shops table
@Serializable
data class ShopDto(
    val id: String, // UUID from backend
    val ownerId: String, // UUID from backend
    val name: String,
    val description: String? = null,
    val logoUrl: String? = null,
    val licenseNumber: String? = null,
    val isApproved: Boolean = false,
    val expiryAlertDays: Int = 30,
    val createdAt: String,
    val deletedAt: String? = null
)

// Pharmacy/Branch model based on pharmacy_branches table
@Serializable
data class PharmacyDto(
    val id: String, // UUID from backend
    val name: String,
    val address: String,
    val ward: String,
    val district: String,
    val province: String,
    val phone: String,
    val email: String? = null,
    val latitude: Double? = null, // Made optional as schema may not have
    val longitude: Double? = null, // Made optional as schema may not have
    val distance: Double? = null, // km from user location - calculated field
    val rating: Double? = null, // May be calculated field
    val totalReviews: Int? = null, // May be calculated field
    val isOpen: Boolean = true, // May be calculated field based on hours
    val openingHours: String? = null,
    val services: List<String> = emptyList(), // May be parsed from text field
    val imageUrl: String? = null,
    val isVerified: Boolean = false,
    val isActive: Boolean = true,
    val managerName: String? = null, // Added from schema
    val managerPhone: String? = null, // Added from schema
    val createdAt: String,
    val updatedAt: String
)

@Serializable
data class NotificationDto(
    val id: String, // UUID from backend
    val userId: String? = null, // UUID from backend - Match schema (can be null for broadcast)
    val title: String,
    val body: String? = null, // Match schema field name (was message)
    val message: String? = null, // Keep for backward compatibility
    val type: String, // ORDER, PROMOTION, REWARD, COMPLAINT, REFUND, CHAT, REVIEW, SYSTEM
    val refId: String? = null, // Match schema field name (was data map)
    val data: Map<String, String> = emptyMap(), // Keep for additional data
    val imageUrl: String? = null,
    val actionUrl: String? = null,
    val isRead: Boolean = false,
    val readAt: String? = null,
    val scheduledAt: String? = null, // Keep for future scheduling
    val expiryDate: String? = null, // Keep for expiring notifications
    val isActive: Boolean = true, // Keep for filtering
    val createdAt: String,
    val updatedAt: String? = null // Keep for updates
)

@Serializable
data class PushTokenRequest(
    val fcmToken: String,
    val platform: String = "ANDROID",
    val deviceId: String? = null,
    val appVersion: String? = null
)

@Serializable
data class PushTokenResponse(
    val id: String,
    val isActive: Boolean
)

@Serializable
data class BannerDto(
    val id: String, // UUID from backend
    val imageUrl: String, // Match schema field name
    val linkUrl: String? = null, // Match schema field name
    val title: String? = null, // Match schema field name
    val description: String? = null, // Keep for additional info
    val actionType: String? = null, // Keep for client logic
    val actionValue: String? = null, // Keep for client logic
    val position: String? = null, // Keep for positioning logic
    val priority: Int? = null, // Keep for ordering logic
    val sortOrder: Int = 0, // Match schema field name
    val startDt: String? = null, // Match schema field name (start_dt)
    val endDt: String? = null, // Match schema field name (end_dt)
    val isActive: Boolean = true,
    val createdAt: String? = null, // Keep for compatibility
    val updatedAt: String? = null // Keep for compatibility
)


@Serializable
data class ChatSessionDto(
    val id: String,
    val userId: String,
    val userName: String? = null,
    val userPhone: String? = null,
    val userEmail: String? = null,
    val productId: String? = null,
    val status: String,
    val createdAt: String,
    val lastMessage: ChatMessageDto? = null,
    val productName: String? = null,
    val productImageUrl: String? = null,
    val productPrice: Double? = null,
    val productUnit: String? = null,
    val consultantId: String? = null,
    val consultantName: String? = null,
    val consultantRole: String? = null,
    val consultantQualificationTitle: String? = null,
    val consultantQualificationSpecialty: String? = null,
    val consultantQualificationInstitution: String? = null,
    val consultantQualificationDocumentUrl: String? = null,
    val consultantQualificationDocumentType: String? = null,
    val consultantVerified: Boolean? = null
)

@Serializable
data class ChatMessageDto(
    val id: String,
    val sessionId: String,
    val senderId: String,
    val senderName: String? = null,
    val senderRole: String? = null,
    val content: String? = null,
    val type: String = "TEXT",
    val metadata: String? = null,
    val createdAt: String
)

@Serializable
data class ChatProductRecommendation(
    val productId: String,
    val productName: String,
    val productImage: String? = null,
    val price: Double,
    val productUnit: String? = null,
    val categoryId: String? = null,
    val description: String = "",
    val reason: String = "",
    val dosageInstructions: String? = null,
    val warnings: String? = null,
    val alternatives: List<String> = emptyList()
)

@Serializable
data class CreateChatSessionRequest(
    val productId: String? = null
)

@Serializable
data class SendChatMessageRequest(
    val content: String,
    val type: String = "TEXT",
    val metadata: String? = null
)

@Serializable
data class UpdateChatSessionStatusRequest(
    val status: String
)

@Serializable
data class PharmacistDto(
    val id: String, // UUID from backend
    val fullName: String,
    val phone: String,
    val email: String,
    val licenseNumber: String,
    val specialization: String?,
    val experience: Int, // years
    val rating: Double,
    val isOnline: Boolean,
    val avatarUrl: String?
)

// Product-level stock management models (for future shop/admin integration)
@Serializable
data class CreateStockReceiptRequest(
    val productId: String, // UUID from backend
    val mfgDate: String? = null, // Manufacturing date
    val expDate: String? = null, // Expiry date
    val quantity: Int,
    val importPrice: Double? = null,
    val note: String? = null
)

@Serializable
data class StockEntryDto(
    val id: String, // UUID from backend
    val productId: String, // UUID from backend
    val product: ProductDto,
    val mfgDate: String?,
    val expDate: String?,
    val quantity: Int,
    val remainingQuantity: Int,
    val importPrice: Double?,
    val note: String?,
    val createdAt: String,
    val updatedAt: String
)

@Serializable
data class ExpiringAlert(
    val id: String, // UUID from backend
    val stockEntry: StockEntryDto,
    val daysUntilExpiry: Int,
    val alertLevel: String // "warning", "critical"
)

@Serializable
data class ExpiringAlertsResponse(
    val alerts: List<ExpiringAlert>,
    val totalExpiring: Int
)

// Prescription model based on prescriptions table
@Serializable
data class PrescriptionDto(
    val id: String, // UUID from backend
    val userId: String, // UUID from backend
    val doctorName: String,
    val hospitalName: String,
    val prescriptionDate: String,
    val notes: String? = null,
    val imageUrls: List<String> = emptyList(), // Multiple prescription images
    val status: String = "PENDING", // "PENDING", "APPROVED", "REJECTED"
    val pharmacistNotes: String? = null,
    val createdAt: String,
    val updatedAt: String
)

// Payment model based on payments table
@Serializable
data class PaymentDto(
    val id: String, // UUID from backend
    val orderId: String, // UUID from backend
    val method: String, // "COD", "MOMO", "ZALOPAY"
    val status: String, // "UNPAID", "PENDING", "COMPLETED", "FAILED", "REFUNDED"
    val amount: Double,
    val transactionId: String? = null, // External transaction ID
    val gatewayResponse: String? = null, // JSON response from payment gateway
    val paidAt: String? = null,
    val refundedAt: String? = null,
    val createdAt: String,
    val updatedAt: String
)

