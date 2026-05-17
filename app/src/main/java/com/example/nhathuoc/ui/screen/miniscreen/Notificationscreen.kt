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
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
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
import com.example.nhathuoc.viewmodel.NotificationViewModel
import kotlinx.coroutines.delay

// GreenTop and GreenLight kept for gradient header only
private val GreenTopNtf = Color(0xFF2E7D32)
private val GreenLightNtf = Color(0xFF66BB6A)

private fun notificationMatchesCategory(item: NotificationDto, categoryKey: String): Boolean {
    if (categoryKey == "ALL") return true
    val type = item.type.uppercase()
    return when (categoryKey) {
        "ORDER" -> type == "ORDER" || type == "ORDER_STATUS" || type.startsWith("ORDER_")
        "REWARD" -> type == "REWARD" || type == "POINT" || type == "VOUCHER" || type.startsWith("REWARD_")
        "COMPLAINT" -> type == "COMPLAINT" || type.startsWith("COMPLAINT_")
        "REFUND" -> type == "REFUND" || type.startsWith("REFUND_")
        "CHAT" -> type == "CHAT" || type == "CONSULTATION" || type.startsWith("CHAT_")
        "REVIEW" -> type == "REVIEW" || type.startsWith("REVIEW_")
        "PROMOTION" -> type == "PROMOTION" || type == "COUPON" || type.startsWith("PROMOTION_")
        else -> type == categoryKey
    }
}

// Category filter model
private data class NotifCategory(val key: String, val label: String)
private data class ReadFilter(val key: String, val label: String)

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

private val readFilters = listOf(
    ReadFilter("ALL", "Tất cả"),
    ReadFilter("UNREAD", "Chưa đọc"),
    ReadFilter("READ", "Đã đọc")
)

// ── Screen ─────────────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationScreen(
    onBack: () -> Unit = {},
    onNotificationClick: (NotificationDto) -> Unit = {}
) {
    val viewModel: NotificationViewModel = hiltViewModel()
    val state by viewModel.state.collectAsState()
    val notifications by viewModel.notifications.collectAsState()
    val unreadCount by viewModel.unreadCount.collectAsState()

    // Pull-to-refresh: manual full reload (shows inline indicator, no full-screen spinner)
    var isRefreshing by remember { mutableStateOf(false) }
    LaunchedEffect(state) {
        if (state !is UiState.Loading) isRefreshing = false
    }

    // Silent background poll every 30s — prepends new items without any flicker
    LaunchedEffect(Unit) {
        while (true) {
            delay(30_000)
            viewModel.refreshSilently()
        }
    }

    var selectedCategoryKey by remember { mutableStateOf("ALL") }
    var selectedReadFilter by remember { mutableStateOf("ALL") }

    val filtered = remember(notifications, selectedCategoryKey, selectedReadFilter) {
        notifications
            .filter { notificationMatchesCategory(it, selectedCategoryKey) }
            .filter {
                when (selectedReadFilter) {
                    "UNREAD" -> !it.isRead
                    "READ" -> it.isRead
                    else -> true
                }
            }
    }

    val unreadCounts = remember(notifications) {
        notifCategories.associate { cat ->
            cat.key to if (cat.key == "ALL") {
                notifications.count { !it.isRead }
            } else {
                notifications.count { !it.isRead && notificationMatchesCategory(it, cat.key) }
            }
        }
    }

    val readCounts = remember(notifications) {
        mapOf(
            "ALL" to notifications.size,
            "UNREAD" to notifications.count { !it.isRead },
            "READ" to notifications.count { it.isRead }
        )
    }

    Scaffold(
        topBar = {
            NotificationTopBar(
                onBack = onBack,
                unreadCount = unreadCount,
                onMarkAllRead = { viewModel.markAllAsRead() }
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // ── Category tab row ───────────────────────────────────────
            Surface(color = MaterialTheme.colorScheme.surface, shadowElevation = 2.dp) {
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
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = Color.White,
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            ),
                            border = null
                        )
                    }
                }
            }

            // ── Read state filter ────────────────────────────────────
            Surface(color = MaterialTheme.colorScheme.surface, shadowElevation = 1.dp) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    readFilters.forEach { filter ->
                        val count = readCounts[filter.key] ?: 0
                        val isSelected = selectedReadFilter == filter.key
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedReadFilter = filter.key },
                            label = {
                                Text(
                                    text = "${filter.label} ($count)",
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.primary,
                                containerColor = MaterialTheme.colorScheme.surface
                            )
                        )
                    }
                }
            }

            // ── "Đọc tất cả" action row ────────────────────────────────
            if (notifications.any { !it.isRead }) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.background)
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.DoneAll,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = "Đọc tất cả",
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.clickable { viewModel.markAllAsRead() }
                    )
                }
            }

            // ── Content (pull-to-refresh wraps everything below filters) ──
            PullToRefreshBox(
                modifier = Modifier.fillMaxSize(),
                isRefreshing = isRefreshing,
                onRefresh = {
                    isRefreshing = true
                    viewModel.loadNotifications()
                }
            ) {
                when {
                    // Initial load — no existing data yet
                    state is UiState.Loading && notifications.isEmpty() -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                                Spacer(Modifier.height(12.dp))
                                Text(
                                    "Đang tải thông báo...",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                    // Error with no existing data
                    state is UiState.Error && notifications.isEmpty() -> {
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
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(Modifier.height(16.dp))
                                Button(
                                    onClick = { viewModel.loadNotifications() },
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                                ) {
                                    Icon(Icons.Outlined.Refresh, null, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text("Thử lại")
                                }
                            }
                        }
                    }
                    // Empty filtered result (data loaded but nothing matches filter)
                    filtered.isEmpty() -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Filled.Notifications,
                                    contentDescription = null,
                                    tint = Color.LightGray,
                                    modifier = Modifier.size(64.dp)
                                )
                                Spacer(Modifier.height(12.dp))
                                Text("Không có thông báo phù hợp", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 15.sp)
                                if (selectedCategoryKey != "ALL") {
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        "trong danh mục này",
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 13.sp
                                    )
                                }
                            }
                        }
                    }
                    // Normal list — also shown during background refresh (isRefreshing=true)
                    else -> {
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
                                    color = MaterialTheme.colorScheme.outlineVariant,
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
    unreadCount: Int,
    onMarkAllRead: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            // Gradient header kept with original green colors (text/icons on dark bg kept white)
            .background(Brush.horizontalGradient(listOf(GreenTopNtf, GreenLightNtf)))
            .statusBarsPadding()
            .height(64.dp)
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
        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Thông báo",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = if (unreadCount > 0) "$unreadCount thông báo chưa đọc" else "Tất cả đã đọc",
                color = Color.White.copy(alpha = 0.85f),
                fontSize = 11.sp
            )
        }
        if (unreadCount > 0) {
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

    val unreadBg = MaterialTheme.colorScheme.primary.copy(alpha = 0.09f)
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        color = if (item.isRead) MaterialTheme.colorScheme.surface else unreadBg
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
                            fontWeight = if (item.isRead) FontWeight.Medium else FontWeight.Bold,
                            color = if (item.isRead) MaterialTheme.colorScheme.onSurfaceVariant
                                    else MaterialTheme.colorScheme.onSurface,
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
                                    .background(MaterialTheme.colorScheme.primary)
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
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
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
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(Modifier.height(6.dp))
                ReadStatusPill(isRead = item.isRead)

                Spacer(Modifier.height(6.dp))

                // Body
                Text(
                    text = item.body ?: item.message.orEmpty(),
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 19.sp,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun ReadStatusPill(isRead: Boolean) {
    val background = if (isRead) MaterialTheme.colorScheme.primaryContainer
                     else Color(0xFFFF8F00).copy(alpha = 0.18f)
    val content = if (isRead) MaterialTheme.colorScheme.primary else Color(0xFFFF8F00)
    Surface(
        shape = RoundedCornerShape(50),
        color = background
    ) {
        Text(
            text = if (isRead) "Đã đọc" else "Chưa đọc",
            color = content,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
        )
    }
}
