package org.example.project.data.network

import kotlinx.serialization.Serializable
import org.example.project.data.models.ChatPriority
import org.example.project.data.models.ChatStatus

@Serializable
data class ProductImagePayload(
    val url: String,
    val publicId: String? = null,
    val sortOrder: Int = 0
)

@Serializable
data class UpdateProductRequest(
    val categoryId: String,
    val name: String,
    val description: String? = null,
    val brand: String? = null,
    val origin: String? = null,
    val sku: String? = null,
    val unit: String,
    val price: Double,
    val originalPrice: Double? = null,
    val stock: Int = 0,
    val discountPct: Int? = null,
    val rewardPoints: Int? = null,
    val registrationNumber: String? = null,
    val riskClassification: String = "A",
    val requiresCertification: Boolean = false,
    val requiresConsultation: Boolean = false,
    val isActive: Boolean = true,
    val attributes: Map<String, String> = emptyMap(),
    val images: List<ProductImagePayload> = emptyList()
)

@Serializable
data class CreateConversationRequest(
    val customerId: String,
    val customerName: String,
    val customerPhone: String,
    val priority: ChatPriority = ChatPriority.NORMAL,
    val notes: String? = null
)

@Serializable
data class UpdateConversationRequest(
    val status: ChatStatus? = null,
    val pharmacistId: String? = null,
    val notes: String? = null,
    val priority: ChatPriority? = null
)

@Serializable
data class SendMessageRequest(
    val conversationId: String,
    val senderId: String,
    val content: String,
    val messageType: String = "TEXT"
)

@Serializable
data class CreateChatTemplateRequest(
    val title: String,
    val content: String,
    val category: String,
    val isActive: Boolean = true
)

@Serializable
data class UpdateChatTemplateRequest(
    val title: String? = null,
    val content: String? = null,
    val category: String? = null,
    val isActive: Boolean? = null
)

@Serializable
data class ResponseTimeAnalytics(
    val averageResponseTimeMinutes: Double = 0.0,
    val minResponseTimeMinutes: Double = 0.0,
    val maxResponseTimeMinutes: Double = 0.0,
    val totalResponses: Int = 0
)


