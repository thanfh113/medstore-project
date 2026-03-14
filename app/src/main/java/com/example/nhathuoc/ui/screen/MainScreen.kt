package com.example.nhathuoc.ui.screen

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.nhathuoc.ui.theme.NhathuocTheme
import com.example.nhathuoc.ui.theme.GreenTop
import com.example.nhathuoc.ui.theme.GreenMedium
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

sealed class BottomNavTab(
    val index: Int,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    object Home    : BottomNavTab(0, "Trang chủ",   Icons.Filled.Home,         Icons.Outlined.Home)
    object Reward  : BottomNavTab(1, "Điểm thưởng", Icons.Filled.Star,         Icons.Outlined.Star)
    object Consult : BottomNavTab(2, "Tư vấn",      Icons.Filled.ChatBubble,   Icons.Outlined.ChatBubble)
    object Cart    : BottomNavTab(3, "Giỏ hàng",    Icons.Filled.ShoppingCart, Icons.Outlined.ShoppingCart)
    object Account : BottomNavTab(4, "Tài khoản",   Icons.Filled.Person,       Icons.Outlined.Person)
}

val bottomNavTabs = listOf(
    BottomNavTab.Home,
    BottomNavTab.Reward,
    BottomNavTab.Consult,
    BottomNavTab.Cart,
    BottomNavTab.Account
)

// Green theme colors
val PrimaryGreen = GreenTop
val ActiveGreen = GreenMedium

private val FAB_SIZE      = 56.dp
private val FAB_OVERLAP   = 22.dp
private val BAR_HEIGHT    = 64.dp
private val NOTCH_RADIUS  = 38.dp
private val CORNER_RADIUS = 16.dp

// ─────────────────────────────────────────────────────────────────
@Composable
fun MainScreen(navController: NavController) {
    var selectedTab       by remember { mutableIntStateOf(0) }
    var showConsultSheet  by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            bottomBar = {
                MainBottomBar(
                    selectedTab       = selectedTab,
                    onTabSelected     = { idx ->
                        if (idx == BottomNavTab.Consult.index) {
                            showConsultSheet = true   // mở sheet, KHÔNG đổi tab
                        } else {
                            selectedTab = idx
                        }
                    },
                    cartBadgeCount    = 1,
                    consultBadgeCount = 1
                )
            },
            containerColor = Color(0xFFF5F5F5)
        ) { innerPadding ->
            Box(Modifier.fillMaxSize().padding(innerPadding)) {
                when (selectedTab) {
                    0 -> HomeScreen(navController = navController)
                    1 -> RewardScreen(onShopNow = { selectedTab = 0 })
                    3 -> CartScreen(navController = navController)
                    4 -> AccountScreen(navController = navController)
                }
            }
        }

        // ── Consult bottom sheet overlay ──────────────────────────
        ConsultBottomSheet(
            visible   = showConsultSheet,
            onDismiss = { showConsultSheet = false },
            onChatClick = {
                showConsultSheet = false
                navController.navigate("ChatScreen")
            }
        )
    }
}

// ── Consult Bottom Sheet ──────────────────────────────────────────
@Composable
fun ConsultBottomSheet(
    visible: Boolean,
    onDismiss: () -> Unit,
    onChatClick: () -> Unit = {}
) {
    val sheetHeightDp = 480.dp
    val density       = LocalDensity.current
    val sheetHeightPx = with(density) { sheetHeightDp.toPx() }

    // offsetY: 0 = fully visible, sheetHeightPx = hidden below screen
    val offsetY = remember { Animatable(sheetHeightPx) }
    val scrimAlpha = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()

    // Trigger animation on visible change
    LaunchedEffect(visible) {
        if (visible) {
            launch { offsetY.animateTo(0f, tween(320, easing = FastOutSlowInEasing)) }
            launch { scrimAlpha.animateTo(0.45f, tween(280)) }
        } else {
            launch { offsetY.animateTo(sheetHeightPx, tween(260, easing = FastOutLinearInEasing)) }
            launch { scrimAlpha.animateTo(0f, tween(240)) }
        }
    }

    if (offsetY.value < sheetHeightPx || visible) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Scrim
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = scrimAlpha.value))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        scope.launch {
                            launch { offsetY.animateTo(sheetHeightPx, tween(260, easing = FastOutLinearInEasing)) }
                            launch { scrimAlpha.animateTo(0f, tween(240)) }
                        }
                        onDismiss()
                    }
            )

            // Sheet panel
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(sheetHeightDp)
                    .align(Alignment.BottomCenter)
                    .offset { IntOffset(0, offsetY.value.roundToInt()) },
                shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
                color = Color.White,
                shadowElevation = 16.dp
            ) {
                ConsultSheetContent(
                    onClose = {
                        scope.launch {
                            launch { offsetY.animateTo(sheetHeightPx, tween(260, easing = FastOutLinearInEasing)) }
                            launch { scrimAlpha.animateTo(0f, tween(240)) }
                        }
                        onDismiss()
                    },
                    onChatClick = onChatClick
                )
            }
        }
    }
}

@Composable
private fun ConsultSheetContent(onClose: () -> Unit, onChatClick: () -> Unit = {}) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Handle bar
        Box(
            modifier = Modifier
                .padding(top = 10.dp)
                .width(36.dp)
                .height(4.dp)
                .background(Color(0xFFDDDDDD), RoundedCornerShape(50))
        )

        // Title row
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Text(
                "Tư vấn với Dược sĩ",
                modifier = Modifier.align(Alignment.Center),
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A1A1A)
            )
            IconButton(
                onClick = onClose,
                modifier = Modifier.align(Alignment.CenterEnd).size(36.dp)
            ) {
                Icon(Icons.Filled.Close, "Đóng", tint = Color(0xFF555555))
            }
        }

        // Illustration area
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 32.dp),
            contentAlignment = Alignment.Center
        ) {
            // Background blob
            Box(
                modifier = Modifier
                    .size(220.dp)
                    .background(
                        Brush.radialGradient(listOf(Color(0xFFE3F2FD), Color(0xFFF0F7FF), Color.White)),
                        CircleShape
                    )
            )
            // Pharmacist icon composition
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .background(Color(0xFFBBDEFB), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Filled.Person,
                        null,
                        tint = ActiveGreen,
                        modifier = Modifier.size(60.dp)
                    )
                }
                Spacer(Modifier.height(8.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left: medicine shelves icon
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .background(Color(0xFFE3F2FD), RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Outlined.LocalPharmacy, null, tint = ActiveGreen, modifier = Modifier.size(30.dp))
                    }
                    // Center: headset
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(Color(0xFFBBDEFB), RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Outlined.HeadsetMic, null, tint = ActiveGreen, modifier = Modifier.size(28.dp))
                    }
                    // Right: clipboard
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .background(Color(0xFFE3F2FD), RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Outlined.Assignment, null, tint = ActiveGreen, modifier = Modifier.size(30.dp))
                    }
                }
            }
        }

        // Instruction text
        Text(
            "Vui lòng chọn hình thức tư vấn",
            fontSize = 15.sp,
            color = Color(0xFF444444)
        )
        Row {
            Text("(Hoàn toàn miễn phí)", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A1A1A))
        }

        Spacer(Modifier.height(20.dp))

        // Nhắn tin button
        Surface(
            onClick = onChatClick,
            shape = RoundedCornerShape(50),
            color = Color(0xFFEEF4FF),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .height(52.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Filled.ChatBubble, null, tint = ActiveGreen, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text("Nhắn tin", color = ActiveGreen, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.width(8.dp))
                Badge(containerColor = Color(0xFFFFAB00)) {
                    Text("1", color = Color.White, fontSize = 10.sp)
                }
            }
        }

        Spacer(Modifier.height(10.dp))

        // Gọi tổng đài button
        Surface(
            onClick = {},
            shape = RoundedCornerShape(50),
            color = Color(0xFFEEF4FF),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .height(52.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Filled.Phone, null, tint = ActiveGreen, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text("Gọi tổng đài (1800 6928)", color = ActiveGreen, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

// ── Bottom bar (unchanged) ────────────────────────────────────────
@Composable
fun MainBottomBar(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    cartBadgeCount: Int = 0,
    consultBadgeCount: Int = 0
) {
    val density       = LocalDensity.current
    val notchRadiusPx = with(density) { NOTCH_RADIUS.toPx() }
    val cornerPx      = with(density) { CORNER_RADIUS.toPx() }

    Column(modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(FAB_OVERLAP + BAR_HEIGHT)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(BAR_HEIGHT)
                    .align(Alignment.BottomCenter)
                    .drawBehind {
                        val w  = size.width
                        val h  = size.height
                        val cx = w / 2f
                        drawIntoCanvas { canvas ->
                            val p = android.graphics.Paint().apply {
                                isAntiAlias = true
                                color = android.graphics.Color.TRANSPARENT
                                setShadowLayer(20f, 0f, -6f, android.graphics.Color.argb(35, 0, 0, 0))
                            }
                            canvas.nativeCanvas.drawPath(
                                buildNotchPath(w, h, cx, notchRadiusPx, cornerPx).asAndroidPath(), p
                            )
                        }
                        drawPath(buildNotchPath(w, h, cx, notchRadiusPx, cornerPx), Color.White)
                    }
            ) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    bottomNavTabs.forEach { tab ->
                        if (tab == BottomNavTab.Consult) {
                            Spacer(modifier = Modifier.width(FAB_SIZE))
                        } else {
                            val isSelected = selectedTab == tab.index
                            val badge      = if (tab == BottomNavTab.Cart) cartBadgeCount else 0
                            NavIcon(tab = tab, isSelected = isSelected, badgeCount = badge, onClick = { onTabSelected(tab.index) })
                        }
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .height(20.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                bottomNavTabs.forEach { tab ->
                    val isSelected = selectedTab == tab.index
                    Box(modifier = Modifier.width(64.dp), contentAlignment = Alignment.Center) {
                        Text(
                            text       = tab.label,
                            fontSize   = 10.sp,
                            color      = if (isSelected) ActiveGreen else Color.Gray,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            textAlign  = TextAlign.Center,
                            maxLines   = 1
                        )
                    }
                }
            }

            Column(
                modifier = Modifier.align(Alignment.TopCenter),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                BadgedBox(badge = {
                    if (consultBadgeCount > 0) {
                        Badge(containerColor = Color.Red) {
                            Text(consultBadgeCount.toString(), color = Color.White, fontSize = 10.sp)
                        }
                    }
                }) {
                    FloatingActionButton(
                        onClick        = { onTabSelected(BottomNavTab.Consult.index) },
                        shape          = CircleShape,
                        containerColor = ActiveGreen,
                        contentColor   = Color.White,
                        elevation      = FloatingActionButtonDefaults.elevation(8.dp, 10.dp),
                        modifier       = Modifier.size(FAB_SIZE)
                    ) {
                        Icon(Icons.Outlined.ChatBubble, "Tư vấn", modifier = Modifier.size(26.dp))
                    }
                }
            }
        }

        Spacer(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsBottomHeight(WindowInsets.navigationBars)
                .background(Color.White)
        )
    }
}

@Composable
private fun NavIcon(tab: BottomNavTab, isSelected: Boolean, badgeCount: Int = 0, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .width(64.dp)
            .fillMaxHeight()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication        = null,
                onClick           = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        BadgedBox(badge = {
            if (badgeCount > 0) {
                Badge(containerColor = Color.Red) { Text(badgeCount.toString(), color = Color.White, fontSize = 10.sp) }
            }
        }) {
            Icon(
                imageVector        = if (isSelected) tab.selectedIcon else tab.unselectedIcon,
                contentDescription = tab.label,
                tint               = if (isSelected) ActiveGreen else Color.Gray,
                modifier           = Modifier.size(24.dp)
            )
        }
    }
}

private fun buildNotchPath(width: Float, height: Float, centerX: Float, notchRadius: Float, cornerRadius: Float): Path =
    Path().apply {
        moveTo(0f, cornerRadius)
        quadraticBezierTo(0f, 0f, cornerRadius, 0f)
        val spread = notchRadius * 1.15f
        lineTo(centerX - spread, 0f)
        cubicTo(centerX - notchRadius * 0.55f, 0f, centerX - notchRadius, notchRadius * 0.9f, centerX, notchRadius * 0.9f)
        cubicTo(centerX + notchRadius, notchRadius * 0.9f, centerX + notchRadius * 0.55f, 0f, centerX + spread, 0f)
        lineTo(width - cornerRadius, 0f)
        quadraticBezierTo(width, 0f, width, cornerRadius)
        lineTo(width, height)
        lineTo(0f, height)
        close()
    }

