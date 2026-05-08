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
import androidx.compose.material3.AlertDialog
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
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import org.example.project.data.models.ChatConversation
import org.example.project.data.models.ChatMessage
import org.example.project.data.models.ChatStatus
import org.example.project.data.models.Product
import org.example.project.data.models.ProductCategory
import org.example.project.data.models.ProductRecommendation
import org.example.project.data.models.SenderType
import org.example.project.data.models.categoryDisplayPath
import org.example.project.data.models.productCategoryMatches
import org.example.project.data.models.topLevelProductCategories
import org.example.project.presentation.viewmodels.ChatUiState
import org.example.project.presentation.viewmodels.ChatViewModel
import org.example.project.ui.components.ProductDetailDialog

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
            onOpenProductPicker = viewModel::openProductSuggestionPicker,
            onOpenProduct = viewModel::openProductDetail,
            onResolve = viewModel::resolveSelectedConversation,
            modifier = Modifier.weight(1f).fillMaxHeight()
        )
    }

    if (uiState.isSuggestionPickerOpen) {
        ProductSuggestionDialog(
            uiState = uiState,
            onDismiss = viewModel::closeProductSuggestionPicker,
            onQueryChange = viewModel::updateProductSuggestionQuery,
            onCategoryChange = viewModel::selectProductSuggestionCategory,
            onSuggest = viewModel::sendProductSuggestion,
            onOpenProduct = viewModel::openProductDetail
        )
    }

    uiState.selectedProductDetail?.let { product ->
        ProductDetailDialog(
            product = product,
            categoryName = categoryDisplayPath(uiState.suggestionCategories, product.categoryId).ifBlank { null },
            onDismiss = viewModel::dismissProductDetail,
            formatVnd = ::formatVnd
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
    onOpenProductPicker: () -> Unit,
    onOpenProduct: (String) -> Unit,
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
    val isAdminViewer = uiState.currentUserRole == "ADMIN"
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
                            text = "Mã vật tư: ${it.takeLast(8)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(
                        text = "Người tư vấn: ${consultantName ?: "Chưa tiếp nhận"}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (isAdminViewer) {
                        Text(
                            text = "Chế độ giám sát: admin không trực tiếp chat với khách.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
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
                    item {
                        ChatProductContextCard(selectedConversation, onOpenProduct)
                    }
                    item {
                        ConsultantProfileCard(selectedConversation)
                    }
                    items(uiState.messages, key = { it.id }) { message ->
                        MessageItem(message, onOpenProduct)
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
                    placeholder = {
                        Text(
                            when {
                                isResolved -> "Phiên tư vấn đã kết thúc"
                                isAdminViewer -> "Admin chỉ xem và giám sát, không trực tiếp chat"
                                else -> "Nhập nội dung trả lời"
                            }
                        )
                    },
                    enabled = !isResolved && !uiState.isSendingMessage && !isAdminViewer,
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )
                OutlinedButton(
                    onClick = onOpenProductPicker,
                    enabled = !isResolved && !uiState.isSendingMessage && !isAdminViewer,
                    shape = RoundedCornerShape(999.dp),
                    modifier = Modifier.height(56.dp)
                ) {
                    Text("Gợi ý sản phẩm")
                }
                Button(
                    onClick = onSend,
                    enabled = !isResolved && !isAdminViewer && messageInput.isNotBlank() && !uiState.isSendingMessage,
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
private fun ChatProductContextCard(
    conversation: ChatConversation,
    onOpenProduct: (String) -> Unit
) {
    val productName = conversation.productName?.takeIf { it.isNotBlank() } ?: return
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = !conversation.productId.isNullOrBlank()) {
                conversation.productId?.let(onOpenProduct)
            },
        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.45f),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f))
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ProductThumb(
                imageUrl = conversation.productImageUrl,
                name = productName,
                modifier = Modifier.size(74.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text("Sản phẩm khách đang hỏi", style = MaterialTheme.typography.labelMedium)
                Text(productName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                val priceText = conversation.productPrice?.let { "${it.toLong()} đ" }
                Text(
                    listOfNotNull(priceText, conversation.productUnit).joinToString(" / "),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun ConsultantProfileCard(conversation: ChatConversation) {
    val consultantName = conversation.pharmacistName?.takeIf { it.isNotBlank() } ?: return
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f))
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("Người phụ trách", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(consultantName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            conversation.consultantQualificationTitle?.takeIf { it.isNotBlank() }?.let {
                Text(it, style = MaterialTheme.typography.bodyMedium)
            }
            conversation.consultantQualificationInstitution?.takeIf { it.isNotBlank() }?.let {
                Text("Đơn vị cấp: $it", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text(
                if (conversation.consultantVerified == true) "Hồ sơ chuyên môn đã xác minh" else "Hồ sơ chuyên môn chưa xác minh",
                style = MaterialTheme.typography.bodySmall,
                color = if (conversation.consultantVerified == true) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
            )
        }
    }
}

@Composable
private fun MessageItem(
    message: ChatMessage,
    onOpenProduct: (String) -> Unit
) {
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

    if (message.messageType == org.example.project.data.models.MessageType.PRODUCT_RECOMMENDATION ||
        message.productRecommendation != null
    ) {
        ProductRecommendationMessageItem(message, onOpenProduct)
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

@Composable
private fun ProductRecommendationMessageItem(
    message: ChatMessage,
    onOpenProduct: (String) -> Unit
) {
    val recommendation = message.productRecommendation
    val isStaff = message.senderType == SenderType.PHARMACIST
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isStaff) Arrangement.End else Arrangement.Start
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.72f)
                .clickable(enabled = recommendation != null) {
                    recommendation?.productId?.let(onOpenProduct)
                },
            shape = RoundedCornerShape(18.dp),
            color = if (isStaff) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
            tonalElevation = 1.dp,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f))
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
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

                if (recommendation == null) {
                    Text(message.content, style = MaterialTheme.typography.bodyLarge)
                    return@Column
                }

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    ProductThumb(
                        imageUrl = recommendation.productImage,
                        name = recommendation.productName,
                        modifier = Modifier.size(82.dp)
                    )
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        Text("Sản phẩm được gợi ý", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                        Text(
                            recommendation.productName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            "${formatVnd(recommendation.price)}${recommendation.productUnit?.let { " / $it" }.orEmpty()}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        recommendation.reason.takeIf { it.isNotBlank() }?.let {
                            Text(
                                it,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
                Text("Bấm vào thẻ để xem chi tiết", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

@Composable
private fun ProductThumb(
    imageUrl: String?,
    name: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        if (!imageUrl.isNullOrBlank()) {
            AsyncImage(
                model = imageUrl,
                contentDescription = name,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Icon(Icons.Default.Chat, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

@Composable
private fun ProductSuggestionDialog(
    uiState: ChatUiState,
    onDismiss: () -> Unit,
    onQueryChange: (String) -> Unit,
    onCategoryChange: (String?) -> Unit,
    onSuggest: (Product) -> Unit,
    onOpenProduct: (String) -> Unit
) {
    val suggestionGroups = topLevelProductCategories(uiState.suggestionCategories)
    val filteredProducts = filterSuggestionProducts(
        products = uiState.suggestionProducts,
        query = uiState.productSuggestionQuery,
        categoryId = uiState.productSuggestionCategoryId,
        categories = uiState.suggestionCategories
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Gợi ý sản phẩm cho khách", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = uiState.productSuggestionQuery,
                    onValueChange = onQueryChange,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Tìm tên, SKU, hãng sản phẩm") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    item {
                        StatusFilterChip(
                            label = "Tất cả",
                            selected = uiState.productSuggestionCategoryId == null,
                            onClick = { onCategoryChange(null) }
                        )
                    }
                    items(suggestionGroups, key = { it.id }) { category ->
                        StatusFilterChip(
                            label = category.displayName,
                            selected = uiState.productSuggestionCategoryId == category.id,
                            onClick = { onCategoryChange(category.id) }
                        )
                    }
                }

                if (uiState.isLoadingSuggestionProducts) {
                    Box(modifier = Modifier.fillMaxWidth().height(220.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else if (filteredProducts.isEmpty()) {
                    Box(modifier = Modifier.fillMaxWidth().height(220.dp), contentAlignment = Alignment.Center) {
                        Text("Không có sản phẩm phù hợp", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth().height(420.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(filteredProducts, key = { it.id }) { product ->
                            ProductSuggestionRow(
                                product = product,
                                categoryName = categoryDisplayPath(uiState.suggestionCategories, product.categoryId).ifBlank { null },
                                onSuggest = { onSuggest(product) },
                                onOpenProduct = { onOpenProduct(product.id) }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Đóng")
            }
        }
    )
}

@Composable
private fun ProductSuggestionRow(
    product: Product,
    categoryName: String?,
    onSuggest: () -> Unit,
    onOpenProduct: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f))
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ProductThumb(
                imageUrl = product.images.firstOrNull()?.url,
                name = product.name,
                modifier = Modifier.size(72.dp)
            )
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(product.name, fontWeight = FontWeight.Bold, maxLines = 2, overflow = TextOverflow.Ellipsis)
                Text(
                    listOfNotNull(categoryName, product.sku).joinToString(" · ").ifBlank { "Chưa phân loại" },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    "${formatVnd(product.price)} / ${product.unit} · Tồn ${product.stockQuantity}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
            }
            OutlinedButton(onClick = onOpenProduct, shape = RoundedCornerShape(999.dp)) {
                Text("Xem")
            }
            Button(onClick = onSuggest, shape = RoundedCornerShape(999.dp), enabled = product.stockQuantity > 0) {
                Text("Gợi ý")
            }
        }
    }
}

private fun filterSuggestionProducts(
    products: List<Product>,
    query: String,
    categoryId: String?,
    categories: List<ProductCategory>
): List<Product> {
    val normalizedQuery = query.trim()
    return products.filter { product ->
        val categoryMatches = productCategoryMatches(product.categoryId, categoryId, categories)
        val queryMatches = normalizedQuery.isBlank() ||
            product.name.contains(normalizedQuery, ignoreCase = true) ||
            product.sku?.contains(normalizedQuery, ignoreCase = true) == true ||
            product.manufacturer.contains(normalizedQuery, ignoreCase = true)
        categoryMatches && queryMatches
    }.sortedWith(compareByDescending<Product> { it.stockQuantity > 0 }.thenBy { it.name })
}

private fun formatVnd(value: Double): String {
    return "%,.0f đ".format(value).replace(",", ".")
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
