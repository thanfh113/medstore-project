package com.example.nhathuoc.data.repository

import com.example.nhathuoc.data.model.AiConversationDto
import com.example.nhathuoc.data.model.AiSendMessageRequest
import com.example.nhathuoc.data.model.AiSendMessageResponse
import com.example.nhathuoc.data.model.CreateAiConversationRequest
import com.example.nhathuoc.data.model.DataMessageResponse
import com.example.nhathuoc.data.model.NetworkResult
import com.example.nhathuoc.data.remote.ApiService
import com.example.nhathuoc.util.ApiErrorHandler
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AiChatRepository @Inject constructor(
    private val apiService: ApiService
) {
    suspend fun createConversation(productId: String? = null): NetworkResult<DataMessageResponse<AiConversationDto>> =
        ApiErrorHandler.safeApiCall("Không thể tạo cuộc trò chuyện AI") {
            apiService.createAiConversation(CreateAiConversationRequest(productId))
        }

    suspend fun getConversations(): NetworkResult<DataMessageResponse<List<AiConversationDto>>> =
        ApiErrorHandler.safeApiCall("Không thể tải lịch sử trò chuyện") {
            apiService.getAiConversations()
        }

    suspend fun getConversation(conversationId: String): NetworkResult<DataMessageResponse<AiConversationDto>> =
        ApiErrorHandler.safeApiCall("Không thể tải cuộc trò chuyện") {
            apiService.getAiConversation(conversationId)
        }

    suspend fun sendMessage(conversationId: String, message: String): NetworkResult<DataMessageResponse<AiSendMessageResponse>> =
        ApiErrorHandler.safeApiCall("Không thể gửi tin nhắn") {
            apiService.sendAiMessage(conversationId, AiSendMessageRequest(message))
        }

    suspend fun escalateToHuman(conversationId: String): NetworkResult<DataMessageResponse<AiSendMessageResponse>> =
        ApiErrorHandler.safeApiCall("Không thể kết nối chuyên viên") {
            apiService.escalateAiToHuman(conversationId)
        }
}
