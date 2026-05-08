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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.AttachFile
import androidx.compose.material.icons.outlined.SupportAgent
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.nhathuoc.data.model.ComplaintDto
import com.example.nhathuoc.data.model.UiState
import com.example.nhathuoc.ui.theme.BgColor
import com.example.nhathuoc.ui.theme.GreenTop
import com.example.nhathuoc.viewmodel.OrderViewModel
import java.text.NumberFormat
import java.util.Locale

private data class ComplaintStatusFilter(
    val value: String?,
    val label: String
)

private val complaintFilters = listOf(
    ComplaintStatusFilter(null, "Tất cả"),
    ComplaintStatusFilter("OPEN", "Mới tạo"),
    ComplaintStatusFilter("IN_REVIEW", "Đang xử lý"),
    ComplaintStatusFilter("NEED_MORE_INFO", "Cần bổ sung"),
    ComplaintStatusFilter("APPROVED", "Đã duyệt"),
    ComplaintStatusFilter("RESOLVED", "Đã giải quyết"),
    ComplaintStatusFilter("REJECTED", "Từ chối"),
    ComplaintStatusFilter("CANCELLED", "Đã hủy")
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

    LaunchedEffect(Unit) {
        viewModel.getComplaints()
    }

    Scaffold(
        containerColor = BgColor,
        topBar = {
            TopAppBar(
                title = { Text("Khiếu nại của tôi", fontWeight = FontWeight.Bold) },
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
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            ComplaintFilterRow(
                selectedStatus = selectedStatus,
                onStatusSelected = { selectedStatus = it }
            )

            when (val current = state) {
                is UiState.Loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = GreenTop)
                }
                is UiState.Error -> ComplaintEmptyState(
                    title = "Không tải được khiếu nại",
                    subtitle = current.message
                )
                is UiState.Success -> {
                    val complaints = current.data.filter { complaint ->
                        selectedStatus == null || complaint.status.equals(selectedStatus, ignoreCase = true)
                    }
                    if (complaints.isEmpty()) {
                        ComplaintEmptyState(
                            title = if (selectedStatus == null) "Bạn chưa có khiếu nại nào" else "Không có khiếu nại ở trạng thái này",
                            subtitle = "Bạn có thể tạo khiếu nại từ màn chi tiết đơn hàng sau khi đơn đã thanh toán hoặc đang được xử lý."
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(complaints, key = { it.id }) { complaint ->
                                ComplaintCard(
                                    complaint = complaint,
                                    onClick = {
                                        navController?.navigate("ComplaintDetailScreen/${complaint.id}")
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
private fun ComplaintFilterRow(
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
        complaintFilters.forEach { filter ->
            val selected = filter.value == selectedStatus
            Surface(
                color = if (selected) GreenTop else Color.White,
                contentColor = if (selected) Color.White else GreenTop,
                shape = RoundedCornerShape(999.dp),
                shadowElevation = if (selected) 0.dp else 1.dp,
                modifier = Modifier.clickable { onStatusSelected(filter.value) }
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
private fun ComplaintCard(
    complaint: ComplaintDto,
    onClick: () -> Unit
) {
    Surface(
        color = Color.White,
        shape = RoundedCornerShape(18.dp),
        shadowElevation = 2.dp,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    text = complaint.complaintCode,
                    fontWeight = FontWeight.Bold,
                    color = GreenTop,
                    fontSize = 16.sp
                )
                Text(
                    text = complaintStatusLabel(complaint.status),
                    color = complaintStatusColor(complaint.status),
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp
                )
            }
            Text(
                text = complaint.title,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "Loại: ${complaintTypeLabel(complaint.type)}",
                color = Color(0xFF4B5563),
                fontSize = 13.sp
            )
            Text(
                text = complaint.description,
                color = Color(0xFF6B7280),
                fontSize = 13.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.AutoMirrored.Outlined.ReceiptLong, contentDescription = null, tint = GreenTop, modifier = Modifier.size(16.dp))
                Text("Đơn ${complaint.orderId.takeLast(8)}", color = Color(0xFF4B5563), fontSize = 12.sp)
                Icon(Icons.Outlined.AttachFile, contentDescription = null, tint = GreenTop, modifier = Modifier.size(16.dp))
                Text("${complaint.attachments.size} file", color = Color(0xFF4B5563), fontSize = 12.sp)
            }
            complaint.resolution?.takeIf { it.isNotBlank() }?.let {
                Surface(color = Color(0xFFEFF6FF), shape = RoundedCornerShape(12.dp)) {
                    Text(
                        text = "Phản hồi: $it",
                        color = Color(0xFF1D4ED8),
                        fontSize = 12.sp,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                    )
                }
            }
            if (complaint.refundStatus != "NONE" || complaint.refundAmount != null) {
                Surface(color = Color(0xFFFFFBEB), shape = RoundedCornerShape(12.dp)) {
                    Text(
                        text = buildString {
                            append("Hoàn tiền: ${refundStatusLabel(complaint.refundStatus)}")
                            complaint.refundAmount?.let { append(" • ${formatMoney(it)}") }
                        },
                        color = Color(0xFF92400E),
                        fontSize = 12.sp,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ComplaintEmptyState(
    title: String,
    subtitle: String
) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(24.dp)) {
            Icon(Icons.Outlined.SupportAgent, contentDescription = null, tint = Color(0xFFBDBDBD), modifier = Modifier.size(56.dp))
            Spacer(Modifier.height(14.dp))
            Text(title, fontWeight = FontWeight.Bold, fontSize = 17.sp, color = Color(0xFF2D2D2D))
            Spacer(Modifier.height(6.dp))
            Text(subtitle, color = Color.Gray, fontSize = 13.sp, lineHeight = 18.sp)
        }
    }
}

private fun complaintStatusLabel(status: String): String {
    return when (status.uppercase()) {
        "OPEN" -> "Mới tạo"
        "IN_REVIEW" -> "Đang xử lý"
        "NEED_MORE_INFO" -> "Cần bổ sung"
        "APPROVED" -> "Đã duyệt"
        "REJECTED" -> "Từ chối"
        "RESOLVED" -> "Đã giải quyết"
        "CANCELLED" -> "Đã hủy"
        else -> status
    }
}

private fun complaintStatusColor(status: String): Color {
    return when (status.uppercase()) {
        "OPEN" -> Color(0xFF2563EB)
        "IN_REVIEW", "NEED_MORE_INFO" -> Color(0xFFB45309)
        "APPROVED", "RESOLVED" -> GreenTop
        "REJECTED", "CANCELLED" -> Color(0xFFE53935)
        else -> Color.Gray
    }
}

private fun complaintTypeLabel(type: String): String {
    return when (type.uppercase()) {
        "MISSING_ITEM" -> "Thiếu sản phẩm"
        "WRONG_ITEM" -> "Giao sai hàng"
        "DAMAGED" -> "Hàng hỏng/vỡ"
        "COUNTERFEIT" -> "Nghi hàng giả"
        "EXPIRED" -> "Hết hạn"
        "PAYMENT" -> "Thanh toán"
        "REFUND" -> "Hoàn tiền"
        else -> "Khác"
    }
}

private fun refundStatusLabel(status: String): String {
    return when (status.uppercase()) {
        "REQUESTED" -> "Chờ duyệt"
        "APPROVED" -> "Đã duyệt"
        "REFUNDED" -> "Đã hoàn"
        "REJECTED" -> "Từ chối"
        else -> "Không hoàn"
    }
}

private fun formatMoney(value: Double): String {
    return NumberFormat.getCurrencyInstance(Locale("vi", "VN")).format(value)
}
