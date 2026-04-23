package org.example.project.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.example.project.presentation.viewmodels.DashboardUiState
import org.example.project.presentation.viewmodels.DashboardViewModel
import org.example.project.presentation.viewmodels.RecentOrderDto
import java.text.NumberFormat
import java.util.Locale


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(viewModel: DashboardViewModel) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tổng quan của hàng", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.primary
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues).padding(24.dp)) {
            when (val state = uiState) {
                is DashboardUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is DashboardUiState.Error -> {
                    Text("Lỗi: ${state.message}", color = MaterialTheme.colorScheme.error, modifier = Modifier.align(Alignment.Center))
                }
                is DashboardUiState.Success -> {
                    Column(modifier = Modifier.fillMaxSize()) {
                        // 1. Dòng thống kê (4 Thẻ)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            StatCard(modifier = Modifier.weight(1f), title = "Doanh thu", value = formatVND(state.data.totalRevenue), icon = Icons.Default.AttachMoney, iconTint = MaterialTheme.colorScheme.primary)
                            StatCard(modifier = Modifier.weight(1f), title = "Đơn hàng", value = state.data.totalOrders.toString(), icon = Icons.Default.ShoppingCart, iconTint = MaterialTheme.colorScheme.secondary)
                            StatCard(modifier = Modifier.weight(1f), title = "Sản phẩm", value = state.data.totalProducts.toString(), icon = Icons.Default.Inventory, iconTint = Color(0xFF2196F3))
                            StatCard(modifier = Modifier.weight(1f), title = "Khách hàng", value = state.data.totalCustomers.toString(), icon = Icons.Default.People, iconTint = Color(0xFFFFAB00))
                        }

                        Spacer(modifier = Modifier.height(32.dp))

                        // 2. Bảng Đơn Hàng Gần Đây
                        Text("Đơn hàng gần đây", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(16.dp))
                        RecentOrdersTable(orders = state.data.recentOrders)
                    }
                }
            }
        }
    }
}

@Composable
fun StatCard(modifier: Modifier = Modifier, title: String, value: String, icon: ImageVector, iconTint: Color) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(20.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(48.dp).background(iconTint.copy(alpha = 0.1f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(24.dp))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(title, color = Color.Gray, fontSize = 14.sp)
                Text(value, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            }
        }
    }
}

@Composable
fun RecentOrdersTable(orders: List<RecentOrderDto>) {
    Card(
        modifier = Modifier.fillMaxWidth().fillMaxHeight(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Header Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.primary)
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Mã đơn", modifier = Modifier.weight(1f), color = Color.White, fontWeight = FontWeight.Bold)
                Text("Khách Hàng", modifier = Modifier.weight(1.5f), color = Color.White, fontWeight = FontWeight.Bold)
                Text("Trạng Thái", modifier = Modifier.weight(1f), color = Color.White, fontWeight = FontWeight.Bold)
                Text("Tổng Tiền", modifier = Modifier.weight(1f), color = Color.White, fontWeight = FontWeight.Bold)
                Text("Ngày Đặt", modifier = Modifier.weight(1f), color = Color.White, fontWeight = FontWeight.Bold)
            }

            // Data Rows
            LazyColumn {
                items(orders) { order ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(order.orderCode, modifier = Modifier.weight(1f), fontWeight = FontWeight.Medium)
                        Text(order.customerName, modifier = Modifier.weight(1.5f))
                        
                        // Trạng thái đơn hàng có màu sắc
                        Box(modifier = Modifier.weight(1f)) {
                            OrderStatusBadge(order.status)
                        }
                        
                        Text(formatVND(order.total), modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        Text(order.createdAt, modifier = Modifier.weight(1f), color = Color.Gray, fontSize = 14.sp)
                    }
                    HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f))
                }
            }
        }
    }
}

@Composable
fun OrderStatusBadge(status: String) {
    val (bgColor, textColor, text) = when (status.uppercase()) {
        "PAID", "DELIVERED", "SUCCESS" -> Triple(Color(0xFFE8F5E9), Color(0xFF2E7D32), "Thành công") // Xanh lá
        "PENDING", "PROCESSING" -> Triple(Color(0xFFFFF8E1), Color(0xFFFFAB00), "Đang chờ") // Vàng
        "FAILED", "CANCELLED" -> Triple(Color(0xFFFFEBEE), Color(0xFFD32F2F), "Thất bại") // Đỏ
        else -> Triple(Color(0xFFF5F5F5), Color.Gray, status)
    }

    Surface(
        color = bgColor,
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            text = text,
            color = textColor,
            fontWeight = FontWeight.SemiBold,
            fontSize = 12.sp,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}