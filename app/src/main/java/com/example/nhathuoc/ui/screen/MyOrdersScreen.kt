package com.example.nhathuoc.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.nhathuoc.data.model.OrderDto
import com.example.nhathuoc.data.model.OrderListResponse
import com.example.nhathuoc.data.model.UiState
import com.example.nhathuoc.ui.theme.GreenTop
import com.example.nhathuoc.viewmodel.OrderViewModel
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

private val BgColor = Color(0xFFF5F7FA)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyOrdersScreen(
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {},
    navController: NavController? = null
) {
    val viewModel: OrderViewModel = viewModel()
    val ordersState by viewModel.ordersListState.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    // Load orders when screen appears
    LaunchedEffect(Unit) {
        viewModel.getOrders()
    }

    Scaffold(
        modifier = modifier,
        containerColor = BgColor,
        topBar = {
            TopAppBar(
                title = { Text("Đơn hàng của tôi", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = Color(0xFF1A1A1A)
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (ordersState) {
                is UiState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = GreenTop)
                    }
                }
                is UiState.Error -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Outlined.WarningAmber,
                                null,
                                tint = Color.Red,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(Modifier.height(12.dp))
                            Text("Lỗi tải đơn hàng", fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(8.dp))
                            Button(onClick = { viewModel.getOrders() }) {
                                Text("Thử lại")
                            }
                        }
                    }
                }
                is UiState.Success<*> -> {
                    val response = (ordersState as? UiState.Success<*>)?.let {
                        it.data as? OrderListResponse
                    }
                    val orders = response?.orders ?: emptyList()

                    if (orders.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    Icons.Outlined.StoreMallDirectory,
                                    null,
                                    tint = Color(0xFFCCCCCC),
                                    modifier = Modifier.size(64.dp)
                                )
                                Spacer(Modifier.height(16.dp))
                                Text(
                                    "Chưa có đơn hàng nào",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF333333)
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    "Hãy mua sắm những sản phẩm y tế chất lượng",
                                    fontSize = 13.sp,
                                    color = Color.Gray
                                )
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(12.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(orders) { order ->
                                OrderCard(
                                    order = order,
                                    onClick = {
                                        navController?.navigate("OrderDetailScreen/${order.id}")
                                    }
                                )
                            }
                        }
                    }
                }
                else -> {}
            }
        }
    }
}

@Composable
private fun OrderCard(
    order: OrderDto,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color.White,
        shadowElevation = 2.dp,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Order header: Code + Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Đơn hàng #${order.orderCode}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1A1A1A)
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = formatOrderDate(order.createdAt),
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }

                // Status badge
                StatusBadge(status = order.status)
            }

            // Order details: items count + total
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF5F7FA), RoundedCornerShape(8.dp))
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "${order.items.size} sản phẩm",
                        fontSize = 13.sp,
                        color = Color.Gray
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = formatCurrency(order.subtotal),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF333333)
                    )
                }

                // Payment method badge
                PaymentMethodBadge(method = order.paymentMethod, status = order.paymentStatus)
            }

            // Items preview (max 2 items show)
            if (order.items.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    order.items.take(2).forEach { item ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = item.name,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF333333),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "x${item.quantity}",
                                    fontSize = 11.sp,
                                    color = Color.Gray
                                )
                            }
                            Text(
                                text = formatCurrency(item.totalPrice ?: (item.price * 1.0)),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = GreenTop
                            )
                        }
                    }
                    if (order.items.size > 2) {
                        Text(
                            text = "+ ${order.items.size - 2} sản phẩm khác",
                            fontSize = 11.sp,
                            color = GreenTop,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            // Total price + Action button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Tổng cộng",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                    Text(
                        text = formatCurrency(order.total),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = GreenTop
                    )
                }

                // Action button based on status
                when (order.status) {
                    "DELIVERED" -> {
                        Button(
                            onClick = { /* Reorder logic */ },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFE8F5E9)
                            )
                        ) {
                            Text("Mua lại", color = GreenTop, fontWeight = FontWeight.SemiBold)
                        }
                    }
                    "CANCELLED" -> {
                        Text(
                            "Đã hủy",
                            fontSize = 12.sp,
                            color = Color.Gray,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    else -> {
                        Icon(
                            Icons.Outlined.KeyboardArrowRight,
                            null,
                            tint = Color.Gray,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StatusBadge(status: String) {
    val (bgColor, textColor, label) = when (status) {
        "PENDING" -> Triple(Color(0xFFFFF3E0), Color(0xFFEF6C00), "Chờ xác nhận")
        "CONFIRMED" -> Triple(Color(0xFFE3F2FD), Color(0xFF1565C0), "Đã xác nhận")
        "PREPARING" -> Triple(Color(0xFFE8F5E9), Color(0xFF2E7D32), "Đang chuẩn bị")
        "SHIPPING" -> Triple(Color(0xFFE0F2F1), Color(0xFF00796B), "Đang giao")
        "DELIVERED" -> Triple(Color(0xFFE8F5E9), Color(0xFF2E7D32), "Đã giao")
        "CANCELLED" -> Triple(Color(0xFFFFEBEE), Color(0xFFE53935), "Đã hủy")
        else -> Triple(Color(0xFFF5F5F5), Color(0xFF666666), status)
    }

    Surface(
        shape = RoundedCornerShape(6.dp),
        color = bgColor
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = textColor,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
private fun PaymentMethodBadge(method: String, status: String) {
    val displayText = when {
        status == "UNPAID" -> "Chưa thanh toán"
        status == "PAID" -> when (method) {
            "COD" -> "Thanh toán khi nhận"
            "VNPAY" -> "VNPay"
            "MOMO" -> "MoMo"
            else -> "Đã thanh toán"
        }
        status == "REFUNDED" -> "Đã hoàn tiền"
        else -> status
    }

    val bgColor = when {
        status == "UNPAID" -> Color(0xFFFFF3E0)
        status == "PAID" -> Color(0xFFE8F5E9)
        else -> Color(0xFFF5F5F5)
    }

    val textColor = when {
        status == "UNPAID" -> Color(0xFFEF6C00)
        status == "PAID" -> GreenTop
        else -> Color(0xFF666666)
    }

    Surface(
        shape = RoundedCornerShape(6.dp),
        color = bgColor
    ) {
        Text(
            text = displayText,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = textColor,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

private fun formatOrderDate(dateString: String): String {
    return try {
        val formatter = DateTimeFormatter.ISO_DATE_TIME
        val dateTime = LocalDateTime.parse(dateString.replace("Z", "+00:00"))
        dateTime.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
    } catch (e: Exception) {
        dateString
    }
}

private fun formatCurrency(amount: Double): String {
    return String.format("₫%.0f", amount)
}
