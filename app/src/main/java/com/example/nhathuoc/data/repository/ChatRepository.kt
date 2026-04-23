package com.example.nhathuoc.data.repository

import com.example.nhathuoc.data.local.SessionManager
import com.example.nhathuoc.data.model.ChatMessageDto
import com.example.nhathuoc.data.model.ChatSessionDto
import com.example.nhathuoc.data.model.CreateChatSessionRequest
import com.example.nhathuoc.data.model.DataMessageResponse
import com.example.nhathuoc.data.model.NetworkResult
import com.example.nhathuoc.data.model.SendChatMessageRequest
import com.example.nhathuoc.data.remote.ApiConstants
import com.example.nhathuoc.data.remote.ApiService
import com.example.nhathuoc.util.ApiErrorHandler
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener

@Singleton
class ChatRepository @Inject constructor(
    private val apiService: ApiService,
    private val okHttpClient: OkHttpClient,
    private val sessionManager: SessionManager
) {
    private val json = Json { ignoreUnknownKeys = true }

    suspend fun getChatSessions(): NetworkResult<DataMessageResponse<List<ChatSessionDto>>> {
        return ApiErrorHandler.safeApiCall(
            defaultErrorMessage = "Không thể tải danh sách phiên tư vấn"
        ) {
            apiService.getChatSessions()
        }
    }

    suspend fun createSession(productId: String?): NetworkResult<DataMessageResponse<ChatSessionDto>> {
        return ApiErrorHandler.safeApiCall(
            defaultErrorMessage = "Không thể tạo phiên tư vấn"
        ) {
            apiService.createChatSession(CreateChatSessionRequest(productId))
        }
    }

    suspend fun getMessages(
        sessionId: String,
        limit: Int = 50,
        offset: Long = 0
    ): NetworkResult<DataMessageResponse<List<ChatMessageDto>>> {
        return ApiErrorHandler.safeApiCall(
            defaultErrorMessage = "Không thể tải nội dung tư vấn"
        ) {
            apiService.getChatMessages(sessionId, limit, offset)
        }
    }

    suspend fun sendMessage(
        sessionId: String,
        content: String,
        type: String = "TEXT",
        metadata: String? = null
    ): NetworkResult<DataMessageResponse<ChatMessageDto>> {
        return ApiErrorHandler.safeApiCall(
            defaultErrorMessage = "Không thể gửi tin nhắn"
        ) {
            apiService.sendChatMessage(
                sessionId = sessionId,
                request = SendChatMessageRequest(
                    content = content,
                    type = type,
                    metadata = metadata
                )
            )
        }
    }

    fun observeMessages(sessionId: String): Flow<ChatMessageDto> = callbackFlow {
        val token = sessionManager.getAccessToken()
        if (token.isNullOrBlank()) {
            close(IllegalStateException("Thiếu access token để kết nối tư vấn"))
            return@callbackFlow
        }

        val request = Request.Builder()
            .url("${websocketBaseUrl()}/api/v1/ws/chat/$sessionId")
            .addHeader(ApiConstants.HEADER_AUTHORIZATION, "Bearer $token")
            .build()

        val listener = object : WebSocketListener() {
            override fun onMessage(webSocket: WebSocket, text: String) {
                runCatching {
                    json.decodeFromString<ChatMessageDto>(text)
                }.onSuccess { message ->
                    trySend(message)
                }
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                close(t)
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                close(IllegalStateException(reason.ifBlank { "Kết nối tư vấn đã đóng" }))
            }
        }

        val socket = okHttpClient.newWebSocket(request, listener)
        awaitClose {
            socket.close(1000, "chat closed")
        }
    }

    private fun websocketBaseUrl(): String {
        return ApiConstants.BASE_URL
            .replaceFirst("http://", "ws://")
            .replaceFirst("https://", "wss://")
            .trimEnd('/')
    }
}
