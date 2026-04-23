@file:Suppress("DEPRECATION")
package com.example.nhathuoc.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.nhathuoc.data.local.SessionManager
import com.example.nhathuoc.data.model.UiState
import com.example.nhathuoc.ui.theme.BgColor
import com.example.nhathuoc.ui.theme.GoldColor
import com.example.nhathuoc.ui.theme.GreenLight
import com.example.nhathuoc.ui.theme.GreenTop
import com.example.nhathuoc.ui.theme.NhathuocTheme
import com.example.nhathuoc.viewmodel.AuthViewModel
import com.example.nhathuoc.viewmodel.RewardViewModel
import kotlinx.coroutines.launch

// ── Data ───────────────────────────────────────────────────────────────────
data class MenuItem(
    val icon: ImageVector,
    val label: String
)

private val orderStatuses = listOf(
    MenuItem(Icons.Outlined.Inventory2,    "Đang xử lý"),
    MenuItem(Icons.Outlined.LocalShipping, "Đang giao"),
    MenuItem(Icons.Outlined.CheckCircle,   "Đã giao"),
    MenuItem(Icons.Outlined.SwapHoriz,     "Đổi/Trả"),
)

private val accountMenuItems = listOf(
    MenuItem(Icons.Outlined.QrCode2,         "Mã QR của tôi"),
    MenuItem(Icons.Outlined.AccountCircle,   "Thông tin cá nhân"),
    MenuItem(Icons.Outlined.LocationOn,      "Quản lý sổ địa chỉ"),
    MenuItem(Icons.Outlined.CreditCard,      "Phương thức thanh toán"),
    MenuItem(Icons.Outlined.MedicalServices, "Thiết bị y tế của tôi"),
)

private val aboutMenuItems = listOf(
    MenuItem(Icons.Outlined.HelpOutline,         "Giới thiệu"),
    MenuItem(Icons.Outlined.VerifiedUser,        "Giấy phép kinh doanh"),
    MenuItem(Icons.Outlined.Article,             "Quy chế hoạt động"),
    MenuItem(Icons.Outlined.LocalShipping,       "Chính sách đặt cọc"),
    MenuItem(Icons.Outlined.Edit,                "Chính sách nội dung"),
    MenuItem(Icons.Outlined.Autorenew,           "Chính sách đổi trả vật tư"),
    MenuItem(Icons.Outlined.MedicalServices,     "Chính sách hoàn hủy vật tư y tế"),
    MenuItem(Icons.Outlined.DeliveryDining,      "Chính sách giao hàng"),
    MenuItem(Icons.Outlined.Shield,              "Chính sách bảo mật"),
    MenuItem(Icons.Outlined.AccountBalanceWallet,"Chính sách thanh toán"),
    MenuItem(Icons.Outlined.AdminPanelSettings,  "Bảo mật dữ liệu cá nhân"),
    MenuItem(Icons.Outlined.Stars,               "Trung tâm bảo hành thiết bị y tế"),
    MenuItem(Icons.Outlined.CardGiftcard,        "Thể lệ chương trình tích điểm"),
    MenuItem(Icons.Outlined.HeadsetMic,          "Liên hệ & Hỗ trợ"),
)

// ── Screen ─────────────────────────────────────────────────────────────────
@Composable
fun AccountScreen(
    modifier: Modifier = Modifier,
    navController: NavController = rememberNavController(),
    mainNavController: NavController? = null
) {
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }
    val isLoggedIn = sessionManager.isLoggedIn.collectAsState(initial = false)
    val userName   = sessionManager.userFullName.collectAsState(initial = null)
    val userPhone  = sessionManager.userPhone.collectAsState(initial = null)
    val userEmail  = sessionManager.userEmail.collectAsState(initial = null)

    val activeNavController = mainNavController ?: navController

    if (!isLoggedIn.value) {
        LoginPromptScreen(navController = activeNavController)
    } else {
        AuthenticatedAccountContent(
            modifier         = modifier,
            navController    = activeNavController,
            userName         = userName.value ?: "Người dùng",
            userPhone        = userPhone.value ?: "",
            userEmail        = userEmail.value ?: ""
        )
    }
}

// ── Login Prompt ────────────────────────────────────────────────────────────
@Composable
private fun LoginPromptScreen(navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgColor)
    ) {
        // Header gradient
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.horizontalGradient(listOf(GreenTop, GreenLight)))
                .statusBarsPadding()
                .height(140.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.25f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.Person, null, tint = Color.White, modifier = Modifier.size(40.dp))
                }
                Spacer(Modifier.height(8.dp))
                Text("Tài khoản", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }
        }

        // Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Đăng nhập để trải nghiệm đầy đủ",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A1A1A),
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "Quản lý đơn hàng, theo dõi điểm thưởng\nvà nhiều tiện ích khác",
                fontSize = 14.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp
            )
            Spacer(Modifier.height(32.dp))

            Button(
                onClick = { navController.navigate("LoginScreen") },
                modifier = Modifier.fillMaxWidth().height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = GreenTop),
                shape = RoundedCornerShape(24.dp)
            ) {
                Text("Đăng nhập", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            }

            Spacer(Modifier.height(12.dp))

            OutlinedButton(
                onClick = { navController.navigate("RegisterScreen") },
                modifier = Modifier.fillMaxWidth().height(48.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, GreenTop),
                shape = RoundedCornerShape(24.dp)
            ) {
                Text("Đăng ký tài khoản", color = GreenTop, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

// ── Authenticated Content ────────────────────────────────────────────────────
@Composable
private fun AuthenticatedAccountContent(
    modifier: Modifier = Modifier,
    navController: NavController,
    userName: String,
    userPhone: String,
    userEmail: String
) {
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }
    val scope = rememberCoroutineScope()

    // Reward data
    val rewardViewModel: RewardViewModel = hiltViewModel()
    val accountState by rewardViewModel.accountState.collectAsState()
    val rewardPoints = when (val s = accountState) {
        is UiState.Success -> s.data.availablePoints
        else               -> 0
    }

    LaunchedEffect(Unit) {
        rewardViewModel.loadRewardAccount()
    }

    Column(modifier = modifier.fillMaxSize()) {

        // ── Gradient header ──────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.horizontalGradient(listOf(GreenTop, GreenLight)))
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // ── Avatar initials ──────────────────────────────
                    AvatarInitials(name = userName, size = 52)
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(userName, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Text(
                            userPhone.ifBlank { userEmail }.ifBlank { "MedStore User" },
                            color = Color.White.copy(alpha = 0.85f),
                            fontSize = 12.sp
                        )
                    }
                }

                // ── Reward points badge ──────────────────────────────
                Surface(
                    shape = RoundedCornerShape(50),
                    color = Color.White.copy(alpha = 0.18f),
                    modifier = Modifier.clickable { }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Filled.EmojiEvents, null, tint = GoldColor, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(5.dp))
                        Text(
                            text = if (accountState is UiState.Loading) "..." else rewardPoints.toString(),
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.width(3.dp))
                        Text("điểm", color = Color.White.copy(alpha = 0.8f), fontSize = 11.sp)
                    }
                }
            }
        }

        // ── Scrollable body ──────────────────────────────────────────
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .background(BgColor)
        ) {

            // ── Đơn của tôi ─────────────────────────────────────────
            Spacer(Modifier.height(12.dp))
            SectionLabel(
                title = "Đơn của tôi",
                action = "Xem tất cả",
                onActionClick = { navController.navigate("MyOrdersScreen") }
            )
            Spacer(Modifier.height(6.dp))
            Surface(
                color = Color.White,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.padding(horizontal = 12.dp).fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    orderStatuses.forEach { item ->
                        OrderStatusItem(
                            item = item,
                            onClick = { navController.navigate("MyOrdersScreen") }
                        )
                    }
                }
            }

            // ── Tài khoản ────────────────────────────────────────────
            Spacer(Modifier.height(16.dp))
            SectionLabel(title = "Tài khoản")
            Spacer(Modifier.height(6.dp))
            MenuGroup(items = accountMenuItems, navController = navController)

            // ── Về MedStore ─────────────────────────────────────────
            Spacer(Modifier.height(16.dp))
            SectionLabel(title = "Về MedStore")
            Spacer(Modifier.height(6.dp))
            MenuGroup(items = aboutMenuItems, navController = navController)

            // ── Đăng xuất ────────────────────────────────────────────
            Spacer(Modifier.height(20.dp))
            Surface(
                color = Color.White,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.padding(horizontal = 12.dp).fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            scope.launch {
                                sessionManager.clearSession()
                            }
                        }
                        .padding(vertical = 16.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Outlined.Logout, null, tint = Color(0xFFE53935), modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Đăng xuất", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFFE53935))
                }
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

// ── Avatar Initials ──────────────────────────────────────────────────────────
@Composable
fun AvatarInitials(name: String, size: Int = 48) {
    val initials = name.trim().split(" ")
        .filter { it.isNotEmpty() }
        .take(2)
        .joinToString("") { it.first().uppercaseChar().toString() }
        .ifEmpty { "U" }

    val avatarBrush = Brush.linearGradient(
        listOf(Color(0xFF43A047), Color(0xFF1B5E20))
    )

    Box(
        modifier = Modifier
            .size(size.dp)
            .clip(CircleShape)
            .background(avatarBrush),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = initials,
            color = Color.White,
            fontSize = (size / 2.5).sp,
            fontWeight = FontWeight.Bold
        )
    }
}

// ── Sub-composables ─────────────────────────────────────────────────────────
@Composable
private fun SectionLabel(title: String, action: String? = null, onActionClick: (() -> Unit)? = null) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, fontSize = 13.sp, color = Color.Gray, fontWeight = FontWeight.Medium)
        if (action != null) {
            Text(
                action,
                fontSize = 13.sp,
                color = GreenTop,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.clickable { onActionClick?.invoke() }
            )
        }
    }
}

@Composable
private fun OrderStatusItem(item: MenuItem, onClick: () -> Unit = {}) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }.padding(horizontal = 8.dp)
    ) {
        Box(
            modifier = Modifier.size(44.dp).clip(CircleShape).background(Color(0xFFE8F5E9)),
            contentAlignment = Alignment.Center
        ) {
            Icon(item.icon, null, tint = GreenTop, modifier = Modifier.size(24.dp))
        }
        Spacer(Modifier.height(6.dp))
        Text(item.label, fontSize = 11.sp, color = Color(0xFF333333), fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun MenuGroup(items: List<MenuItem>, navController: NavController) {
    Surface(
        color = Color.White,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.padding(horizontal = 12.dp).fillMaxWidth()
    ) {
        Column {
            items.forEachIndexed { i, item ->
                MenuRow(
                    icon = item.icon,
                    label = item.label,
                    showDivider = i < items.lastIndex,
                    onClick = {
                        when (item.label) {
                            "Thông tin cá nhân"    -> { /* TODO: ProfileScreen */ }
                            "Quản lý sổ địa chỉ"  -> { /* TODO: AddressScreen */ }
                            "Đơn hàng của tôi"     -> navController.navigate("MyOrdersScreen")
                            "Liên hệ & Hỗ trợ"    -> { /* TODO: Support */ }
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun MenuRow(
    icon: ImageVector,
    label: String,
    showDivider: Boolean = true,
    onClick: () -> Unit = {}
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onClick() }
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(36.dp).clip(RoundedCornerShape(10.dp)).background(Color(0xFFE8F5E9)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = GreenTop, modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.width(14.dp))
            Text(
                label,
                fontSize = 14.sp,
                color = Color(0xFF1A1A1A),
                modifier = Modifier.weight(1f),
                lineHeight = 19.sp
            )
            Icon(Icons.Outlined.ChevronRight, null, tint = Color(0xFFBBBBBB), modifier = Modifier.size(20.dp))
        }
        if (showDivider) {
            HorizontalDivider(
                modifier = Modifier.padding(start = 66.dp, end = 16.dp),
                color = Color(0xFFF0F0F0),
                thickness = 0.8.dp
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun AccountScreenPreview() {
    NhathuocTheme { AccountScreen(navController = rememberNavController()) }
}