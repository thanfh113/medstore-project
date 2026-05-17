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
import kotlin.math.max
import kotlin.math.min

// Using GreenTop from theme
private val GreenTop = Color(0xFF2E7D32)
private val GoldColor = Color(0xFFFFAB00)
private val DrawerWidth = 300.dp

// -- Data ---------------------------------------------------------
data class DrawerMenuItem(
    val label: String,
    val categoryId: String? = null,
    val icon: ImageVector? = null,
    val children: List<DrawerMenuItem> = emptyList(),
    val badge: Int = 0
)

val defaultDrawerMenuItems = listOf(
    DrawerMenuItem("Thông báo"),
    DrawerMenuItem("Dụng cụ tiêm truyền", categoryId = "cat-supplies", children = listOf(
        DrawerMenuItem("Bơm tiêm - Ống xi lanh", categoryId = "cat-syringe"),
        DrawerMenuItem("Kim tiêm", categoryId = "cat-needle"),
        DrawerMenuItem("Dây truyền dịch", categoryId = "cat-infusion-set"),
        DrawerMenuItem("Ống thông", categoryId = "cat-tube"),
    )),
    DrawerMenuItem("Băng gạc - Cầm máu", categoryId = "cat-bandage", children = listOf(
        DrawerMenuItem("Gạc vô trùng", categoryId = "cat-sterile-gauze"),
        DrawerMenuItem("Băng dính y tế", categoryId = "cat-medical-tape"),
        DrawerMenuItem("Băng cuộn", categoryId = "cat-bandage-roll"),
        DrawerMenuItem("Băng keo thấm tẩm kháng sinh", categoryId = "cat-antimicrobial-dressing"),
    )),
    DrawerMenuItem("Thiết bị chẩn đoán", categoryId = "cat-device", children = listOf(
        DrawerMenuItem("Máy theo dõi - Máy thở", categoryId = "cat-monitor"),
        DrawerMenuItem("Máy đo huyết áp", categoryId = "cat-blood-pressure"),
        DrawerMenuItem("Nhiệt kế y tế", categoryId = "cat-thermometer"),
        DrawerMenuItem("Máy đo SpO2", categoryId = "cat-spo2"),
        DrawerMenuItem("Máy đo đường huyết", categoryId = "cat-glucose-meter"),
    )),
    DrawerMenuItem("Khẩu trang - PPE", categoryId = "cat-protect", children = listOf(
        DrawerMenuItem("Khẩu trang y tế", categoryId = "cat-mask"),
        DrawerMenuItem("Khẩu trang N95", categoryId = "cat-n95-mask"),
        DrawerMenuItem("Găng tay y tế", categoryId = "cat-gloves"),
        DrawerMenuItem("Quần áo bảo hộ", categoryId = "cat-protective-clothing"),
        DrawerMenuItem("Kính bảo hộ", categoryId = "cat-goggles"),
    )),
    DrawerMenuItem("Thiết bị phẫu thuật", categoryId = "cat-instrument", children = listOf(
        DrawerMenuItem("Dụng cụ vi phẫu", categoryId = "cat-surgical-tools"),
        DrawerMenuItem("Kẹp phẫu thuật", categoryId = "cat-forceps"),
        DrawerMenuItem("Dây khâu", categoryId = "cat-suture"),
        DrawerMenuItem("Van cầm máu", categoryId = "cat-hemostatic-valve"),
    )),
    DrawerMenuItem("Chống nhiễm khuẩn", categoryId = "cat-infection-control", children = listOf(
        DrawerMenuItem("Dung dịch sát khuẩn", categoryId = "cat-sanitizer"),
        DrawerMenuItem("Dung dịch khử khuẩn", categoryId = "cat-disinfectant"),
        DrawerMenuItem("Vật tư tiệt khuẩn", categoryId = "cat-sterilization"),
    )),
    DrawerMenuItem("Phục hồi chức năng", categoryId = "cat-therapy", children = listOf(
        DrawerMenuItem("Nạng - Xe lăn", categoryId = "cat-crutch-wheelchair"),
        DrawerMenuItem("Dụng cụ vật lý trị liệu", categoryId = "cat-physio-tools"),
        DrawerMenuItem("Nẹp chỉnh hình", categoryId = "cat-orthopedic-brace"),
    )),
    DrawerMenuItem("Vật tư xét nghiệm", categoryId = "cat-lab", children = listOf(
        DrawerMenuItem("Kit xét nghiệm", categoryId = "cat-test-kit"),
        DrawerMenuItem("Vật tư phòng xét nghiệm", categoryId = "cat-lab-consumables"),
        DrawerMenuItem("Dụng cụ lấy mẫu", categoryId = "cat-sample-container"),
    )),
)


// -- Drawer State -------------------------------------------------
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

// -- Main composable -----------------------------------------------
@Composable
fun AppDrawer(
    drawerState: DrawerState,
    userName: String = "Thành",
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
        // -- Main content ------------------------------------------
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

        // -- Scrim -------------------------------------------------
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

        // -- Drawer panel ------------------------------------------
        Box(
            modifier = Modifier
                .width(DrawerWidth)
                .fillMaxHeight()
                .graphicsLayer { this.translationX = translationX }
                .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(topEnd = 0.dp, bottomEnd = 0.dp))
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

// -- Drawer content ------------------------------------------------
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
    val effectiveMenuItems = remember(menuItems, notificationCount) {
        menuItems.map { item ->
            if (item.label == "Thông báo") item.copy(badge = notificationCount) else item
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {

        // -- Header: Logo + Close ----------------------------------
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Logo
            Column {
                Text("VẬT TƯ Y TẾ", fontSize = 10.sp, color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium, letterSpacing = 1.sp)
                Text("MedStore", fontSize = 18.sp, color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.ExtraBold, letterSpacing = 0.5.sp)
            }
            IconButton(onClick = onClose) {
                Icon(Icons.Filled.Close, contentDescription = "Đóng", tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        // -- User info banner --------------------------------------
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
                    Text("Chào $userName", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
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

        // -- Menu items --------------------------------------------
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            effectiveMenuItems.forEach { item ->
                DrawerMenuRow(
                    item = item,
                    onClick = onMenuItemClick
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), thickness = 0.8.dp)
            }
        }

        // -- Footer: Hotline + Version -----------------------------
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                shape = RoundedCornerShape(50),
                color = MaterialTheme.colorScheme.primaryContainer,
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
                    Icon(Icons.Outlined.Phone, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Hotline hỗ trợ: 1800 1234", color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                }
            }
            Spacer(Modifier.height(8.dp))
            Text("Phiên bản: 4.3.9", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
        }
    }
}

// -- Single menu row (collapsible) --------------------------------
@Composable
private fun DrawerMenuRow(
    item: DrawerMenuItem,
    onClick: (DrawerMenuItem) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val hasChildren = item.children.isNotEmpty()

    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    if (hasChildren) expanded = !expanded
                    else onClick(item)
                }
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(item.label, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
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
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .size(20.dp)
                        .graphicsLayer { rotationZ = rotation }
                )
            }
        }

        // Sub-items
        if (expanded && hasChildren) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onClick(item.copy(label = "Tất cả ${item.label}", children = emptyList())) }
                    .padding(start = 36.dp, end = 20.dp, top = 10.dp, bottom = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(5.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.tertiary)
                )
                Spacer(Modifier.width(10.dp))
                Text("Tất cả ${item.label}", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
            }
            item.children.forEach { child ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onClick(child) }
                        .padding(start = 36.dp, end = 20.dp, top = 10.dp, bottom = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(5.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary)
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(child.label, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Spacer(Modifier.height(6.dp))
        }
    }
}
