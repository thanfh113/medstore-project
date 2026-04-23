package com.example.nhathuoc.ui.screen.miniscreen

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import com.example.nhathuoc.data.model.OrderDto
import com.example.nhathuoc.data.model.OrderItemDto
import com.example.nhathuoc.data.model.UiState
import com.example.nhathuoc.data.model.UserAddress
import com.example.nhathuoc.ui.theme.BgColor
import com.example.nhathuoc.ui.theme.GreenTop
import com.example.nhathuoc.viewmodel.OrderViewModel
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderDetailScreen(
    orderId: String,
    onBack: () -> Unit,
    viewModel: OrderViewModel = hiltViewModel()
) {
    val orderState by viewModel.orderState.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    var showCancelDialog by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(orderId) {
        viewModel.getOrderById(orderId)
    }

    if (showCancelDialog) {
        AlertDialog(
            onDismissRequest = { showCancelDialog = false },
            title = { Text("Hủy đơn hàng") },
            text = { Text("Bạn có chắc muốn hủy đơn hàng này không?") },
            confirmButton = {
                Button(
                    onClick = {
                        showCancelDialog = false
                        viewModel.cancelOrder(orderId, "Người dùng hủy trên ứng dụng")
                    }
                ) {
                    Text("Xác nhận")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showCancelDialog = false }) {
                    Text("Đóng")
                }
            }
        )
    }

    Scaffold(
        containerColor = BgColor,
        topBar = {
            TopAppBar(
                title = { Text("Chi tiết đơn hàng", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Quay lại")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = Color(0xFF1A1A1A)
                )
            )
        }
    ) { innerPadding ->
        when (val state = orderState) {
            UiState.Idle, UiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = GreenTop)
                }
            }

            is UiState.Error -> {
                ErrorState(
                    message = state.message,
                    onRetry = { viewModel.getOrderById(orderId) },
                    modifier = Modifier.padding(innerPadding)
                )
            }

            is UiState.Success -> {
                val order = state.data
                OrderDetailContent(
                    order = order,
                    isBusy = isLoading,
                    onRetry = { viewModel.getOrderById(orderId) },
                    onCancelOrder = { showCancelDialog = true },
                    modifier = Modifier.padding(innerPadding)
                )
            }
        }
    }
}

@Composable
private fun OrderDetailContent(
    order: OrderDto,
    isBusy: Boolean,
    onRetry: () -> Unit,
    onCancelOrder: () -> Unit,
    modifier: Modifier = Modifier
) {
    val canCancel = order.status.uppercase() in setOf("PENDING", "PROCESSING")

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                HeaderCard(order = order)
            }
            item {
                ShippingCard(order = order)
            }
            if (order.shippingAddress != null) {
                item {
                    AddressCard(address = order.shippingAddress)
                }
            }
            item {
                ItemsCard(items = order.items)
            }
            item {
                PaymentCard(order = order)
            }
            item {
                SummaryCard(order = order)
            }
            if (!order.note.isNullOrBlank() || !order.cancelReason.isNullOrBlank()) {
                item {
                    NoteCard(order = order)
                }
            }
            item {
                ActionCard(
                    canCancel = canCancel,
                    onRetry = onRetry,
                    onCancelOrder = onCancelOrder
                )
            }
            item { Spacer(Modifier.height(16.dp)) }
        }

        if (isBusy) {
            Surface(
                color = Color.Black.copy(alpha = 0.08f),
                modifier = Modifier.fillMaxSize()
            ) {
                Box(contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = GreenTop)
                }
            }
        }
    }
}

@Composable
private fun HeaderCard(order: OrderDto) {
    val (statusLabel, statusColor) = orderStatusPresentation(order.status)
    val (paymentLabel, paymentColor) = paymentStatusPresentation(order.paymentStatus)
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = order.orderCode,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = GreenTop
                    )
                    Text(
                        text = "Đặt lúc ${formatDateTime(order.createdAt)}",
                        color = Color(0xFF6B7280),
                        fontSize = 13.sp
                    )
                }
                Column(
                    modifier = Modifier.padding(start = 12.dp),
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StatusChip(label = statusLabel, color = statusColor)
                    StatusChip(label = paymentLabel, color = paymentColor)
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AssistChip(label = pickupTypeLabel(order.pickupType), icon = if (order.pickupType.equals("PICKUP", true)) Icons.Filled.Storefront else Icons.Filled.LocalShipping)
                AssistChip(label = paymentMethodLabel(order.paymentMethod), icon = Icons.Filled.Payments)
            }
        }
    }
}

@Composable
private fun ShippingCard(order: OrderDto) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Vận chuyển và tiến độ", fontWeight = FontWeight.Bold, color = GreenTop, fontSize = 18.sp)
            DetailRow("Hình thức nhận hàng", pickupTypeLabel(order.pickupType))
            order.estimatedDelivery?.takeIf { it.isNotBlank() }?.let {
                DetailRow("Dự kiến giao", formatDateTime(it))
            }
            order.deliveredAt?.takeIf { it.isNotBlank() }?.let {
                DetailRow("Đã giao lúc", formatDateTime(it))
            }
            order.cancelledAt?.takeIf { it.isNotBlank() }?.let {
                DetailRow("Đã hủy lúc", formatDateTime(it))
            }
        }
    }
}

@Composable
private fun AddressCard(address: UserAddress) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Địa chỉ nhận hàng", fontWeight = FontWeight.Bold, color = GreenTop, fontSize = 18.sp)
            DetailRow("Người nhận", address.recipientName)
            DetailRow("Số điện thoại", address.recipientPhone, icon = Icons.Filled.Phone)
            DetailRow("Địa chỉ", address.fullAddress, singleLine = false)
        }
    }
}

@Composable
private fun ItemsCard(items: List<OrderItemDto>) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Text("Sản phẩm đã đặt", fontWeight = FontWeight.Bold, color = GreenTop, fontSize = 18.sp)
            items.forEachIndexed { index, item ->
                OrderItemRow(item = item)
                if (index != items.lastIndex) {
                    HorizontalDivider(color = Color(0xFFE5E7EB))
                }
            }
        }
    }
}

@Composable
private fun OrderItemRow(item: OrderItemDto) {
    val lineTotal = item.totalPrice ?: item.price * item.quantity
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(item.name, fontWeight = FontWeight.SemiBold, color = Color(0xFF111827), fontSize = 16.sp)
            Spacer(Modifier.height(4.dp))
            Text(
                text = "${item.quantity} x ${item.unit} • ${formatCurrency(item.price)}",
                color = Color(0xFF6B7280),
                fontSize = 13.sp
            )
            item.product?.brand?.takeIf { it.isNotBlank() }?.let {
                Spacer(Modifier.height(2.dp))
                Text(text = it, color = Color(0xFF9CA3AF), fontSize = 12.sp)
            }
        }
        Spacer(Modifier.width(12.dp))
        Text(
            text = formatCurrency(lineTotal),
            fontWeight = FontWeight.Bold,
            color = GreenTop,
            fontSize = 15.sp
        )
    }
}

@Composable
private fun PaymentCard(order: OrderDto) {
    val (paymentLabel, paymentColor) = paymentStatusPresentation(order.paymentStatus)
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Thanh toán", fontWeight = FontWeight.Bold, color = GreenTop, fontSize = 18.sp)
            DetailRow("Phương thức", paymentMethodLabel(order.paymentMethod))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Trạng thái thanh toán", color = Color(0xFF6B7280), fontSize = 14.sp)
                StatusChip(label = paymentLabel, color = paymentColor)
            }
        }
    }
}

@Composable
private fun SummaryCard(order: OrderDto) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Tổng thanh toán", fontWeight = FontWeight.Bold, color = GreenTop, fontSize = 18.sp)
            DetailRow("Tạm tính", formatCurrency(order.subtotal))
            DetailRow("Phí vận chuyển", if (order.shippingFee == 0.0) "Miễn phí" else formatCurrency(order.shippingFee))
            DetailRow("Giảm giá", formatCurrency(order.discount))
            if (order.pointsUsed > 0) {
                DetailRow("Điểm đã dùng", order.pointsUsed.toString())
            }
            if (order.pointsEarned > 0) {
                DetailRow("Điểm nhận được", order.pointsEarned.toString())
            }
            HorizontalDivider(color = Color(0xFFE5E7EB))
            DetailRow(
                label = "Tổng cộng",
                value = formatCurrency(order.total),
                valueColor = GreenTop,
                emphasized = true
            )
        }
    }
}

@Composable
private fun NoteCard(order: OrderDto) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Ghi chú", fontWeight = FontWeight.Bold, color = GreenTop, fontSize = 18.sp)
            order.note?.takeIf { it.isNotBlank() }?.let {
                Text(text = it, color = Color(0xFF374151), fontSize = 14.sp)
            }
            order.cancelReason?.takeIf { it.isNotBlank() }?.let {
                Text(text = "Lý do hủy: $it", color = Color(0xFFB45309), fontSize = 14.sp, fontWeight = FontWeight.Medium)
            }
        }
    }
}

@Composable
private fun ActionCard(
    canCancel: Boolean,
    onRetry: () -> Unit,
    onCancelOrder: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = onRetry,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(Icons.Filled.Refresh, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Tải lại đơn hàng")
            }
            if (canCancel) {
                OutlinedButton(
                    onClick = onCancelOrder,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(Icons.Filled.WarningAmber, contentDescription = null, tint = Color(0xFFB45309))
                    Spacer(Modifier.width(8.dp))
                    Text("Hủy đơn hàng", color = Color(0xFFB45309))
                }
            }
        }
    }
}

@Composable
private fun ErrorState(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Filled.WarningAmber,
                contentDescription = null,
                tint = Color(0xFFE57373)
            )
            Spacer(Modifier.height(12.dp))
            Text("Không tải được chi tiết đơn hàng", fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(6.dp))
            Text(message, color = Color(0xFF6B7280))
            Spacer(Modifier.height(16.dp))
            Button(onClick = onRetry) {
                Text("Thử lại")
            }
        }
    }
}

@Composable
private fun DetailRow(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    singleLine: Boolean = true,
    emphasized: Boolean = false,
    valueColor: Color = Color(0xFF111827)
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = if (singleLine) Alignment.CenterVertically else Alignment.Top
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (icon != null) {
                Icon(icon, contentDescription = null, tint = Color(0xFF9CA3AF))
                Spacer(Modifier.width(8.dp))
            }
            Text(
                text = label,
                color = Color(0xFF6B7280),
                fontSize = if (emphasized) 16.sp else 14.sp,
                fontWeight = if (emphasized) FontWeight.Bold else FontWeight.Normal
            )
        }
        Spacer(Modifier.width(12.dp))
        Text(
            text = value,
            color = valueColor,
            fontSize = if (emphasized) 18.sp else 14.sp,
            fontWeight = if (emphasized) FontWeight.ExtraBold else FontWeight.SemiBold,
            maxLines = if (singleLine) 1 else Int.MAX_VALUE,
            overflow = if (singleLine) TextOverflow.Ellipsis else TextOverflow.Clip
        )
    }
}

@Composable
private fun AssistChip(label: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Surface(
        color = Color(0xFFF3F4F6),
        shape = RoundedCornerShape(999.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(icon, contentDescription = null, tint = GreenTop)
            Text(text = label, color = GreenTop, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun StatusChip(label: String, color: Color) {
    Surface(
        color = color.copy(alpha = 0.12f),
        shape = RoundedCornerShape(999.dp)
    ) {
        Text(
            text = label,
            color = color,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
        )
    }
}

private fun pickupTypeLabel(value: String): String = when (value.uppercase()) {
    "PICKUP" -> "Nhận tại cửa hàng"
    else -> "Giao tận nơi"
}

private fun paymentMethodLabel(value: String): String = when (value.uppercase()) {
    "COD" -> "Thanh toán khi nhận hàng"
    "MOMO" -> "Ví MoMo"
    "VNPAY" -> "VNPay"
    "ZALOPAY" -> "ZaloPay"
    else -> value
}

private fun orderStatusPresentation(value: String): Pair<String, Color> = when (value.uppercase()) {
    "PENDING" -> "Đơn chờ xác nhận" to Color(0xFFF59E0B)
    "PROCESSING" -> "Đơn đang xử lý" to Color(0xFF2563EB)
    "SHIPPING" -> "Đơn đang giao" to Color(0xFF7C3AED)
    "DELIVERED" -> "Đơn đã giao" to Color(0xFF2E7D32)
    "CANCELLED" -> "Đơn đã hủy" to Color(0xFFDC2626)
    "RETURNED" -> "Đơn hoàn trả" to Color(0xFF6B7280)
    else -> value to Color(0xFF6B7280)
}

private fun paymentStatusPresentation(value: String): Pair<String, Color> = when (value.uppercase()) {
    "UNPAID" -> "Chưa thanh toán" to Color(0xFFB45309)
    "PENDING" -> "Đang chờ" to Color(0xFFF59E0B)
    "COMPLETED" -> "Đã thanh toán" to Color(0xFF2E7D32)
    "FAILED" -> "Thất bại" to Color(0xFFDC2626)
    "REFUNDED" -> "Đã hoàn tiền" to Color(0xFF7C3AED)
    else -> value to Color(0xFF6B7280)
}

private fun formatCurrency(value: Double): String {
    val symbols = DecimalFormatSymbols(Locale.US).apply {
        groupingSeparator = '.'
        decimalSeparator = ','
    }
    return DecimalFormat("#,###", symbols).format(value) + " đ"
}

private fun formatDateTime(raw: String): String {
    val output = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
    return runCatching { OffsetDateTime.parse(raw).format(output) }
        .recoverCatching { LocalDateTime.parse(raw).format(output) }
        .recoverCatching { LocalDate.parse(raw).format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) }
        .getOrElse { raw.replace('T', ' ').take(16) }
}
