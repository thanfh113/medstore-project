package com.example.nhathuoc.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.outlined.LocalShipping
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.example.nhathuoc.ui.theme.BgColor
import com.example.nhathuoc.ui.theme.GreenTop
import com.example.nhathuoc.viewmodel.OrderViewModel
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

private data class OnlineOrderStatusFilter(
    val backendValue: String?,
    val label: String
)

private val orderStatusFilters = listOf(
    OnlineOrderStatusFilter(backendValue = null, label = "Tất cả"),
    OnlineOrderStatusFilter(backendValue = "PENDING", label = "Chờ xác nhận"),
    OnlineOrderStatusFilter(backendValue = "PROCESSING", label = "Đang xử lý"),
    OnlineOrderStatusFilter(backendValue = "SHIPPING", label = "Đang giao"),
    OnlineOrderStatusFilter(backendValue = "DELIVERED", label = "Đã giao"),
    OnlineOrderStatusFilter(backendValue = "CANCELLED", label = "Đã hủy"),
    OnlineOrderStatusFilter(backendValue = "RETURNED", label = "Hoàn trả")
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

    LaunchedEffect(selectedStatus) {
        viewModel.getOrders(status = selectedStatus)
    }

    Scaffold(
        modifier = modifier,
        containerColor = BgColor,
        topBar = {
            TopAppBar(
                title = { Text("Đơn hàng online", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Quay lại"
                        )
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
            OrderStatusFilterRow(
                selectedStatus = selectedStatus,
                onStatusSelected = { selectedStatus = it }
            )

            when (val state = ordersState) {
                is UiState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = GreenTop)
                    }
                }

                is UiState.Error -> {
                    EmptyState(
                        icon = Icons.Outlined.WarningAmber,
                        iconTint = Color(0xFFE57373),
                        title = "Không tải được danh sách đơn hàng",
                        subtitle = state.message
                    )
                }

                is UiState.Success<*> -> {
                    val response = state.data as OrderListResponse
                    val onlineOrders = response.orders.filter(::isOnlineOrder)

                    if (onlineOrders.isEmpty()) {
                        EmptyState(
                            icon = Icons.Outlined.ShoppingBag,
                            iconTint = Color(0xFFBDBDBD),
                            title = if (selectedStatus == null) {
                                "Bạn chưa có đơn hàng online nào"
                            } else {
                                "Không có đơn ở trạng thái này"
                            },
                            subtitle = "Khi bạn đặt hàng trên ứng dụng, đơn sẽ xuất hiện ở đây."
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(onlineOrders, key = { it.id }) { order ->
                                OrderCard(
                                    order = order,
                                    onClick = {
                                        navController?.navigate("OrderDetailScreen/${order.id}")
                                    },
                                    onResumePayment = {
                                        navController?.currentBackStackEntry?.savedStateHandle?.set("resumeOrderId", order.id)
                                        navController?.currentBackStackEntry?.savedStateHandle?.set(
                                            "resumePaymentMethod",
                                            order.paymentMethod.uppercase()
                                        )
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
private fun OrderStatusFilterRow(
    selectedStatus: String?,
    onStatusSelected: (String?) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        orderStatusFilters.forEach { filter ->
            val isSelected = filter.backendValue == selectedStatus
            Surface(
                color = if (isSelected) GreenTop else Color.White,
                contentColor = if (isSelected) Color.White else GreenTop,
                shape = RoundedCornerShape(999.dp),
                tonalElevation = if (isSelected) 0.dp else 1.dp,
                shadowElevation = if (isSelected) 0.dp else 1.dp,
                modifier = Modifier.clickable { onStatusSelected(filter.backendValue) }
            ) {
                Text(
                    text = filter.label,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                )
            }
        }
    }
}

@Composable
private fun EmptyState(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color,
    title: String,
    subtitle: String
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(56.dp)
            )
            Spacer(Modifier.height(16.dp))
            Text(
                text = title,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2D2D2D)
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = subtitle,
                fontSize = 13.sp,
                color = Color(0xFF777777)
            )
        }
    }
}

@Composable
private fun OrderCard(
    order: OrderDto,
    onClick: () -> Unit,
    onResumePayment: () -> Unit
) {
    val canResumePayment = canResumeGatewayPayment(order)
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = Color.White,
        shadowElevation = 3.dp,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Đơn #${order.orderCode}",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E1E1E)
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = formatOrderDate(order.createdAt),
                        fontSize = 12.sp,
                        color = Color(0xFF7A7A7A)
                    )
                }

                StatusBadge(status = order.status)
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                InfoPill(text = labelForPickupType(order.pickupType))
                PaymentBadge(method = order.paymentMethod, status = order.paymentStatus)
            }

            Surface(
                color = Color(0xFFF6F8FB),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Sản phẩm",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF666666)
                    )

                    order.items.take(2).forEach { item ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = item.name,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF2D2D2D),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "x${item.quantity} • ${item.unit}",
                                    fontSize = 11.sp,
                                    color = Color(0xFF888888)
                                )
                            }

                            Spacer(Modifier.width(12.dp))

                            Text(
                                text = formatCurrency(item.totalPrice ?: item.price),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = GreenTop
                            )
                        }
                    }

                    if (order.items.size > 2) {
                        Text(
                            text = "+ ${order.items.size - 2} sản phẩm khác",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = GreenTop
                        )
                    }
                }
            }

            if (canResumePayment) {
                OutlinedButton(
                    onClick = onResumePayment,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text(
                        text = "Tiếp tục thanh toán",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = GreenTop
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Tổng thanh toán",
                        fontSize = 12.sp,
                        color = Color(0xFF7A7A7A)
                    )
                    Text(
                        text = formatCurrency(order.total),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = GreenTop
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (order.status == "SHIPPING") {
                        Icon(
                            imageVector = Icons.Outlined.LocalShipping,
                            contentDescription = null,
                            tint = GreenTop,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                    }

                    Text(
                        text = "Xem chi tiết",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = GreenTop
                    )
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowRight,
                        contentDescription = null,
                        tint = GreenTop,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun StatusBadge(status: String) {
    val appearance = when (status) {
        "PENDING" -> BadgeAppearance("Chờ xác nhận", Color(0xFFFFF3E0), Color(0xFFEF6C00))
        "PROCESSING" -> BadgeAppearance("Đang xử lý", Color(0xFFE3F2FD), Color(0xFF1565C0))
        "SHIPPING" -> BadgeAppearance("Đang giao", Color(0xFFE0F2F1), Color(0xFF00796B))
        "DELIVERED" -> BadgeAppearance("Đã giao", Color(0xFFE8F5E9), Color(0xFF2E7D32))
        "CANCELLED" -> BadgeAppearance("Đã hủy", Color(0xFFFFEBEE), Color(0xFFC62828))
        "RETURNED" -> BadgeAppearance("Hoàn trả", Color(0xFFF3E5F5), Color(0xFF7B1FA2))
        else -> BadgeAppearance(status, Color(0xFFF1F3F4), Color(0xFF5F6368))
    }

    Surface(
        color = appearance.background,
        contentColor = appearance.content,
        shape = CircleShape
    ) {
        Text(
            text = appearance.label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
        )
    }
}

@Composable
private fun PaymentBadge(
    method: String,
    status: String
) {
    val label = buildString {
        append(labelForPaymentMethod(method))
        append(" • ")
        append(labelForPaymentStatus(status))
    }

    val (background, content) = when (status.uppercase()) {
        "COMPLETED" -> Color(0xFFE8F5E9) to GreenTop
        "PENDING" -> Color(0xFFFFF3E0) to Color(0xFFEF6C00)
        "FAILED", "REFUNDED" -> Color(0xFFFFEBEE) to Color(0xFFC62828)
        else -> Color(0xFFF1F3F4) to Color(0xFF5F6368)
    }

    InfoPill(
        text = label,
        background = background,
        content = content
    )
}

@Composable
private fun InfoPill(
    text: String,
    background: Color = Color(0xFFF1F6F1),
    content: Color = GreenTop
) {
    Surface(
        color = background,
        contentColor = content,
        shape = CircleShape
    ) {
        Text(
            text = text,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
        )
    }
}

private data class BadgeAppearance(
    val label: String,
    val background: Color,
    val content: Color
)

private fun isOnlineOrder(order: OrderDto): Boolean {
    return !order.orderCode.startsWith("POS-", ignoreCase = true)
}

private fun canResumeGatewayPayment(order: OrderDto): Boolean {
    val method = order.paymentMethod.uppercase()
    val paymentStatus = order.paymentStatus.uppercase()
    val orderStatus = order.status.uppercase()
    return isOnlineOrder(order) &&
        method in setOf("MOMO", "ZALOPAY") &&
        paymentStatus == "PENDING" &&
        orderStatus !in setOf("CANCELLED", "RETURNED", "DELIVERED")
}

private fun labelForPickupType(pickupType: String): String {
    return when (pickupType.uppercase()) {
        "PICKUP" -> "Nhận tại cửa hàng"
        else -> "Giao tận nơi"
    }
}

private fun labelForPaymentMethod(method: String): String {
    return when (method.uppercase()) {
        "COD" -> "COD"
        "MOMO" -> "MoMo"
        "VNPAY" -> "Thanh toán online"
        "ZALOPAY" -> "ZaloPay"
        else -> method
    }
}

private fun labelForPaymentStatus(status: String): String {
    return when (status.uppercase()) {
        "UNPAID" -> "Chưa thanh toán"
        "PENDING" -> "Đang xử lý"
        "COMPLETED" -> "Đã thanh toán"
        "FAILED" -> "Thất bại"
        "PARTIALLY_REFUNDED" -> "Hoàn tiền một phần"
        "REFUNDED" -> "Đã hoàn tiền"
        else -> status
    }
}

private fun formatOrderDate(dateString: String): String {
    return try {
        OffsetDateTime.parse(dateString).format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
    } catch (_: Exception) {
        try {
            LocalDateTime.parse(dateString).format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
        } catch (_: Exception) {
            dateString
        }
    }
}

private fun formatCurrency(amount: Double): String {
    return String.format("%,.0f đ", amount)
}
