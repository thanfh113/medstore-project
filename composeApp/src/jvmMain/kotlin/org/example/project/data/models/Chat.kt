package org.example.project.data.models

import kotlinx.serialization.Serializable

@Serializable
data class ChatConversation(
    val id: String,
    val customerId: String,
    val customerName: String,
    val customerPhone: String,
    val customerAvatar: String? = null,
    val pharmacistId: String? = null,
    val pharmacistName: String? = null,
    val status: ChatStatus,
    val lastMessage: ChatMessage? = null,
    val lastMessageAt: String,
    val unreadCount: Int = 0,
    val tags: List<String> = emptyList(),
    val priority: ChatPriority = ChatPriority.NORMAL,
    val createdAt: String,
    val updatedAt: String,
    val notes: String? = null,
    val customerHealthInfo: CustomerHealthInfo? = null,
    val productId: String? = null
)

@Serializable
data class ChatMessage(
    val id: String,
    val conversationId: String,
    val senderId: String,
    val senderName: String,
    val senderType: SenderType,
    val content: String,
    val messageType: MessageType,
    val timestamp: String,
    val attachmentUrl: String? = null,
    val attachmentType: AttachmentType? = null,
    val productRecommendation: ProductRecommendation? = null,
    val isRead: Boolean = false,
    val replyToMessageId: String? = null,
    val isEdited: Boolean = false,
    val editedAt: String? = null,
    val metadata: String? = null
)

@Serializable
enum class ChatStatus(val value: String, val displayName: String, val color: String) {
    PENDING("PENDING", "Chờ tiếp nhận", "#FFAB00"),
    ASSIGNED("ASSIGNED", "Đang tư vấn", "#4CAF50"),
    RESOLVED("RESOLVED", "Đã kết thúc", "#2196F3");

    companion object {
        fun fromValue(value: String?): ChatStatus =
            entries.firstOrNull { it.value.equals(value, ignoreCase = true) } ?: PENDING
    }
}

@Serializable
enum class ChatPriority(val value: String, val displayName: String, val color: String) {
    LOW("LOW", "Thấp", "#4CAF50"),
    NORMAL("NORMAL", "Bình thường", "#2196F3"),
    HIGH("HIGH", "Cao", "#FFAB00"),
    URGENT("URGENT", "Khẩn cấp", "#F44336")
}

@Serializable
enum class SenderType(val value: String, val displayName: String) {
    CUSTOMER("CUSTOMER", "Khách hàng"),
    PHARMACIST("PHARMACIST", "Nhân viên chuyên môn"),
    SYSTEM("SYSTEM", "Hệ thống")
}

@Serializable
enum class MessageType(val value: String, val displayName: String) {
    TEXT("TEXT", "Tin nhắn"),
    IMAGE("IMAGE", "Hình ảnh"),
    DOCUMENT("DOCUMENT", "Tài liệu"),
    PRODUCT_RECOMMENDATION("PRODUCT_RECOMMENDATION", "Gợi ý sản phẩm"),
    SYSTEM_MESSAGE("SYSTEM_MESSAGE", "Thông báo hệ thống")
}

@Serializable
enum class AttachmentType(val value: String, val displayName: String) {
    IMAGE("IMAGE", "Hình ảnh"),
    PDF("PDF", "File PDF"),
    DOCUMENT("DOCUMENT", "Tài liệu")
}

@Serializable
data class ProductRecommendation(
    val productId: String,
    val productName: String,
    val productImage: String? = null,
    val price: Double,
    val description: String,
    val reason: String,
    val dosageInstructions: String? = null,
    val warnings: String? = null,
    val alternatives: List<String> = emptyList()
)

@Serializable
data class CustomerHealthInfo(
    val allergies: List<String> = emptyList(),
    val chronicConditions: List<String> = emptyList(),
    val currentMedications: List<String> = emptyList(),
    val age: Int? = null,
    val weight: Double? = null,
    val height: Double? = null,
    val bloodType: String? = null,
    val emergencyContact: String? = null,
    val lastUpdated: String? = null
)

@Serializable
data class ChatTemplate(
    val id: String,
    val title: String,
    val content: String,
    val category: String,
    val isActive: Boolean = true,
    val usageCount: Int = 0,
    val createdAt: String
)

@Serializable
data class ChatStatistics(
    val totalConversations: Int,
    val activeConversations: Int,
    val avgResponseTime: Double,
    val customerSatisfactionRating: Double,
    val resolvedToday: Int,
    val pendingConversations: Int,
    val date: String
)
