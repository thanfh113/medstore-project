package com.example.nhathuoc.ui.screen.miniscreen

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.nhathuoc.data.model.ComplaintDto
import com.example.nhathuoc.data.model.OrderDto
import com.example.nhathuoc.data.model.OrderItemDto
import com.example.nhathuoc.data.model.UiState
import com.example.nhathuoc.data.model.UserAddress
import com.example.nhathuoc.ui.theme.GreenTop
import com.example.nhathuoc.viewmodel.OrderViewModel
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

// Design tokens
private val BgGray = Color(0xFFF3F7F4)
private val CardBg = Color.White
private val TextPrimary = Color(0xFF1B2B1F)
private val TextSecondary = Color(0xFF5A7A62)
private val GreenLight = Color(0xFFE8F5E9)
private val DividerColor = Color(0xFFE0EDE3)

private data class PickedComplaintAttachment(
    val uri: Uri,
    val name: String,
    val fileType: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderDetailScreen(
    orderId: String,
    onBack: () -> Unit,
    onOpenProduct: (String, Boolean) -> Unit = { _, _ -> },
    onResumePayment: (String, String) -> Unit = { _, _ -> },
    viewModel: OrderViewModel = hiltViewModel()
) {
    val orderState by viewModel.orderState.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val complaintState by viewModel.complaintState.collectAsState()
    var showCancelDialog by rememberSaveable { mutableStateOf(false) }
    var showReceivedDialog by rememberSaveable { mutableStateOf(false) }
    var showComplaintDialog by rememberSaveable { mutableStateOf(false) }
    var showReviewPromptDialog by rememberSaveable { mutableStateOf(false) }
    var pendingReviewProductId by rememberSaveable { mutableStateOf<String?>(null) }
    var promptReviewAfterReceive by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(orderId) { viewModel.getOrderById(orderId) }
    LaunchedEffect(complaintState) { if (complaintState is UiState.Success) showComplaintDialog = false }
    LaunchedEffect(orderState, promptReviewAfterReceive) {
        val state = orderState
        if (promptReviewAfterReceive && state is UiState.Success && state.data.status.equals("DELIVERED", ignoreCase = true)) {
            pendingReviewProductId = state.data.items.firstOrNull()?.productId
            showReviewPromptDialog = pendingReviewProductId != null
            promptReviewAfterReceive = false
        }
    }

    // Dialogs
    if (showCancelDialog) {
        StyledAlertDialog(
            icon = Icons.Outlined.Cancel,
            iconBg = Color(0xFFFFEBEE),
            iconTint = Color(0xFFE53935),
            title = "Hủy đơn hàng?",
            body = "Bạn có chắc muốn hủy đơn hàng này không?",
            confirmLabel = "Xác nhận hủy",
            confirmColor = Color(0xFFE53935),
            onConfirm = { showCancelDialog = false; viewModel.cancelOrder(orderId, "Người dùng hủy trên ứng dụng") },
            onDismiss = { showCancelDialog = false }
        )
    }

    if (showReceivedDialog) {
        StyledAlertDialog(
            icon = Icons.Outlined.CheckCircle,
            iconBg = GreenLight,
            iconTint = GreenTop,
            title = "Xác nhận đã nhận hàng?",
            body = "Đơn sẽ chuyển sang đã giao. Sau đó bạn có thể đánh giá từng sản phẩm trong đơn.",
            confirmLabel = "Đã nhận hàng",
            confirmColor = GreenTop,
            onConfirm = { showReceivedDialog = false; promptReviewAfterReceive = true; viewModel.confirmOrderReceived(orderId) },
            onDismiss = { showReceivedDialog = false }
        )
    }

    if (showReviewPromptDialog) {
        StyledAlertDialog(
            icon = Icons.Filled.Star,
            iconBg = Color(0xFFFFF8E1),
            iconTint = Color(0xFFFFAB00),
            title = "Viết đánh giá sản phẩm?",
            body = "Đánh giá 5 sao sẽ được cộng thêm 200 điểm thưởng.",
            confirmLabel = "Viết đánh giá",
            confirmColor = GreenTop,
            onConfirm = {
                val pid = pendingReviewProductId
                showReviewPromptDialog = false
                pendingReviewProductId = null
                if (!pid.isNullOrBlank()) onOpenProduct(pid, true)
            },
            onDismiss = { showReviewPromptDialog = false; pendingReviewProductId = null },
            dismissLabel = "Để sau"
        )
    }

    if (showComplaintDialog && orderState is UiState.Success) {
        ComplaintDialog(
            order = (orderState as UiState.Success<OrderDto>).data,
            state = complaintState,
            onDismiss = { showComplaintDialog = false; viewModel.clearComplaintState() },
            onSubmit = { type, title, desc, attachments ->
                viewModel.createComplaint(orderId = orderId, type = type, title = title, description = desc, attachmentUris = attachments)
            }
        )
    }

    Scaffold(
        containerColor = BgGray,
        topBar = {
            Surface(shadowElevation = 2.dp, color = CardBg) {
                CenterAlignedTopAppBar(
                    title = {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Chi tiết đơn hàng", fontWeight = FontWeight.Bold, fontSize = 17.sp, color = TextPrimary)
                            if (orderState is UiState.Success) {
                                Text(
                                    "#${(orderState as UiState.Success<OrderDto>).data.orderCode}",
                                    fontSize = 12.sp, color = TextSecondary
                                )
                            }
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Box(
                                modifier = Modifier.size(36.dp).clip(CircleShape).background(BgGray),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Quay lại",
                                    tint = GreenTop,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = CardBg,
                        titleContentColor = Color(0xFF1B2B1F),
                        navigationIconContentColor = Color(0xFF1B2B1F)
                    )
                )
            }
        }
    ) { innerPadding ->
        when (val state = orderState) {
            UiState.Idle, UiState.Loading -> Box(
                Modifier.fillMaxSize().padding(innerPadding),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator(color = GreenTop) }

            is UiState.Error -> ErrorState(state.message, { viewModel.getOrderById(orderId) }, Modifier.padding(innerPadding))

            is UiState.Success -> {
                val order = state.data
                OrderDetailContent(
                    order = order,
                    isBusy = isLoading,
                    onRetry = { viewModel.getOrderById(orderId) },
                    onCancelOrder = { showCancelDialog = true },
                    onConfirmReceived = { showReceivedDialog = true },
                    onOpenComplaint = { showComplaintDialog = true },
                    onOpenProduct = onOpenProduct,
                    onResumePayment = onResumePayment,
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
    onConfirmReceived: () -> Unit,
    onOpenComplaint: () -> Unit,
    onOpenProduct: (String, Boolean) -> Unit,
    onResumePayment: (String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    val status = order.status.uppercase()
    val canCancel = status in setOf("PENDING", "PROCESSING")
    val canConfirmReceived = status == "SHIPPING"
    val canReviewProducts = status == "DELIVERED"
    val canComplaint = status !in setOf("CANCELLED", "RETURNED")
    val canResume = canResumeGatewayPayment(order)

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item { HeaderCard(order) }
            item { ShippingCard(order) }
            if (order.shippingAddress != null) item { AddressCard(order.shippingAddress) }
            item { ItemsCard(order.items, canReviewProducts, onOpenProduct) }
            if (canReviewProducts && order.items.isNotEmpty()) {
                item { ReviewPromptCard { onOpenProduct(order.items.first().productId, true) } }
            }
            item { PaymentCard(order) }
            item { SummaryCard(order) }
            if (!order.note.isNullOrBlank() || !order.cancelReason.isNullOrBlank()) {
                item { NoteCard(order) }
            }
            item {
                ActionCard(
                    canCancel, canConfirmReceived, canComplaint, canResume,
                    onRetry, onCancelOrder, onConfirmReceived, onOpenComplaint,
                    onResumePayment = { onResumePayment(order.id, order.paymentMethod.uppercase()) }
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

// ─── Section card wrapper ─────────────────────────────────────────────────────
@Composable
private fun SectionCard(
    title: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = CardBg,
        shadowElevation = 2.dp
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier.size(34.dp).clip(RoundedCornerShape(10.dp)).background(GreenLight),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = null, tint = GreenTop, modifier = Modifier.size(18.dp))
                }
                Text(title, fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 15.sp)
            }
            Spacer(Modifier.height(14.dp))
            HorizontalDivider(color = DividerColor)
            Spacer(Modifier.height(14.dp))
            content()
        }
    }
}

// ─── Cards ────────────────────────────────────────────────────────────────────
@Composable
private fun HeaderCard(order: OrderDto) {
    val (statusLabel, statusColor) = orderStatusPresentation(order.status)
    val (paymentLabel, paymentColor) = paymentStatusPresentation(order.paymentStatus)

    SectionCard(title = "Thông tin đơn hàng", icon = Icons.Outlined.Receipt) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(order.orderCode, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = GreenTop)
                Spacer(Modifier.height(3.dp))
                Text("Đặt lúc ${formatDateTime(order.createdAt)}", fontSize = 12.sp, color = TextSecondary)
            }
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                StatusBadge(statusLabel, statusColor)
                StatusBadge(paymentLabel, paymentColor)
            }
        }

        Spacer(Modifier.height(12.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            InfoChip(
                label = if (order.pickupType.uppercase() == "PICKUP") "Nhận tại cửa hàng" else "Giao tận nơi",
                icon = if (order.pickupType.uppercase() == "PICKUP") Icons.Outlined.Storefront else Icons.Outlined.LocalShipping
            )
            InfoChip(label = paymentMethodLabel(order.paymentMethod), icon = Icons.Outlined.Payments)
        }

        orderProgressMessage(order)?.let { msg ->
            Spacer(Modifier.height(10.dp))
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = GreenLight,
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Outlined.Info, null, tint = GreenTop, modifier = Modifier.size(16.dp))
                    Text(msg, color = GreenTop, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                }
            }
        }
    }
}

@Composable
private fun ShippingCard(order: OrderDto) {
    SectionCard(title = "Vận chuyển & Tiến độ", icon = Icons.Outlined.LocalShipping) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            DetailRow("Hình thức", if (order.pickupType.uppercase() == "PICKUP") "Nhận tại cửa hàng" else "Giao tận nơi")
            order.estimatedDelivery?.takeIf { it.isNotBlank() }?.let { DetailRow("Dự kiến giao", formatDateTime(it)) }
            order.deliveredAt?.takeIf { it.isNotBlank() }?.let { DetailRow("Đã giao lúc", formatDateTime(it)) }
            order.cancelledAt?.takeIf { it.isNotBlank() }?.let { DetailRow("Đã hủy lúc", formatDateTime(it)) }
        }
    }
}

@Composable
private fun AddressCard(address: UserAddress) {
    SectionCard(title = "Địa chỉ nhận hàng", icon = Icons.Outlined.LocationOn) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier.size(42.dp).clip(RoundedCornerShape(12.dp)).background(GreenLight),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.LocationOn, null, tint = GreenTop, modifier = Modifier.size(22.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(address.recipientName, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TextPrimary)
                Spacer(Modifier.height(2.dp))
                Text(address.recipientPhone, fontSize = 13.sp, color = TextSecondary)
                Spacer(Modifier.height(4.dp))
                Text(address.fullAddress, fontSize = 13.sp, color = Color(0xFF4B5563), lineHeight = 19.sp)
            }
        }
    }
}

@Composable
private fun ItemsCard(
    items: List<OrderItemDto>,
    canReviewProducts: Boolean,
    onOpenProduct: (String, Boolean) -> Unit
) {
    SectionCard(title = "Sản phẩm đã đặt (${items.size})", icon = Icons.Outlined.MedicalServices) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            items.forEachIndexed { index, item ->
                val lineTotal = item.totalPrice ?: (item.price * item.quantity)
                Column {
                    // Tên + giá cùng hàng
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                item.name,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp,
                                color = TextPrimary,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(Modifier.height(3.dp))
                            Text(
                                "${item.quantity} × ${item.unit}  •  ${formatCurrency(item.price)}",
                                fontSize = 12.sp,
                                color = TextSecondary
                            )
                            item.product?.brand?.takeIf { it.isNotBlank() }?.let { brand ->
                                Spacer(Modifier.height(2.dp))
                                Surface(shape = RoundedCornerShape(4.dp), color = Color(0xFFF0F4F1)) {
                                    Text(brand, fontSize = 10.sp, color = TextSecondary, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                }
                            }
                        }
                        Spacer(Modifier.width(12.dp))
                        Text(formatCurrency(lineTotal), fontWeight = FontWeight.Bold, color = GreenTop, fontSize = 14.sp)
                    }
                    // Nút đánh giá — full width bên dưới, KHÔNG bị che bởi giá
                    if (canReviewProducts) {
                        Spacer(Modifier.height(10.dp))
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFFFFBEC),
                            onClick = { onOpenProduct(item.productId, true) }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 9.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                                ) {
                                    // 5 sao đầy đủ
                                    repeat(5) {
                                        Icon(Icons.Filled.Star, contentDescription = null, tint = Color(0xFFFFAB00), modifier = Modifier.size(14.dp))
                                    }
                                    Spacer(Modifier.width(6.dp))
                                    Text("Đánh giá sản phẩm này", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF78350F))
                                }
                                Icon(Icons.AutoMirrored.Outlined.KeyboardArrowRight, contentDescription = null, tint = Color(0xFFFFAB00), modifier = Modifier.size(15.dp))
                            }
                        }
                    }
                }
                if (index != items.lastIndex) {
                    Spacer(Modifier.height(4.dp))
                    HorizontalDivider(color = DividerColor)
                    Spacer(Modifier.height(4.dp))
                }
            }
        }
    }
}

@Composable
private fun ReviewPromptCard(onReviewClick: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = Color(0xFFFFFBEC),
        shadowElevation = 1.dp
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFFFFE57F)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.Star, null, tint = Color(0xFFFF8F00), modifier = Modifier.size(24.dp))
                }
                Column {
                    Text("Nhận điểm thưởng từ đánh giá", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF78350F))
                    Text("Đánh giá 5 ⭐ cho mỗi sản phẩm → +200 điểm", fontSize = 12.sp, color = Color(0xFF92400E))
                }
            }
            // 5 sao preview
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(5) {
                    Icon(Icons.Filled.Star, contentDescription = null, tint = Color(0xFFFFAB00), modifier = Modifier.size(28.dp))
                }
            }
            Button(
                onClick = onReviewClick,
                modifier = Modifier.fillMaxWidth().height(44.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF8F00)),
                elevation = ButtonDefaults.buttonElevation(0.dp)
            ) {
                Icon(Icons.Filled.Star, null, tint = Color.White, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text("Viết đánh giá ngay", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
    }
}

@Composable
private fun PaymentCard(order: OrderDto) {
    val (paymentLabel, paymentColor) = paymentStatusPresentation(order.paymentStatus)
    SectionCard(title = "Thanh toán", icon = Icons.Outlined.CreditCard) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            DetailRow("Phương thức", paymentMethodLabel(order.paymentMethod))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Trạng thái", fontSize = 13.sp, color = TextSecondary)
                Surface(shape = RoundedCornerShape(20.dp), color = paymentColor.copy(alpha = 0.12f)) {
                    Text(paymentLabel, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = paymentColor,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp))
                }
            }
        }
    }
}

@Composable
private fun SummaryCard(order: OrderDto) {
    SectionCard(title = "Tóm tắt thanh toán", icon = Icons.Outlined.AccountBalance) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            DetailRow("Tạm tính", formatCurrency(order.subtotal))
            DetailRow("Phí vận chuyển", if (order.shippingFee == 0.0) "Miễn phí" else formatCurrency(order.shippingFee))
            if (order.discount > 0) DetailRow("Giảm giá", "-${formatCurrency(order.discount)}")
            if (order.pointsUsed > 0) DetailRow("Điểm đã dùng", "${order.pointsUsed} điểm")
            if (order.pointsEarned > 0) DetailRow("Điểm nhận được", "+${order.pointsEarned} điểm")
            Spacer(Modifier.height(2.dp))
            HorizontalDivider(color = DividerColor)
            Spacer(Modifier.height(2.dp))
            DetailRow("Tổng cộng", formatCurrency(order.total), valueColor = GreenTop, emphasized = true)
        }
    }
}

@Composable
private fun NoteCard(order: OrderDto) {
    SectionCard(title = "Ghi chú", icon = Icons.Outlined.Notes) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            order.note?.takeIf { it.isNotBlank() }?.let {
                Text(it, fontSize = 14.sp, color = Color(0xFF374151), lineHeight = 20.sp)
            }
            order.cancelReason?.takeIf { it.isNotBlank() }?.let {
                Surface(shape = RoundedCornerShape(10.dp), color = Color(0xFFFFF3E0)) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Outlined.Info, null, tint = Color(0xFFB45309), modifier = Modifier.size(16.dp))
                        Text("Lý do hủy: $it", fontSize = 13.sp, color = Color(0xFFB45309), fontWeight = FontWeight.Medium)
                    }
                }
            }
        }
    }
}

@Composable
private fun ActionCard(
    canCancel: Boolean,
    canConfirmReceived: Boolean,
    canComplaint: Boolean,
    canResumePayment: Boolean,
    onRetry: () -> Unit,
    onCancelOrder: () -> Unit,
    onConfirmReceived: () -> Unit,
    onOpenComplaint: () -> Unit,
    onResumePayment: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = CardBg,
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text("Thao tác", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TextPrimary)
            HorizontalDivider(color = DividerColor)
            Spacer(Modifier.height(2.dp))

            if (canResumePayment) {
                ActionButton(
                    label = "Tiếp tục thanh toán",
                    icon = Icons.Outlined.Payments,
                    bg = GreenTop,
                    fg = Color.White,
                    onClick = onResumePayment
                )
            }
            if (canConfirmReceived) {
                ActionButton(
                    label = "Đã nhận được hàng",
                    icon = Icons.Outlined.CheckCircle,
                    bg = GreenTop,
                    fg = Color.White,
                    onClick = onConfirmReceived
                )
            }
            if (canComplaint) {
                ActionButton(
                    label = "Khiếu nại đơn hàng",
                    icon = Icons.Outlined.Flag,
                    bg = Color(0xFFEFF6FF),
                    fg = Color(0xFF2563EB),
                    outlined = true,
                    outlinedBorder = Color(0xFFBFDBFE),
                    onClick = onOpenComplaint
                )
            }
            if (canCancel) {
                ActionButton(
                    label = "Hủy đơn hàng",
                    icon = Icons.Outlined.Cancel,
                    bg = Color(0xFFFFEBEE),
                    fg = Color(0xFFE53935),
                    outlined = true,
                    outlinedBorder = Color(0xFFFFCDD2),
                    onClick = onCancelOrder
                )
            }
            ActionButton(
                label = "Tải lại đơn hàng",
                icon = Icons.Outlined.Refresh,
                bg = Color(0xFFF0F4F1),
                fg = TextSecondary,
                onClick = onRetry
            )
        }
    }
}

@Composable
private fun ActionButton(
    label: String,
    icon: ImageVector,
    bg: Color,
    fg: Color,
    outlined: Boolean = false,
    outlinedBorder: Color = Color.Transparent,
    onClick: () -> Unit
) {
    if (outlined) {
        OutlinedButton(
            onClick = onClick,
            modifier = Modifier.fillMaxWidth().height(48.dp),
            shape = RoundedCornerShape(14.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, outlinedBorder),
            colors = ButtonDefaults.outlinedButtonColors(containerColor = bg, contentColor = fg)
        ) {
            Icon(icon, null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text(label, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
        }
    } else {
        Button(
            onClick = onClick,
            modifier = Modifier.fillMaxWidth().height(48.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = bg, contentColor = fg),
            elevation = ButtonDefaults.buttonElevation(0.dp)
        ) {
            Icon(icon, null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text(label, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
        }
    }
}

// ─── Reusable UI components ───────────────────────────────────────────────────
@Composable
private fun StatusBadge(label: String, color: Color) {
    Surface(shape = RoundedCornerShape(20.dp), color = color.copy(alpha = 0.12f)) {
        Text(
            label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = color,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
        )
    }
}

@Composable
private fun InfoChip(label: String, icon: ImageVector) {
    Surface(color = Color(0xFFF0F4F1), shape = RoundedCornerShape(999.dp)) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Icon(icon, null, tint = GreenTop, modifier = Modifier.size(14.dp))
            Text(label, color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun DetailRow(
    label: String,
    value: String,
    emphasized: Boolean = false,
    valueColor: Color = TextPrimary
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            label,
            fontSize = if (emphasized) 15.sp else 13.sp,
            fontWeight = if (emphasized) FontWeight.SemiBold else FontWeight.Normal,
            color = TextSecondary,
            modifier = Modifier.weight(1f)
        )
        Spacer(Modifier.width(12.dp))
        Text(
            value,
            fontSize = if (emphasized) 17.sp else 13.sp,
            fontWeight = if (emphasized) FontWeight.ExtraBold else FontWeight.SemiBold,
            color = valueColor,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun StyledAlertDialog(
    icon: ImageVector,
    iconBg: Color,
    iconTint: Color,
    title: String,
    body: String,
    confirmLabel: String,
    confirmColor: Color,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    dismissLabel: String = "Đóng"
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        containerColor = CardBg,
        icon = {
            Box(
                modifier = Modifier.size(52.dp).clip(CircleShape).background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = iconTint, modifier = Modifier.size(28.dp))
            }
        },
        title = { Text(title, fontWeight = FontWeight.Bold, fontSize = 17.sp, color = TextPrimary) },
        text = { Text(body, fontSize = 14.sp, color = TextSecondary, lineHeight = 21.sp) },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = confirmColor),
                shape = RoundedCornerShape(12.dp),
                elevation = ButtonDefaults.buttonElevation(0.dp)
            ) { Text(confirmLabel, fontWeight = FontWeight.Bold) }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, DividerColor),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary)
            ) { Text(dismissLabel) }
        }
    )
}

@Composable
private fun ErrorState(message: String, onRetry: () -> Unit, modifier: Modifier = Modifier) {
    Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(
                modifier = Modifier.size(72.dp).clip(CircleShape).background(Color(0xFFFFEBEE)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Outlined.WarningAmber, null, tint = Color(0xFFE57373), modifier = Modifier.size(36.dp))
            }
            Text("Không tải được đơn hàng", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextPrimary)
            Text(message, fontSize = 13.sp, color = TextSecondary)
            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(containerColor = GreenTop),
                shape = RoundedCornerShape(12.dp),
                elevation = ButtonDefaults.buttonElevation(0.dp)
            ) { Text("Thử lại") }
        }
    }
}

// ─── Complaint dialog ─────────────────────────────────────────────────────────
@Composable
private fun ComplaintDialog(
    order: OrderDto,
    state: UiState<ComplaintDto>,
    onDismiss: () -> Unit,
    onSubmit: (String, String, String, List<Uri>) -> Unit
) {
    val context = LocalContext.current
    val options = listOf(
        "DAMAGED" to "Hàng hỏng/vỡ",
        "WRONG_ITEM" to "Giao sai hàng",
        "MISSING_ITEM" to "Thiếu sản phẩm",
        "PAYMENT" to "Thanh toán",
        "OTHER" to "Khác"
    )
    var selectedType by rememberSaveable { mutableStateOf(options.first().first) }
    var title by rememberSaveable { mutableStateOf("") }
    var description by rememberSaveable { mutableStateOf("") }
    var attachments by remember { mutableStateOf<List<PickedComplaintAttachment>>(emptyList()) }
    val isSubmitting = state is UiState.Loading
    val filePicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenMultipleDocuments()) { uris ->
        val picked = uris.map { uri ->
            val name = context.displayName(uri).ifBlank { "file_dinh_kem" }
            PickedComplaintAttachment(uri, name, detectComplaintFileType(context.contentResolver.getType(uri), name))
        }
        attachments = (attachments + picked).distinctBy { it.uri }.take(5)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        containerColor = CardBg,
        title = {
            Text(
                "Khiếu nại đơn #${order.orderCode}",
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp,
                color = TextPrimary
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Loại khiếu nại", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = TextSecondary)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    options.chunked(3).forEach { row ->
                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            row.forEach { (value, label) ->
                                Surface(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (selectedType == value) GreenTop else Color(0xFFF0F4F1),
                                    onClick = { selectedType = value }
                                ) {
                                    Text(
                                        label,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = if (selectedType == value) Color.White else TextPrimary,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)
                                    )
                                }
                            }
                        }
                    }
                }
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Tiêu đề") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GreenTop,
                        unfocusedBorderColor = Color(0xFFE0EDE3),
                        focusedLabelColor = GreenTop,
                        unfocusedLabelColor = Color(0xFF5A7A62),
                        focusedTextColor = Color(0xFF1B2B1F),
                        unfocusedTextColor = Color(0xFF1B2B1F),
                        cursorColor = GreenTop,
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color(0xFFFAFCFA)
                    )
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Mô tả chi tiết") },
                    minLines = 3,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GreenTop,
                        unfocusedBorderColor = Color(0xFFE0EDE3),
                        focusedLabelColor = GreenTop,
                        unfocusedLabelColor = Color(0xFF5A7A62),
                        focusedTextColor = Color(0xFF1B2B1F),
                        unfocusedTextColor = Color(0xFF1B2B1F),
                        cursorColor = GreenTop,
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color(0xFFFAFCFA)
                    )
                )
                OutlinedButton(
                    onClick = { filePicker.launch(arrayOf("image/*", "application/pdf")) },
                    enabled = !isSubmitting,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, DividerColor)
                ) {
                    Icon(Icons.Outlined.AttachFile, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Thêm ảnh/PDF (${attachments.size}/5)", fontSize = 13.sp)
                }
                attachments.forEach { att ->
                    Surface(shape = RoundedCornerShape(8.dp), color = Color(0xFFF0F4F1)) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "${att.fileType} • ${att.name}",
                                fontSize = 12.sp,
                                color = TextSecondary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                            TextButton(
                                onClick = { attachments = attachments.filterNot { it.uri == att.uri } },
                                enabled = !isSubmitting,
                                contentPadding = PaddingValues(horizontal = 8.dp)
                            ) {
                                Text("Xóa", fontSize = 12.sp, color = Color(0xFFE53935))
                            }
                        }
                    }
                }
                if (state is UiState.Error) {
                    Text(state.message, color = Color(0xFFE53935), fontSize = 12.sp)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onSubmit(selectedType, title, description, attachments.map { it.uri }) },
                enabled = !isSubmitting && title.isNotBlank() && description.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = GreenTop),
                shape = RoundedCornerShape(12.dp),
                elevation = ButtonDefaults.buttonElevation(0.dp)
            ) {
                Text(if (isSubmitting) "Đang gửi..." else "Gửi khiếu nại", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                enabled = !isSubmitting,
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, DividerColor)
            ) { Text("Đóng") }
        }
    )
}

// ─── Helpers ──────────────────────────────────────────────────────────────────
private fun Context.displayName(uri: Uri): String =
    contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)
        ?.use { c -> if (c.moveToFirst()) c.getString(c.getColumnIndex(OpenableColumns.DISPLAY_NAME).coerceAtLeast(0)).orEmpty() else "" }
        .orEmpty()
        .ifBlank { uri.lastPathSegment.orEmpty().substringAfterLast('/') }

private fun detectComplaintFileType(mimeType: String?, name: String): String = when {
    mimeType == "application/pdf" || name.lowercase().endsWith(".pdf") -> "PDF"
    mimeType?.startsWith("image/") == true -> "IMAGE"
    else -> "FILE"
}

private fun canResumeGatewayPayment(order: OrderDto): Boolean {
    val method = order.paymentMethod.uppercase()
    val ps = order.paymentStatus.uppercase()
    val os = order.status.uppercase()
    return method in setOf("MOMO", "ZALOPAY") && ps == "PENDING" && os !in setOf("CANCELLED", "RETURNED", "DELIVERED")
}

private fun paymentMethodLabel(v: String) = when (v.uppercase()) {
    "COD" -> "Thanh toán khi nhận hàng"; "MOMO" -> "Ví MoMo"; "VNPAY" -> "Thanh toán online"; "ZALOPAY" -> "ZaloPay"; else -> v
}

private fun orderStatusPresentation(v: String): Pair<String, Color> = when (v.uppercase()) {
    "PENDING" -> "Chờ xác nhận" to Color(0xFFF59E0B)
    "PROCESSING" -> "Đang xử lý" to Color(0xFF2563EB)
    "SHIPPING" -> "Đang giao" to Color(0xFF7C3AED)
    "DELIVERED" -> "Đã giao" to Color(0xFF2E7D32)
    "CANCELLED" -> "Đã hủy" to Color(0xFFDC2626)
    "RETURNED" -> "Hoàn trả" to Color(0xFF6B7280)
    else -> v to Color(0xFF6B7280)
}

private fun paymentStatusPresentation(v: String): Pair<String, Color> = when (v.uppercase()) {
    "UNPAID" -> "Chưa thanh toán" to Color(0xFFB45309)
    "PENDING" -> "Chờ thanh toán" to Color(0xFFF59E0B)
    "COMPLETED" -> "Đã thanh toán" to Color(0xFF2E7D32)
    "FAILED" -> "Thất bại" to Color(0xFFDC2626)
    "PARTIALLY_REFUNDED", "REFUNDED" -> "Đã hoàn tiền" to Color(0xFF7C3AED)
    else -> v to Color(0xFF6B7280)
}

private fun orderProgressMessage(order: OrderDto): String? {
    val ps = order.paymentStatus.uppercase()
    val os = order.status.uppercase()
    return when {
        ps == "COMPLETED" && os == "PROCESSING" -> "Đã thanh toán, đơn đang được nhà thuốc xử lý."
        ps == "COMPLETED" && os == "PENDING" -> "Đã thanh toán, đơn đang chờ nhà thuốc xác nhận."
        ps == "COMPLETED" && os == "SHIPPING" -> "Đã thanh toán, đơn đang được giao."
        ps == "COMPLETED" && os == "DELIVERED" -> "Đã thanh toán, đơn đã giao thành công."
        ps == "PENDING" -> "Thanh toán đang chờ xác nhận từ cổng thanh toán."
        else -> null
    }
}

private fun formatCurrency(value: Double): String {
    val symbols = DecimalFormatSymbols(Locale.US).apply { groupingSeparator = '.'; decimalSeparator = ',' }
    return DecimalFormat("#,###", symbols).format(value) + " đ"
}

private fun formatDateTime(raw: String): String {
    val output = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
    return runCatching { OffsetDateTime.parse(raw).format(output) }
        .recoverCatching { LocalDateTime.parse(raw).format(output) }
        .recoverCatching { LocalDate.parse(raw).format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) }
        .getOrElse { raw.replace('T', ' ').take(16) }
}