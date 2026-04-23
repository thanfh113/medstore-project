package org.example.project.data.repositories

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.websocket.DefaultClientWebSocketSession
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import io.ktor.websocket.Frame
import io.ktor.websocket.readText
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.isActive
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.example.project.data.models.ChatConversation
import org.example.project.data.models.ChatMessage
import org.example.project.data.models.ChatStatus
import org.example.project.data.models.MessageType
import org.example.project.data.models.SenderType

@Serializable
private data class ChatDataEnvelope<T>(
    val data: T,
    val message: String? = null
)

@Serializable
private data class ChatSessionResponseDto(
    val id: String,
    val userId: String,
    val productId: String? = null,
    val status: String,
    val createdAt: String,
    val lastMessage: ChatMessageResponseDto? = null
)

@Serializable
private data class ChatMessageResponseDto(
    val id: String,
    val sessionId: String,
    val senderId: String,
    val senderName: String? = null,
    val senderRole: String? = null,
    val content: String? = null,
    val type: String,
    val metadata: String? = null,
    val createdAt: String
)

@Serializable
private data class SendChatMessageRequestDto(
    val content: String,
    val type: String = "TEXT",
    val metadata: String? = null
)

class ChatRepository(private val client: HttpClient) {

    private val restBaseUrl = "http://localhost:8080/api/v1/internal/chat"
    private val wsBaseUrl = "ws://localhost:8080/api/v1/ws/chat"
    private val json = Json { ignoreUnknownKeys = true }
    private var session: DefaultClientWebSocketSession? = null

    suspend fun getSessions(token: String, status: String? = null): Result<List<ChatConversation>> = try {
        val response = client.get("$restBaseUrl/sessions") {
            header(HttpHeaders.Authorization, "Bearer $token")
            status?.takeIf { it.isNotBlank() }?.let { url.parameters.append("status", it) }
        }

        if (!response.status.isSuccess()) {
            return Result.failure(IllegalStateException(extractErrorMessage(response.bodyAsText())))
        }

        val payload = response.body<ChatDataEnvelope<List<ChatSessionResponseDto>>>()
        Result.success(payload.data.map(::mapSession))
    } catch (e: Exception) {
        Result.failure(IllegalStateException(e.message ?: "Không thể tải danh sách tư vấn"))
    }

    suspend fun getMessages(token: String, sessionId: String): Result<List<ChatMessage>> = try {
        val response = client.get("$restBaseUrl/sessions/$sessionId/messages") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }

        if (!response.status.isSuccess()) {
            return Result.failure(IllegalStateException(extractErrorMessage(response.bodyAsText())))
        }

        val payload = response.body<ChatDataEnvelope<List<ChatMessageResponseDto>>>()
        Result.success(payload.data.map(::mapMessage))
    } catch (e: Exception) {
        Result.failure(IllegalStateException(e.message ?: "Không thể tải tin nhắn"))
    }

    suspend fun sendMessage(token: String, sessionId: String, content: String): Result<ChatMessage> = try {
        val response = client.post("$restBaseUrl/sessions/$sessionId/messages") {
            header(HttpHeaders.Authorization, "Bearer $token")
            contentType(ContentType.Application.Json)
            setBody(SendChatMessageRequestDto(content = content.trim()))
        }

        if (!response.status.isSuccess()) {
            return Result.failure(IllegalStateException(extractErrorMessage(response.bodyAsText())))
        }

        val payload = response.body<ChatDataEnvelope<ChatMessageResponseDto>>()
        Result.success(mapMessage(payload.data))
    } catch (e: Exception) {
        Result.failure(IllegalStateException(e.message ?: "Không thể gửi tin nhắn"))
    }

    suspend fun assignSession(token: String, sessionId: String): Result<ChatConversation> = try {
        val response = client.patch("$restBaseUrl/sessions/$sessionId/assign") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }

        if (!response.status.isSuccess()) {
            return Result.failure(IllegalStateException(extractErrorMessage(response.bodyAsText())))
        }

        val payload = response.body<ChatDataEnvelope<ChatSessionResponseDto>>()
        Result.success(mapSession(payload.data))
    } catch (e: Exception) {
        Result.failure(IllegalStateException(e.message ?: "Không thể nhận phiên tư vấn"))
    }

    suspend fun resolveSession(token: String, sessionId: String): Result<ChatConversation> = try {
        val response = client.patch("$restBaseUrl/sessions/$sessionId/resolve") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }

        if (!response.status.isSuccess()) {
            return Result.failure(IllegalStateException(extractErrorMessage(response.bodyAsText())))
        }

        val payload = response.body<ChatDataEnvelope<ChatSessionResponseDto>>()
        Result.success(mapSession(payload.data))
    } catch (e: Exception) {
        Result.failure(IllegalStateException(e.message ?: "Không thể kết thúc phiên tư vấn"))
    }

    fun observeMessages(sessionId: String, token: String): Flow<ChatMessage> = flow {
        var retryDelayMs = 1_000L

        while (currentCoroutineContext().isActive) {
            try {
                client.webSocket(
                    urlString = "$wsBaseUrl/$sessionId",
                    request = {
                        header(HttpHeaders.Authorization, "Bearer $token")
                    }
                ) {
                    session = this
                    retryDelayMs = 1_000L
                    while (isActive) {
                        val frame = incoming.receive()
                        if (frame is Frame.Text) {
                            val dto = json.decodeFromString(ChatMessageResponseDto.serializer(), frame.readText())
                            emit(mapMessage(dto))
                        }
                    }
                }
            } catch (e: Exception) {
                if (!currentCoroutineContext().isActive) throw e
            } finally {
                session = null
            }

            if (currentCoroutineContext().isActive) {
                delay(retryDelayMs)
                retryDelayMs = (retryDelayMs * 2).coerceAtMost(10_000L)
            }
        }
    }

    suspend fun disconnect() {
        session = null
    }

    private fun mapSession(dto: ChatSessionResponseDto): ChatConversation {
        val lastMessage = dto.lastMessage?.let(::mapMessage)
        val consultantMessage = lastMessage?.takeIf { it.senderType == SenderType.PHARMACIST }
        val subtitle = dto.productId?.let { "Vật tư: $it" } ?: "Mã khách: ${dto.userId.takeLast(8)}"
        return ChatConversation(
            id = dto.id,
            customerId = dto.userId,
            customerName = buildCustomerLabel(dto.userId),
            customerPhone = subtitle,
            pharmacistId = consultantMessage?.senderId,
            pharmacistName = consultantMessage?.senderName,
            status = ChatStatus.fromValue(dto.status),
            lastMessage = lastMessage,
            lastMessageAt = lastMessage?.timestamp ?: dto.createdAt,
            createdAt = dto.createdAt,
            updatedAt = lastMessage?.timestamp ?: dto.createdAt,
            productId = dto.productId
        )
    }

    private fun mapMessage(dto: ChatMessageResponseDto): ChatMessage {
        return ChatMessage(
            id = dto.id,
            conversationId = dto.sessionId,
            senderId = dto.senderId,
            senderName = dto.senderName ?: defaultSenderName(dto.senderRole),
            senderType = mapSenderType(dto.senderRole),
            content = dto.content.orEmpty(),
            messageType = mapMessageType(dto.type),
            timestamp = dto.createdAt,
            metadata = dto.metadata
        )
    }

    private fun mapSenderType(senderRole: String?): SenderType {
        val normalized = senderRole?.trim()?.uppercase()
        return when {
            normalized == null -> SenderType.CUSTOMER
            normalized == "AI" || normalized == "SYSTEM" -> SenderType.SYSTEM
            normalized == "USER" || normalized == "CUSTOMER" -> SenderType.CUSTOMER
            else -> SenderType.PHARMACIST
        }
    }

    private fun defaultSenderName(senderRole: String?): String {
        return when (mapSenderType(senderRole)) {
            SenderType.CUSTOMER -> "Khách hàng"
            SenderType.PHARMACIST -> "Nhân viên chuyên môn"
            SenderType.SYSTEM -> "Hệ thống"
        }
    }

    private fun mapMessageType(type: String?): MessageType {
        val normalized = type?.trim()?.uppercase()
        return MessageType.entries.firstOrNull { it.value == normalized } ?: MessageType.TEXT
    }

    private fun buildCustomerLabel(userId: String): String = "Khách hàng ${userId.takeLast(6)}"

    private fun extractErrorMessage(raw: String): String {
        return runCatching {
            val element = json.parseToJsonElement(raw).jsonObject
            element["error"]?.jsonPrimitive?.content
                ?: element["message"]?.jsonPrimitive?.content
                ?: raw
        }.getOrElse { raw.ifBlank { "Không thể xử lý chat tư vấn" } }
    }
}
