package com.example.nhathuoc.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.nhathuoc.data.model.OrderDto
import com.example.nhathuoc.data.model.OrderListResponse
import com.example.nhathuoc.data.model.UiState
import com.example.nhathuoc.viewmodel.OrderViewModel
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

private data class OnlineOrderStatusFilter(val backendValue: String?, val label: String)

private val orderStatusFilters = listOf(
    OnlineOrderStatusFilter(null, "Tất cả"),
    OnlineOrderStatusFilter("PENDING", "Chờ xác nhận"),
    OnlineOrderStatusFilter("PROCESSING", "Đang xử lý"),
    OnlineOrderStatusFilter("SHIPPING", "Đang giao"),
    OnlineOrderStatusFilter("DELIVERED", "Đã giao"),
    OnlineOrderStatusFilter("CANCELLED", "Đã hủy"),
    OnlineOrderStatusFilter("RETURNED", "Hoàn trả")
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyOrdersScreen(
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {},
    navController: NavController? = null
) {
    val viewModel: OrderViewModel = hiltViewModel()
    val ordersState by viewModel.ordersListState.collectAsState()
    var selectedStatus by rememberSaveable { mutableStateOf<String?>(null) }

    LaunchedEffect(selectedStatus) { viewModel.getOrders(status = selectedStatus) }

    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            com.example.nhathuoc.ui.component.GreenAppTopBar(
                title = "Đơn hàng của tôi",
                subtitle = "Theo dõi tất cả đơn hàng",
                onBack = onBack
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Filter chips
            Surface(color = MaterialTheme.colorScheme.surface, shadowElevation = 1.dp) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    orderStatusFilters.forEach { filter ->
                        val isSelected = filter.backendValue == selectedStatus
                        Surface(
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                            contentColor = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                            shape = RoundedCornerShape(999.dp),
                            shadowElevation = if (isSelected) 0.dp else 1.dp,
                            modifier = Modifier.clickable { selectedStatus = filter.backendValue }
                        ) {
                            Text(
                                text = filter.label,
                                fontSize = 13.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                            )
                        }
                    }
                }
            }

            when (val state = ordersState) {
                is UiState.Loading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                }

                is UiState.Error -> {
                    EmptyState(
                        icon = Icons.Outlined.WarningAmber,
                        iconBg = Color(0xFFFFEBEE),
                        iconTint = Color(0xFFE57373),
                        title = "Không tải được đơn hàng",
                        subtitle = state.message
                    )
                }

                is UiState.Success<*> -> {
                    val response = state.data as OrderListResponse
                    val orders = response.orders.filter { !it.orderCode.startsWith("POS-", ignoreCase = true) }

                    if (orders.isEmpty()) {
                        EmptyState(
                            icon = Icons.Outlined.ShoppingBag,
                            iconBg = Color(0xFFE8F5E9),
                            iconTint = MaterialTheme.colorScheme.primary,
                            title = if (selectedStatus == null) "Chưa có đơn hàng nào" else "Không có đơn ở trạng thái này",
                            subtitle = "Khi bạn đặt hàng trên ứng dụng, đơn sẽ xuất hiện ở đây."
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(orders, key = { it.id }) { order ->
                                OrderCard(
                                    order = order,
                                    onClick = { navController?.navigate("OrderDetailScreen/${order.id}") },
                                    onResumePayment = {
                                        navController?.currentBackStackEntry?.savedStateHandle?.set("resumeOrderId", order.id)
                                        navController?.currentBackStackEntry?.savedStateHandle?.set("resumePaymentMethod", order.paymentMethod.uppercase())
                                        navController?.navigate("CheckoutScreen")
                                    }
                                )
                            }
                        }
                    }
                }

                else -> Unit
            }
        }
    }
}

@Composable
private fun EmptyState(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconBg: Color,
    iconTint: Color,
    title: String,
    subtitle: String
) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(24.dp)
        ) {
            Box(
                modifier = Modifier.size(80.dp).clip(CircleShape).background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(40.dp))
            }
            Text(title, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            Text(subtitle, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun OrderCard(
    order: OrderDto,
    onClick: () -> Unit,
    onResumePayment: () -> Unit
) {
    val canResume = canResumeGatewayPayment(order)
    val (statusLabel, statusBg, statusFg) = statusAppearance(order.status)

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 2.dp,
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            // — Top row: order code + status badge —
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "Đơn #${order.orderCode}",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        formatOrderDate(order.createdAt),
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = statusBg
                ) {
                    Text(
                        statusLabel,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = statusFg,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }
            }

            Spacer(Modifier.height(10.dp))

            // — Pill row: pickup type + payment —
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                InfoPill(labelForPickupType(order.pickupType))
                PaymentPill(method = order.paymentMethod, status = order.paymentStatus)
            }

            Spacer(Modifier.height(12.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(Modifier.height(12.dp))

            // — Product list —
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                order.items.take(2).forEach { item ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary)
                            )
                            Text(
                                item.name,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        Spacer(Modifier.width(8.dp))
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                formatCurrency(item.totalPrice ?: item.price),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                "×${item.quantity} ${item.unit}",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                if (order.items.size > 2) {
                    Text(
                        "+ ${order.items.size - 2} sản phẩm khác",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(Modifier.height(12.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(Modifier.height(12.dp))

            // — Bottom row: total + continue payment / view detail —
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Tổng tiền", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        formatCurrency(order.total),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                if (canResume) {
                    Button(
                        onClick = onResumePayment,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                        elevation = ButtonDefaults.buttonElevation(0.dp)
                    ) {
                        Text("Tiếp tục thanh toán", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (order.status == "SHIPPING") {
                            Icon(
                                Icons.Outlined.LocalShipping,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(4.dp))
                        }
                        Text(
                            "Xem chi tiết",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Icon(
                            Icons.AutoMirrored.Outlined.KeyboardArrowRight,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun InfoPill(
    text: String,
    bg: Color = MaterialTheme.colorScheme.surfaceVariant,
    fg: Color = MaterialTheme.colorScheme.onSurfaceVariant
) {
    Surface(color = bg, shape = RoundedCornerShape(999.dp)) {
        Text(
            text,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = fg,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
        )
    }
}

@Composable
private fun PaymentPill(method: String, status: String) {
    val label = "${labelForPaymentMethod(method)} • ${labelForPaymentStatus(status)}"
    val (bg, fg) = when (status.uppercase()) {
        "COMPLETED" -> Color(0xFFE8F5E9) to MaterialTheme.colorScheme.primary
        "PENDING" -> Color(0xFFFFF3E0) to Color(0xFFEF6C00)
        "FAILED", "REFUNDED" -> Color(0xFFFFEBEE) to Color(0xFFC62828)
        else -> Color(0xFFF1F3F4) to Color(0xFF5F6368)
    }
    InfoPill(label, bg, fg)
}

private fun statusAppearance(status: String): Triple<String, Color, Color> = when (status.uppercase()) {
    "PENDING" -> Triple("Chờ xác nhận", Color(0xFFFFF3E0), Color(0xFFEF6C00))
    "PROCESSING" -> Triple("Đang xử lý", Color(0xFFE3F2FD), Color(0xFF1565C0))
    "SHIPPING" -> Triple("Đang giao", Color(0xFFE0F2F1), Color(0xFF00796B))
    "DELIVERED" -> Triple("Đã giao", Color(0xFFE8F5E9), Color(0xFF2E7D32))
    "CANCELLED" -> Triple("Đã hủy", Color(0xFFFFEBEE), Color(0xFFC62828))
    "RETURNED" -> Triple("Hoàn trả", Color(0xFFF3E5F5), Color(0xFF7B1FA2))
    else -> Triple(status, Color(0xFFF1F3F4), Color(0xFF5F6368))
}

private fun canResumeGatewayPayment(order: OrderDto): Boolean {
    val method = order.paymentMethod.uppercase()
    val ps = order.paymentStatus.uppercase()
    val os = order.status.uppercase()
    return !order.orderCode.startsWith("POS-", ignoreCase = true) &&
            method in setOf("MOMO", "ZALOPAY") &&
            ps == "PENDING" &&
            os !in setOf("CANCELLED", "RETURNED", "DELIVERED")
}

private fun labelForPickupType(v: String) = if (v.uppercase() == "PICKUP") "Nhận tại cửa hàng" else "Giao tận nơi"
private fun labelForPaymentMethod(v: String) = when (v.uppercase()) {
    "COD" -> "COD"; "MOMO" -> "MoMo"; "VNPAY" -> "Online"; "ZALOPAY" -> "ZaloPay"; else -> v
}
private fun labelForPaymentStatus(v: String) = when (v.uppercase()) {
    "UNPAID" -> "Chưa TT"; "PENDING" -> "Đang xử lý"; "COMPLETED" -> "Đã TT"
    "FAILED" -> "Thất bại"; "REFUNDED" -> "Hoàn tiền"; else -> v
}

private fun formatOrderDate(dateString: String): String = try {
    OffsetDateTime.parse(dateString).format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
} catch (_: Exception) {
    try { LocalDateTime.parse(dateString).format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) }
    catch (_: Exception) { dateString }
}

private fun formatCurrency(amount: Double) = String.format("%,.0f đ", amount)
