package com.example.nhathuoc.ui.screen.miniscreen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.outlined.DoneAll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nhathuoc.ui.theme.GreenLight
private val GreenTop = Color(0xFF2E7D32)


// ── Data ─────────────────────────────────────────────────────────────
data class NotificationItem(
    val id: Int,
    val title: String,
    val body: String,
    val time: String,
    val isRead: Boolean = false,
    val category: String = "Khuyến mãi"
)

private val sampleNotifications = listOf(
    NotificationItem(
        id = 1,
        title = "BẠN ĐI LÀM LẠI RỒI HA, TRONG NGƯỜI CẢM THẤY THẾ NÀO?",
        body = "Sinh hoạt thất thường ngày Tết dễ kéo theo các bệnh vặt. Hãy chủ động phục hồi sức khỏe để trở lại nhịp công việc và đón một năm hanh thông!\n🔥 Hàng chính hãng\n🔥 Đủ thuốc: cảm ho, nhức đầu, tiêu hóa, vitamin\n🔥 Giao nhanh 1H miễn phí\n🛒 Mua ngay!",
        time = "15:46, 26/02/2026",
        isRead = false,
        category = "Khuyến mãi"
    ),
    NotificationItem(
        id = 2,
        title = "CÒN MỪNG CÒN TẾT CÒN LÌ XÌ",
        body = "🎵 Thần Tài đến, Thần Tài đến, hãy giang tay đón mời! Mừng 10 này, rước lộc Thần Tài từ Long Châu khi săn hóa đơn với số đuôi 79 – 7979 – 797979, bà con sẽ được lì xì ĐẾN 7.9 TRIỆU!\nChốt đơn để khởi đầu năm Mã đỏ tài vận nào!",
        time = "13:24, 25/02/2026",
        isRead = false,
        category = "Khuyến mãi"
    ),
    NotificationItem(
        id = 3,
        title = "✨ Xuân sang – Da sáng – Deal hời",
        body = "Tết đến xuân về, làn da cũng cần được \"tân trang\" đúng cách. Khám phá ngay bộ sưu tập dưỡng da Tết với ưu đãi lên đến 50%!",
        time = "18:00, 10/02/2026",
        isRead = false,
        category = "Khuyến mãi"
    ),
    NotificationItem(
        id = 4,
        title = "Đơn hàng #LC2402001 đã được giao thành công",
        body = "Đơn hàng của bạn đã được giao thành công. Cảm ơn bạn đã tin tưởng Long Châu. Hãy đánh giá sản phẩm để nhận thêm ưu đãi nhé!",
        time = "09:15, 24/02/2026",
        isRead = true,
        category = "Đơn hàng"
    ),
    NotificationItem(
        id = 5,
        title = "Yêu cầu tư vấn của bạn đã được tiếp nhận",
        body = "Dược sĩ của chúng tôi sẽ liên hệ với bạn trong vòng 15 phút. Cảm ơn bạn đã sử dụng dịch vụ tư vấn của Long Châu.",
        time = "14:30, 23/02/2026",
        isRead = true,
        category = "Yêu cầu tư vấn"
    ),
)

private val categories = listOf("Tất cả", "Khuyến mãi", "Đơn hàng", "Yêu cầu tư vấn")

// ── Screen ────────────────────────────────────────────────────────────
@Composable
fun NotificationScreen(onBack: () -> Unit = {}) {
    var selectedCategory by remember { mutableStateOf("Tất cả") }
    var notifications by remember { mutableStateOf(sampleNotifications) }

    val filtered = if (selectedCategory == "Tất cả") notifications
    else notifications.filter { it.category == selectedCategory }

    val unreadCounts = mapOf(
        "Tất cả"            to notifications.count { !it.isRead },
        "Khuyến mãi"        to notifications.count { !it.isRead && it.category == "Khuyến mãi" },
        "Đơn hàng"          to notifications.count { !it.isRead && it.category == "Đơn hàng" },
        "Yêu cầu tư vấn"    to notifications.count { !it.isRead && it.category == "Yêu cầu tư vấn" },
    )

    Scaffold(
        topBar = {
            NotificationTopBar(
                onBack = onBack,
                onMarkAllRead = {
                    notifications = notifications.map { it.copy(isRead = true) }
                }
            )
        },
        containerColor = Color(0xFFF5F7FA)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Category tab row
            Surface(
                color = Color.White,
                shadowElevation = 2.dp
            ) {
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(categories) { cat ->
                        val count = unreadCounts[cat] ?: 0
                        val isSelected = selectedCategory == cat
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedCategory = cat },
                            label = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = cat,
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
                                selectedContainerColor = GreenTop,
                                selectedLabelColor = Color.White,
                                containerColor = Color(0xFFF0F0F0)
                            ),
                            border = null
                        )
                    }
                }
            }

            // "Mark all read" hint row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF5F7FA))
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.DoneAll,
                    contentDescription = null,
                    tint = GreenTop,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    text = "Đọc tất cả",
                    color = GreenTop,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.clickable {
                        notifications = notifications.map { it.copy(isRead = true) }
                    }
                )
            }

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
                    }
                }
            } else {
                // Enable notification banner
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFF0F4FF),
                    tonalElevation = 0.dp
                ) {
                    var showBanner by remember { mutableStateOf(true) }
                    if (showBanner) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFE8EAF6)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Notifications,
                                    contentDescription = null,
                                    tint = GreenTop,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    "Bật thông báo để không bỏ lỡ những cập nhật và tin tức mới nhất từ Long Châu nhé!",
                                    fontSize = 12.sp,
                                    color = Color(0xFF333333),
                                    lineHeight = 17.sp
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    "Mở cài đặt",
                                    fontSize = 12.sp,
                                    color = GreenTop,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            IconButton(
                                onClick = { showBanner = false },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(Icons.Filled.Close, contentDescription = "Đóng", tint = Color.Gray, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    items(filtered, key = { it.id }) { notif ->
                        NotificationCard(
                            item = notif,
                            onDismiss = {
                                notifications = notifications.filter { it.id != notif.id }
                            },
                            onClick = {
                                notifications = notifications.map {
                                    if (it.id == notif.id) it.copy(isRead = true) else it
                                }
                            }
                        )
                        HorizontalDivider(color = Color(0xFFF0F0F0), thickness = 0.8.dp)
                    }
                }
            }
        }
    }
}

// ── TopBar ────────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NotificationTopBar(
    onBack: () -> Unit,
    onMarkAllRead: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Brush.horizontalGradient(listOf(GreenTop, GreenLight)))
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
    }
}

// ── Notification Card ─────────────────────────────────────────────────
@Composable
private fun NotificationCard(
    item: NotificationItem,
    onDismiss: () -> Unit,
    onClick: () -> Unit
) {
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
            // Icon
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(GreenTop),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Notifications,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(Modifier.width(12.dp))

            // Content
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
                                    .background(GreenTop)
                            )
                        }
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(20.dp)
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
                Text(
                    text = item.time,
                    fontSize = 11.sp,
                    color = Color.Gray
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = item.body,
                    fontSize = 13.sp,
                    color = Color(0xFF444444),
                    lineHeight = 19.sp
                )
            }
        }
    }
}