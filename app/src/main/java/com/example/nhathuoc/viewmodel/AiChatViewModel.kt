package com.example.nhathuoc.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nhathuoc.data.model.AiConversationDto
import com.example.nhathuoc.data.model.AiMessageDto
import com.example.nhathuoc.data.model.NetworkResult
import com.example.nhathuoc.data.repository.AiChatRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AiConversationsState(
    val conversations: List<AiConversationDto> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

data class AiChatUiState(
    val conversationId: String? = null,
    val messages: List<AiMessageDto> = emptyList(),
    val inputText: String = "",
    val isLoading: Boolean = false,
    val isSending: Boolean = false,
    val isEscalated: Boolean = false,
    val isClosed: Boolean = false,
    val humanSessionId: String? = null,
    val error: String? = null
)

@HiltViewModel
class AiChatViewModel @Inject constructor(
    private val repository: AiChatRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AiChatUiState())
    val uiState: StateFlow<AiChatUiState> = _uiState.asStateFlow()

    private val _conversationsState = MutableStateFlow(AiConversationsState())
    val conversationsState: StateFlow<AiConversationsState> = _conversationsState.asStateFlow()

    fun loadConversations() {
        viewModelScope.launch {
            _conversationsState.update { it.copy(isLoading = true, error = null) }
            when (val result = repository.getConversations()) {
                is NetworkResult.Success ->
                    _conversationsState.update { it.copy(isLoading = false, conversations = result.data.data) }
                is NetworkResult.Error ->
                    _conversationsState.update { it.copy(isLoading = false, error = result.message) }
                is NetworkResult.Exception ->
                    _conversationsState.update { it.copy(isLoading = false, error = result.e.localizedMessage ?: "Lỗi kết nối") }
            }
        }
    }

    fun startConversation(productId: String? = null) {
        if (_uiState.value.conversationId != null) return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (val result = repository.createConversation(productId)) {
                is NetworkResult.Success -> {
                    val conv = result.data.data
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            conversationId = conv.id,
                            messages = conv.messages,
                            isEscalated = conv.escalatedToConsultant,
                            humanSessionId = conv.chatSessionId
                        )
                    }
                }
                is NetworkResult.Error ->
                    _uiState.update { it.copy(isLoading = false, error = result.message) }
                is NetworkResult.Exception ->
                    _uiState.update { it.copy(isLoading = false, error = result.e.localizedMessage ?: "Lỗi kết nối") }
            }
        }
    }

    fun openConversation(conversationId: String) {
        if (_uiState.value.conversationId == conversationId) return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (val result = repository.getConversation(conversationId)) {
                is NetworkResult.Success -> {
                    val conv = result.data.data
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            conversationId = conv.id,
                            messages = conv.messages,
                            isEscalated = conv.escalatedToConsultant,
                            humanSessionId = conv.chatSessionId,
                            isClosed = conv.status.equals("CLOSED", ignoreCase = true)
                        )
                    }
                }
                is NetworkResult.Error ->
                    _uiState.update { it.copy(isLoading = false, error = result.message) }
                is NetworkResult.Exception ->
                    _uiState.update { it.copy(isLoading = false, error = result.e.localizedMessage ?: "Lỗi kết nối") }
            }
        }
    }

    fun setInput(text: String) {
        _uiState.update { it.copy(inputText = text) }
    }

    fun sendMessage(text: String = _uiState.value.inputText.trim()) {
        val convId = _uiState.value.conversationId ?: return
        if (text.isBlank() || _uiState.value.isSending || _uiState.value.isEscalated || _uiState.value.isClosed) return

        val userMsg = AiMessageDto(role = "user", text = text)
        _uiState.update {
            it.copy(
                isSending = true,
                inputText = "",
                error = null,
                messages = it.messages + userMsg
            )
        }

        viewModelScope.launch {
            when (val result = repository.sendMessage(convId, text)) {
                is NetworkResult.Success -> {
                    val resp = result.data.data
                    val aiMsg = AiMessageDto(
                        role = "ai",
                        text = resp.reply,
                        recommendations = resp.recommendations
                    )
                    _uiState.update {
                        it.copy(
                            isSending = false,
                            messages = it.messages + aiMsg,
                            isEscalated = resp.escalatedToConsultant,
                            humanSessionId = resp.chatSessionId
                        )
                    }
                }
                is NetworkResult.Error ->
                    _uiState.update { it.copy(isSending = false, error = result.message) }
                is NetworkResult.Exception ->
                    _uiState.update { it.copy(isSending = false, error = result.e.localizedMessage ?: "Không thể gửi tin nhắn") }
            }
        }
    }

    fun escalateToHuman() {
        val convId = _uiState.value.conversationId ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isSending = true, error = null) }
            when (val result = repository.escalateToHuman(convId)) {
                is NetworkResult.Success -> {
                    val resp = result.data.data
                    val aiMsg = AiMessageDto(role = "ai", text = resp.reply)
                    _uiState.update {
                        it.copy(
                            isSending = false,
                            messages = it.messages + aiMsg,
                            isEscalated = true,
                            humanSessionId = resp.chatSessionId
                        )
                    }
                }
                is NetworkResult.Error ->
                    _uiState.update { it.copy(isSending = false, error = result.message) }
                is NetworkResult.Exception ->
                    _uiState.update { it.copy(isSending = false, error = result.e.localizedMessage ?: "Không thể kết nối chuyên viên") }
            }
        }
    }

    fun closeCurrentConversation() {
        val convId = _uiState.value.conversationId ?: run {
            _uiState.update { it.copy(isClosed = true) }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isSending = true, error = null) }
            when (val result = repository.closeConversation(convId)) {
                is NetworkResult.Success ->
                    _uiState.update { it.copy(isSending = false, isClosed = true) }
                is NetworkResult.Error ->
                    _uiState.update { it.copy(isSending = false, isClosed = true, error = result.message) }
                is NetworkResult.Exception ->
                    _uiState.update { it.copy(isSending = false, isClosed = true) }
            }
            _conversationsState.update { it.copy(conversations = emptyList()) }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
