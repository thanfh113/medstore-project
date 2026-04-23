package org.example.project.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.example.project.presentation.viewmodels.DashboardPeriodStatsDto
import org.example.project.presentation.viewmodels.DashboardUiState
import org.example.project.presentation.viewmodels.DashboardViewModel
import org.example.project.presentation.viewmodels.RecentOrderDto
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StoreOverviewScreen(viewModel: DashboardViewModel) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.fetchDashboardData()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tổng quan cửa hàng", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = viewModel::fetchDashboardData) {
                        Icon(Icons.Default.Refresh, contentDescription = "Tải lại tổng quan")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.primary
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp)
        ) {
            when (val state = uiState) {
                is DashboardUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }

                is DashboardUiState.Error -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "Không tải được dữ liệu tổng quan.",
                            color = MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(state.message, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            "Kiểm tra backend đang chạy ở http://localhost:8080 rồi bấm tải lại.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 13.sp
                        )
                    }
                }

                is DashboardUiState.Success -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            SummaryCard(
                                modifier = Modifier.weight(1f),
                                title = "Doanh thu lũy kế",
                                value = formatDashboardVnd(state.data.totalRevenue),
                                icon = Icons.Default.AttachMoney,
                                iconTint = MaterialTheme.colorScheme.primary
                            )
                            SummaryCard(
                                modifier = Modifier.weight(1f),
                                title = "Tổng đơn",
                                value = state.data.totalOrders.toString(),
                                icon = Icons.Default.ShoppingCart,
                                iconTint = MaterialTheme.colorScheme.secondary
                            )
                            SummaryCard(
                                modifier = Modifier.weight(1f),
                                title = "Đơn chờ xử lý",
                                value = state.data.pendingOrders.toString(),
                                icon = Icons.Default.Schedule,
                                iconTint = Color(0xFFEF6C00)
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            SummaryCard(
                                modifier = Modifier.weight(1f),
                                title = "Sản phẩm đang bán",
                                value = state.data.totalProducts.toString(),
                                icon = Icons.Default.Inventory,
                                iconTint = Color(0xFF1565C0)
                            )
                            SummaryCard(
                                modifier = Modifier.weight(1f),
                                title = "Khách hàng",
                                value = state.data.totalCustomers.toString(),
                                icon = Icons.Default.People,
                                iconTint = Color(0xFF8E24AA)
                            )
                            InfoPanel(
                                modifier = Modifier.weight(1f),
                                title = "Điểm cần theo dõi",
                                lines = listOf(
                                    "So sánh doanh thu POS và Online để phát hiện kênh đang tăng trưởng.",
                                    "Ưu tiên xử lý các đơn còn chờ để giảm tồn đọng vận hành."
                                )
                            )
                        }

                        Text(
                            "Hiệu suất theo thời gian",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            PeriodCard(
                                modifier = Modifier.weight(1f),
                                title = "Hôm nay",
                                subtitle = "Doanh thu và đơn phát sinh trong ngày",
                                stats = state.data.today
                            )
                            PeriodCard(
                                modifier = Modifier.weight(1f),
                                title = "Tháng này",
                                subtitle = "Tổng hợp từ ngày đầu tháng đến hiện tại",
                                stats = state.data.month
                            )
                        }

                        Text(
                            "Đơn hàng gần đây",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )

                        RecentOrdersOverviewTable(orders = state.data.recentOrders)
                    }
                }
            }
        }
    }
}

@Composable
private fun SummaryCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    icon: ImageVector,
    iconTint: Color
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .background(iconTint.copy(alpha = 0.12f), RoundedCornerShape(14.dp))
                    .padding(12.dp)
            ) {
                Icon(icon, contentDescription = null, tint = iconTint)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(title, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
                Text(
                    value,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun InfoPanel(
    modifier: Modifier = Modifier,
    title: String,
    lines: List<String>
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF4FAF2)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(title, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            lines.forEach { line ->
                Text(line, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
            }
        }
    }
}

@Composable
private fun PeriodCard(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String,
    stats: DashboardPeriodStatsDto
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MetricTile(
                    modifier = Modifier.weight(1f),
                    title = "Doanh thu",
                    value = formatDashboardVnd(stats.revenue)
                )
                MetricTile(
                    modifier = Modifier.weight(1f),
                    title = "Đơn mới",
                    value = stats.orderCount.toString()
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MetricTile(
                    modifier = Modifier.weight(1f),
                    title = "Hoàn thành",
                    value = stats.completedOrderCount.toString()
                )
                MetricTile(
                    modifier = Modifier.weight(1f),
                    title = "Đang chờ",
                    value = stats.pendingOrderCount.toString()
                )
            }

            Text("Tách theo kênh", fontWeight = FontWeight.SemiBold)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ChannelBreakdownCard(
                    modifier = Modifier.weight(1f),
                    title = "POS",
                    revenue = stats.posRevenue,
                    orderCount = stats.posOrderCount,
                    completedOrderCount = stats.posCompletedOrderCount,
                    backgroundColor = Color(0xFFE8F5E9),
                    accentColor = Color(0xFF2E7D32)
                )
                ChannelBreakdownCard(
                    modifier = Modifier.weight(1f),
                    title = "Online",
                    revenue = stats.onlineRevenue,
                    orderCount = stats.onlineOrderCount,
                    completedOrderCount = stats.onlineCompletedOrderCount,
                    backgroundColor = Color(0xFFE3F2FD),
                    accentColor = Color(0xFF1565C0)
                )
            }
        }
    }
}

@Composable
private fun MetricTile(
    modifier: Modifier = Modifier,
    title: String,
    value: String
) {
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Text(title, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(value, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
        }
    }
}

@Composable
private fun ChannelBreakdownCard(
    modifier: Modifier = Modifier,
    title: String,
    revenue: Double,
    orderCount: Int,
    completedOrderCount: Int,
    backgroundColor: Color,
    accentColor: Color
) {
    Surface(
        modifier = modifier,
        color = backgroundColor,
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(title, color = accentColor, fontWeight = FontWeight.Bold)
            Text(formatDashboardVnd(revenue), color = accentColor, fontWeight = FontWeight.Bold)
            Text("Đơn tạo mới: $orderCount", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
            Text("Đơn hoàn thành: $completedOrderCount", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
        }
    }
}

@Composable
private fun RecentOrdersOverviewTable(orders: List<RecentOrderDto>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.primary)
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Mã đơn", modifier = Modifier.weight(1f), color = Color.White, fontWeight = FontWeight.Bold)
                Text("Khách hàng", modifier = Modifier.weight(1.4f), color = Color.White, fontWeight = FontWeight.Bold)
                Text("Kênh", modifier = Modifier.weight(0.9f), color = Color.White, fontWeight = FontWeight.Bold)
                Text("Trạng thái", modifier = Modifier.weight(1.1f), color = Color.White, fontWeight = FontWeight.Bold)
                Text("Tổng tiền", modifier = Modifier.weight(1f), color = Color.White, fontWeight = FontWeight.Bold)
                Text("Ngày tạo", modifier = Modifier.weight(1.2f), color = Color.White, fontWeight = FontWeight.Bold)
            }

            if (orders.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Chưa có đơn hàng gần đây")
                }
            } else {
                Column {
                    orders.forEachIndexed { index, order ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(order.orderCode, modifier = Modifier.weight(1f), fontWeight = FontWeight.Medium)
                            Text(order.customerName, modifier = Modifier.weight(1.4f))
                            Box(modifier = Modifier.weight(0.9f)) {
                                DashboardChannelBadge(order.orderChannel)
                            }
                            Box(modifier = Modifier.weight(1.1f)) {
                                DashboardOrderStatusBadge(order.status, order.paymentStatus)
                            }
                            Text(
                                formatDashboardVnd(order.total),
                                modifier = Modifier.weight(1f),
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                order.createdAt.replace('T', ' ').take(16),
                                modifier = Modifier.weight(1.2f),
                                color = Color.Gray,
                                fontSize = 14.sp
                            )
                        }
                        if (index < orders.lastIndex) {
                            HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DashboardChannelBadge(channel: String) {
    val isPos = channel.uppercase() == "POS"
    val background = if (isPos) Color(0xFFE8F5E9) else Color(0xFFE3F2FD)
    val textColor = if (isPos) Color(0xFF2E7D32) else Color(0xFF1565C0)
    val label = if (isPos) "POS" else "Online"

    Surface(color = background, shape = RoundedCornerShape(8.dp)) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            color = textColor,
            fontWeight = FontWeight.SemiBold,
            fontSize = 12.sp
        )
    }
}

@Composable
private fun DashboardOrderStatusBadge(status: String, paymentStatus: String) {
    val normalizedStatus = status.uppercase()
    val normalizedPayment = paymentStatus.uppercase()
    val (bgColor, textColor, text) = when {
        normalizedStatus == "DELIVERED" || normalizedPayment == "COMPLETED" ->
            Triple(Color(0xFFE8F5E9), Color(0xFF2E7D32), "Hoàn thành")
        normalizedStatus == "PENDING" || normalizedStatus == "PROCESSING" || normalizedStatus == "SHIPPING" ->
            Triple(Color(0xFFFFF3E0), Color(0xFFEF6C00), "Đang xử lý")
        normalizedStatus == "CANCELLED" || normalizedStatus == "RETURNED" ->
            Triple(Color(0xFFFFEBEE), Color(0xFFD32F2F), "Đã hủy")
        else -> Triple(Color(0xFFF5F5F5), Color.Gray, status)
    }

    Surface(color = bgColor, shape = RoundedCornerShape(8.dp)) {
        Text(
            text = text,
            color = textColor,
            fontWeight = FontWeight.SemiBold,
            fontSize = 12.sp,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}

private fun formatDashboardVnd(amount: Double): String {
    return NumberFormat.getNumberInstance(Locale.forLanguageTag("vi-VN")).format(amount) + " đ"
}
