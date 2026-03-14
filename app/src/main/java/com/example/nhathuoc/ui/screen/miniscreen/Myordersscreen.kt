package com.example.nhathuoc.ui.screen.miniscreen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nhathuoc.ui.theme.NhathuocTheme
import com.example.nhathuoc.ui.theme.GreenLight
private val GreenTop = Color(0xFF2E7D32)


data class OrderItem(
    val id: Int,
    val date: String,
    val orderCode: String,
    val pickupType: String,
    val status: String,
    val statusColor: Color,
    val icon: ImageVector,
    val iconTint: Color,
    val iconBg: Color,
    val productName: String,
    val extraCount: Int = 0,
    val total: Int
)

private val sampleOrders = listOf(
    OrderItem(1, "28/10/2025", "#4497026", "Nhận tại cửa hàng", "Đã giao", Color(0xFF2E7D32),
        Icons.Outlined.MedicalServices, Color(0xFFC2185B), Color(0xFFFCE4EC),
        "Kem bôi da Acyclovir Stella Cream (5g) điều trị nhiễm virus Herpes simplex", 0, 17_000),
    OrderItem(2, "02/07/2025", "#3666920", "Nhận tại cửa hàng", "Đã giao", Color(0xFF2E7D32),
        Icons.Outlined.RemoveRedEye, Color(0xFF0277BD), Color(0xFFE1F5FE),
        "Thuốc nhỏ mắt V.Rohto Vitamin hỗ trợ cải thiện tình trạng giảm thị lực, mắt mờ (13ml)", 0, 51_990),
    OrderItem(3, "03/06/2025", "#7866368", "Nhận tại cửa hàng", "Đã giao", Color(0xFF2E7D32),
        Icons.Outlined.Air, Color(0xFF00838F), Color(0xFFE0F7FA),
        "Muối rửa mũi xoang Sinufresh Cát Linh (30 gói và 1 chai 180ml)", 1, 89_000),
    OrderItem(4, "15/05/2025", "#5521234", "Giao tận nơi", "Đã giao", Color(0xFF2E7D32),
        Icons.Outlined.Science, Color(0xFF6A1B9A), Color(0xFFF3E5F5),
        "Vitamin C 500mg tăng sức đề kháng hộp 100 viên", 0, 45_000),
    OrderItem(5, "10/04/2025", "#4412099", "Nhận tại cửa hàng", "Đã huỷ", Color(0xFFE53935),
        Icons.Outlined.LocalPharmacy, Color(0xFF1565C0), Color(0xFFE3F2FD),
        "Omega-3 hỗ trợ tim mạch DHA EPA 1000mg 100 viên", 0, 120_000),
)

private val statusTabs = listOf("Tất cả", "Đang xử lý", "Đang giao", "Đã giao", "Đã huỷ")

@Composable
fun MyOrdersScreen(
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {}
) {
    var selectedTab  by remember { mutableStateOf("Tất cả") }
    var searchQuery  by remember { mutableStateOf("") }

    val filtered = remember(selectedTab, searchQuery) {
        sampleOrders.filter { order ->
            (selectedTab == "Tất cả" || order.status == selectedTab) &&
                    (searchQuery.isEmpty() || order.productName.contains(searchQuery, ignoreCase = true)
                            || order.orderCode.contains(searchQuery))
        }
    }

    Column(modifier = modifier.fillMaxSize()) {
        // TopAppBar + search
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.horizontalGradient(listOf(GreenTop, GreenLight)))
                .statusBarsPadding()
        ) {
            Box(
                modifier = Modifier.fillMaxWidth().height(56.dp),
                contentAlignment = Alignment.Center
            ) {
                IconButton(onClick = onBack, modifier = Modifier.align(Alignment.CenterStart).padding(start = 4.dp)) {
                    Icon(Icons.Filled.ArrowBackIos, null, tint = Color.White, modifier = Modifier.size(20.dp))
                }
                Text("Đơn của tôi", color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
            }
            // Search bar
            Surface(
                shape = RoundedCornerShape(50),
                color = Color.White,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).padding(bottom = 12.dp).height(42.dp)
            ) {
                Row(modifier = Modifier.fillMaxSize().padding(horizontal = 14.dp),
                    verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.Search, null, tint = Color.Gray, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    androidx.compose.foundation.text.BasicTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        singleLine = true,
                        textStyle = androidx.compose.ui.text.TextStyle(fontSize = 13.sp, color = Color(0xFF1A1A1A)),
                        decorationBox = { inner ->
                            if (searchQuery.isEmpty()) Text("Tìm tên sản phẩm, tên đơn, mã...", color = Color.Gray, fontSize = 13.sp)
                            inner()
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Tabs
        Surface(color = Color.White, shadowElevation = 2.dp) {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(statusTabs) { tab ->
                    val isSelected = selectedTab == tab
                    TextButton(
                        onClick = { selectedTab = tab },
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(tab,
                            fontSize = 13.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) GreenTop else Color.Gray)
                    }
                    if (isSelected) {
                        // underline indicator
                    }
                }
            }
            HorizontalDivider(color = Color(0xFFEEEEEE))
        }

        // Orders
        LazyColumn(
            modifier = Modifier.weight(1f).background(Color(0xFFF5F7FA)),
            contentPadding = PaddingValues(vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(filtered, key = { it.id }) { order ->
                OrderCard(order = order)
            }
            if (filtered.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(top = 60.dp), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Outlined.Inventory2, null, tint = Color.LightGray, modifier = Modifier.size(64.dp))
                            Spacer(Modifier.height(12.dp))
                            Text("Không có đơn hàng nào", color = Color.Gray, fontSize = 14.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun OrderCard(order: OrderItem) {
    Surface(
        color = Color.White,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Đơn hàng ${order.date}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A1A1A))
                    Spacer(Modifier.width(6.dp))
                    Icon(Icons.Outlined.Edit, null, tint = GreenTop, modifier = Modifier.size(14.dp))
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(8.dp).background(order.statusColor, RoundedCornerShape(50)))
                    Spacer(Modifier.width(4.dp))
                    Text(order.status, fontSize = 12.sp, color = order.statusColor, fontWeight = FontWeight.SemiBold)
                }
            }
            Text("${order.pickupType} • ${order.orderCode}", fontSize = 11.sp, color = Color.Gray)
            Spacer(Modifier.height(10.dp))

            // Product row
            Row(verticalAlignment = Alignment.Top) {
                Box(
                    modifier = Modifier.size(64.dp)
                        .background(order.iconBg, RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(order.icon, null, tint = order.iconTint, modifier = Modifier.size(36.dp))
                }
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(order.productName, fontSize = 13.sp, color = Color(0xFF1A1A1A), lineHeight = 18.sp, maxLines = 2)
                    if (order.extraCount > 0) {
                        Text("+${order.extraCount} sản phẩm khác", fontSize = 12.sp, color = Color.Gray)
                    }
                }
            }

            Spacer(Modifier.height(10.dp))
            HorizontalDivider(color = Color(0xFFEEEEEE))
            Spacer(Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.clickable { },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Xem chi tiết", color = GreenTop, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    Icon(Icons.Outlined.ChevronRight, null, tint = GreenTop, modifier = Modifier.size(16.dp))
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Thành tiền: ", fontSize = 12.sp, color = Color.Gray)
                    Text("%,dđ".format(order.total).replace(",", "."),
                        fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = GreenTop)
                }
            }

            Spacer(Modifier.height(12.dp))
            Button(
                onClick = {},
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(containerColor = GreenTop),
                modifier = Modifier.fillMaxWidth().height(44.dp)
            ) {
                Text("Mua lại", fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun MyOrdersScreenPreview() { NhathuocTheme { MyOrdersScreen() } }