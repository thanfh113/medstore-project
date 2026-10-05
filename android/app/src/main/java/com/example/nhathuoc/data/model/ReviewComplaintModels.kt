package com.example.nhathuoc.data.model

import kotlinx.serialization.Serializable

@Serializable
data class ReviewAttachmentInput(
    val fileUrl: String,
    val fileType: String = "IMAGE",
    val publicId: String? = null,
    val sortOrder: Int = 0
)

@Serializable
data class CreateReviewRequest(
    val orderId: String? = null,
    val orderItemId: String? = null,
    val rating: Int,
    val title: String? = null,
    val comment: String? = null,
    val attachments: List<ReviewAttachmentInput> = emptyList()
)

@Serializable
data class ReportReviewRequest(
    val reason: String,
    val note: String? = null
)

@Serializable
data class ReviewIdResponse(
    val id: String
)

@Serializable
data class ReviewAttachmentDto(
    val id: String,
    val fileUrl: String,
    val fileType: String,
    val publicId: String? = null,
    val sortOrder: Int = 0,
    val createdAt: String
)

@Serializable
data class ReviewDto(
    val id: String,
    val productId: String,
    val userId: String,
    val userName: String? = null,
    val orderId: String? = null,
    val orderItemId: String? = null,
    val rating: Int,
    val title: String? = null,
    val comment: String? = null,
    val status: String,
    val isVerifiedPurchase: Boolean,
    val helpfulCount: Int = 0,
    val reportCount: Int = 0,
    val hiddenReason: String? = null,
    val moderatedBy: String? = null,
    val moderatedAt: String? = null,
    val createdAt: String,
    val updatedAt: String,
    val attachments: List<ReviewAttachmentDto> = emptyList()
)

@Serializable
data class ProductReviewSummaryDto(
    val productId: String,
    val averageRating: Double,
    val totalReviews: Int,
    val ratingCounts: Map<Int, Int> = emptyMap()
)

@Serializable
data class ProductReviewsResponse(
    val summary: ProductReviewSummaryDto,
    val reviews: List<ReviewDto>
)

@Serializable
data class ComplaintAttachmentInput(
    val fileUrl: String,
    val fileType: String = "IMAGE",
    val publicId: String? = null
)

@Serializable
data class CreateComplaintRequest(
    val orderId: String,
    val orderItemId: String? = null,
    val productId: String? = null,
    val type: String,
    val title: String,
    val description: String,
    val attachments: List<ComplaintAttachmentInput> = emptyList()
)

@Serializable
data class ComplaintMessageRequest(
    val message: String,
    val isInternal: Boolean = false
)

@Serializable
data class AddComplaintAttachmentsRequest(
    val attachments: List<ComplaintAttachmentInput>
)

@Serializable
data class ComplaintAttachmentDto(
    val id: String,
    val fileUrl: String,
    val fileType: String,
    val publicId: String? = null,
    val createdAt: String
)

@Serializable
data class ComplaintMessageDto(
    val id: String,
    val senderUserId: String,
    val senderName: String? = null,
    val senderRole: String,
    val message: String,
    val isInternal: Boolean,
    val createdAt: String
)

@Serializable
data class ComplaintEventDto(
    val id: String,
    val actorUserId: String? = null,
    val actorName: String? = null,
    val actorRole: String? = null,
    val eventType: String,
    val title: String,
    val description: String? = null,
    val fromStatus: String? = null,
    val toStatus: String? = null,
    val fromPriority: String? = null,
    val toPriority: String? = null,
    val dueAt: String? = null,
    val createdAt: String
)

@Serializable
data class ComplaintDto(
    val id: String,
    val complaintCode: String,
    val userId: String,
    val userName: String? = null,
    val orderId: String,
    val orderItemId: String? = null,
    val productId: String? = null,
    val productName: String? = null,
    val type: String,
    val title: String,
    val description: String,
    val status: String,
    val priority: String,
    val resolution: String? = null,
    val refundAmount: Double? = null,
    val refundStatus: String = "NONE",
    val refundMethod: String? = null,
    val refundTransactionId: String? = null,
    val refundedBy: String? = null,
    val refundedAt: String? = null,
    val handledBy: String? = null,
    val createdAt: String,
    val updatedAt: String,
    val resolvedAt: String? = null,
    val closedAt: String? = null,
    val attachments: List<ComplaintAttachmentDto> = emptyList(),
    val messages: List<ComplaintMessageDto> = emptyList(),
    val events: List<ComplaintEventDto> = emptyList()
)
