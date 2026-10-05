package com.example.nhathuoc.ui.screen

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MoneyOff
import androidx.compose.material.icons.outlined.AttachFile
import androidx.compose.material.icons.outlined.SupportAgent
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.nhathuoc.data.model.ComplaintDto
import com.example.nhathuoc.data.model.ComplaintEventDto
import com.example.nhathuoc.data.model.ComplaintMessageDto
import com.example.nhathuoc.data.model.UiState
import com.example.nhathuoc.data.remote.BackendUrlResolver
import com.example.nhathuoc.util.formatUtcToVnDateTime
import com.example.nhathuoc.util.formatVnDateTime
import com.example.nhathuoc.viewmodel.OrderViewModel
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComplaintDetailScreen(
    complaintId: String,
    onBack: () -> Unit = {},
    viewModel: OrderViewModel = hiltViewModel()
) {
    val complaintState by viewModel.complaintDetailState.collectAsState()
    val messageState by viewModel.complaintMessageState.collectAsState()
    val addAttachmentsState by viewModel.addAttachmentsState.collectAsState()
    val requestRefundState by viewModel.requestRefundState.collectAsState()
    var message by rememberSaveable { mutableStateOf("") }
    var pendingUris by remember { mutableStateOf<List<Uri>>(emptyList()) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val filePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris -> if (uris.isNotEmpty()) pendingUris = pendingUris + uris }

    val closedStatuses = setOf("RESOLVED", "REJECTED", "CANCELLED")
    val isClosed = (complaintState as? UiState.Success)?.data?.status?.uppercase() in closedStatuses
    val isSending = messageState is UiState.Loading || addAttachmentsState is UiState.Loading

    LaunchedEffect(complaintId) {
        viewModel.getComplaintById(complaintId)
        viewModel.clearComplaintMessageState()
    }
    LaunchedEffect(messageState) {
        if (messageState is UiState.Success) {
            message = ""
            scope.launch { snackbarHostState.showSnackbar("Đã gửi thành công") }
            viewModel.clearComplaintMessageState()
        }
    }
    LaunchedEffect(addAttachmentsState) {
        when (addAttachmentsState) {
            is UiState.Success -> {
                pendingUris = emptyList()
                scope.launch { snackbarHostState.showSnackbar("Đã thêm file đính kèm") }
                viewModel.clearAddAttachmentsState()
            }
            is UiState.Error -> {
                scope.launch { snackbarHostState.showSnackbar((addAttachmentsState as UiState.Error).message) }
                viewModel.clearAddAttachmentsState()
            }
            else -> Unit
        }
    }
    LaunchedEffect(requestRefundState) {
        when (requestRefundState) {
            is UiState.Success -> {
                scope.launch { snackbarHostState.showSnackbar("Đã gửi yêu cầu hoàn tiền") }
                viewModel.clearRequestRefundState()
            }
            is UiState.Error -> {
                scope.launch { snackbarHostState.showSnackbar((requestRefundState as UiState.Error).message) }
                viewModel.clearRequestRefundState()
            }
            else -> Unit
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            com.example.nhathuoc.ui.component.GreenAppTopBar(
                title = "Chi tiết khiếu nại",
                onBack = onBack
            )
        },
        bottomBar = {
            if (complaintState is UiState.Success && !isClosed) {
                ComplaintReplyBar(
                    message = message,
                    pendingFileCount = pendingUris.size,
                    isSending = isSending,
                    onMessageChange = { message = it },
                    onAttachClick = { filePicker.launch("*/*") },
                    onClearFile = { idx -> pendingUris = pendingUris.toMutableList().also { it.removeAt(idx) } },
                    onSend = {
                        if (pendingUris.isNotEmpty()) viewModel.addComplaintAttachments(complaintId, pendingUris)
                        if (message.isNotBlank()) viewModel.sendComplaintMessage(complaintId, message.trim())
                    }
                )
            }
        }
    ) { paddingValues ->
        when (val state = complaintState) {
            is UiState.Loading -> Box(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
            is UiState.Error -> Box(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text(state.message, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(24.dp))
            }
            is UiState.Success -> ComplaintDetailContent(
                complaint = state.data,
                messageError = (messageState as? UiState.Error)?.message,
                isRequestingRefund = requestRefundState is UiState.Loading,
                onRequestRefund = { viewModel.requestRefundForComplaint(complaintId) },
                modifier = Modifier.padding(paddingValues)
            )
            else -> Unit
        }
    }
}

@Composable
private fun ComplaintDetailContent(
    complaint: ComplaintDto,
    messageError: String?,
    isRequestingRefund: Boolean = false,
    onRequestRefund: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { ComplaintInfoCard(complaint, isRequestingRefund, onRequestRefund) }
        if (complaint.attachments.isNotEmpty()) {
            item { ComplaintAttachmentsCard(complaint) }
        }
        if (complaint.events.isNotEmpty()) {
            item { ComplaintTimelineCard(complaint) }
        }
        item {
            Text("Trao đổi xử lý", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        }
        if (complaint.messages.isEmpty()) {
            item {
                Surface(color = MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
                    Text(
                        "Chưa có trao đổi mới. Bạn có thể gửi thêm thông tin cho CSKH ở ô bên dưới.",
                        color = Color.Gray,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        } else {
            items(complaint.messages, key = { it.id }) { message ->
                ComplaintMessageBubble(message)
            }
        }
        messageError?.let {
            item {
                Text(it, color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
            }
        }
        item { Spacer(Modifier.height(88.dp)) }
    }
}

@Composable
private fun ComplaintInfoCard(
    complaint: ComplaintDto,
    isRequestingRefund: Boolean = false,
    onRequestRefund: () -> Unit = {}
) {
    val closedStatuses = setOf("RESOLVED", "REJECTED", "CANCELLED")
    val canRequestRefund = complaint.refundStatus in setOf("NONE", "REFUND_FAILED") &&
        complaint.status.uppercase() !in closedStatuses

    Surface(color = MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(18.dp), shadowElevation = 2.dp) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(complaint.complaintCode, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Text(
                    complaintStatusLabel(complaint.status),
                    color = complaintStatusColor(complaint.status),
                    fontWeight = FontWeight.SemiBold
                )
            }
            Text(complaint.title, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Text("Đơn: ${complaint.orderId.takeLast(8)}", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
            Text("Loại: ${complaintTypeLabel(complaint.type)} • Ưu tiên: ${complaint.priority}", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
            Text(complaint.description, color = MaterialTheme.colorScheme.onSurface, fontSize = 14.sp, lineHeight = 20.sp)
            complaint.resolution?.takeIf { it.isNotBlank() }?.let {
                Surface(color = Color(0xFFEFF6FF), shape = RoundedCornerShape(12.dp)) {
                    Text("Phản hồi xử lý: $it", color = Color(0xFF1D4ED8), modifier = Modifier.padding(12.dp))
                }
            }
            if (complaint.refundStatus != "NONE" || complaint.refundAmount != null) {
                val isRefundFailed = complaint.refundStatus == "REFUND_FAILED"
                Surface(
                    color = if (isRefundFailed) Color(0xFFFFEDED) else Color(0xFFFFFBEB),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            "Thông tin hoàn tiền",
                            fontWeight = FontWeight.Bold,
                            color = if (isRefundFailed) Color(0xFFB91C1C) else Color(0xFFB45309)
                        )
                        Text(
                            "Trạng thái: ${refundStatusLabel(complaint.refundStatus)}",
                            color = if (isRefundFailed) Color(0xFFB91C1C) else Color(0xFF92400E),
                            fontSize = 13.sp,
                            fontWeight = if (isRefundFailed) FontWeight.SemiBold else FontWeight.Normal
                        )
                        if (isRefundFailed) {
                            Text(
                                "Hệ thống không thể tự động hoàn tiền. Nhấn \"Yêu cầu hoàn tiền\" bên dưới để được hỗ trợ hoàn theo cách khác.",
                                color = Color(0xFFB91C1C),
                                fontSize = 12.sp,
                                lineHeight = 18.sp
                            )
                        }
                        complaint.refundAmount?.let {
                            Text("Số tiền: ${formatMoney(it)}", color = Color(0xFF92400E), fontSize = 13.sp)
                        }
                        complaint.refundMethod?.let {
                            Text("Phương thức: ${refundMethodLabel(it)}", color = Color(0xFF92400E), fontSize = 13.sp)
                        }
                        complaint.refundTransactionId?.takeIf { !isRefundFailed }?.let {
                            Text("Mã giao dịch hoàn: $it", color = Color(0xFF92400E), fontSize = 13.sp)
                        }
                        complaint.refundedAt?.let {
                            Text("Đã hoàn lúc: ${formatUtcToVnDateTime(it)}", color = Color(0xFF92400E), fontSize = 13.sp)
                        }
                    }
                }
            }
            if (canRequestRefund) {
                val isRetryRefund = complaint.refundStatus == "REFUND_FAILED"
                OutlinedButton(
                    onClick = onRequestRefund,
                    enabled = !isRequestingRefund,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = if (isRetryRefund) Color(0xFFB91C1C) else Color(0xFFB45309)
                    )
                ) {
                    if (isRequestingRefund) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp,
                            color = if (isRetryRefund) Color(0xFFB91C1C) else Color(0xFFB45309))
                    } else {
                        Icon(Icons.Default.MoneyOff, contentDescription = null, modifier = Modifier.size(16.dp))
                    }
                    Spacer(Modifier.size(6.dp))
                    Text(
                        if (isRetryRefund) "Yêu cầu hoàn tiền theo cách khác" else "Yêu cầu hoàn tiền",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
private fun ComplaintTimelineCard(complaint: ComplaintDto) {
    Surface(color = MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(18.dp), shadowElevation = 1.dp) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Timeline xử lý & SLA", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            complaint.events.forEach { event ->
                ComplaintEventRow(event)
            }
        }
    }
}

@Composable
private fun ComplaintEventRow(event: ComplaintEventDto) {
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
        Text("•", color = complaintEventColor(event.eventType), fontSize = 24.sp, lineHeight = 20.sp)
        Column(verticalArrangement = Arrangement.spacedBy(3.dp), modifier = Modifier.weight(1f)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    event.title,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
                Text(formatUtcToVnDateTime(event.createdAt), fontSize = 11.sp, color = Color.Gray)
            }
            val actor = event.actorName ?: event.actorRole ?: "Hệ thống"
            Text(actor, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            event.description?.takeIf { it.isNotBlank() }?.let {
                Text(it, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface, lineHeight = 18.sp)
            }
            if (!event.fromStatus.isNullOrBlank() || !event.toStatus.isNullOrBlank()) {
                Text(
                    "Trạng thái: ${event.fromStatus ?: "-"} → ${event.toStatus ?: "-"}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (!event.fromPriority.isNullOrBlank() || !event.toPriority.isNullOrBlank()) {
                Text(
                    "Ưu tiên: ${event.fromPriority ?: "-"} → ${event.toPriority ?: "-"}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            event.dueAt?.takeIf { it.isNotBlank() }?.let {
                Text(
                    "SLA phản hồi trước: ${formatUtcToVnDateTime(it)}",
                    fontSize = 12.sp,
                    color = Color(0xFFB45309),
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

private fun complaintEventColor(type: String): Color {
    return when (type.uppercase()) {
        "CREATED" -> Color(0xFF2563EB)
        "STATUS_CHANGED" -> Color(0xFF2E7D32)
        "INTERNAL_NOTE" -> Color(0xFF7C3AED)
        "MESSAGE_ADDED" -> Color(0xFF0891B2)
        "REFUND_UPDATED" -> Color(0xFFB45309)
        "ATTACHMENT_ADDED" -> Color(0xFF059669)
        else -> Color(0xFF6B7280)
    }
}

@Composable
private fun ComplaintAttachmentsCard(complaint: ComplaintDto) {
    val uriHandler = LocalUriHandler.current
    fun openAttachment(fileUrl: String) {
        val resolvedUrl = BackendUrlResolver.resolveFileUrl(fileUrl)
        if (resolvedUrl.isNotBlank()) {
            runCatching { uriHandler.openUri(resolvedUrl) }
        }
    }

    Surface(color = MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(18.dp), shadowElevation = 1.dp) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("File minh chứng", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            complaint.attachments.forEachIndexed { index, attachment ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { openAttachment(attachment.fileUrl) },
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Outlined.AttachFile, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                    Text(
                        "${index + 1}. ${attachment.fileType} • ${attachment.fileUrl.substringAfterLast('/').take(42)}",
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 12.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    TextButton(onClick = { openAttachment(attachment.fileUrl) }) {
                        Text("Mở", color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }
    }
}

@Composable
private fun ComplaintMessageBubble(message: ComplaintMessageDto) {
    val isUser = message.senderRole.equals("USER", ignoreCase = true)
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        Surface(
            color = if (isUser) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
            contentColor = if (isUser) Color.White else MaterialTheme.colorScheme.onSurface,
            shape = RoundedCornerShape(16.dp),
            shadowElevation = if (isUser) 0.dp else 1.dp,
            modifier = Modifier.fillMaxWidth(0.82f)
        ) {
            Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    if (!isUser) {
                        Icon(Icons.Outlined.SupportAgent, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                    }
                    Text(
                        message.senderName ?: if (isUser) "Bạn" else "CSKH MedStore",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
                Text(message.message, fontSize = 14.sp, lineHeight = 20.sp)
                Text(formatUtcToVnDateTime(message.createdAt), fontSize = 11.sp, color = if (isUser) Color.White.copy(alpha = 0.8f) else Color.Gray)
            }
        }
    }
}

@Composable
private fun ComplaintReplyBar(
    message: String,
    pendingFileCount: Int,
    isSending: Boolean,
    onMessageChange: (String) -> Unit,
    onAttachClick: () -> Unit,
    onClearFile: (Int) -> Unit,
    onSend: () -> Unit
) {
    val canSend = !isSending && (message.isNotBlank() || pendingFileCount > 0)
    Surface(color = MaterialTheme.colorScheme.surface, shadowElevation = 8.dp) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp)) {
            if (pendingFileCount > 0) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.padding(bottom = 6.dp)
                ) {
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(Icons.Outlined.AttachFile, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(14.dp))
                            Text("$pendingFileCount file đính kèm", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                            IconButton(
                                onClick = { repeat(pendingFileCount) { onClearFile(0) } },
                                modifier = Modifier.size(18.dp)
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "Xóa file", tint = Color.Gray, modifier = Modifier.size(14.dp))
                            }
                        }
                    }
                }
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(onClick = onAttachClick, modifier = Modifier.size(40.dp)) {
                    Icon(Icons.Outlined.AttachFile, contentDescription = "Đính kèm", tint = MaterialTheme.colorScheme.primary)
                }
                OutlinedTextField(
                    value = message,
                    onValueChange = onMessageChange,
                    label = { Text("Bổ sung thông tin") },
                    modifier = Modifier.weight(1f),
                    minLines = 1,
                    maxLines = 4
                )
                Button(
                    onClick = onSend,
                    enabled = canSend,
                    shape = RoundedCornerShape(14.dp)
                ) {
                    if (isSending) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = Color.White)
                    } else {
                        Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null)
                    }
                }
            }
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
        "APPROVED", "RESOLVED" -> Color(0xFF2E7D32)
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
        "REQUESTED" -> "Đang chờ duyệt hoàn tiền"
        "APPROVED" -> "Đã duyệt hoàn tiền"
        "REFUND_PROCESSING" -> "Đang xử lý hoàn tiền"
        "REFUNDED" -> "Đã hoàn tiền"
        "REJECTED" -> "Từ chối hoàn tiền"
        "REFUND_FAILED" -> "Hoàn tiền tự động thất bại"
        else -> "Không hoàn tiền"
    }
}

private fun refundMethodLabel(method: String): String {
    return when (method.uppercase()) {
        "ORIGINAL_PAYMENT" -> "Hoàn về phương thức thanh toán ban đầu"
        "BANK_TRANSFER" -> "Chuyển khoản ngân hàng"
        "CASH" -> "Tiền mặt"
        "POINTS" -> "Điểm thưởng"
        else -> "Khác"
    }
}

private fun formatMoney(value: Double): String {
    return NumberFormat.getCurrencyInstance(Locale("vi", "VN")).format(value)
}
