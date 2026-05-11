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
import androidx.compose.material3.*
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
import com.example.nhathuoc.data.model.ChatSessionDto
import com.example.nhathuoc.viewmodel.ChatViewModel

private val GreenTop = Color(0xFF2E7D32)
private val GreenLight = Color(0xFF66BB6A)

@Composable
fun ChatHistoryScreen(
    onBack: () -> Unit = {},
    onOpenSession: (String) -> Unit = {},
    onNewChat: () -> Unit = {},
    viewModel: ChatViewModel = hiltViewModel()
) {
    val historyState by viewModel.historyState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadSessions()
    }

    Scaffold(
        containerColor = Color(0xFFF5F7FA),
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Brush.horizontalGradient(listOf(GreenTop, GreenLight)))
                    .padding(top = 12.dp, start = 4.dp, end = 12.dp, bottom = 12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 28.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBackIos, contentDescription = "Quay lại", tint = Color.White)
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Lịch sử tư vấn", fontWeight = FontWeight.Bold, fontSize = 17.sp, color = Color.White)
                        Text("Xem lại các phiên hỏi đáp với chuyên viên", fontSize = 12.sp, color = Color.White.copy(alpha = 0.8f))
                    }
                    IconButton(onClick = onNewChat) {
                        Icon(Icons.Filled.AddComment, contentDescription = "Tư vấn mới", tint = Color.White)
                    }
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNewChat,
                containerColor = GreenTop,
                contentColor = Color.White,
                shape = CircleShape
            ) {
                Icon(Icons.Filled.AddComment, contentDescription = "Tư vấn mới")
            }
        }
    ) { padding ->
        when {
            historyState.isLoading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = GreenTop)
                }
            }
            historyState.sessions.isEmpty() -> {
                EmptyChatHistory(onNewChat = onNewChat)
            }
            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(historyState.sessions, key = { it.id }) { session ->
                        ChatSessionCard(
                            session = session,
                            onClick = { onOpenSession(session.id) }
                        )
                    }
                    item { Spacer(Modifier.height(72.dp)) }
                }
            }
        }

        historyState.error?.let { err ->
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(err, color = Color.Gray, fontSize = 14.sp)
                    Button(onClick = { viewModel.loadSessions() }, colors = ButtonDefaults.buttonColors(containerColor = GreenTop)) {
                        Text("Thử lại")
                    }
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
        else -> GreenTop
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
        else -> Color(0xFFE8F5E9)
    }

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = Color.White,
        shadowElevation = 2.dp,
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Product thumbnail or default icon
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFFE8F5E9)),
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
                    Icon(Icons.Filled.ChatBubble, contentDescription = null, tint = GreenTop, modifier = Modifier.size(26.dp))
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
                        color = Color(0xFF1A1A1A),
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

                // Consultant name if assigned
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

                // Last message preview
                val lastMsgText = session.lastMessage?.content
                Text(
                    text = when {
                        !lastMsgText.isNullOrBlank() -> lastMsgText
                        else -> "Nhấn để xem cuộc trò chuyện"
                    },
                    fontSize = 12.sp,
                    color = Color(0xFF757575),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 17.sp
                )

                Spacer(Modifier.height(4.dp))

                Text(
                    text = formatChatDate(session.createdAt),
                    fontSize = 11.sp,
                    color = Color(0xFFBDBDBD)
                )
            }
        }
    }
}

@Composable
private fun EmptyChatHistory(onNewChat: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Filled.ChatBubble,
            contentDescription = null,
            tint = Color(0xFFBDBDBD),
            modifier = Modifier.size(72.dp)
        )
        Spacer(Modifier.height(16.dp))
        Text("Chưa có phiên tư vấn nào", fontSize = 17.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF555555))
        Spacer(Modifier.height(8.dp))
        Text(
            "Bắt đầu cuộc trò chuyện với chuyên viên dược để được tư vấn sản phẩm phù hợp",
            fontSize = 13.sp,
            color = Color.Gray,
            lineHeight = 19.sp
        )
        Spacer(Modifier.height(24.dp))
        Button(
            onClick = onNewChat,
            colors = ButtonDefaults.buttonColors(containerColor = GreenTop),
            shape = RoundedCornerShape(50),
            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp)
        ) {
            Icon(Icons.Filled.AddComment, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text("Tư vấn ngay")
        }
    }
}

private fun formatChatDate(dateStr: String): String {
    return try {
        // dateStr format: "2025-10-28T09:30:00"
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
