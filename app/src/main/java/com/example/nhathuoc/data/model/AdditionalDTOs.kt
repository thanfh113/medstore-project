package com.example.nhathuoc.data.model

// User Profile DTO
data class UserProfileDto(
    val id: String,
    val fullName: String,
    val email: String,
    val phone: String,
    val dateOfBirth: String? = null,
    val avatar: String? = null,
    val gender: Int? = null,
    val role: String = "USER",
    val createdAt: String? = null,
    val updatedAt: String? = null
)

// Reward DTO
data class RewardDto(
    val id: String,
    val userId: String,
    val totalPoints: Int,
    val pointValue: Double = totalPoints * 1000.0,
    val tier: String = "BRONZE", // BRONZE, SILVER, GOLD, PLATINUM
    val nextTierPoints: Int = 0,
    val createdAt: String? = null,
    val updatedAt: String? = null
)

// Reward History DTO
data class RewardHistoryDto(
    val id: String,
    val type: String, // EARNING, REDEMPTION
    val points: Int,
    val description: String,
    val relatedOrderId: String? = null,
    val createdAt: String? = null
)

// Notification DTO
data class NotificationDto(
    val id: String,
    val userId: String,
    val title: String,
    val message: String,
    val type: String, // ORDER, PROMOTION, SYSTEM
    val relatedId: String? = null,
    val isRead: Boolean = false,
    val createdAt: String? = null
)

// Doctor DTO
data class DoctorDto(
    val id: String,
    val name: String,
    val specialization: String,
    val description: String? = null,
    val avatar: String? = null,
    val rating: Double = 5.0,
    val reviewCount: Int = 0,
    val price: Double = 500000.0,
    val experience: String? = null,
    val qualification: String? = null,
    val workingHours: String? = null,
    val isAvailable: Boolean = true,
    val createdAt: String? = null
)

// Consultation DTO
data class ConsultationDto(
    val id: String,
    val consultationCode: String,
    val userId: String,
    val doctorId: String,
    val doctorName: String? = null,
    val scheduledAt: String,
    val status: String, // SCHEDULED, COMPLETED, CANCELLED
    val sessionType: String, // VIDEO, AUDIO, CHAT
    val reason: String,
    val notes: String? = null,
    val fee: Double = 500000.0,
    val paymentStatus: String = "UNPAID", // UNPAID, PAID, REFUNDED
    val rating: Double? = null,
    val review: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null
)

// Message DTO for consultation chat
data class ConsultationMessageDto(
    val id: String,
    val consultationId: String,
    val senderId: String,
    val senderName: String,
    val senderType: String, // USER, DOCTOR
    val message: String,
    val attachmentUrl: String? = null,
    val createdAt: String? = null
)

// Upload response DTO
data class UploadResponseDto(
    val url: String,
    val publicId: String,
    val size: Long,
    val mimeType: String,
    val createdAt: String? = null
)

// Pagination helper
data class PaginatedResponse<T>(
    val data: List<T>,
    val totalCount: Int,
    val page: Int,
    val limit: Int,
    val hasMore: Boolean
)
