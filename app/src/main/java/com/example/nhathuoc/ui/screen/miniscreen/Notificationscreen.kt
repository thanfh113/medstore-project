package com.example.nhathuoc.ui.screen.miniscreen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material.icons.outlined.DoneAll
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.nhathuoc.data.model.NotificationDto
import com.example.nhathuoc.data.model.UiState
import com.example.nhathuoc.ui.theme.GreenLight
import com.example.nhathuoc.viewmodel.NotificationViewModel

private val GreenTopNtf = Color(0xFF2E7D32)

// Category filter model
private data class NotifCategory(val key: String, val label: String)
private val notifCategories = listOf(
    NotifCategory("ALL", "Tất cả"),
    NotifCategory("PROMOTION", "Khuyến mãi"),
    NotifCategory("ORDER", "Đơn hàng"),
    NotifCategory("REWARD", "Điểm thưởng"),
    NotifCategory("COMPLAINT", "Khiếu nại"),
    NotifCategory("REFUND", "Hoàn tiền"),
    NotifCategory("CHAT", "Tư vấn"),
    NotifCategory("REVIEW", "Đánh giá"),
    NotifCategory("SYSTEM", "Hệ thống")
)

// ── Screen ─────────────────────────────────────────────────────────────────
@Composable
fun NotificationScreen(
    onBack: () -> Unit = {},
    onNotificationClick: (NotificationDto) -> Unit = {}
) {
    val viewModel: NotificationViewModel = hiltViewModel()
    val state by viewModel.state.collectAsState()
    val notifications by viewModel.notifications.collectAsState()

    var selectedCategoryKey by remember { mutableStateOf("ALL") }

    val filtered = remember(notifications, selectedCategoryKey) {
        if (selectedCategoryKey == "ALL") notifications
        else notifications.filter { it.type.uppercase() == selectedCategoryKey }
    }

    val unreadCounts = remember(notifications) {
        notifCategories.associate { cat ->
            cat.key to if (cat.key == "ALL") {
                notifications.count { !it.isRead }
            } else {
                notifications.count { !it.isRead && it.type.uppercase() == cat.key }
            }
        }
    }

    Scaffold(
        topBar = {
            NotificationTopBar(
                onBack = onBack,
                onMarkAllRead = { viewModel.markAllAsRead() }
            )
        },
        containerColor = Color(0xFFF5F7FA)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // ── Category tab row ───────────────────────────────────────
            Surface(color = Color.White, shadowElevation = 2.dp) {
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(notifCategories) { cat ->
                        val count = unreadCounts[cat.key] ?: 0
                        val isSelected = selectedCategoryKey == cat.key
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedCategoryKey = cat.key },
                            label = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = cat.label,
                                        fontSize = 13.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                    if (count > 0) {
                                        Spacer(Modifier.width(4.dp))
                                        Badge(containerColor = Color(0xFFFF6D00)) {
                                            Text(count.toString(), color = Color.White, fontSize = 9.sp)
                                        }
                                    }
                                }
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = GreenTopNtf,
                                selectedLabelColor = Color.White,
                                containerColor = Color(0xFFF0F0F0)
                            ),
                            border = null
                        )
                    }
                }
            }

            // ── "Đọc tất cả" action row ────────────────────────────────
            if (notifications.any { !it.isRead }) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFF5F7FA))
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.DoneAll,
                        contentDescription = null,
                        tint = GreenTopNtf,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = "Đọc tất cả",
                        color = GreenTopNtf,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.clickable { viewModel.markAllAsRead() }
                    )
                }
            }

            // ── Content state ──────────────────────────────────────────
            when (state) {
                is UiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = GreenTopNtf)
                            Spacer(Modifier.height(12.dp))
                            Text(
                                "Đang tải thông báo...",
                                color = Color.Gray,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
                is UiState.Error -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(24.dp)
                        ) {
                            Icon(
                                Icons.Outlined.Notifications,
                                null,
                                tint = Color.LightGray,
                                modifier = Modifier.size(64.dp)
                            )
                            Spacer(Modifier.height(12.dp))
                            Text(
                                "Không thể tải thông báo",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                (state as UiState.Error).message,
                                color = Color.Gray,
                                textAlign = TextAlign.Center
                            )
                            Spacer(Modifier.height(16.dp))
                            Button(
                                onClick = { viewModel.loadNotifications() },
                                colors = ButtonDefaults.buttonColors(containerColor = GreenTopNtf)
                            ) {
                                Icon(Icons.Outlined.Refresh, null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Thử lại")
                            }
                        }
                    }
                }
                else -> {
                    if (filtered.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Filled.Notifications,
                                    contentDescription = null,
                                    tint = Color.LightGray,
                                    modifier = Modifier.size(64.dp)
                                )
                                Spacer(Modifier.height(12.dp))
                                Text("Không có thông báo", color = Color.Gray, fontSize = 15.sp)
                                if (selectedCategoryKey != "ALL") {
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        "trong danh mục này",
                                        color = Color(0xFFAAAAAA),
                                        fontSize = 13.sp
                                    )
                                }
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(vertical = 8.dp)
                        ) {
                            itemsIndexed(filtered, key = { _, it -> it.id }) { _, notif ->
                                NotificationCard(
                                    item = notif,
                                    onDismiss = { viewModel.dismissNotification(notif.id) },
                                    onClick = {
                                        if (!notif.isRead) viewModel.markAsRead(notif.id)
                                        onNotificationClick(notif)
                                    }
                                )
                                HorizontalDivider(
                                    color = Color(0xFFF0F0F0),
                                    thickness = 0.8.dp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ── TopBar ──────────────────────────────────────────────────────────────────
@Composable
private fun NotificationTopBar(
    onBack: () -> Unit,
    onMarkAllRead: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Brush.horizontalGradient(listOf(GreenTopNtf, GreenLight)))
            .statusBarsPadding()
            .height(56.dp)
    ) {
        IconButton(
            onClick = onBack,
            modifier = Modifier.align(Alignment.CenterStart)
        ) {
            Icon(
                imageVector = Icons.Filled.ArrowBackIosNew,
                contentDescription = "Quay lại",
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
        }
        Text(
            text = "Thông báo",
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.Center)
        )
        IconButton(
            onClick = onMarkAllRead,
            modifier = Modifier.align(Alignment.CenterEnd)
        ) {
            Icon(
                Icons.Outlined.DoneAll,
                contentDescription = "Đọc tất cả",
                tint = Color.White,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

// ── Notification Card ────────────────────────────────────────────────────────
@Composable
private fun NotificationCard(
    item: NotificationDto,
    onDismiss: () -> Unit,
    onClick: () -> Unit
) {
    val (iconVec, iconColor) = when (item.type.uppercase()) {
        "ORDER", "ORDER_STATUS" -> Pair(Icons.Filled.LocalShipping, Color(0xFF1565C0))
        "PROMOTION" -> Pair(Icons.Filled.Campaign, Color(0xFFE53935))
        "REWARD" -> Pair(Icons.Filled.ShoppingBag, Color(0xFFEF6C00))
        "CHAT" -> Pair(Icons.Filled.Notifications, Color(0xFF00897B))
        "COMPLAINT", "REFUND", "REVIEW" -> Pair(Icons.Filled.SystemUpdate, Color(0xFF6A1B9A))
        else -> Pair(Icons.Filled.SystemUpdate, Color(0xFF2E7D32))
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        color = if (item.isRead) Color.White else Color(0xFFF0F4FF)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.Top
        ) {
            // ── Icon ──────────────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(iconColor.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = iconVec,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(Modifier.width(12.dp))

            // ── Content ──────────────────────────────────────────────────
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = item.title,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1A1A1A),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        if (!item.isRead) {
                            Spacer(Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(GreenTopNtf)
                            )
                        }
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "Xoá",
                            tint = Color.Gray,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }

                Spacer(Modifier.height(4.dp))

                // Time
                val displayTime = item.createdAt.take(16).replace("T", " ")
                Text(
                    text = displayTime,
                    fontSize = 11.sp,
                    color = Color.Gray
                )

                Spacer(Modifier.height(6.dp))

                // Body
                Text(
                    text = item.body ?: item.message.orEmpty(),
                    fontSize = 13.sp,
                    color = Color(0xFF444444),
                    lineHeight = 19.sp,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
