package com.example.nhathuoc.data.model

import kotlinx.serialization.Serializable

/**
 * Additional models for completing the API service
 */

@Serializable
data class PharmacyDto(
    val id: Int,
    val name: String,
    val address: String,
    val ward: String,
    val district: String,
    val province: String,
    val phone: String,
    val email: String?,
    val latitude: Double,
    val longitude: Double,
    val distance: Double? = null, // km from user location
    val rating: Double,
    val totalReviews: Int,
    val isOpen: Boolean,
    val openingHours: String,
    val services: List<String>,
    val imageUrl: String?,
    val isVerified: Boolean = false,
    val isActive: Boolean = true,
    val createdAt: String,
    val updatedAt: String
)

@Serializable
data class NotificationDto(
    val id: Int,
    val userId: Int?,
    val title: String,
    val message: String,
    val type: String, // "order", "promotion", "system", "reminder"
    val data: Map<String, String> = emptyMap(), // Additional data like orderId, etc.
    val imageUrl: String? = null,
    val actionUrl: String? = null,
    val isRead: Boolean = false,
    val readAt: String? = null,
    val scheduledAt: String? = null,
    val expiryDate: String? = null,
    val isActive: Boolean = true,
    val createdAt: String,
    val updatedAt: String
)

@Serializable
data class BannerDto(
    val id: Int,
    val title: String,
    val description: String?,
    val imageUrl: String,
    val actionType: String, // "url", "product", "category", "none"
    val actionValue: String?, // URL, productId, categoryId based on actionType
    val position: String, // "home_hero", "home_middle", "category_top"
    val priority: Int, // Display order
    val startDate: String?,
    val endDate: String?,
    val isActive: Boolean = true,
    val createdAt: String,
    val updatedAt: String
)

@Serializable
data class VaccineDto(
    val id: Int,
    val name: String,
    val description: String,
    val manufacturer: String,
    val diseasesPrevented: List<String>,
    val ageGroup: String, // "infant", "child", "adult", "elderly", "all"
    val doses: Int,
    val interval: String, // Between doses
    val price: String,
    val imageUrl: String?,
    val sideEffects: String?,
    val contraindications: String?,
    val isActive: Boolean = true,
    val createdAt: String,
    val updatedAt: String
)

@Serializable
data class VaccineBookingDto(
    val id: Int,
    val userId: Int,
    val vaccineId: Int,
    val vaccine: VaccineDto,
    val pharmacyId: Int,
    val pharmacy: PharmacyDto,
    val appointmentDate: String,
    val appointmentTime: String,
    val patientName: String,
    val patientAge: Int,
    val patientGender: String,
    val notes: String?,
    val status: String, // "scheduled", "completed", "cancelled", "missed"
    val completedAt: String?,
    val cancelledAt: String?,
    val createdAt: String,
    val updatedAt: String
)

@Serializable
data class ChatSessionDto(
    val id: Int,
    val userId: Int,
    val pharmacistId: Int?,
    val pharmacist: PharmacistDto?,
    val type: String, // "general", "product", "order", "prescription"
    val subject: String?,
    val status: String, // "active", "closed", "waiting"
    val lastMessage: ChatMessageDto?,
    val unreadCount: Int,
    val createdAt: String,
    val updatedAt: String
)

@Serializable
data class ChatMessageDto(
    val id: Int,
    val sessionId: Int,
    val senderId: Int,
    val senderType: String, // "user", "pharmacist", "system"
    val message: String,
    val messageType: String, // "text", "image", "file", "product"
    val attachmentUrl: String? = null,
    val metadata: Map<String, String> = emptyMap(),
    val isRead: Boolean = false,
    val readAt: String? = null,
    val createdAt: String
)

@Serializable
data class PharmacistDto(
    val id: Int,
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