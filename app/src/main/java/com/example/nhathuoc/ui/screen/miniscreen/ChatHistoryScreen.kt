package com.example.nhathuoc.ui.screen.miniscreen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBackIos
import androidx.compose.material.icons.filled.AddComment
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.nhathuoc.data.model.AiConversationDto
import com.example.nhathuoc.data.model.ChatSessionDto
import com.example.nhathuoc.viewmodel.AiChatViewModel
import com.example.nhathuoc.viewmodel.ChatViewModel

private val ChatHistGreenTop = Color(0xFF2E7D32)
private val ChatHistGreenLight = Color(0xFF66BB6A)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatHistoryScreen(
    onBack: () -> Unit = {},
    onOpenSession: (String) -> Unit = {},
    onOpenAiConversation: (String) -> Unit = {},
    onNewChat: () -> Unit = {},
    showBackButton: Boolean = true,
    viewModel: ChatViewModel = hiltViewModel(),
    aiViewModel: AiChatViewModel = hiltViewModel()
) {
    val historyState by viewModel.historyState.collectAsState()
    val aiConversationsState by aiViewModel.conversationsState.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) }
    var isRefreshing by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.loadSessions()
        aiViewModel.loadConversations()
    }
    LaunchedEffect(aiConversationsState.isLoading, historyState.isLoading) {
        if (!aiConversationsState.isLoading && !historyState.isLoading) isRefreshing = false
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Brush.horizontalGradient(listOf(ChatHistGreenTop, ChatHistGreenLight)))
                    .statusBarsPadding()
                    .height(64.dp)
            ) {
                if (showBackButton) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.align(Alignment.CenterStart)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBackIos, contentDescription = "Quay lại", tint = Color.White)
                    }
                }
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Lịch sử tư vấn", fontWeight = FontWeight.Bold, fontSize = 17.sp, color = Color.White)
                    Text("Xem lại các phiên hỏi đáp", fontSize = 11.sp, color = Color.White.copy(alpha = 0.8f))
                }
                IconButton(
                    onClick = onNewChat,
                    modifier = Modifier.align(Alignment.CenterEnd)
                ) {
                    Icon(Icons.Filled.AddComment, contentDescription = "Tư vấn mới", tint = Color.White)
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNewChat,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
                shape = CircleShape
            ) {
                Icon(Icons.Filled.AddComment, contentDescription = "Tư vấn mới")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("AI Medstore", fontSize = 13.sp) },
                    icon = { Icon(Icons.Filled.SmartToy, contentDescription = null, modifier = Modifier.size(18.dp)) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Chuyên viên", fontSize = 13.sp) },
                    icon = { Icon(Icons.Filled.ChatBubble, contentDescription = null, modifier = Modifier.size(18.dp)) }
                )
            }

            PullToRefreshBox(
                modifier = Modifier.fillMaxSize().weight(1f),
                isRefreshing = isRefreshing,
                onRefresh = {
                    isRefreshing = true
                    if (selectedTab == 0) aiViewModel.loadConversations()
                    else viewModel.loadSessions()
                }
            ) {
                when (selectedTab) {
                    0 -> AiConversationList(
                        state = aiConversationsState,
                        onOpenConversation = onOpenAiConversation,
                        onDelete = { aiViewModel.deleteConversation(it) },
                        onNewChat = onNewChat,
                        onRetry = { aiViewModel.loadConversations() }
                    )
                    1 -> ConsultantSessionList(
                        historyState = historyState,
                        onOpenSession = onOpenSession,
                        onNewChat = onNewChat,
                        onRetry = { viewModel.loadSessions() }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AiConversationList(
    state: com.example.nhathuoc.viewmodel.AiConversationsState,
    onOpenConversation: (String) -> Unit,
    onDelete: (String) -> Unit,
    onNewChat: () -> Unit,
    onRetry: () -> Unit
) {
    when {
        state.isLoading -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        }
        state.error != null -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(state.error, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
                    Button(onClick = onRetry) { Text("Thử lại") }
                }
            }
        }
        state.conversations.isEmpty() -> {
            Column(
                modifier = Modifier.fillMaxSize().padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    Icons.Filled.SmartToy,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                    modifier = Modifier.size(72.dp)
                )
                Spacer(Modifier.height(16.dp))
                Text("Chưa có cuộc hội thoại AI nào", fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(8.dp))
                Text(
                    "Bắt đầu hỏi AI Medstore về vật tư y tế và sản phẩm phù hợp",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 19.sp
                )
                Spacer(Modifier.height(24.dp))
                Button(
                    onClick = onNewChat,
                    shape = RoundedCornerShape(50),
                    contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp)
                ) {
                    Icon(Icons.Filled.SmartToy, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Hỏi AI ngay")
                }
            }
        }
        else -> {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(state.conversations, key = { it.id }) { conv ->
                    val dismissState = rememberSwipeToDismissBoxState(
                        confirmValueChange = { value ->
                            if (value == SwipeToDismissBoxValue.EndToStart) {
                                onDelete(conv.id)
                                true
                            } else false
                        }
                    )
                    SwipeToDismissBox(
                        state = dismissState,
                        enableDismissFromStartToEnd = false,
                        backgroundContent = {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(MaterialTheme.colorScheme.errorContainer),
                                contentAlignment = Alignment.CenterEnd
                            ) {
                                Icon(
                                    Icons.Filled.Delete,
                                    contentDescription = "Xóa",
                                    tint = MaterialTheme.colorScheme.onErrorContainer,
                                    modifier = Modifier.padding(end = 24.dp)
                                )
                            }
                        }
                    ) {
                        AiConversationCard(conversation = conv, onClick = { onOpenConversation(conv.id) })
                    }
                }
                item { Spacer(Modifier.height(72.dp)) }
            }
        }
    }
}

@Composable
private fun ConsultantSessionList(
    historyState: com.example.nhathuoc.viewmodel.ChatHistoryUiState,
    onOpenSession: (String) -> Unit,
    onNewChat: () -> Unit,
    onRetry: () -> Unit
) {
    when {
        historyState.isLoading -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        }
        historyState.sessions.isEmpty() -> {
            Column(
                modifier = Modifier.fillMaxSize().padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    Icons.Filled.ChatBubble,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                    modifier = Modifier.size(72.dp)
                )
                Spacer(Modifier.height(16.dp))
                Text("Chưa có phiên tư vấn nào", fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(8.dp))
                Text(
                    "Bắt đầu cuộc trò chuyện với chuyên viên dược để được tư vấn sản phẩm phù hợp",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 19.sp
                )
                Spacer(Modifier.height(24.dp))
                Button(
                    onClick = onNewChat,
                    shape = RoundedCornerShape(50),
                    contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp)
                ) {
                    Icon(Icons.Filled.AddComment, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Tư vấn ngay")
                }
            }
        }
        else -> {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(historyState.sessions, key = { it.id }) { session ->
                    ChatSessionCard(session = session, onClick = { onOpenSession(session.id) })
                }
                item { Spacer(Modifier.height(72.dp)) }
            }
        }
    }

    historyState.error?.let { err ->
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(err, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
                Button(onClick = onRetry) { Text("Thử lại") }
            }
        }
    }
}

@Composable
private fun AiConversationCard(
    conversation: AiConversationDto,
    onClick: () -> Unit
) {
    val isActive = conversation.status.equals("ACTIVE", ignoreCase = true)
    val isClosed = conversation.status.equals("CLOSED", ignoreCase = true)
    val isEscalated = conversation.escalatedToConsultant

    val statusColor = when {
        isEscalated -> Color(0xFF1565C0)
        isClosed -> Color(0xFF757575)
        else -> MaterialTheme.colorScheme.primary
    }
    val statusLabel = when {
        isEscalated -> "Đã kết nối chuyên viên"
        isClosed -> "Đã kết thúc"
        isActive -> "Đang hoạt động"
        else -> conversation.status
    }
    val statusBg = when {
        isEscalated -> Color(0xFFE3F2FD)
        isClosed -> Color(0xFFF5F5F5)
        else -> MaterialTheme.colorScheme.primaryContainer
    }

    val lastMsg = conversation.messages.lastOrNull()

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 2.dp,
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.SmartToy, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(30.dp))
            }

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "AI Medstore",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(Modifier.width(8.dp))
                    Surface(shape = RoundedCornerShape(50), color = statusBg) {
                        Text(
                            statusLabel,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = statusColor,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }

                Spacer(Modifier.height(3.dp))

                Text(
                    text = when {
                        lastMsg != null && lastMsg.text.isNotBlank() -> lastMsg.text
                        else -> "Nhấn để tiếp tục cuộc trò chuyện"
                    },
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 17.sp
                )

                if (conversation.createdAt.isNotBlank()) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = formatChatDate(conversation.createdAt),
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }
            }
        }
    }
}

@Composable
private fun ChatSessionCard(
    session: ChatSessionDto,
    onClick: () -> Unit
) {
    val isResolved = session.status.equals("RESOLVED", ignoreCase = true)
    val isPending = session.status.equals("PENDING", ignoreCase = true)
    val isAssigned = session.status.equals("ASSIGNED", ignoreCase = true)

    val statusColor = when {
        isResolved -> Color(0xFF757575)
        isAssigned -> Color(0xFF1565C0)
        isPending -> Color(0xFFE65100)
        else -> MaterialTheme.colorScheme.primary
    }
    val statusLabel = when {
        isResolved -> "Đã kết thúc"
        isAssigned -> "Đang tư vấn"
        isPending -> "Đang chờ"
        else -> session.status
    }
    val statusBg = when {
        isResolved -> Color(0xFFF5F5F5)
        isAssigned -> Color(0xFFE3F2FD)
        isPending -> Color(0xFFFFF3E0)
        else -> MaterialTheme.colorScheme.primaryContainer
    }

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 2.dp,
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                if (!session.productImageUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = session.productImageUrl,
                        contentDescription = session.productName,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(Icons.Filled.ChatBubble, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(26.dp))
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = session.productName ?: "Tư vấn chung",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(Modifier.width(8.dp))
                    Surface(shape = RoundedCornerShape(50), color = statusBg) {
                        Text(
                            statusLabel,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = statusColor,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }

                Spacer(Modifier.height(3.dp))

                if (!session.consultantName.isNullOrBlank()) {
                    Text(
                        "Chuyên viên: ${session.consultantName}",
                        fontSize = 11.sp,
                        color = Color(0xFF1565C0),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.height(2.dp))
                }

                val lastMsgText = session.lastMessage?.content
                Text(
                    text = when {
                        !lastMsgText.isNullOrBlank() -> lastMsgText
                        else -> "Nhấn để xem cuộc trò chuyện"
                    },
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 17.sp
                )

                Spacer(Modifier.height(4.dp))

                Text(
                    text = formatChatDate(session.createdAt),
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }
        }
    }
}

private fun formatChatDate(dateStr: String): String {
    return try {
        val parts = dateStr.take(16).split("T")
        if (parts.size == 2) {
            val dateParts = parts[0].split("-")
            if (dateParts.size == 3) "${dateParts[2]}/${dateParts[1]}/${dateParts[0]} ${parts[1]}"
            else dateStr.take(16)
        } else dateStr.take(10)
    } catch (_: Exception) {
        dateStr.take(10)
    }
}
