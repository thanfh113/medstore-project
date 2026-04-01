package com.example.nhathuoc.ui.component

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nhathuoc.ui.theme.GreenLight
import com.example.nhathuoc.ui.theme.GreenTop
import kotlin.math.max
import kotlin.math.min

// Using GreenTop from theme
private val GreenTop = Color(0xFF2E7D32)
private val GoldColor = Color(0xFFFFAB00)
private val DrawerWidth = 300.dp

// ── Data ─────────────────────────────────────────────────────────
data class DrawerMenuItem(
    val label: String,
    val icon: ImageVector? = null,
    val children: List<String> = emptyList(),
    val badge: Int = 0
)

val defaultDrawerMenuItems = listOf(
    DrawerMenuItem("Thông báo",             badge = 3),
    DrawerMenuItem("Dụng cụ tiêm truyền",   children = listOf("Kim tiêm", "Ống xi lanh", "Dây truyền dịch", "Bướm tiêm")),
    DrawerMenuItem("Băng gạc - Cầm máu",    children = listOf("Băng dính y tế", "Gạc vô trùng", "Băng cuộn", "Băng keo thông tấm kháng sinh")),
    DrawerMenuItem("Thiết bị chẩn đoán",    children = listOf("Máy đo huyết áp", "Nhiệt kế y tế", "Máy đo SpO2", "Máy đo đường huyết")),
    DrawerMenuItem("Khẩu trang - PPE",      children = listOf("Khẩu trang y tế", "Khẩu trang N95", "Quần áo bảo hộ", "Kính bảo hộ")),
    DrawerMenuItem("Thiết bị phẫu thuật",   children = listOf("Dụng cụ vi phẫu", "Kẹp phẫu thuật", "Dây khâu", "Van cầm máu")),
    DrawerMenuItem("Chống nhiễm khuẩn"),
    DrawerMenuItem("Phục hồi chức năng",    children = listOf("Nạng - Xe lăn", "Dụng cụ vật lý trị liệu", "Nẹp chỉnh hình")),
    DrawerMenuItem("Tin tức - Kiến thức",   children = listOf("Tin tức ngành", "Hướng dẫn sử dụng", "Tiêu chuẩn chất lượng")),
    DrawerMenuItem("Hệ thống cửa hàng"),
)


// ── Drawer State ─────────────────────────────────────────────────
class DrawerState {
    var isOpen by mutableStateOf(false)
    // 0f = closed, 1f = fully open
    var dragProgress by mutableFloatStateOf(0f)

    fun open()  { isOpen = true;  dragProgress = 1f }
    fun close() { isOpen = false; dragProgress = 0f }
    fun toggle() { if (isOpen) close() else open() }
}

@Composable
fun rememberDrawerState() = remember { DrawerState() }

// ── Main composable ───────────────────────────────────────────────
@Composable
fun AppDrawer(
    drawerState: DrawerState,
    userName: String = "bào ngọc",
    rewardPoints: Int = 246,
    notificationCount: Int = 3,
    onMenuItemClick: (DrawerMenuItem) -> Unit = {},
    onCallHotline: () -> Unit = {},
    content: @Composable () -> Unit
) {
    val density = LocalDensity.current
    val drawerWidthPx = with(density) { DrawerWidth.toPx() }
    // Edge zone width for starting drag (30dp from left)
    val edgeZonePx = with(density) { 30.dp.toPx() }

    // Animated translation: 0 = off-screen left, drawerWidthPx = fully visible
    val animatedProgress by animateFloatAsState(
        targetValue = if (drawerState.isOpen) 1f else drawerState.dragProgress,
        animationSpec = tween(durationMillis = if (drawerState.isOpen && drawerState.dragProgress == 1f) 0 else 260),
        label = "drawerProgress"
    )

    val translationX = -drawerWidthPx + animatedProgress * drawerWidthPx
    val scrimAlpha   = animatedProgress * 0.5f

    // Track drag start position
    var dragStartX by remember { mutableFloatStateOf(0f) }
    var isDragging by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        // ── Main content ──────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxSize()
                // Detect horizontal drag from left edge OR drag-to-close anywhere when open
                .pointerInput(Unit) {
                    detectHorizontalDragGestures(
                        onDragStart = { offset ->
                            dragStartX = offset.x
                            isDragging = (offset.x < edgeZonePx && !drawerState.isOpen) || drawerState.isOpen
                        },
                        onDragEnd = {
                            if (isDragging) {
                                if (drawerState.dragProgress > 0.4f) drawerState.open()
                                else drawerState.close()
                            }
                            isDragging = false
                        },
                        onDragCancel = {
                            if (isDragging) drawerState.close()
                            isDragging = false
                        },
                        onHorizontalDrag = { _, dragAmount ->
                            if (isDragging) {
                                val newProgress = (drawerState.dragProgress + dragAmount / drawerWidthPx)
                                    .coerceIn(0f, 1f)
                                drawerState.dragProgress = newProgress
                                // Sync isOpen state
                                if (newProgress >= 1f) drawerState.isOpen = true
                                else if (newProgress <= 0f) drawerState.isOpen = false
                            }
                        }
                    )
                }
        ) {
            content()
        }

        // ── Scrim ─────────────────────────────────────────────────
        if (animatedProgress > 0f) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer { alpha = scrimAlpha }
                    .background(Color.Black)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = { drawerState.close() }
                    )
            )
        }

        // ── Drawer panel ──────────────────────────────────────────
        Box(
            modifier = Modifier
                .width(DrawerWidth)
                .fillMaxHeight()
                .graphicsLayer { this.translationX = translationX }
                .background(Color.White, RoundedCornerShape(topEnd = 0.dp, bottomEnd = 0.dp))
        ) {
            DrawerContent(
                userName          = userName,
                rewardPoints      = rewardPoints,
                notificationCount = notificationCount,
                menuItems         = defaultDrawerMenuItems,
                onClose           = { drawerState.close() },
                onMenuItemClick   = onMenuItemClick,
                onCallHotline     = onCallHotline
            )
        }
    }
}

// ── Drawer content ────────────────────────────────────────────────
@Composable
private fun DrawerContent(
    userName: String,
    rewardPoints: Int,
    notificationCount: Int,
    menuItems: List<DrawerMenuItem>,
    onClose: () -> Unit,
    onMenuItemClick: (DrawerMenuItem) -> Unit,
    onCallHotline: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {

        // ── Header: Logo + Close ──────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Logo
            Column {
                Text("VẬT TƯ Y TẾ", fontSize = 10.sp, color = GreenTop,
                    fontWeight = FontWeight.Medium, letterSpacing = 1.sp)
                Text("MedStore", fontSize = 18.sp, color = GreenTop,
                    fontWeight = FontWeight.ExtraBold, letterSpacing = 0.5.sp)
            }
            IconButton(onClick = onClose) {
                Icon(Icons.Filled.Close, contentDescription = "Đóng", tint = Color.Gray)
            }
        }

        // ── User info banner ──────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(androidx.compose.ui.graphics.Brush.horizontalGradient(listOf(GreenTop, GreenLight)))
                .padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Avatar
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.Person, null, tint = Color.White, modifier = Modifier.size(28.dp))
                }
                Spacer(Modifier.width(12.dp))
                Column {
                    Text("b $userName", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Spacer(Modifier.height(4.dp))
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = Color.White.copy(alpha = 0.2f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Filled.EmojiEvents, null, tint = GoldColor, modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("$rewardPoints điểm thưởng", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }

        // ── Menu items ────────────────────────────────────────────
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            menuItems.forEach { item ->
                DrawerMenuRow(
                    item = item,
                    onClick = { onMenuItemClick(item) }
                )
                HorizontalDivider(color = Color(0xFFF0F0F0), thickness = 0.8.dp)
            }
        }

        // ── Footer: Hotline + Version ─────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                shape = RoundedCornerShape(50),
                color = Color(0xFFEEF2FF),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onCallHotline() }
                        .padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Outlined.Phone, null, tint = GreenTop, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Hotline hỗ trợ: 1800 1234", color = GreenTop,
                        fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                }
            }
            Spacer(Modifier.height(8.dp))
            Text("Phiên bản: 4.3.9", color = Color.Gray, fontSize = 12.sp)
        }
    }
}

// ── Single menu row (collapsible) ────────────────────────────────
@Composable
private fun DrawerMenuRow(
    item: DrawerMenuItem,
    onClick: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val hasChildren = item.children.isNotEmpty()

    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    if (hasChildren) expanded = !expanded
                    else onClick()
                }
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(item.label, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF1A1A1A))
                if (item.badge > 0) {
                    Spacer(Modifier.width(8.dp))
                    Badge(containerColor = GoldColor) {
                        Text(item.badge.toString(), color = Color.White, fontSize = 10.sp)
                    }
                }
            }
            if (hasChildren) {
                val rotation by animateFloatAsState(if (expanded) 180f else 0f, tween(200))
                Icon(
                    imageVector = Icons.Outlined.KeyboardArrowDown,
                    contentDescription = null,
                    tint = Color.Gray,
                    modifier = Modifier
                        .size(20.dp)
                        .graphicsLayer { rotationZ = rotation }
                )
            }
        }

        // Sub-items
        if (expanded && hasChildren) {
            item.children.forEach { child ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onClick() }
                        .padding(start = 36.dp, end = 20.dp, top = 10.dp, bottom = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(5.dp)
                            .clip(CircleShape)
                            .background(GreenTop)
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(child, fontSize = 13.sp, color = Color(0xFF555555))
                }
            }
            Spacer(Modifier.height(6.dp))
        }
    }
}