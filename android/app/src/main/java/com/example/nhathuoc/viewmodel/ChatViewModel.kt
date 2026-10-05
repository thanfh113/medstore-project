package com.example.nhathuoc.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nhathuoc.data.local.SessionManager
import com.example.nhathuoc.data.model.ChatMessageDto
import com.example.nhathuoc.data.model.ChatSessionDto
import com.example.nhathuoc.data.model.NetworkResult
import com.example.nhathuoc.data.repository.ChatRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class ChatUiState(
    val session: ChatSessionDto? = null,
    val messages: List<ChatMessageDto> = emptyList(),
    val inputText: String = "",
    val isLoading: Boolean = false,
    val isSending: Boolean = false,
    val isRealtimeConnected: Boolean = false,
    val isRealtimeReconnecting: Boolean = false,
    val error: String? = null,
    val currentUserId: String? = null,
    val productId: String? = null
)

data class ChatHistoryUiState(
    val sessions: List<ChatSessionDto> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val chatRepository: ChatRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    private val _historyState = MutableStateFlow(ChatHistoryUiState())
    val historyState: StateFlow<ChatHistoryUiState> = _historyState.asStateFlow()

    private var initializedKey: String? = null
    private var realtimeJob: Job? = null
    private var statusRefreshJob: Job? = null
    private var realtimeSessionId: String? = null

    // ── New session (from product or general) ────────────────────────────────
    fun initSession(productId: String? = null) {
        val normalizedProductId = productId?.takeIf { it.isNotBlank() }
        val key = normalizedProductId ?: "__general__"
        if (_uiState.value.session != null && initializedKey == key) return

        initializedKey = key
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    error = null,
                    productId = normalizedProductId,
                    currentUserId = sessionManager.getUserId()
                )
            }

            when (val result = chatRepository.createSession(normalizedProductId)) {
                is NetworkResult.Success -> {
                    val session = result.data.data
                    _uiState.update { state -> state.copy(session = session) }
                    startSessionStatusRefresh(session.id)
                    loadMessages(session.id, connectRealtimeAfterLoad = true)
                }
                is NetworkResult.Error -> {
                    _uiState.update { it.copy(isLoading = false, error = result.message) }
                }
                is NetworkResult.Exception -> {
                    _uiState.update { it.copy(isLoading = false, error = result.e.localizedMessage ?: "Không thể khởi tạo tư vấn") }
                }
            }
        }
    }

    // ── Open existing session from history ───────────────────────────────────
    fun openExistingSession(sessionId: String) {
        val key = "session:$sessionId"
        if (_uiState.value.session?.id == sessionId && initializedKey == key) return

        initializedKey = key
        realtimeJob?.cancel()
        statusRefreshJob?.cancel()
        realtimeSessionId = null

        viewModelScope.launch {
            _uiState.update {
                ChatUiState(
                    isLoading = true,
                    currentUserId = sessionManager.getUserId()
                )
            }

            when (val result = chatRepository.getChatSessions()) {
                is NetworkResult.Success -> {
                    val session = result.data.data.firstOrNull { it.id == sessionId }
                    if (session != null) {
                        _uiState.update { it.copy(session = session) }
                        val isOpen = !session.status.equals("RESOLVED", ignoreCase = true)
                        if (isOpen) startSessionStatusRefresh(sessionId)
                        loadMessages(sessionId, connectRealtimeAfterLoad = isOpen)
                    } else {
                        _uiState.update { it.copy(isLoading = false, error = "Không tìm thấy phiên tư vấn") }
                    }
                }
                is NetworkResult.Error -> _uiState.update { it.copy(isLoading = false, error = result.message) }
                is NetworkResult.Exception -> _uiState.update { it.copy(isLoading = false, error = "Không thể tải phiên tư vấn") }
            }
        }
    }

    // ── Load sessions list for history screen ─────────────────────────────────
    fun loadSessions() {
        viewModelScope.launch {
            _historyState.update { it.copy(isLoading = true, error = null) }
            when (val result = chatRepository.getChatSessions()) {
                is NetworkResult.Success -> {
                    val sorted = result.data.data.sortedByDescending { it.createdAt }
                    _historyState.update { it.copy(sessions = sorted, isLoading = false) }
                }
                is NetworkResult.Error -> _historyState.update { it.copy(isLoading = false, error = result.message) }
                is NetworkResult.Exception -> _historyState.update { it.copy(isLoading = false, error = "Không thể tải lịch sử tư vấn") }
            }
        }
    }

    fun resetSession() {
        realtimeJob?.cancel()
        statusRefreshJob?.cancel()
        realtimeSessionId = null
        initializedKey = null
        _uiState.update { ChatUiState(currentUserId = it.currentUserId) }
    }

    // ── Messages ─────────────────────────────────────────────────────────────
    fun loadMessages(
        sessionId: String? = _uiState.value.session?.id,
        connectRealtimeAfterLoad: Boolean = false,
        showLoading: Boolean = true
    ) {
        val resolvedSessionId = sessionId ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = if (showLoading) true else it.isLoading, error = null) }
            when (val result = chatRepository.getMessages(resolvedSessionId)) {
                is NetworkResult.Success -> {
                    _uiState.update { it.copy(isLoading = false, messages = result.data.data) }
                    if (connectRealtimeAfterLoad) connectRealtime(resolvedSessionId)
                }
                is NetworkResult.Error -> _uiState.update { it.copy(isLoading = false, error = result.message) }
                is NetworkResult.Exception -> _uiState.update { it.copy(isLoading = false, error = result.e.localizedMessage ?: "Không thể tải tin nhắn") }
            }
        }
    }

    fun setInput(text: String) {
        _uiState.update { it.copy(inputText = text) }
    }

    fun sendCurrentMessage() {
        val session = _uiState.value.session ?: return
        if (session.status.equals("RESOLVED", ignoreCase = true)) {
            _uiState.update { it.copy(error = "Phiên tư vấn đã kết thúc") }
            return
        }
        val content = _uiState.value.inputText.trim()
        if (content.isEmpty() || _uiState.value.isSending) return

        viewModelScope.launch {
            _uiState.update { it.copy(isSending = true, error = null) }
            when (val result = chatRepository.sendMessage(session.id, content)) {
                is NetworkResult.Success -> {
                    appendMessageIfMissing(result.data.data)
                    _uiState.update { it.copy(isSending = false, inputText = "") }
                }
                is NetworkResult.Error -> {
                    refreshCurrentSessionStatus(session.id)
                    _uiState.update { it.copy(isSending = false, error = result.message) }
                }
                is NetworkResult.Exception -> {
                    refreshCurrentSessionStatus(session.id)
                    _uiState.update { it.copy(isSending = false, error = result.e.localizedMessage ?: "Không thể gửi tin nhắn") }
                }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    // ── Realtime ─────────────────────────────────────────────────────────────
    private fun connectRealtime(sessionId: String) {
        if (realtimeSessionId == sessionId && realtimeJob?.isActive == true) return

        realtimeJob?.cancel()
        realtimeSessionId = sessionId
        _uiState.update { it.copy(isRealtimeConnected = false, isRealtimeReconnecting = false) }

        realtimeJob = viewModelScope.launch {
            var retryDelayMs = 1_000L
            while (currentCoroutineContext().isActive && realtimeSessionId == sessionId) {
                _uiState.update { it.copy(isRealtimeConnected = false, isRealtimeReconnecting = retryDelayMs > 1_000L) }

                runCatching {
                    chatRepository.observeMessages(sessionId).collect { message ->
                        retryDelayMs = 1_000L
                        appendMessageIfMissing(message)
                        refreshCurrentSessionStatus(sessionId)
                        _uiState.update { it.copy(isRealtimeConnected = true, isRealtimeReconnecting = false) }
                    }
                }.onFailure { throwable ->
                    _uiState.update { it.copy(isRealtimeConnected = false, isRealtimeReconnecting = true, error = throwable.localizedMessage ?: "Kết nối realtime thất bại") }
                }

                refreshCurrentSessionStatus(sessionId)
                if (currentCoroutineContext().isActive && realtimeSessionId == sessionId) {
                    delay(retryDelayMs)
                    retryDelayMs = (retryDelayMs * 2).coerceAtMost(10_000L)
                }
            }
        }
    }

    private fun appendMessageIfMissing(message: ChatMessageDto) {
        _uiState.update { state ->
            if (state.messages.any { it.id == message.id }) state
            else state.copy(messages = state.messages + message)
        }
    }

    private fun startSessionStatusRefresh(sessionId: String) {
        statusRefreshJob?.cancel()
        statusRefreshJob = viewModelScope.launch {
            while (currentCoroutineContext().isActive) {
                delay(10_000)
                refreshCurrentSessionStatus(sessionId)
            }
        }
    }

    private fun refreshCurrentSessionStatus(sessionId: String) {
        viewModelScope.launch {
            when (val result = chatRepository.getChatSessions()) {
                is NetworkResult.Success -> {
                    val latestSession = result.data.data.firstOrNull { it.id == sessionId } ?: return@launch
                    _uiState.update { it.copy(session = latestSession) }
                    if (latestSession.status.equals("RESOLVED", ignoreCase = true)) {
                        realtimeSessionId = null
                        realtimeJob?.cancel()
                        _uiState.update { it.copy(isRealtimeConnected = false, isRealtimeReconnecting = false) }
                    }
                }
                else -> Unit
            }
        }
    }

    override fun onCleared() {
        realtimeJob?.cancel()
        statusRefreshJob?.cancel()
        super.onCleared()
    }
}
