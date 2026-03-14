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
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.nhathuoc.ui.theme.NhathuocTheme
import com.example.nhathuoc.ui.theme.GreenTop
import com.example.nhathuoc.ui.theme.GreenLight
import com.example.nhathuoc.ui.theme.GoldColor
import com.example.nhathuoc.ui.theme.BgColor
import com.example.nhathuoc.data.local.SessionManager
import com.example.nhathuoc.util.AuthenticatedAction
import kotlinx.coroutines.launch

// ── Data ──────────────────────────────────────────────────────────
data class MenuItem(
    val icon: ImageVector,
    val label: String
)

private val orderStatuses = listOf(
    MenuItem(Icons.Outlined.Inventory2,       "Đang xử lý"),
    MenuItem(Icons.Outlined.LocalShipping,    "Đang giao"),
    MenuItem(Icons.Outlined.CheckCircle,      "Đã giao"),
    MenuItem(Icons.Outlined.SwapHoriz,        "Đổi/Trả"),
)

private val accountMenuItems = listOf(
    MenuItem(Icons.Outlined.QrCode2,          "Mã QR của tôi"),
    MenuItem(Icons.Outlined.AccountCircle,    "Thông tin cá nhân"),
    MenuItem(Icons.Outlined.LocationOn,       "Quản lý sổ địa chỉ"),
    MenuItem(Icons.Outlined.CreditCard,       "Quản lý phương thức thanh toán"),
    MenuItem(Icons.Outlined.MedicalServices,  "Đơn thuốc của tôi"),
)

private val aboutMenuItems = listOf(
    MenuItem(Icons.Outlined.HelpOutline,          "Giới thiệu nhà thuốc"),
    MenuItem(Icons.Outlined.VerifiedUser,          "Giấy phép kinh doanh"),
    MenuItem(Icons.Outlined.Article,               "Quy chế hoạt động"),
    MenuItem(Icons.Outlined.LocalShipping,         "Chính sách đặt cọc"),
    MenuItem(Icons.Outlined.Edit,                  "Chính sách nội dung"),
    MenuItem(Icons.Outlined.Autorenew,             "Chính sách đổi trả thuốc"),
    MenuItem(Icons.Outlined.Vaccines,              "Chính sách hoàn hủy đổi trả Vắc xin"),
    MenuItem(Icons.Outlined.DeliveryDining,        "Chính sách giao hàng"),
    MenuItem(Icons.Outlined.Shield,                "Chính sách bảo mật"),
    MenuItem(Icons.Outlined.AccountBalanceWallet,  "Chính sách thanh toán"),
    MenuItem(Icons.Outlined.AdminPanelSettings,    "Chính sách bảo mật dữ liệu cá nhân"),
    MenuItem(Icons.Outlined.Stars,                 "Thông tin trung tâm bảo hành máy thiết bị y tế từng hãng"),
    MenuItem(Icons.Outlined.CardGiftcard,          "Thể lệ chương trình \"Tích điểm nhận đặc quyền\""),
    MenuItem(Icons.Outlined.Gavel,                 "Điều khoản sử dụng Long Châu 247"),
    MenuItem(Icons.Outlined.HeadsetMic,            "Liên hệ & Hỗ trợ"),
)

// ── Screen ────────────────────────────────────────────────────────
@Composable
fun AccountScreen(
    modifier: Modifier = Modifier,
    navController: NavController = rememberNavController()
) {
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }
    val isLoggedIn = sessionManager.isLoggedIn.collectAsState(initial = false)
    val userName = sessionManager.userFullName.collectAsState(initial = null)
    val userPhone = sessionManager.userPhone.collectAsState(initial = null)

    if (!isLoggedIn.value) {
        // Show login prompt when not authenticated
        LoginPromptScreen(navController = navController)
    } else {
        // Show account content when authenticated
        AuthenticatedAccountContent(
            modifier = modifier,
            navController = navController,
            userName = userName.value ?: "User",
            userPhone = userPhone.value ?: ""
        )
    }
}

// ── Login Prompt Screen ──────────────────────────────────────────
@Composable
private fun LoginPromptScreen(navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgColor)
    ) {
        // Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.horizontalGradient(listOf(GreenTop, GreenLight)))
                .statusBarsPadding()
                .height(120.dp)
        ) {
            Column(
                modifier = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    Icons.Filled.Person,
                    null,
                    tint = Color.White,
                    modifier = Modifier.size(48.dp)
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "Tài khoản",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
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
            // Icon
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Outlined.AccountCircle,
                    null,
                    tint = GreenTop,
                    modifier = Modifier.size(50.dp)
                )
            }

            Spacer(Modifier.height(24.dp))

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

            // Login Button
            Button(
                onClick = { navController.navigate("LoginScreen") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = GreenTop),
                shape = RoundedCornerShape(24.dp)
            ) {
                Text(
                    "Đăng nhập",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(Modifier.height(12.dp))

            // Register Button
            OutlinedButton(
                onClick = { navController.navigate("RegisterScreen") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, GreenTop),
                shape = RoundedCornerShape(24.dp)
            ) {
                Text(
                    "Đăng ký tài khoản",
                    color = GreenTop,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

// ── Authenticated Account Content ────────────────────────────────
@Composable
private fun AuthenticatedAccountContent(
    modifier: Modifier = Modifier,
    navController: NavController,
    userName: String,
    userPhone: String
) {
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }

    Column(modifier = modifier.fillMaxSize()) {

        // ── Sticky header xanh ────────────────────────────────────
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
                    // Avatar
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.25f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Filled.Person, null, tint = Color.White, modifier = Modifier.size(30.dp))
                    }
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(userName, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Text(userPhone, color = Color.White.copy(alpha = 0.85f), fontSize = 13.sp)
                    }
                }
                // Points badge
                Surface(
                    shape = RoundedCornerShape(50),
                    color = Color.White.copy(alpha = 0.15f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Filled.EmojiEvents, null, tint = GoldColor, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("246", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // ── Scrollable body ───────────────────────────────────────
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .background(BgColor)
        ) {

            // ── Đơn của tôi ───────────────────────────────────────
            Spacer(Modifier.height(12.dp))
            SectionLabel(
                title = "Đơn của tôi",
                action = "Xem tất cả",
                onActionClick = {
                    navController.navigate("MyOrdersScreen")
                }
            )
            Spacer(Modifier.height(6.dp))
            Surface(
                color = Color.White,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .padding(horizontal = 12.dp)
                    .fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    orderStatuses.forEach { item ->
                        OrderStatusItem(item)
                    }
                }
            }

            // ── Cài đặt giao diện ─────────────────────────────────
            Spacer(Modifier.height(16.dp))
            Row(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Cài đặt giao diện", fontSize = 13.sp, color = Color.Gray, fontWeight = FontWeight.Medium)
                Spacer(Modifier.width(8.dp))
                Surface(
                    shape = RoundedCornerShape(50),
                    color = Color(0xFFE53935)
                ) {
                    Text(
                        "MỚI",
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                    )
                }
            }
            Spacer(Modifier.height(6.dp))
            Surface(
                color = Color.White,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .padding(horizontal = 12.dp)
                    .fillMaxWidth()
            ) {
                MenuRow(
                    icon = Icons.Outlined.PhoneAndroid,
                    label = "Chuyển sang bản cá nhân hóa",
                    showDivider = false,
                    onClick = { /* TODO: Implement */ }
                )
            }

            // ── Tài khoản ─────────────────────────────────────────
            Spacer(Modifier.height(16.dp))
            SectionLabel(title = "Tài khoản")
            Spacer(Modifier.height(6.dp))
            MenuGroup(
                items = accountMenuItems,
                navController = navController
            )

            // ── Về Nhà thuốc FPT Long Châu ────────────────────────
            Spacer(Modifier.height(16.dp))
            SectionLabel(title = "Về Nhà thuốc Hà Tiến Thành")
            Spacer(Modifier.height(6.dp))
            MenuGroup(
                items = aboutMenuItems,
                navController = navController
            )

            // ── Đăng xuất ─────────────────────────────────────────
            Spacer(Modifier.height(20.dp))
            Surface(
                color = Color.White,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .padding(horizontal = 12.dp)
                    .fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            // Perform logout
                            kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
                                sessionManager.clearSession()
                            }
                        }
                        .padding(vertical = 16.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Outlined.Logout, null, tint = Color(0xFF444444), modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Đăng xuất", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF1A1A1A))
                }
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

// ── Sub-composables ───────────────────────────────────────────────
@Composable
private fun SectionLabel(title: String, action: String? = null, onActionClick: (() -> Unit)? = null) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
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
private fun OrderStatusItem(item: MenuItem) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable { }
            .padding(horizontal = 8.dp)
    ) {
        Icon(item.icon, null, tint = GreenTop, modifier = Modifier.size(30.dp))
        Spacer(Modifier.height(6.dp))
        Text(item.label, fontSize = 11.sp, color = Color(0xFF333333), fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun MenuGroup(items: List<MenuItem>, navController: NavController) {
    Surface(
        color = Color.White,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .padding(horizontal = 12.dp)
            .fillMaxWidth()
    ) {
        Column {
            items.forEachIndexed { i, item ->
                MenuRow(
                    icon = item.icon,
                    label = item.label,
                    showDivider = i < items.lastIndex,
                    onClick = {
                        // Handle navigation based on menu item
                        when (item.label) {
                            "Thông tin cá nhân" -> {
                                // Navigate to profile edit screen
                                // navController.navigate("ProfileScreen")
                            }
                            "Quản lý sổ địa chỉ" -> {
                                // Navigate to address management
                                // navController.navigate("AddressScreen")
                            }
                            "Đơn thuốc của tôi" -> {
                                // Navigate to prescription screen
                                navController.navigate("MyOrdersScreen")
                            }
                            // Add other navigation cases as needed
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
            Icon(icon, null, tint = GreenTop, modifier = Modifier.size(22.dp))
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
                modifier = Modifier.padding(start = 52.dp, end = 16.dp),
                color = Color(0xFFF0F0F0),
                thickness = 0.8.dp
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun AccountScreenPreview() {
    NhathuocTheme {
        AccountScreen(navController = rememberNavController())
    }
}