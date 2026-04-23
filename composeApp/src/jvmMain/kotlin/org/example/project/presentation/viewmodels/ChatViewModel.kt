package org.example.project.presentation.viewmodels

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.example.project.data.models.ChatConversation
import org.example.project.data.models.ChatMessage
import org.example.project.data.models.ChatStatus
import org.example.project.data.models.SenderType
import org.example.project.data.repositories.ChatRepository

data class ChatUiState(
    val searchQuery: String = "",
    val statusFilter: ChatStatus? = null,
    val isLoading: Boolean = false,
    val isRefreshingQueue: Boolean = false,
    val conversations: List<ChatConversation> = emptyList(),
    val displayConversations: List<ChatConversation> = emptyList(),
    val selectedConversation: ChatConversation? = null,
    val messages: List<ChatMessage> = emptyList(),
    val isLoadingMessages: Boolean = false,
    val isSendingMessage: Boolean = false,
    val isResolvingSession: Boolean = false,
    val isRealtimeConnected: Boolean = false,
    val isRealtimeReconnecting: Boolean = false,
    val error: String? = null
)

class ChatViewModel(
    private val chatRepository: ChatRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private var listenJob: Job? = null
    private var refreshJob: Job? = null
    private var queueRefreshJob: Job? = null
    private var listeningSessionId: String? = null

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    private val _messageInput = MutableStateFlow("")
    val messageInput: StateFlow<String> = _messageInput.asStateFlow()

    init {
        loadConversations()
        startQueueRefreshLoop()
    }

    fun loadConversations(status: ChatStatus? = null) {
        refreshQueue(status = status, showLoading = true)
    }

    fun refreshQueueNow() {
        refreshQueue(showLoading = false)
    }

    fun filterConversations(query: String) {
        _uiState.update { state ->
            state.copy(
                searchQuery = query,
                displayConversations = filterList(
                    conversations = state.conversations,
                    query = query,
                    statusFilter = state.statusFilter
                )
            )
        }
    }

    fun filterByStatus(status: ChatStatus?) {
        _uiState.update { state ->
            state.copy(
                statusFilter = status,
                displayConversations = filterList(
                    conversations = state.conversations,
                    query = state.searchQuery,
                    statusFilter = status
                )
            )
        }
    }

    fun selectConversation(conversation: ChatConversation) {
        val token = currentToken() ?: return
        if (_uiState.value.selectedConversation?.id == conversation.id) return

        scope.launch {
            _uiState.update {
                it.copy(
                    selectedConversation = conversation.copy(unreadCount = 0),
                isLoadingMessages = true,
                    isRealtimeConnected = false,
                    isRealtimeReconnecting = false,
                    error = null
                )
            }

            val assignedConversation = if (conversation.status == ChatStatus.PENDING) {
                chatRepository.assignSession(token, conversation.id).getOrElse { conversation }
            } else {
                conversation
            }

            chatRepository.getMessages(token, assignedConversation.id)
                .onSuccess { messages ->
                    val updatedConversation = assignedConversation.copy(unreadCount = 0)
                    updateConversationInState(updatedConversation)
                    _uiState.update {
                        it.copy(
                            selectedConversation = updatedConversation,
                            messages = messages,
                            isLoadingMessages = false
                        )
                    }
                    startListeningForMessages(updatedConversation.id, token)
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isLoadingMessages = false,
                            error = error.message ?: "Không thể tải tin nhắn"
                        )
                    }
                }
        }
    }

    fun updateMessageInput(text: String) {
        _messageInput.value = text
    }

    fun sendMessage(content: String) {
        val token = currentToken() ?: return
        val selectedConversation = _uiState.value.selectedConversation ?: return
        if (selectedConversation.status == ChatStatus.RESOLVED) return

        val trimmed = content.trim()
        if (trimmed.isBlank()) return

        scope.launch {
            _uiState.update { it.copy(isSendingMessage = true, error = null) }
            chatRepository.sendMessage(token, selectedConversation.id, trimmed)
                .onSuccess { message ->
                    appendMessageIfMissing(message)
                    _messageInput.value = ""
                    _uiState.update { it.copy(isSendingMessage = false) }
                }
                .onFailure { error ->
                    markSelectedResolvedIfClosed(error)
                    _uiState.update {
                        it.copy(
                            isSendingMessage = false,
                            error = error.message ?: "Không thể gửi tin nhắn"
                        )
                    }
                }
        }
    }

    fun resolveSelectedConversation() {
        val token = currentToken() ?: return
        scope.launch {
            val selectedConversation = _uiState.value.selectedConversation ?: return@launch
            if (selectedConversation.status == ChatStatus.RESOLVED) return@launch

            _uiState.update { it.copy(isResolvingSession = true, error = null) }
            chatRepository.resolveSession(token, selectedConversation.id)
                .onSuccess { resolvedConversation ->
                    val updatedConversation = resolvedConversation.copy(unreadCount = 0)
                    updateConversationInState(updatedConversation)
                    _uiState.update {
                        it.copy(
                            selectedConversation = updatedConversation,
                            isResolvingSession = false
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isResolvingSession = false,
                            error = error.message ?: "Không thể kết thúc phiên tư vấn"
                        )
                    }
                }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    private fun startListeningForMessages(sessionId: String, token: String) {
        listenJob?.cancel()
        listeningSessionId = sessionId
        listenJob = scope.launch {
            while (currentCoroutineContext().isActive && listeningSessionId == sessionId) {
                _uiState.update {
                    it.copy(
                        isRealtimeConnected = false,
                        isRealtimeReconnecting = true
                    )
                }

                runCatching {
                    chatRepository.observeMessages(sessionId, token).collect { incomingMessage ->
                        _uiState.update {
                            it.copy(
                                isRealtimeConnected = true,
                                isRealtimeReconnecting = false
                            )
                        }
                        appendMessageIfMissing(incomingMessage)
                    }
                }

                if (currentCoroutineContext().isActive && listeningSessionId == sessionId) {
                    delay(2_000)
                }
            }
        }
    }

    private fun startQueueRefreshLoop() {
        refreshJob?.cancel()
        refreshJob = scope.launch {
            while (true) {
                delay(5_000)
                refreshQueue(showLoading = false)
            }
        }
    }

    private fun refreshQueue(status: ChatStatus? = null, showLoading: Boolean) {
        val token = currentToken() ?: return
        if (queueRefreshJob?.isActive == true) return

        queueRefreshJob = scope.launch {
            _uiState.update {
                it.copy(
                    isLoading = if (showLoading) true else it.isLoading,
                    isRefreshingQueue = !showLoading,
                    error = null
                )
            }
            chatRepository.getSessions(token, status?.value)
                .onSuccess { conversations ->
                    _uiState.update { state ->
                        val mergedConversations = mergeConversations(
                            oldList = state.conversations,
                            freshList = conversations,
                            selectedId = state.selectedConversation?.id
                        )
                        val selectedConversation = state.selectedConversation?.let { selected ->
                            mergedConversations.firstOrNull { it.id == selected.id }
                        }
                        state.copy(
                            isLoading = false,
                            isRefreshingQueue = false,
                            conversations = mergedConversations,
                            displayConversations = filterList(
                                conversations = mergedConversations,
                                query = state.searchQuery,
                                statusFilter = state.statusFilter
                            ),
                            selectedConversation = selectedConversation
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isRefreshingQueue = false,
                            error = error.message ?: "Không thể tải danh sách tư vấn"
                        )
                    }
                }
        }
    }

    private fun appendMessageIfMissing(message: ChatMessage) {
        _uiState.update { state ->
            if (state.messages.any { it.id == message.id }) return@update state

            val updatedMessages = if (state.selectedConversation?.id == message.conversationId) {
                state.messages + message
            } else {
                state.messages
            }

            val updatedConversations = state.conversations.map { conversation ->
                if (conversation.id == message.conversationId) {
                    val isSelected = state.selectedConversation?.id == conversation.id
                    val shouldCountUnread = !isSelected && message.senderType == SenderType.CUSTOMER
                    conversation.copy(
                        lastMessage = message,
                        lastMessageAt = message.timestamp,
                        unreadCount = if (shouldCountUnread) conversation.unreadCount + 1 else conversation.unreadCount
                    )
                } else {
                    conversation
                }
            }

            state.copy(
                conversations = updatedConversations,
                displayConversations = filterList(
                    conversations = updatedConversations,
                    query = state.searchQuery,
                    statusFilter = state.statusFilter
                ),
                selectedConversation = state.selectedConversation?.let { selected ->
                    updatedConversations.firstOrNull { it.id == selected.id } ?: selected
                },
                messages = updatedMessages
            )
        }
    }

    private fun mergeConversations(
        oldList: List<ChatConversation>,
        freshList: List<ChatConversation>,
        selectedId: String?
    ): List<ChatConversation> {
        val oldById = oldList.associateBy { it.id }
        return freshList.map { freshConversation ->
            val oldConversation = oldById[freshConversation.id]
            val unreadCount = when {
                selectedId == freshConversation.id -> 0
                oldConversation == null -> if (freshConversation.lastMessage?.senderType == SenderType.CUSTOMER) 1 else 0
                freshConversation.lastMessage?.id != null &&
                    freshConversation.lastMessage.id != oldConversation.lastMessage?.id &&
                    freshConversation.lastMessage.senderType == SenderType.CUSTOMER -> oldConversation.unreadCount + 1
                else -> oldConversation.unreadCount
            }
            freshConversation.copy(unreadCount = unreadCount)
        }
    }

    private fun updateConversationInState(conversation: ChatConversation) {
        _uiState.update { state ->
            val updatedConversations = state.conversations.map {
                if (it.id == conversation.id) conversation else it
            }
            state.copy(
                conversations = updatedConversations,
                displayConversations = filterList(
                    conversations = updatedConversations,
                    query = state.searchQuery,
                    statusFilter = state.statusFilter
                )
            )
        }
    }

    private fun markSelectedResolvedIfClosed(error: Throwable) {
        val message = error.message.orEmpty()
        if (!message.contains("no longer active", ignoreCase = true)) return

        _uiState.update { state ->
            val selected = state.selectedConversation ?: return@update state
            val resolved = selected.copy(status = ChatStatus.RESOLVED, unreadCount = 0)
            val updatedConversations = state.conversations.map {
                if (it.id == selected.id) resolved else it
            }
            state.copy(
                selectedConversation = resolved,
                conversations = updatedConversations,
                displayConversations = filterList(
                    conversations = updatedConversations,
                    query = state.searchQuery,
                    statusFilter = state.statusFilter
                )
            )
        }
    }

    private fun filterList(
        conversations: List<ChatConversation>,
        query: String,
        statusFilter: ChatStatus?
    ): List<ChatConversation> {
        return conversations.filter { conversation ->
            val statusMatches = statusFilter == null || conversation.status == statusFilter
            val queryMatches = query.isBlank() ||
                conversation.customerName.contains(query, ignoreCase = true) ||
                conversation.customerPhone.contains(query, ignoreCase = true) ||
                (conversation.productId?.contains(query, ignoreCase = true) == true)
            statusMatches && queryMatches
        }
    }

    private fun currentToken(): String? {
        val token = sessionManager.session.value?.accessToken
        if (token.isNullOrBlank()) {
            _uiState.update { it.copy(error = "Chưa đăng nhập để xem chat tư vấn") }
            return null
        }
        return token
    }

    override fun onCleared() {
        listenJob?.cancel()
        refreshJob?.cancel()
        queueRefreshJob?.cancel()
        scope.launch {
            chatRepository.disconnect()
        }
    }
}
