package com.example.nhathuoc.ui.screen.miniscreen

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
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
import com.example.nhathuoc.viewmodel.OrderViewModel
import com.example.nhathuoc.util.formatUtcToVnDateTime
import com.example.nhathuoc.util.formatVnDateTime
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale


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
    onOpenProduct: (productId: String, openReview: Boolean, orderId: String?, orderItemId: String?) -> Unit = { _, _, _, _ -> },
    onResumePayment: (String, String) -> Unit = { _, _ -> },
    viewModel: OrderViewModel = hiltViewModel()
) {
    val orderState by viewModel.orderState.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val complaintState by viewModel.complaintState.collectAsState()
    val complaintsListState by viewModel.complaintsListState.collectAsState()
    val batchReviewState by viewModel.batchReviewState.collectAsState()
    var showCancelDialog by rememberSaveable { mutableStateOf(false) }
    var showReceivedDialog by rememberSaveable { mutableStateOf(false) }
    var showComplaintDialog by rememberSaveable { mutableStateOf(false) }
    var showBatchReviewDialog by rememberSaveable { mutableStateOf(false) }
    var promptReviewAfterReceive by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(orderId) {
        viewModel.getOrderById(orderId)
        viewModel.getComplaints()
    }
    LaunchedEffect(complaintState) { if (complaintState is UiState.Success) showComplaintDialog = false }
    LaunchedEffect(orderState, promptReviewAfterReceive) {
        val state = orderState
        if (promptReviewAfterReceive && state is UiState.Success && state.data.status.equals("DELIVERED", ignoreCase = true)) {
            if (state.data.items.any { !it.hasReviewed }) showBatchReviewDialog = true
            promptReviewAfterReceive = false
        }
    }
    LaunchedEffect(batchReviewState) {
        if (batchReviewState is UiState.Success) {
            showBatchReviewDialog = false
            viewModel.clearBatchReviewState()
            viewModel.getOrderById(orderId)
        }
    }

    // Dialogs
    if (showCancelDialog) {
        StyledAlertDialog(
            icon = Icons.Outlined.Cancel,
            iconBg = MaterialTheme.colorScheme.errorContainer,
            iconTint = MaterialTheme.colorScheme.error,
            title = "Hủy đơn hàng?",
            body = "Bạn có chắc muốn hủy đơn hàng này không?",
            confirmLabel = "Xác nhận hủy",
            confirmColor = MaterialTheme.colorScheme.error,
            onConfirm = { showCancelDialog = false; viewModel.cancelOrder(orderId, "Người dùng hủy trên ứng dụng") },
            onDismiss = { showCancelDialog = false }
        )
    }

    if (showReceivedDialog) {
        StyledAlertDialog(
            icon = Icons.Outlined.CheckCircle,
            iconBg = MaterialTheme.colorScheme.primaryContainer,
            iconTint = MaterialTheme.colorScheme.primary,
            title = "Xác nhận đã nhận hàng?",
            body = "Đơn sẽ chuyển sang đã giao. Sau đó bạn có thể đánh giá từng sản phẩm trong đơn.",
            confirmLabel = "Đã nhận hàng",
            confirmColor = MaterialTheme.colorScheme.primary,
            onConfirm = { showReceivedDialog = false; promptReviewAfterReceive = true; viewModel.confirmOrderReceived(orderId) },
            onDismiss = { showReceivedDialog = false }
        )
    }

    if (showBatchReviewDialog && orderState is UiState.Success) {
        val unreviewedItems = (orderState as UiState.Success<OrderDto>).data.items.filter { !it.hasReviewed }
        if (unreviewedItems.isNotEmpty()) {
            BatchReviewDialog(
                items = unreviewedItems,
                isSubmitting = batchReviewState is UiState.Loading,
                errorMessage = (batchReviewState as? UiState.Error)?.message,
                onSubmit = { ratings, titles, comments, attachmentUris ->
                    viewModel.submitBatchReviews(unreviewedItems, ratings, titles, comments, attachmentUris)
                },
                onDismiss = {
                    showBatchReviewDialog = false
                    viewModel.clearBatchReviewState()
                }
            )
        }
    }

    if (showComplaintDialog && orderState is UiState.Success) {
        val order = (orderState as UiState.Success<OrderDto>).data
        val dialogComplaints = (complaintsListState as? UiState.Success)?.data
            ?.filter { it.orderId == orderId } ?: emptyList()
        val complainedItemIds = dialogComplaints.mapNotNull { it.orderItemId }.toSet()
        val availableItems = order.items.filter { it.id !in complainedItemIds && !it.hasReviewed }
        val hasWholeOrderComplaint = dialogComplaints.any { it.orderItemId == null }
        ComplaintDialog(
            order = order,
            availableItems = availableItems,
            hasWholeOrderComplaint = hasWholeOrderComplaint,
            state = complaintState,
            onDismiss = { showComplaintDialog = false; viewModel.clearComplaintState() },
            onSubmit = { type, title, desc, attachments, itemId, productId ->
                viewModel.createComplaint(
                    orderId = orderId, type = type, title = title, description = desc,
                    attachmentUris = attachments, orderItemId = itemId, productId = productId
                )
            }
        )
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            com.example.nhathuoc.ui.component.GreenAppTopBar(
                title = "Chi tiết đơn hàng",
                subtitle = if (orderState is UiState.Success) "#${(orderState as UiState.Success<OrderDto>).data.orderCode}" else null,
                onBack = onBack
            )
        }
    ) { innerPadding ->
        when (val state = orderState) {
            UiState.Idle, UiState.Loading -> Box(
                Modifier.fillMaxSize().padding(innerPadding),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator(color = MaterialTheme.colorScheme.primary) }

            is UiState.Error -> ErrorState(state.message, { viewModel.getOrderById(orderId) }, Modifier.padding(innerPadding))

            is UiState.Success -> {
                val order = state.data
                val closedStatuses = setOf("RESOLVED", "REJECTED", "CANCELLED")
                val allComplaintsForOrder = (complaintsListState as? UiState.Success)?.data
                    ?.filter { it.orderId == orderId } ?: emptyList()
                OrderDetailContent(
                    order = order,
                    isBusy = isLoading,
                    allComplaintsForOrder = allComplaintsForOrder,
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
    allComplaintsForOrder: List<ComplaintDto>,
    onRetry: () -> Unit,
    onCancelOrder: () -> Unit,
    onConfirmReceived: () -> Unit,
    onOpenComplaint: () -> Unit,
    onOpenProduct: (productId: String, openReview: Boolean, orderId: String?, orderItemId: String?) -> Unit,
    onResumePayment: (String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    val status = order.status.uppercase()
    val canCancel = status in setOf("PENDING", "PROCESSING")
    val canConfirmReceived = status == "SHIPPING"
    // Per-item complaint tracking
    val complainedItemIds = allComplaintsForOrder.mapNotNull { it.orderItemId }.toSet()
    val hasWholeOrderComplaint = allComplaintsForOrder.any { it.orderItemId == null }
    // Map itemId → complaint status (for indicators in ItemsCard)
    val complaintsForItems: Map<String, String> = allComplaintsForOrder
        .filter { it.orderItemId != null }
        .associate { it.orderItemId!! to it.status }
    // Review: allowed per-item if that item has no complaint (any status)
    val within30Days = run {
        val delivered = order.deliveredAt
        if (delivered.isNullOrBlank()) true
        else runCatching {
            val dt = try { OffsetDateTime.parse(delivered).toLocalDateTime() }
                     catch (_: Exception) { LocalDateTime.parse(delivered.replace(" ", "T")) }
            ChronoUnit.DAYS.between(dt, LocalDateTime.now()) <= 30
        }.getOrDefault(true)
    }
    val canReviewProducts = status == "DELIVERED" && within30Days
    // Complaint: per-item if not reviewed AND not already complained; whole-order slot if no whole-order complaint yet
    val availableComplaintItems = order.items.filter { it.id !in complainedItemIds && !it.hasReviewed }
    val canComplaint = status == "DELIVERED" && availableComplaintItems.isNotEmpty()
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
            item { ItemsCard(order.items, canReviewProducts, complaintsForItems, onOpenProduct) }
            val firstUnreviewed = if (canReviewProducts) order.items.firstOrNull { !it.hasReviewed && it.id !in complainedItemIds } else null
            if (firstUnreviewed != null) {
                item {
                    ReviewPromptCard {
                        onOpenProduct(firstUnreviewed.productId, true, firstUnreviewed.orderId, firstUnreviewed.id)
                    }
                }
            }
            item { PaymentCard(order) }
            item { SummaryCard(order) }
            if (!order.note.isNullOrBlank() || !order.cancelReason.isNullOrBlank()) {
                item { NoteCard(order) }
            }
            item {
                ActionCard(
                    canCancel = canCancel,
                    canConfirmReceived = canConfirmReceived,
                    canComplaint = canComplaint,
                    canResumePayment = canResume,
                    allComplaintsForOrder = allComplaintsForOrder,
                    onRetry = onRetry,
                    onCancelOrder = onCancelOrder,
                    onConfirmReceived = onConfirmReceived,
                    onOpenComplaint = onOpenComplaint,
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
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
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
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 2.dp
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier.size(34.dp).clip(RoundedCornerShape(10.dp)).background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                }
                Text(title, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface, fontSize = 15.sp)
            }
            Spacer(Modifier.height(14.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(Modifier.height(14.dp))
            content()
        }
    }
}

// ─── Cards ────────────────────────────────────────────────────────────────────
@Composable
private fun HeaderCard(order: OrderDto) {
    val (statusLabel, statusColor) = orderStatusPresentation(order.status, order.paymentStatus)
    val (paymentLabel, paymentColor) = paymentStatusPresentation(order.paymentStatus)

    SectionCard(title = "Thông tin đơn hàng", icon = Icons.Outlined.Receipt) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(order.orderCode, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.height(3.dp))
                Text("Đặt lúc ${formatDateTime(order.createdAt)}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Outlined.Info, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                    Text(msg, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
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
            order.deliveredAt?.takeIf { it.isNotBlank() }?.let { DetailRow("Đã giao lúc", formatUtcToVnDateTime(it)) }
            order.cancelledAt?.takeIf { it.isNotBlank() }?.let { DetailRow("Đã hủy lúc", formatUtcToVnDateTime(it)) }
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
                modifier = Modifier.size(42.dp).clip(RoundedCornerShape(12.dp)).background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.LocationOn, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(22.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(address.recipientName.orEmpty(), fontWeight = FontWeight.Bold, fontSize = 15.sp, color = MaterialTheme.colorScheme.onSurface)
                Spacer(Modifier.height(2.dp))
                Text(address.recipientPhone.orEmpty(), fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(4.dp))
                Text(
                    text = (address.fullAddress?.takeIf(String::isNotBlank) ?: address.address),
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 19.sp
                )
            }
        }
    }
}

@Composable
private fun ItemsCard(
    items: List<OrderItemDto>,
    canReviewProducts: Boolean,
    complaintsForItems: Map<String, String>,
    onOpenProduct: (productId: String, openReview: Boolean, orderId: String?, orderItemId: String?) -> Unit
) {
    val closedStatuses = setOf("RESOLVED", "REJECTED", "CANCELLED")
    SectionCard(title = "Sản phẩm đã đặt (${items.size})", icon = Icons.Outlined.MedicalServices) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            items.forEachIndexed { index, item ->
                val lineTotal = item.totalPrice ?: (item.price * item.quantity)
                val complaintStatus = complaintsForItems[item.id]
                val hasActiveComplaint = complaintStatus != null && complaintStatus !in closedStatuses
                val hasClosedComplaint = complaintStatus != null && complaintStatus in closedStatuses
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
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(Modifier.height(3.dp))
                            Text(
                                "${item.quantity} × ${item.unit}  •  ${formatCurrency(item.price)}",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            item.product?.brand?.takeIf { it.isNotBlank() }?.let { brand ->
                                Spacer(Modifier.height(2.dp))
                                Surface(shape = RoundedCornerShape(4.dp), color = MaterialTheme.colorScheme.surfaceVariant) {
                                    Text(brand, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                }
                            }
                            // Per-item complaint badge
                            if (hasActiveComplaint || hasClosedComplaint) {
                                Spacer(Modifier.height(4.dp))
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = if (hasActiveComplaint) Color(0xFFEFF6FF) else MaterialTheme.colorScheme.surfaceVariant
                                ) {
                                    Text(
                                        if (hasActiveComplaint) "Đang khiếu nại" else "Khiếu nại đã xử lý",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (hasActiveComplaint) Color(0xFF2563EB) else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                                    )
                                }
                            }
                        }
                        Spacer(Modifier.width(12.dp))
                        Text(formatCurrency(lineTotal), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, fontSize = 14.sp)
                    }
                    // Nút đánh giá — chỉ hiện nếu item chưa có khiếu nại
                    if (canReviewProducts && !item.hasReviewed && complaintStatus == null) {
                        Spacer(Modifier.height(10.dp))
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFFFFBEC),
                            onClick = { onOpenProduct(item.productId, true, item.orderId, item.id) }
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
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
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
                Text("Trạng thái", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(Modifier.height(2.dp))
            DetailRow("Tổng cộng", formatCurrency(order.total), valueColor = MaterialTheme.colorScheme.primary, emphasized = true)
        }
    }
}

@Composable
private fun NoteCard(order: OrderDto) {
    SectionCard(title = "Ghi chú", icon = Icons.Outlined.Notes) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            order.note?.takeIf { it.isNotBlank() }?.let {
                Text(it, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface, lineHeight = 20.sp)
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
    allComplaintsForOrder: List<ComplaintDto>,
    onRetry: () -> Unit,
    onCancelOrder: () -> Unit,
    onConfirmReceived: () -> Unit,
    onOpenComplaint: () -> Unit,
    onResumePayment: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text("Thao tác", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = MaterialTheme.colorScheme.onSurface)
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(Modifier.height(2.dp))

            if (canResumePayment) {
                ActionButton(
                    label = "Tiếp tục thanh toán",
                    icon = Icons.Outlined.Payments,
                    bg = MaterialTheme.colorScheme.primary,
                    fg = Color.White,
                    onClick = onResumePayment
                )
            }
            if (canConfirmReceived) {
                ActionButton(
                    label = "Đã nhận được hàng",
                    icon = Icons.Outlined.CheckCircle,
                    bg = MaterialTheme.colorScheme.primary,
                    fg = Color.White,
                    onClick = onConfirmReceived
                )
            }
            if (canComplaint) {
                ActionButton(
                    label = "Khiếu nại sản phẩm",
                    icon = Icons.Outlined.Flag,
                    bg = Color(0xFFEFF6FF),
                    fg = Color(0xFF2563EB),
                    outlined = true,
                    outlinedBorder = Color(0xFFBFDBFE),
                    onClick = onOpenComplaint
                )
            }
            val closedStatuses = setOf("RESOLVED", "REJECTED", "CANCELLED")
            allComplaintsForOrder.forEach { complaint ->
                val (statusLabel, statusColor) = complaintStatusPresentation(complaint.status)
                val isActive = complaint.status !in closedStatuses
                ComplaintInfoBanner(
                    title = if (isActive) "Đang có khiếu nại" else "Khiếu nại đã kết thúc",
                    subtitle = complaint.productName?.takeIf { it.isNotBlank() },
                    code = complaint.complaintCode,
                    statusLabel = statusLabel,
                    statusColor = statusColor,
                    containerColor = if (isActive) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                    iconTint = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (canCancel) {
                ActionButton(
                    label = "Hủy đơn hàng",
                    icon = Icons.Outlined.Cancel,
                    bg = MaterialTheme.colorScheme.errorContainer,
                    fg = MaterialTheme.colorScheme.error,
                    outlined = true,
                    outlinedBorder = MaterialTheme.colorScheme.errorContainer,
                    onClick = onCancelOrder
                )
            }
            ActionButton(
                label = "Tải lại đơn hàng",
                icon = Icons.Outlined.Refresh,
                bg = MaterialTheme.colorScheme.surfaceVariant,
                fg = MaterialTheme.colorScheme.onSurfaceVariant,
                onClick = onRetry
            )
        }
    }
}

private fun complaintStatusPresentation(status: String): Pair<String, Color> = when (status.uppercase()) {
    "OPEN" -> "Mới" to Color(0xFFDC2626)
    "IN_REVIEW" -> "Đang xử lý" to Color(0xFF2563EB)
    "NEED_MORE_INFO" -> "Cần bổ sung" to Color(0xFF7C3AED)
    "APPROVED" -> "Đã duyệt" to Color(0xFF2E7D32)
    "RESOLVED" -> "Đã giải quyết" to Color(0xFF2E7D32)
    "REJECTED" -> "Từ chối" to Color(0xFFDC2626)
    "CANCELLED" -> "Đã hủy" to Color(0xFF6B7280)
    else -> status to Color(0xFF6B7280)
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

@Composable
private fun ComplaintInfoBanner(
    title: String,
    code: String,
    statusLabel: String,
    statusColor: Color,
    containerColor: Color,
    iconTint: Color,
    subtitle: String? = null
) {
    Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp), color = containerColor) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier.size(36.dp).clip(RoundedCornerShape(10.dp))
                    .background(iconTint.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Outlined.Flag, null, tint = iconTint, modifier = Modifier.size(18.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = iconTint)
                subtitle?.let {
                    Text(it, fontSize = 11.sp, color = iconTint.copy(alpha = 0.75f), maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(code, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Surface(shape = RoundedCornerShape(20.dp), color = statusColor.copy(alpha = 0.15f)) {
                        Text(
                            statusLabel,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = statusColor,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
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
    Surface(color = MaterialTheme.colorScheme.surfaceVariant, shape = RoundedCornerShape(999.dp)) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(14.dp))
            Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun DetailRow(
    label: String,
    value: String,
    emphasized: Boolean = false,
    valueColor: Color = MaterialTheme.colorScheme.onSurface
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
            color = MaterialTheme.colorScheme.onSurfaceVariant,
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
        containerColor = MaterialTheme.colorScheme.surface,
        icon = {
            Box(
                modifier = Modifier.size(52.dp).clip(CircleShape).background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = iconTint, modifier = Modifier.size(28.dp))
            }
        },
        title = { Text(title, fontWeight = FontWeight.Bold, fontSize = 17.sp, color = MaterialTheme.colorScheme.onSurface) },
        text = { Text(body, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, lineHeight = 21.sp) },
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
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onSurface)
            ) { Text(dismissLabel) }
        }
    )
}

@Composable
private fun ErrorState(message: String, onRetry: () -> Unit, modifier: Modifier = Modifier) {
    Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(
                modifier = Modifier.size(72.dp).clip(CircleShape).background(MaterialTheme.colorScheme.errorContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Outlined.WarningAmber, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(36.dp))
            }
            Text("Không tải được đơn hàng", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
            Text(message, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                shape = RoundedCornerShape(12.dp),
                elevation = ButtonDefaults.buttonElevation(0.dp)
            ) { Text("Thử lại") }
        }
    }
}

// ─── Batch review dialog ──────────────────────────────────────────────────────
@Composable
private fun BatchReviewDialog(
    items: List<OrderItemDto>,
    isSubmitting: Boolean,
    errorMessage: String?,
    onSubmit: (
        ratings: Map<String, Int>,
        titles: Map<String, String>,
        comments: Map<String, String>,
        attachmentUris: Map<String, List<Uri>>
    ) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val ratings = remember { mutableStateMapOf<String, Int>() }
    val titles = remember { mutableStateMapOf<String, String>() }
    val comments = remember { mutableStateMapOf<String, String>() }
    val attachments = remember { mutableStateMapOf<String, List<PickedComplaintAttachment>>() }
    var currentPickingItemId by remember { mutableStateOf<String?>(null) }
    val ratedCount = items.count { (ratings[it.id] ?: 0) > 0 }

    val filePicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenMultipleDocuments()) { uris ->
        val itemId = currentPickingItemId ?: return@rememberLauncherForActivityResult
        val existing = attachments[itemId].orEmpty()
        val picked = uris.map { uri ->
            val name = context.displayName(uri).ifBlank { "file_dinh_kem" }
            PickedComplaintAttachment(uri, name, detectComplaintFileType(context.contentResolver.getType(uri), name))
        }
        attachments[itemId] = (existing + picked).distinctBy { it.uri }.take(5)
        currentPickingItemId = null
    }

    Dialog(
        onDismissRequest = { if (!isSubmitting) onDismiss() },
        properties = DialogProperties(usePlatformDefaultWidth = false, dismissOnBackPress = !isSubmitting)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column {
                // Header
                Row(
                    modifier = Modifier.padding(start = 20.dp, top = 20.dp, end = 20.dp, bottom = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier.size(40.dp).clip(RoundedCornerShape(12.dp)).background(Color(0xFFFFE57F)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Filled.Star, null, tint = Color(0xFFFF8F00), modifier = Modifier.size(22.dp))
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Đánh giá sản phẩm", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
                        Text("Đánh giá 5 ⭐ → +200 điểm mỗi sản phẩm", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                // Items list
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 500.dp)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    for (index in items.indices) {
                        val item = items[index]
                        if (index > 0) {
                            HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = MaterialTheme.colorScheme.outlineVariant)
                        }
                        BatchReviewItemRow(
                            item = item,
                            rating = ratings[item.id] ?: 0,
                            title = titles[item.id] ?: "",
                            comment = comments[item.id] ?: "",
                            itemAttachments = attachments[item.id].orEmpty(),
                            isSubmitting = isSubmitting,
                            onRatingChange = { ratings[item.id] = it },
                            onTitleChange = { titles[item.id] = it },
                            onCommentChange = { comments[item.id] = it },
                            onAddFiles = { currentPickingItemId = item.id; filePicker.launch(arrayOf("image/*", "application/pdf")) },
                            onRemoveFile = { uri ->
                                attachments[item.id] = attachments[item.id].orEmpty().filterNot { it.uri == uri }
                            }
                        )
                    }
                }

                // Error
                if (!errorMessage.isNullOrBlank()) {
                    Surface(
                        color = MaterialTheme.colorScheme.errorContainer,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.padding(horizontal = 20.dp).fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Outlined.ErrorOutline, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(14.dp))
                            Text(errorMessage, color = MaterialTheme.colorScheme.onErrorContainer, fontSize = 12.sp)
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                }

                // Action buttons
                HorizontalDivider()
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 20.dp, end = 20.dp, bottom = 20.dp, top = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        enabled = !isSubmitting,
                        modifier = Modifier.weight(1f).height(46.dp),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) { Text("Để sau") }

                    Button(
                        onClick = {
                            onSubmit(
                                ratings.toMap(),
                                titles.toMap(),
                                comments.toMap(),
                                attachments.mapValues { (_, list) -> list.map { it.uri } }
                            )
                        },
                        enabled = !isSubmitting && ratedCount > 0,
                        modifier = Modifier.weight(2f).height(46.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        elevation = ButtonDefaults.buttonElevation(0.dp)
                    ) {
                        if (isSubmitting) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                        } else {
                            Text(
                                if (ratedCount > 0) "Gửi $ratedCount đánh giá" else "Chọn số sao để gửi",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BatchReviewItemRow(
    item: OrderItemDto,
    rating: Int,
    title: String,
    comment: String,
    itemAttachments: List<PickedComplaintAttachment>,
    isSubmitting: Boolean,
    onRatingChange: (Int) -> Unit,
    onTitleChange: (String) -> Unit,
    onCommentChange: (String) -> Unit,
    onAddFiles: () -> Unit,
    onRemoveFile: (Uri) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        // Product name
        Text(
            item.name,
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
        // Stars
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            (1..5).forEach { star ->
                Icon(
                    imageVector = if (star <= rating) Icons.Filled.Star else Icons.Outlined.Star,
                    contentDescription = null,
                    tint = if (star <= rating) Color(0xFFFFAB00) else MaterialTheme.colorScheme.outlineVariant,
                    modifier = Modifier
                        .size(34.dp)
                        .clickable(enabled = !isSubmitting) { onRatingChange(star) }
                )
            }
            if (rating > 0) {
                Spacer(Modifier.width(4.dp))
                Text(
                    text = when (rating) {
                        1 -> "Tệ"; 2 -> "Không tốt"; 3 -> "Bình thường"; 4 -> "Tốt"; else -> "Tuyệt vời ⭐"
                    },
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium
                )
            }
        }
        // Bonus hint
        if (rating == 5) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                color = Color(0xFFFFFBEB)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text("⭐", fontSize = 12.sp)
                    Text(
                        "+200 điểm thưởng khi đánh giá 5 sao",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF92400E)
                    )
                }
            }
        }
        // Title
        OutlinedTextField(
            value = title,
            onValueChange = { if (!isSubmitting) onTitleChange(it) },
            placeholder = { Text("Tiêu đề (tùy chọn)", fontSize = 12.sp) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp),
            enabled = !isSubmitting,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
            )
        )
        // Comment
        OutlinedTextField(
            value = comment,
            onValueChange = { if (!isSubmitting) onCommentChange(it) },
            placeholder = { Text("Nhận xét của bạn (tùy chọn)", fontSize = 12.sp) },
            modifier = Modifier.fillMaxWidth(),
            minLines = 2,
            maxLines = 3,
            shape = RoundedCornerShape(10.dp),
            enabled = !isSubmitting,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
            )
        )
        // File picker button
        OutlinedButton(
            onClick = onAddFiles,
            enabled = !isSubmitting && itemAttachments.size < 5,
            modifier = Modifier.fillMaxWidth().height(40.dp),
            shape = RoundedCornerShape(10.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary),
            contentPadding = PaddingValues(horizontal = 12.dp)
        ) {
            Icon(Icons.Outlined.AttachFile, null, modifier = Modifier.size(14.dp))
            Spacer(Modifier.width(6.dp))
            Text("Thêm ảnh / PDF  (${itemAttachments.size}/5)", fontSize = 12.sp)
        }
        // File list
        if (itemAttachments.isNotEmpty()) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                itemAttachments.forEach { att ->
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    if (att.fileType == "IMAGE") Icons.Outlined.Image else Icons.Outlined.PictureAsPdf,
                                    null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(att.name, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            }
                            TextButton(
                                onClick = { onRemoveFile(att.uri) },
                                enabled = !isSubmitting,
                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text("Xóa", color = Color(0xFFE53935), fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }
        }
    }
}

// ─── Complaint dialog ─────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ComplaintDialog(
    order: OrderDto,
    availableItems: List<OrderItemDto>,
    hasWholeOrderComplaint: Boolean,
    state: UiState<ComplaintDto>,
    onDismiss: () -> Unit,
    onSubmit: (String, String, String, List<Uri>, String?, String?) -> Unit
) {
    val context = LocalContext.current
    val options = listOf(
        "DAMAGED" to "Hàng hỏng/vỡ",
        "WRONG_ITEM" to "Giao sai hàng",
        "MISSING_ITEM" to "Thiếu sản phẩm",
        "COUNTERFEIT" to "Nghi hàng giả",
        "EXPIRED" to "Hết hạn",
        "PAYMENT" to "Thanh toán",
        "REFUND" to "Yêu cầu hoàn tiền",
        "OTHER" to "Khác"
    )
    var selectedType by rememberSaveable { mutableStateOf(options.first().first) }
    var typeExpanded by remember { mutableStateOf(false) }
    var selectedItemId by rememberSaveable { mutableStateOf<String?>(null) }
    val selectedItem = availableItems.firstOrNull { it.id == selectedItemId }
    var title by rememberSaveable { mutableStateOf("") }
    var description by rememberSaveable { mutableStateOf("") }
    var attachments by remember { mutableStateOf<List<PickedComplaintAttachment>>(emptyList()) }
    val isSubmitting = state is UiState.Loading
    val mustSelectItem = hasWholeOrderComplaint && selectedItemId == null
    val filePicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenMultipleDocuments()) { uris ->
        val picked = uris.map { uri ->
            val name = context.displayName(uri).ifBlank { "file_dinh_kem" }
            PickedComplaintAttachment(uri, name, detectComplaintFileType(context.contentResolver.getType(uri), name))
        }
        attachments = (attachments + picked).distinctBy { it.uri }.take(5)
    }

    val daysRemaining: Long? = remember(order.deliveredAt) {
        order.deliveredAt?.let { raw ->
            runCatching {
                val delivered = when {
                    raw.contains('T') -> LocalDateTime.parse(raw.substringBefore('.')).toLocalDate()
                    else -> LocalDate.parse(raw.take(10))
                }
                30L - ChronoUnit.DAYS.between(delivered, LocalDate.now())
            }.getOrNull()
        }
    }

    Dialog(
        onDismissRequest = { if (!isSubmitting) onDismiss() },
        properties = DialogProperties(usePlatformDefaultWidth = false, dismissOnBackPress = !isSubmitting)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column {
                // ── Header ────────────────────────────────────────────────────────
                Row(
                    modifier = Modifier.padding(start = 20.dp, top = 20.dp, end = 20.dp, bottom = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.errorContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Outlined.Flag, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(22.dp))
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Khiếu nại đơn #${order.orderCode}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            "Trong vòng 30 ngày sau khi nhận hàng",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                // ── Scrollable body ───────────────────────────────────────────────
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Days remaining banner
                    if (daysRemaining != null) {
                        when {
                            daysRemaining < 0 -> Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.errorContainer
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(10.dp),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Outlined.Block, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                                    Text(
                                        "Đã hết thời hạn khiếu nại (30 ngày sau khi giao hàng)",
                                        fontSize = 12.sp, color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                            daysRemaining <= 7 -> Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color(0xFFFFF3E0)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(10.dp),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Outlined.Warning, null, tint = Color(0xFFE65100), modifier = Modifier.size(16.dp))
                                    Text(
                                        "Còn $daysRemaining ngày để khiếu nại",
                                        fontSize = 12.sp, color = Color(0xFFE65100), fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                            else -> {}
                        }
                    }

                    // Item picker
                    Text("Sản phẩm bị lỗi", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        if (!hasWholeOrderComplaint) {
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                color = if (selectedItemId == null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                onClick = { selectedItemId = null }
                            ) {
                                Text(
                                    "Không liên quan đến sản phẩm cụ thể",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = if (selectedItemId == null) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)
                                )
                            }
                        }
                        availableItems.forEach { item ->
                            val isSelected = selectedItemId == item.id
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                onClick = { selectedItemId = if (isSelected) null else item.id }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            item.name,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            "×${item.quantity} ${item.unit}  •  ${formatCurrency(item.price)}",
                                            fontSize = 11.sp,
                                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.75f)
                                                    else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    if (isSelected) {
                                        Icon(
                                            Icons.Default.Check,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onPrimary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Complaint type dropdown
                    val selectedLabel = options.find { it.first == selectedType }?.second ?: ""
                    ExposedDropdownMenuBox(
                        expanded = typeExpanded,
                        onExpandedChange = { typeExpanded = !typeExpanded }
                    ) {
                        OutlinedTextField(
                            value = selectedLabel,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Loại khiếu nại") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = typeExpanded) },
                            modifier = Modifier.fillMaxWidth().menuAnchor(),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                                focusedLabelColor = MaterialTheme.colorScheme.primary,
                                unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                            )
                        )
                        ExposedDropdownMenu(
                            expanded = typeExpanded,
                            onDismissRequest = { typeExpanded = false }
                        ) {
                            options.forEach { (value, label) ->
                                DropdownMenuItem(
                                    text = { Text(label, fontSize = 14.sp) },
                                    onClick = { selectedType = value; typeExpanded = false },
                                    trailingIcon = if (selectedType == value) ({
                                        Icon(
                                            Icons.Default.Check,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }) else null
                                )
                            }
                        }
                    }

                    // Title
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Tiêu đề") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                            focusedLabelColor = MaterialTheme.colorScheme.primary,
                            unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                            cursorColor = MaterialTheme.colorScheme.primary,
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    )

                    // Description
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Mô tả chi tiết") },
                        minLines = 3,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                            focusedLabelColor = MaterialTheme.colorScheme.primary,
                            unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                            cursorColor = MaterialTheme.colorScheme.primary,
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    )

                    // File picker
                    OutlinedButton(
                        onClick = { filePicker.launch(arrayOf("image/*", "application/pdf")) },
                        enabled = !isSubmitting && attachments.size < 5,
                        modifier = Modifier.fillMaxWidth().height(44.dp),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (attachments.size < 5) MaterialTheme.colorScheme.outlineVariant
                            else MaterialTheme.colorScheme.surfaceVariant
                        ),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(Icons.Outlined.AttachFile, null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(
                            if (attachments.size < 5) "Thêm ảnh / PDF  (${attachments.size}/5)"
                            else "Đã đạt giới hạn 5 file",
                            fontSize = 13.sp
                        )
                    }
                    // File list
                    if (attachments.isNotEmpty()) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            attachments.forEach { att ->
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 6.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Icon(
                                                if (att.fileType == "IMAGE") Icons.Outlined.Image else Icons.Outlined.PictureAsPdf,
                                                null,
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Text(att.name, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                        }
                                        TextButton(
                                            onClick = { attachments = attachments.filterNot { it.uri == att.uri } },
                                            enabled = !isSubmitting,
                                            contentPadding = PaddingValues(horizontal = 8.dp)
                                        ) {
                                            Text("Xóa", fontSize = 11.sp, color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.SemiBold)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Error messages
                    if (mustSelectItem) {
                        Surface(shape = RoundedCornerShape(8.dp), color = MaterialTheme.colorScheme.errorContainer) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(10.dp),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Outlined.Info, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(14.dp))
                                Text(
                                    "Đã có khiếu nại chung cho đơn này. Vui lòng chọn sản phẩm cụ thể.",
                                    fontSize = 12.sp, color = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                    if (state is UiState.Error) {
                        Surface(shape = RoundedCornerShape(8.dp), color = MaterialTheme.colorScheme.errorContainer) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(10.dp),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Outlined.ErrorOutline, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(14.dp))
                                Text(state.message, fontSize = 12.sp, color = MaterialTheme.colorScheme.onErrorContainer)
                            }
                        }
                    }
                }

                // ── Action buttons (pinned at bottom) ─────────────────────────────
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 20.dp, end = 20.dp, bottom = 20.dp, top = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = { if (!isSubmitting) onDismiss() },
                        enabled = !isSubmitting,
                        modifier = Modifier.weight(1f).height(46.dp),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) { Text("Đóng") }
                    Button(
                        onClick = {
                            onSubmit(selectedType, title, description, attachments.map { it.uri }, selectedItem?.id, selectedItem?.productId)
                        },
                        enabled = !isSubmitting && title.isNotBlank() && description.isNotBlank()
                            && (daysRemaining == null || daysRemaining >= 0) && !mustSelectItem,
                        modifier = Modifier.weight(2f).height(46.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        elevation = ButtonDefaults.buttonElevation(0.dp)
                    ) {
                        if (isSubmitting) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                        } else {
                            Text("Gửi khiếu nại", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                    }
                }
            }
        }
    }
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

private fun orderStatusPresentation(status: String, paymentStatus: String = ""): Pair<String, Color> {
    val refunded = paymentStatus.uppercase() in setOf("REFUNDED", "PARTIALLY_REFUNDED", "REFUND_PROCESSING")
    return when (status.uppercase()) {
        "PENDING" -> "Chờ xác nhận" to Color(0xFFF59E0B)
        "PROCESSING" -> "Đang xử lý" to Color(0xFF2563EB)
        "SHIPPING" -> "Đang giao" to Color(0xFF7C3AED)
        "DELIVERED" -> if (refunded) "Hoàn tiền" to Color(0xFF0277BD) else "Đã giao" to Color(0xFF2E7D32)
        "CANCELLED" -> "Đã hủy" to Color(0xFFDC2626)
        "RETURNED" -> "Trả hàng" to Color(0xFF7B1FA2)
        else -> status to Color(0xFF6B7280)
    }
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
        ps == "COMPLETED" && os == "PROCESSING" -> "Đã thanh toán, đơn đang được Medstore xử lý."
        ps == "COMPLETED" && os == "PENDING" -> "Đã thanh toán, đơn đang chờ Medstore xác nhận."
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

private fun formatDateTime(raw: String): String = formatVnDateTime(raw)