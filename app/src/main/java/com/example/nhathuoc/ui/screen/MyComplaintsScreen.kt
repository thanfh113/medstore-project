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
import androidx.compose.material.icons.automirrored.outlined.ReceiptLong
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
import com.example.nhathuoc.data.model.ComplaintDto
import com.example.nhathuoc.data.model.UiState
import com.example.nhathuoc.ui.theme.GreenTop
import com.example.nhathuoc.viewmodel.OrderViewModel
import java.text.NumberFormat
import java.util.Locale


private data class ComplaintStatusFilter(val value: String?, val label: String)

private val complaintFilters = listOf(
    ComplaintStatusFilter(null,             "Tất cả"),
    ComplaintStatusFilter("OPEN",           "Mới tạo"),
    ComplaintStatusFilter("IN_REVIEW",      "Đang xử lý"),
    ComplaintStatusFilter("NEED_MORE_INFO", "Cần bổ sung"),
    ComplaintStatusFilter("APPROVED",       "Đã duyệt"),
    ComplaintStatusFilter("RESOLVED",       "Đã giải quyết"),
    ComplaintStatusFilter("REJECTED",       "Từ chối"),
    ComplaintStatusFilter("CANCELLED",      "Đã hủy")
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyComplaintsScreen(
    onBack: () -> Unit = {},
    navController: NavController? = null,
    viewModel: OrderViewModel = hiltViewModel()
) {
    val state by viewModel.complaintsListState.collectAsState()
    var selectedStatus by rememberSaveable { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) { viewModel.getComplaints() }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            com.example.nhathuoc.ui.component.GreenAppTopBar(
                title = "Khiếu nại của tôi",
                subtitle = "Theo dõi trạng thái xử lý",
                onBack = onBack
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // ── Filter bar ────────────────────────────────────────────────────
            Surface(color = MaterialTheme.colorScheme.surface, shadowElevation = 1.dp) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    complaintFilters.forEach { filter ->
                        val isSelected = filter.value == selectedStatus
                        Surface(
                            color = if (isSelected) GreenTop else MaterialTheme.colorScheme.surface,
                            contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                            shape = RoundedCornerShape(999.dp),
                            shadowElevation = if (isSelected) 0.dp else 1.dp,
                            modifier = Modifier.clickable { selectedStatus = filter.value }
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

            // ── Content ───────────────────────────────────────────────────────
            when (val current = state) {
                is UiState.Loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }

                is UiState.Error -> ComplaintEmptyState(
                    iconTint = MaterialTheme.colorScheme.error,
                    iconBg = MaterialTheme.colorScheme.errorContainer,
                    title = "Không tải được khiếu nại",
                    subtitle = current.message
                )

                is UiState.Success -> {
                    val complaints = current.data.filter { c ->
                        selectedStatus == null || c.status.equals(selectedStatus, ignoreCase = true)
                    }
                    if (complaints.isEmpty()) {
                        ComplaintEmptyState(
                            iconTint = GreenTop,
                            iconBg = MaterialTheme.colorScheme.primaryContainer,
                            title = if (selectedStatus == null) "Chưa có khiếu nại nào" else "Không có khiếu nại ở trạng thái này",
                            subtitle = "Bạn có thể tạo khiếu nại từ trang chi tiết đơn hàng sau khi đơn đã được xử lý.",
                            onGoToOrders = if (selectedStatus == null) ({ navController?.navigate("MyOrdersScreen") }) else null
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(complaints, key = { it.id }) { complaint ->
                                ComplaintCard(
                                    complaint = complaint,
                                    onClick = { navController?.navigate("ComplaintDetailScreen/${complaint.id}") }
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

// ─── Complaint Card ───────────────────────────────────────────────────────────
@Composable
private fun ComplaintCard(complaint: ComplaintDto, onClick: () -> Unit) {
    val (statusLabel, statusBg, statusFg) = complaintStatusAppearance(complaint.status)

    Surface(
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(20.dp),
        shadowElevation = 2.dp,
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            // ── Top: code + status badge ──────────────────────────────────────
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
                            .size(40.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Outlined.Flag,
                            contentDescription = null,
                            tint = GreenTop,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Column {
                        Text(
                            complaint.complaintCode,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = GreenTop
                        )
                        Text(
                            complaintTypeLabel(complaint.type),
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Surface(shape = RoundedCornerShape(20.dp), color = statusBg) {
                    Text(
                        statusLabel,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = statusFg,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            // ── Title + description ───────────────────────────────────────────
            Text(
                complaint.title,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(4.dp))
            Text(
                complaint.description,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 18.sp
            )

            Spacer(Modifier.height(10.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(Modifier.height(10.dp))

            // ── Meta row: order + attachments ─────────────────────────────────
            Row(
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                MetaPill(
                    icon = Icons.AutoMirrored.Outlined.ReceiptLong,
                    text = "Đơn …${complaint.orderId.takeLast(8)}"
                )
                if (complaint.attachments.isNotEmpty()) {
                    MetaPill(
                        icon = Icons.Outlined.AttachFile,
                        text = "${complaint.attachments.size} file đính kèm"
                    )
                }
            }

            // ── Resolution banner ─────────────────────────────────────────────
            complaint.resolution?.takeIf { it.isNotBlank() }?.let { res ->
                Spacer(Modifier.height(10.dp))
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFEFF6FF)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Outlined.SupportAgent, null, tint = Color(0xFF2563EB), modifier = Modifier.size(16.dp))
                        Text(
                            "Phản hồi: $res",
                            fontSize = 12.sp,
                            color = Color(0xFF1D4ED8),
                            lineHeight = 17.sp,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // ── Refund banner ─────────────────────────────────────────────────
            if (complaint.refundStatus != "NONE" || complaint.refundAmount != null) {
                Spacer(Modifier.height(8.dp))
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFFFFBEB)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Outlined.AccountBalanceWallet, null, tint = Color(0xFFD97706), modifier = Modifier.size(16.dp))
                        Text(
                            buildString {
                                append("Hoàn tiền: ${refundStatusLabel(complaint.refundStatus)}")
                                complaint.refundAmount?.let { append("  •  ${formatMoney(it)}") }
                            },
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF92400E)
                        )
                    }
                }
            }

            Spacer(Modifier.height(10.dp))

            // ── Footer: view detail ───────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Xem chi tiết", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = GreenTop)
                Icon(
                    Icons.AutoMirrored.Outlined.KeyboardArrowRight,
                    contentDescription = null,
                    tint = GreenTop,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
private fun MetaPill(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(14.dp))
        Text(text, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

// ─── Empty state ──────────────────────────────────────────────────────────────
@Composable
private fun ComplaintEmptyState(
    iconBg: Color,
    iconTint: Color,
    title: String,
    subtitle: String,
    onGoToOrders: (() -> Unit)? = null
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
                Icon(Icons.Outlined.SupportAgent, null, tint = iconTint, modifier = Modifier.size(42.dp))
            }
            Text(title, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
            Text(subtitle, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, lineHeight = 19.sp, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
            if (onGoToOrders != null) {
                Button(
                    onClick = onGoToOrders,
                    colors = ButtonDefaults.buttonColors(containerColor = GreenTop),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Text("Xem đơn hàng của tôi", fontSize = 14.sp)
                }
            }
        }
    }
}

// ─── Helpers ──────────────────────────────────────────────────────────────────
private fun complaintStatusAppearance(status: String): Triple<String, Color, Color> = when (status.uppercase()) {
    "OPEN"           -> Triple("Mới tạo",        Color(0xFFDBEAFE), Color(0xFF2563EB))
    "IN_REVIEW"      -> Triple("Đang xử lý",     Color(0xFFFEF3C7), Color(0xFFD97706))
    "NEED_MORE_INFO" -> Triple("Cần bổ sung",     Color(0xFFFFF3E0), Color(0xFFB45309))
    "APPROVED"       -> Triple("Đã duyệt",        Color(0xFFE8F5E9),        GreenTop)
    "RESOLVED"       -> Triple("Đã giải quyết",   Color(0xFFE8F5E9), Color(0xFF2E7D32))
    "REJECTED"       -> Triple("Từ chối",          Color(0xFFFFEBEE), Color(0xFFE53935))
    "CANCELLED"      -> Triple("Đã hủy",           Color(0xFFF3F4F6), Color(0xFF6B7280))
    else             -> Triple(status,              Color(0xFFF3F4F6), Color(0xFF6B7280))
}

private fun complaintTypeLabel(type: String) = when (type.uppercase()) {
    "MISSING_ITEM" -> "Thiếu sản phẩm"
    "WRONG_ITEM"   -> "Giao sai hàng"
    "DAMAGED"      -> "Hàng hỏng/vỡ"
    "COUNTERFEIT"  -> "Nghi hàng giả"
    "EXPIRED"      -> "Hết hạn"
    "PAYMENT"      -> "Thanh toán"
    "REFUND"       -> "Hoàn tiền"
    else           -> "Khác"
}

private fun refundStatusLabel(status: String) = when (status.uppercase()) {
    "REQUESTED" -> "Chờ duyệt"
    "APPROVED"  -> "Đã duyệt"
    "REFUNDED"  -> "Đã hoàn"
    "REJECTED"  -> "Từ chối"
    else        -> "Không hoàn"
}

private fun formatMoney(value: Double): String =
    NumberFormat.getCurrencyInstance(Locale("vi", "VN")).format(value)