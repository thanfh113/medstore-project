package org.example.project.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Refresh
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
import org.example.project.presentation.viewmodels.DashboardUiState
import org.example.project.presentation.viewmodels.DashboardViewModel
import org.example.project.presentation.viewmodels.RecentOrderDto
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardOverviewScreen(viewModel: DashboardViewModel) {
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
                        Text(
                            state.message,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            "Kiểm tra backend đang chạy ở http://localhost:8080 rồi bấm tải lại.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 13.sp
                        )
                    }
                }

                is DashboardUiState.Success -> {
                    Column(modifier = Modifier.fillMaxSize()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            DashboardStatCard(
                                modifier = Modifier.weight(1f),
                                title = "Doanh thu",
                                value = formatDashboardVnd(state.data.totalRevenue),
                                icon = Icons.Default.AttachMoney,
                                iconTint = MaterialTheme.colorScheme.primary
                            )
                            DashboardStatCard(
                                modifier = Modifier.weight(1f),
                                title = "Đơn hàng",
                                value = state.data.totalOrders.toString(),
                                icon = Icons.Default.ShoppingCart,
                                iconTint = MaterialTheme.colorScheme.secondary
                            )
                            DashboardStatCard(
                                modifier = Modifier.weight(1f),
                                title = "Sản phẩm",
                                value = state.data.totalProducts.toString(),
                                icon = Icons.Default.Inventory,
                                iconTint = Color(0xFF1E88E5)
                            )
                            DashboardStatCard(
                                modifier = Modifier.weight(1f),
                                title = "Khách hàng",
                                value = state.data.totalCustomers.toString(),
                                icon = Icons.Default.People,
                                iconTint = Color(0xFFFFA000)
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        Text(
                            "Đơn hàng gần đây",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        RecentOrdersOverviewTable(orders = state.data.recentOrders)
                    }
                }
            }
        }
    }
}

@Composable
private fun DashboardStatCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    icon: ImageVector,
    iconTint: Color
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(iconTint.copy(alpha = 0.12f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(24.dp))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(title, color = Color.Gray, fontSize = 14.sp)
                Text(
                    value,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
private fun RecentOrdersOverviewTable(orders: List<RecentOrderDto>) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
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
                Text("Trạng thái", modifier = Modifier.weight(1f), color = Color.White, fontWeight = FontWeight.Bold)
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
                LazyColumn {
                    items(orders) { order ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(order.orderCode, modifier = Modifier.weight(1f), fontWeight = FontWeight.Medium)
                            Text(order.customerName, modifier = Modifier.weight(1.4f))
                            Box(modifier = Modifier.weight(1f)) {
                                DashboardOrderStatusBadge(order.status)
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
                        HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f))
                    }
                }
            }
        }
    }
}

@Composable
private fun DashboardOrderStatusBadge(status: String) {
    val (bgColor, textColor, text) = when (status.uppercase()) {
        "PAID", "DELIVERED", "SUCCESS", "COMPLETED" ->
            Triple(Color(0xFFE8F5E9), Color(0xFF2E7D32), "Thành công")
        "PENDING", "PROCESSING", "SHIPPING" ->
            Triple(Color(0xFFFFF3E0), Color(0xFFEF6C00), "Đang xử lý")
        "FAILED", "CANCELLED", "RETURNED" ->
            Triple(Color(0xFFFFEBEE), Color(0xFFD32F2F), "Không thành công")
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

private fun formatDashboardVnd(amount: Double): String {
    return NumberFormat.getNumberInstance(Locale("vi", "VN")).format(amount) + " đ"
}
