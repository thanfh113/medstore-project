package org.example.project.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import org.example.project.data.models.ChatConversation
import org.example.project.data.models.ChatMessage
import org.example.project.data.models.ChatStatus
import org.example.project.data.models.SenderType
import org.example.project.presentation.viewmodels.ChatUiState
import org.example.project.presentation.viewmodels.ChatViewModel

@Composable
fun ChatScreen(viewModel: ChatViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val messageInput by viewModel.messageInput.collectAsState()

    Row(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        ConversationPane(
            uiState = uiState,
            onSearchChange = viewModel::filterConversations,
            onStatusFilter = viewModel::filterByStatus,
            onRefresh = viewModel::refreshQueueNow,
            onSelectConversation = viewModel::selectConversation,
            modifier = Modifier.width(360.dp).fillMaxHeight()
        )

        Box(
            modifier = Modifier
                .width(1.dp)
                .fillMaxHeight()
                .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f))
        )

        MessagePane(
            uiState = uiState,
            messageInput = messageInput,
            onInputChange = viewModel::updateMessageInput,
            onSend = { viewModel.sendMessage(messageInput) },
            onResolve = viewModel::resolveSelectedConversation,
            modifier = Modifier.weight(1f).fillMaxHeight()
        )
    }
}

@Composable
private fun ConversationPane(
    uiState: ChatUiState,
    onSearchChange: (String) -> Unit,
    onStatusFilter: (ChatStatus?) -> Unit,
    onRefresh: () -> Unit,
    onSelectConversation: (ChatConversation) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .background(MaterialTheme.colorScheme.surface)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Tư vấn khách hàng",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "${uiState.displayConversations.size} phiên",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            OutlinedButton(
                onClick = onRefresh,
                enabled = !uiState.isRefreshingQueue,
                shape = RoundedCornerShape(999.dp)
            ) {
                if (uiState.isRefreshingQueue) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                } else {
                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                }
                Spacer(Modifier.width(8.dp))
                Text("Tải lại")
            }
        }

        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = uiState.searchQuery,
            onValueChange = onSearchChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Tìm phiên tư vấn") },
            singleLine = true,
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(Modifier.height(12.dp))

        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            item {
                StatusFilterChip(
                    label = "Tất cả",
                    selected = uiState.statusFilter == null,
                    onClick = { onStatusFilter(null) }
                )
            }
            items(ChatStatus.entries) { status ->
                StatusFilterChip(
                    label = status.displayName,
                    selected = uiState.statusFilter == status,
                    onClick = { onStatusFilter(status) }
                )
            }
        }

        Spacer(Modifier.height(16.dp))
        HorizontalDivider()

        Box(modifier = Modifier.fillMaxSize()) {
            when {
                uiState.isLoading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                uiState.displayConversations.isEmpty() -> Text(
                    text = "Chưa có phiên tư vấn nào",
                    modifier = Modifier.align(Alignment.Center),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                else -> LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(uiState.displayConversations, key = { it.id }) { conversation ->
                        ConversationItem(
                            conversation = conversation,
                            selected = uiState.selectedConversation?.id == conversation.id,
                            onClick = { onSelectConversation(conversation) }
                        )
                    }
                }
            }

            uiState.error?.takeIf { it.isNotBlank() }?.let { error ->
                Surface(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth(),
                    color = MaterialTheme.colorScheme.errorContainer,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = error,
                        modifier = Modifier.padding(12.dp),
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}

@Composable
private fun StatusFilterChip(label: String, selected: Boolean, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(999.dp),
        color = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
        border = BorderStroke(
            1.dp,
            if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
        )
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            style = MaterialTheme.typography.labelLarge,
            color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun ConversationItem(
    conversation: ChatConversation,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        color = if (selected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f) else MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = conversation.customerName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (conversation.unreadCount > 0) {
                            Spacer(Modifier.width(8.dp))
                            UnreadBadge(conversation.unreadCount)
                        }
                    }
                    Text(
                        text = conversation.customerPhone,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Text(
                    text = formatChatTime(conversation.lastMessageAt),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(Modifier.height(8.dp))
            Text(
                text = conversation.lastMessage?.content?.takeIf { it.isNotBlank() } ?: "Chưa có tin nhắn",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                StatusBadge(conversation.status)
                conversation.pharmacistName?.takeIf { it.isNotBlank() }?.let {
                    Text(
                        text = "Tư vấn: $it",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
private fun UnreadBadge(count: Int) {
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = MaterialTheme.colorScheme.error
    ) {
        Text(
            text = count.coerceAtMost(99).toString(),
            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onError,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun StatusBadge(status: ChatStatus) {
    val color = statusColor(status)
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = color.copy(alpha = 0.14f)
    ) {
        Text(
            text = status.displayName,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            style = MaterialTheme.typography.labelMedium,
            color = color,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun MessagePane(
    uiState: ChatUiState,
    messageInput: String,
    onInputChange: (String) -> Unit,
    onSend: () -> Unit,
    onResolve: () -> Unit,
    modifier: Modifier = Modifier
) {
    val selectedConversation = uiState.selectedConversation
    if (selectedConversation == null) {
        Box(modifier = modifier.background(MaterialTheme.colorScheme.background)) {
            Column(
                modifier = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Chat,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Chọn một phiên tư vấn",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        return
    }

    val isResolved = selectedConversation.status == ChatStatus.RESOLVED
    val listState = rememberLazyListState()
    val consultantName = selectedConversation.pharmacistName?.takeIf { it.isNotBlank() }
        ?: uiState.messages.lastOrNull { it.senderType == SenderType.PHARMACIST }?.senderName

    LaunchedEffect(selectedConversation.id, uiState.messages.size) {
        if (uiState.messages.isNotEmpty()) {
            listState.animateScrollToItem(uiState.messages.lastIndex)
        }
    }

    Column(modifier = modifier.background(MaterialTheme.colorScheme.background)) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 1.dp
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 18.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = selectedConversation.customerName,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.width(10.dp))
                        StatusBadge(selectedConversation.status)
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = selectedConversation.customerPhone,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    selectedConversation.productId?.let {
                        Text(
                            text = "Mã vật tư: $it",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(
                        text = "Người tư vấn: ${consultantName ?: "Chưa tiếp nhận"}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (uiState.isRealtimeReconnecting) {
                        Text(
                            text = "Đang kết nối lại realtime...",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }

                FilledTonalButton(
                    onClick = onResolve,
                    enabled = !isResolved && !uiState.isResolvingSession,
                    shape = RoundedCornerShape(999.dp)
                ) {
                    Icon(Icons.Default.DoneAll, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text(if (isResolved) "Đã kết thúc" else "Kết thúc tư vấn")
                }
            }
        }

        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            if (uiState.isLoadingMessages) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 24.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(uiState.messages, key = { it.id }) { message ->
                        MessageItem(message)
                    }
                }
            }
        }

        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 2.dp
        ) {
            Row(
                modifier = Modifier.padding(20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = messageInput,
                    onValueChange = onInputChange,
                    modifier = Modifier.weight(1f),
                    placeholder = { Text(if (isResolved) "Phiên tư vấn đã kết thúc" else "Nhập nội dung trả lời") },
                    enabled = !isResolved && !uiState.isSendingMessage,
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )
                Button(
                    onClick = onSend,
                    enabled = !isResolved && messageInput.isNotBlank() && !uiState.isSendingMessage,
                    shape = RoundedCornerShape(999.dp),
                    modifier = Modifier.height(56.dp)
                ) {
                    if (uiState.isSendingMessage) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Icon(Icons.Default.Send, contentDescription = null)
                    }
                }
            }
        }
    }
}

@Composable
private fun MessageItem(message: ChatMessage) {
    if (message.senderType == SenderType.SYSTEM) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
            Surface(
                shape = RoundedCornerShape(999.dp),
                color = MaterialTheme.colorScheme.secondaryContainer
            ) {
                Text(
                    text = message.content,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }
        return
    }

    val isStaff = message.senderType == SenderType.PHARMACIST
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isStaff) Arrangement.End else Arrangement.Start
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(0.72f),
            shape = RoundedCornerShape(
                topStart = if (isStaff) 18.dp else 6.dp,
                topEnd = if (isStaff) 6.dp else 18.dp,
                bottomStart = 18.dp,
                bottomEnd = 18.dp
            ),
            color = if (isStaff) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
            tonalElevation = 1.dp,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f))
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isStaff) "Bạn · ${message.senderName}" else message.senderName,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = if (isStaff) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = formatChatTime(message.timestamp),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(Modifier.height(6.dp))
                Text(
                    text = message.content,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

private fun statusColor(status: ChatStatus): Color = when (status) {
    ChatStatus.PENDING -> Color(0xFFFF9800)
    ChatStatus.ASSIGNED -> Color(0xFF2E7D32)
    ChatStatus.RESOLVED -> Color(0xFF1976D2)
}

private fun formatChatTime(value: String): String {
    val dateTime = parseDateTime(value) ?: return value.take(16)
    val today = LocalDate.now()
    return when (dateTime.toLocalDate()) {
        today -> dateTime.format(DateTimeFormatter.ofPattern("HH:mm"))
        today.minusDays(1) -> "Hôm qua ${dateTime.format(DateTimeFormatter.ofPattern("HH:mm"))}"
        else -> dateTime.format(DateTimeFormatter.ofPattern("dd/MM HH:mm"))
    }
}

private fun parseDateTime(value: String): LocalDateTime? {
    return runCatching { LocalDateTime.parse(value) }.getOrNull()
        ?: runCatching {
            Instant.parse(value).atZone(ZoneId.systemDefault()).toLocalDateTime()
        }.getOrNull()
}
