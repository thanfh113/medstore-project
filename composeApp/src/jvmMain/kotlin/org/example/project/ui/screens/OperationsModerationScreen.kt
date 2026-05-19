package org.example.project.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.Surface
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.example.project.data.repositories.OperationsComplaintAttachmentDto
import org.example.project.data.repositories.OperationsComplaintDto
import org.example.project.data.repositories.OperationsComplaintEventDto
import org.example.project.data.repositories.OperationsComplaintMessageDto
import org.example.project.data.repositories.OperationsReviewAttachmentDto
import org.example.project.data.repositories.OperationsReviewReportDto
import org.example.project.data.repositories.OperationsReviewDto
import org.example.project.data.repositories.OperationsRewardRedemptionDto
import org.example.project.presentation.viewmodels.OperationsViewModel
import org.example.project.util.formatUtcToVnDateTime
import org.example.project.util.formatVnDate
import org.example.project.util.formatVnDateTime
import java.awt.Desktop
import java.net.URI

private val reviewStatusFilters = listOf(
    null to "Tất cả",
    "VISIBLE" to "Đang hiện",
    "HIDDEN" to "Đã ẩn",
    "REMOVED" to "Đã gỡ"
)

private fun reviewStatusLabel(status: String) = when (status.uppercase()) {
    "VISIBLE" -> "Đang hiện"
    "HIDDEN"  -> "Đã ẩn"
    "REMOVED" -> "Đã gỡ"
    else      -> status
}

private fun reportStatusLabel(status: String) = when (status.uppercase()) {
    "OPEN"     -> "Chờ xử lý"
    "RESOLVED" -> "Đã xử lý"
    "REJECTED" -> "Không hợp lệ"
    else       -> status
}

private val reviewReportStatusFilters = listOf(
    "OPEN" to "Chờ xử lý",
    "RESOLVED" to "Đã xử lý",
    "REJECTED" to "Không hợp lệ",
    null to "Tất cả"
)

private val complaintStatusFilters = listOf(
    null to "Tất cả",
    "OPEN" to "Mới",
    "IN_REVIEW" to "Đang xử lý",
    "NEED_MORE_INFO" to "Cần bổ sung",
    "APPROVED" to "Đã duyệt",
    "RESOLVED" to "Đã xong",
    "REJECTED" to "Từ chối",
    "CANCELLED" to "Đã hủy"
)

private val complaintPriorityFilters = listOf(
    null to "Mọi mức",
    "LOW" to "Thấp",
    "NORMAL" to "Thường",
    "HIGH" to "Cao",
    "URGENT" to "Gấp"
)

private val complaintTypeFilters = listOf(
    null to "Mọi loại",
    "MISSING_ITEM" to "Thiếu hàng",
    "WRONG_ITEM" to "Sai hàng",
    "DAMAGED" to "Hỏng",
    "COUNTERFEIT" to "Nghi hàng giả",
    "EXPIRED" to "Hết hạn",
    "PAYMENT" to "Thanh toán",
    "REFUND" to "Hoàn tiền",
    "OTHER" to "Khác"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OperationsModerationScreen(viewModel: OperationsViewModel) {
    val state by viewModel.uiState.collectAsState()
    val tabs = listOf("Đánh giá", "Khiếu nại", "Đổi điểm")
    var selectedReviewReport by remember { mutableStateOf<OperationsReviewReportDto?>(null) }
    var selectedReview by remember { mutableStateOf<OperationsReviewDto?>(null) }

    LaunchedEffect(Unit) {
        viewModel.loadAll()
    }

    state.selectedComplaint?.let { complaint ->
        ComplaintDetailDialog(
            complaint = complaint,
            isLoading = state.isComplaintDetailLoading,
            isSendingMessage = state.isSendingComplaintMessage,
            processingId = state.processingId,
            onDismiss = viewModel::closeComplaintDetail,
            onRefresh = { viewModel.openComplaintDetail(complaint.id) },
            onUpdate = viewModel::updateComplaint,
            onSendMessage = viewModel::sendComplaintMessage
        )
    }

    selectedReviewReport?.let { report ->
        ReviewReportDetailDialog(
            report = report,
            processingId = state.processingId,
            onDismiss = { selectedReviewReport = null },
            onHandleReport = { reportId, reportStatus, reviewStatus, reason ->
                viewModel.updateReviewReport(reportId, reportStatus, reviewStatus, reason)
                selectedReviewReport = null
            }
        )
    }

    selectedReview?.let { review ->
        ReviewDetailDialog(
            review = review,
            processingId = state.processingId,
            onDismiss = { selectedReview = null },
            onModerate = { reviewId, status, reason ->
                viewModel.moderateReview(reviewId, status, reason)
                selectedReview = null
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Vận hành khách hàng", fontWeight = FontWeight.Bold) },
                actions = {
                    state.successMessage?.let {
                        Text(it, color = MaterialTheme.colorScheme.primary, fontSize = 12.sp, modifier = Modifier.padding(end = 4.dp), maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                    state.error?.let {
                        Text(it, color = MaterialTheme.colorScheme.error, fontSize = 12.sp, modifier = Modifier.padding(end = 4.dp), maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                    IconButton(onClick = viewModel::loadAll, enabled = !state.isLoading) {
                        if (state.isLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                        } else {
                            Icon(Icons.Default.Refresh, contentDescription = "Tải lại")
                        }
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            TabRow(selectedTabIndex = state.selectedTab) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = state.selectedTab == index,
                        onClick = { viewModel.selectTab(index) },
                        text = { Text(title) }
                    )
                }
            }

            when (state.selectedTab) {
                0 -> ReviewsTab(
                    reviews = state.reviews,
                    reports = state.reviewReports,
                    processingId = state.processingId,
                    statusFilter = state.reviewStatusFilter,
                    reportStatusFilter = state.reviewReportStatusFilter,
                    onStatusFilterChange = viewModel::setReviewStatusFilter,
                    onReportStatusFilterChange = viewModel::setReviewReportStatusFilter,
                    onOpenReport = { selectedReviewReport = it },
                    onOpenReview = { selectedReview = it },
                    onHandleReport = viewModel::updateReviewReport,
                    onModerate = viewModel::moderateReview
                )

                1 -> ComplaintsTab(
                    complaints = state.complaints,
                    processingId = state.processingId,
                    statusFilter = state.complaintStatusFilter,
                    priorityFilter = state.complaintPriorityFilter,
                    typeFilter = state.complaintTypeFilter,
                    onStatusFilterChange = viewModel::setComplaintStatusFilter,
                    onPriorityFilterChange = viewModel::setComplaintPriorityFilter,
                    onTypeFilterChange = viewModel::setComplaintTypeFilter,
                    onOpenDetail = viewModel::openComplaintDetail,
                    onUpdate = viewModel::updateComplaint
                )

                2 -> RewardsTab(
                    redemptions = state.redemptions,
                    processingId = state.processingId,
                    onUpdateRedemption = viewModel::updateRedemption,
                    onAdjustPoints = viewModel::adjustPoints
                )
            }
        }
    }
}

@Composable
private fun FilterDropdown(
    label: String,
    options: List<Pair<String?, String>>,
    selected: String?,
    onSelected: (String?) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedLabel = options.find { it.first == selected }?.second ?: options.firstOrNull()?.second ?: label

    Box {
        OutlinedButton(
            onClick = { expanded = true },
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(
                "$label: $selectedLabel",
                style = MaterialTheme.typography.labelLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.size(4.dp))
            Icon(Icons.Default.ArrowDropDown, contentDescription = null, modifier = Modifier.size(18.dp))
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { (value, optLabel) ->
                DropdownMenuItem(
                    text = { Text(optLabel) },
                    onClick = { onSelected(value); expanded = false },
                    trailingIcon = if (selected == value) ({
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                    }) else null
                )
            }
        }
    }
}

@Composable
private fun FilterPill(label: String, selected: Boolean, onClick: () -> Unit) {
    if (selected) {
        Button(onClick = onClick) { Text(label) }
    } else {
        OutlinedButton(onClick = onClick) { Text(label) }
    }
}

@Composable
private fun ComplaintStatusBadge(status: String) {
    val (label, color) = when (status) {
        "OPEN" -> "Mới" to MaterialTheme.colorScheme.error
        "IN_REVIEW" -> "Đang xử lý" to MaterialTheme.colorScheme.primary
        "NEED_MORE_INFO" -> "Cần bổ sung" to MaterialTheme.colorScheme.tertiary
        "APPROVED" -> "Đã duyệt" to MaterialTheme.colorScheme.secondary
        "RESOLVED" -> "Hoàn tất" to MaterialTheme.colorScheme.secondary
        "REJECTED" -> "Từ chối" to MaterialTheme.colorScheme.error
        "CANCELLED" -> "Đã hủy" to MaterialTheme.colorScheme.outline
        else -> status to MaterialTheme.colorScheme.outline
    }
    Surface(shape = RoundedCornerShape(20.dp), color = color.copy(alpha = 0.12f)) {
        Text(
            label,
            color = color,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
        )
    }
}

@Composable
private fun ComplaintPriorityBadge(priority: String) {
    val (label, color) = when (priority) {
        "URGENT" -> "Gấp" to MaterialTheme.colorScheme.error
        "HIGH" -> "Cao" to MaterialTheme.colorScheme.tertiary
        "NORMAL" -> "Thường" to MaterialTheme.colorScheme.primary
        "LOW" -> "Thấp" to MaterialTheme.colorScheme.outline
        else -> priority to MaterialTheme.colorScheme.outline
    }
    Text(label, color = color, style = MaterialTheme.typography.labelSmall)
}

@Composable
private fun ReviewsTab(
    reviews: List<OperationsReviewDto>,
    reports: List<OperationsReviewReportDto>,
    processingId: String?,
    statusFilter: String?,
    reportStatusFilter: String?,
    onStatusFilterChange: (String?) -> Unit,
    onReportStatusFilterChange: (String?) -> Unit,
    onOpenReport: (OperationsReviewReportDto) -> Unit,
    onOpenReview: (OperationsReviewDto) -> Unit,
    onHandleReport: (String, String, String?, String?) -> Unit,
    onModerate: (String, String, String?) -> Unit
) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxSize()) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FilterDropdown(
                        label = "Báo cáo",
                        options = reviewReportStatusFilters,
                        selected = reportStatusFilter,
                        onSelected = onReportStatusFilterChange
                    )
                    FilterDropdown(
                        label = "Đánh giá",
                        options = reviewStatusFilters,
                        selected = statusFilter,
                        onSelected = onStatusFilterChange
                    )
                }
                if (reports.isEmpty()) {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Text(
                            "Không có báo cáo đánh giá theo bộ lọc hiện tại.",
                            modifier = Modifier.padding(16.dp),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
        items(reports, key = { "report-${it.id}" }) { report ->
            ReviewReportCard(
                report = report,
                processingId = processingId,
                onOpen = { onOpenReport(report) },
                onHandleReport = onHandleReport
            )
        }
        item { Spacer(Modifier.height(4.dp)) }
        items(reviews, key = { it.id }) { review ->
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(
                            "${review.rating}/5 sao — ${review.userName ?: review.userId.takeLast(8)}",
                            fontWeight = FontWeight.Bold
                        )
                        Text(reviewStatusLabel(review.status), color = MaterialTheme.colorScheme.primary)
                    }
                    Text("Sản phẩm: ${review.productId}", style = MaterialTheme.typography.bodySmall)
                    review.title?.takeIf { it.isNotBlank() }?.let { Text(it, fontWeight = FontWeight.SemiBold) }
                    review.comment?.takeIf { it.isNotBlank() }?.let {
                        Text(it, maxLines = 3, overflow = TextOverflow.Ellipsis)
                    }
                    Text(
                        "Báo cáo: ${review.reportCount} · File: ${review.attachments.size} · Mua thật: ${if (review.isVerifiedPurchase) "Có" else "Không"} · ${formatVnDateTime(review.createdAt)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = { onOpenReview(review) },
                            enabled = processingId != review.id
                        ) { Text("Xem chi tiết") }
                        OutlinedButton(
                            onClick = { onModerate(review.id, "VISIBLE", null) },
                            enabled = processingId != review.id
                        ) { Text("Hiện") }
                        OutlinedButton(
                            onClick = { onModerate(review.id, "HIDDEN", "Ẩn vì cần kiểm tra") },
                            enabled = processingId != review.id
                        ) { Text("Ẩn") }
                        OutlinedButton(
                            onClick = { onModerate(review.id, "REMOVED", "Vi phạm nội dung") },
                            enabled = processingId != review.id
                        ) { Text("Gỡ") }
                    }
                }
            }
        }
    }
}

@Composable
private fun ReviewReportCard(
    report: OperationsReviewReportDto,
    processingId: String?,
    onOpen: () -> Unit,
    onHandleReport: (String, String, String?, String?) -> Unit
) {
    val review = report.review
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    "Báo cáo: ${report.reason} — ${report.reporterName ?: report.reporterUserId.takeLast(8)}",
                    fontWeight = FontWeight.Bold
                )
                Text(reportStatusLabel(report.status), color = MaterialTheme.colorScheme.primary)
            }
            report.note?.takeIf { it.isNotBlank() }?.let { Text("Ghi chú: $it") }
            Text(
                "Báo cáo lúc: ${formatVnDateTime(report.createdAt)} · Review: ${report.reviewId.takeLast(8)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.outline
            )
            review?.let {
                Text(
                    "${it.rating}/5 sao — ${it.userName ?: it.userId.takeLast(8)} | ${reviewStatusLabel(it.status)}",
                    fontWeight = FontWeight.SemiBold
                )
                it.title?.takeIf { title -> title.isNotBlank() }?.let { title -> Text(title) }
                it.comment?.takeIf { comment -> comment.isNotBlank() }?.let { comment ->
                    Text(comment, maxLines = 2, overflow = TextOverflow.Ellipsis)
                }
            } ?: Text("Review gốc không còn tồn tại", color = MaterialTheme.colorScheme.error)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onOpen, enabled = processingId != report.id) {
                    Text("Xem chi tiết")
                }
                OutlinedButton(
                    onClick = { onHandleReport(report.id, "REJECTED", "VISIBLE", null) },
                    enabled = processingId != report.id && report.status == "OPEN"
                ) { Text("Giữ review") }
                OutlinedButton(
                    onClick = { onHandleReport(report.id, "RESOLVED", "HIDDEN", "Ẩn do báo cáo hợp lệ") },
                    enabled = processingId != report.id && report.status == "OPEN"
                ) { Text("Ẩn review") }
                OutlinedButton(
                    onClick = { onHandleReport(report.id, "RESOLVED", "REMOVED", "Gỡ do vi phạm nội dung") },
                    enabled = processingId != report.id && report.status == "OPEN"
                ) { Text("Gỡ review") }
            }
        }
    }
}

@Composable
private fun ReviewReportDetailDialog(
    report: OperationsReviewReportDto,
    processingId: String?,
    onDismiss: () -> Unit,
    onHandleReport: (String, String, String?, String?) -> Unit
) {
    var hiddenReason by remember(report.id) {
        mutableStateOf(report.review?.hiddenReason.orEmpty())
    }
    val review = report.review

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.widthIn(min = 680.dp, max = 980.dp),
        title = {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Báo cáo đánh giá", fontWeight = FontWeight.Bold)
                Text(
                    "${report.reason} | ${reportStatusLabel(report.status)} | ${formatVnDateTime(report.createdAt)}",
                    color = MaterialTheme.colorScheme.primary
                )
            }
        },
        text = {
            LazyColumn(
                modifier = Modifier.heightIn(max = 620.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Thông tin báo cáo", fontWeight = FontWeight.SemiBold)
                        Text("Người báo cáo: ${report.reporterName ?: report.reporterUserId}")
                        Text("Lý do: ${report.reason}")
                        report.note?.takeIf { it.isNotBlank() }?.let { Text("Ghi chú: $it") }
                        report.handledAt?.let {
                            Text("Đã xử lý lúc: ${formatVnDateTime(it)} bởi ${report.handledBy?.takeLast(8) ?: "N/A"}")
                        }
                    }
                }

                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Review gốc", fontWeight = FontWeight.SemiBold)
                        if (review == null) {
                            Text("Review gốc không còn tồn tại", color = MaterialTheme.colorScheme.error)
                        } else {
                            Text("${review.rating}/5 sao — ${review.userName ?: review.userId.takeLast(8)} | ${reviewStatusLabel(review.status)}")
                            Text("Sản phẩm: ${review.productId} | Đơn: ${review.orderId?.takeLast(8) ?: "N/A"}")
                            review.title?.takeIf { it.isNotBlank() }?.let { Text(it, fontWeight = FontWeight.SemiBold) }
                            review.comment?.takeIf { it.isNotBlank() }?.let { Text(it) }
                            Text("Mua thật: ${if (review.isVerifiedPurchase) "Có" else "Không"} | Báo cáo: ${review.reportCount} | ${formatVnDateTime(review.createdAt)}")
                            review.hiddenReason?.takeIf { it.isNotBlank() }?.let {
                                Text("Lý do ẩn hiện tại: $it", color = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }

                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("File đính kèm (${review?.attachments?.size ?: 0})", fontWeight = FontWeight.SemiBold)
                        review?.attachments.orEmpty().forEach { attachment ->
                            ReviewAttachmentRow(attachment)
                        }
                    }
                }

                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Quyết định xử lý", fontWeight = FontWeight.SemiBold)
                        OutlinedTextField(
                            value = hiddenReason,
                            onValueChange = { hiddenReason = it },
                            label = { Text("Lý do ẩn/gỡ nếu report hợp lệ") },
                            minLines = 2,
                            modifier = Modifier.fillMaxWidth()
                        )
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = { onHandleReport(report.id, "REJECTED", "VISIBLE", null) },
                                enabled = processingId != report.id && report.status == "OPEN"
                            ) { Text("Giữ review") }
                            OutlinedButton(
                                onClick = {
                                    onHandleReport(
                                        report.id, "RESOLVED", "HIDDEN",
                                        hiddenReason.ifBlank { "Ẩn do báo cáo hợp lệ" }
                                    )
                                },
                                enabled = processingId != report.id && report.status == "OPEN"
                            ) { Text("Ẩn review") }
                            OutlinedButton(
                                onClick = {
                                    onHandleReport(
                                        report.id, "RESOLVED", "REMOVED",
                                        hiddenReason.ifBlank { "Gỡ do vi phạm nội dung" }
                                    )
                                },
                                enabled = processingId != report.id && report.status == "OPEN"
                            ) { Text("Gỡ review") }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Đóng") }
        }
    )
}

@Composable
private fun ReviewDetailDialog(
    review: OperationsReviewDto,
    processingId: String?,
    onDismiss: () -> Unit,
    onModerate: (String, String, String?) -> Unit
) {
    var hiddenReason by remember(review.id) { mutableStateOf(review.hiddenReason.orEmpty()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.widthIn(min = 680.dp, max = 980.dp),
        title = {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Chi tiết đánh giá", fontWeight = FontWeight.Bold)
                Text(
                    "${review.rating}/5 sao — ${review.userName ?: review.userId.takeLast(8)} | ${reviewStatusLabel(review.status)}",
                    color = MaterialTheme.colorScheme.primary
                )
            }
        },
        text = {
            LazyColumn(
                modifier = Modifier.heightIn(max = 620.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Thông tin đánh giá", fontWeight = FontWeight.SemiBold)
                        Text("Người dùng: ${review.userName ?: review.userId}")
                        Text("Sản phẩm: ${review.productId}")
                        review.orderId?.let { Text("Đơn hàng: $it") }
                        Text("Mua thật: ${if (review.isVerifiedPurchase) "Có" else "Không"} · Báo cáo: ${review.reportCount} · ${formatVnDateTime(review.createdAt)}")
                        review.hiddenReason?.takeIf { it.isNotBlank() }?.let {
                            Text("Lý do ẩn: $it", color = MaterialTheme.colorScheme.error)
                        }
                    }
                }
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Nội dung đánh giá", fontWeight = FontWeight.SemiBold)
                        review.title?.takeIf { it.isNotBlank() }?.let {
                            Text(it, fontWeight = FontWeight.Medium, fontSize = 15.sp)
                        }
                        review.comment?.takeIf { it.isNotBlank() }?.let {
                            Text(it)
                        }
                        if (review.title.isNullOrBlank() && review.comment.isNullOrBlank()) {
                            Text("(Không có nội dung)", color = MaterialTheme.colorScheme.outline)
                        }
                    }
                }
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("File đính kèm (${review.attachments.size})", fontWeight = FontWeight.SemiBold)
                        if (review.attachments.isEmpty()) {
                            Text("Không có file đính kèm", color = MaterialTheme.colorScheme.outline)
                        } else {
                            review.attachments.forEach { attachment ->
                                ReviewAttachmentRow(attachment)
                            }
                        }
                    }
                }
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Kiểm duyệt", fontWeight = FontWeight.SemiBold)
                        OutlinedTextField(
                            value = hiddenReason,
                            onValueChange = { hiddenReason = it },
                            label = { Text("Lý do ẩn/gỡ (nếu có)") },
                            minLines = 2,
                            modifier = Modifier.fillMaxWidth()
                        )
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = { onModerate(review.id, "VISIBLE", null) },
                                enabled = processingId != review.id
                            ) { Text("Hiện") }
                            OutlinedButton(
                                onClick = { onModerate(review.id, "HIDDEN", hiddenReason.ifBlank { "Ẩn vì cần kiểm tra" }) },
                                enabled = processingId != review.id
                            ) { Text("Ẩn") }
                            OutlinedButton(
                                onClick = { onModerate(review.id, "REMOVED", hiddenReason.ifBlank { "Vi phạm nội dung" }) },
                                enabled = processingId != review.id
                            ) { Text("Gỡ") }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Đóng") }
        }
    )
}

@Composable
private fun ReviewAttachmentRow(attachment: OperationsReviewAttachmentDto) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    attachment.fileUrl.substringAfterLast('/').substringBefore('?'),
                    fontWeight = FontWeight.SemiBold
                )
                Text("${attachment.fileType} | ${formatVnDateTime(attachment.createdAt)}", style = MaterialTheme.typography.bodySmall)
            }
            OutlinedButton(onClick = { openExternalUrl(attachment.fileUrl) }) { Text("Mở") }
        }
    }
}

@Composable
private fun ComplaintsTab(
    complaints: List<OperationsComplaintDto>,
    processingId: String?,
    statusFilter: String?,
    priorityFilter: String?,
    typeFilter: String?,
    onStatusFilterChange: (String?) -> Unit,
    onPriorityFilterChange: (String?) -> Unit,
    onTypeFilterChange: (String?) -> Unit,
    onOpenDetail: (String) -> Unit,
    onUpdate: (String, String, String, String?, Double?, String?, String?, String?, Boolean) -> Unit
) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxSize()) {
        item {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilterDropdown("Trạng thái", complaintStatusFilters, statusFilter, onStatusFilterChange)
                FilterDropdown("Ưu tiên", complaintPriorityFilters, priorityFilter, onPriorityFilterChange)
                FilterDropdown("Loại", complaintTypeFilters, typeFilter, onTypeFilterChange)
            }
        }
        if (complaints.isEmpty()) {
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Text(
                        "Không có khiếu nại nào theo bộ lọc hiện tại.",
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
        items(complaints, key = { it.id }) { complaint ->
            ComplaintListCard(
                complaint = complaint,
                processingId = processingId,
                onOpenDetail = onOpenDetail,
                onUpdate = onUpdate
            )
        }
    }
}

@Composable
private fun ComplaintListCard(
    complaint: OperationsComplaintDto,
    processingId: String?,
    onOpenDetail: (String) -> Unit,
    onUpdate: (String, String, String, String?, Double?, String?, String?, String?, Boolean) -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            // Header row: code + badges
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "${complaint.complaintCode} — ${complaint.title}",
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                    ComplaintStatusBadge(complaint.status)
                    ComplaintPriorityBadge(complaint.priority)
                    if (complaint.refundStatus != "NONE") {
                        Surface(shape = RoundedCornerShape(20.dp), color = MaterialTheme.colorScheme.tertiaryContainer) {
                            Text(
                                refundStatusLabel(complaint.refundStatus),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onTertiaryContainer,
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                            )
                        }
                    }
                }
            }
            // Meta row
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    complaint.userName ?: "Khách ${complaint.userId.takeLast(8)}",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium
                )
                Text("·", color = MaterialTheme.colorScheme.outline)
                Text(complaintTypeLabel(complaint.type), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                Text("·", color = MaterialTheme.colorScheme.outline)
                Text(formatVnDate(complaint.createdAt), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
            }
            // Description
            complaint.description.takeIf { it.isNotBlank() }?.let {
                Text(
                    it,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            // Refund amount if set
            complaint.refundAmount?.let { amt ->
                Text(
                    "Hoàn tiền: ${formatCurrency(amt)}${complaint.refundMethod?.let { " · ${refundMethodLabel(it)}" } ?: ""}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium
                )
            }
            Text(
                "${complaint.attachments.size} file · ${complaint.messages.size} tin nhắn",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.outline
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { onOpenDetail(complaint.id) },
                    enabled = processingId != complaint.id
                ) { Text("Xem chi tiết") }

                if (complaint.status == "OPEN") {
                    OutlinedButton(
                        onClick = { onUpdate(complaint.id, "IN_REVIEW", complaint.priority, complaint.resolution, null, null, null, null, false) },
                        enabled = processingId != complaint.id
                    ) { Text("Tiếp nhận") }
                }
                if (complaint.status == "NEED_MORE_INFO") {
                    OutlinedButton(
                        onClick = { onUpdate(complaint.id, "IN_REVIEW", complaint.priority, complaint.resolution, null, null, null, null, false) },
                        enabled = processingId != complaint.id
                    ) { Text("Tiếp tục xử lý") }
                }
            }
        }
    }
}

@Composable
private fun ComplaintStatusStepper(status: String) {
    val isClosed = status in setOf("REJECTED", "CANCELLED")
    if (isClosed) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.errorContainer
        ) {
            Text(
                if (status == "REJECTED") "✕ Từ chối" else "✕ Đã hủy",
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                color = MaterialTheme.colorScheme.error,
                fontWeight = FontWeight.SemiBold,
                fontSize = 12.sp
            )
        }
    } else {
        val stepIndex = when (status) {
            "OPEN" -> 0
            "IN_REVIEW", "NEED_MORE_INFO" -> 1
            "APPROVED" -> 2
            "RESOLVED" -> 3
            else -> 0
        }
        val steps = listOf("Tiếp nhận", "Đang xử lý", "Duyệt", "Hoàn tất")

        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                steps.forEachIndexed { index, label ->
                    val isDone = index < stepIndex
                    val isCurrent = index == stepIndex
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = when {
                            isCurrent -> MaterialTheme.colorScheme.primary
                            isDone -> MaterialTheme.colorScheme.primaryContainer
                            else -> MaterialTheme.colorScheme.surfaceVariant
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            if (isDone) {
                                Icon(
                                    Icons.Default.Check, null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(11.dp)
                                )
                                Spacer(Modifier.width(3.dp))
                            }
                            Text(
                                label,
                                fontSize = 10.sp,
                                fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                                color = when {
                                    isCurrent -> Color.White
                                    isDone -> MaterialTheme.colorScheme.primary
                                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                                },
                                textAlign = TextAlign.Center,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                    if (index < steps.lastIndex) {
                        Text(
                            "›",
                            color = if (index < stepIndex) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            if (status == "NEED_MORE_INFO") {
                Text(
                    "⚠ Đang chờ khách hàng bổ sung thông tin",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.tertiary,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun ComplaintActionZone(
    complaint: OperationsComplaintDto,
    priority: String,
    resolution: String,
    refundAmountText: String,
    refundMethod: String,
    refundTransactionId: String,
    restoreStock: Boolean,
    isProcessing: Boolean,
    onPriorityChange: (String) -> Unit,
    onResolutionChange: (String) -> Unit,
    onRefundAmountChange: (String) -> Unit,
    onRefundMethodChange: (String) -> Unit,
    onRefundTransactionIdChange: (String) -> Unit,
    onRestoreStockChange: (Boolean) -> Unit,
    onUpdate: (String, String, String, String?, Double?, String?, String?, String?, Boolean) -> Unit
) {
    val id = complaint.id

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            when (complaint.status) {
                "OPEN" -> {
                    Text("Hành động", fontWeight = FontWeight.SemiBold)
                    Text(
                        "Khiếu nại mới — tiếp nhận để bắt đầu xử lý và thông báo cho khách.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Button(
                        onClick = { onUpdate(id, "IN_REVIEW", priority, null, null, null, null, null, false) },
                        enabled = !isProcessing,
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Tiếp nhận xử lý") }
                }

                "IN_REVIEW", "NEED_MORE_INFO" -> {
                    Text("Đang xử lý", fontWeight = FontWeight.SemiBold)

                    Text("Mức ưu tiên", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("LOW" to "Thấp", "NORMAL" to "Thường", "HIGH" to "Cao", "URGENT" to "Gấp").forEach { (v, l) ->
                            FilterPill(label = l, selected = priority == v, onClick = { onPriorityChange(v) })
                        }
                    }
                    OutlinedTextField(
                        value = resolution,
                        onValueChange = onResolutionChange,
                        label = { Text("Hướng xử lý / ghi chú nội bộ") },
                        minLines = 2,
                        modifier = Modifier.fillMaxWidth()
                    )
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (complaint.status == "IN_REVIEW") {
                            OutlinedButton(
                                onClick = { onUpdate(id, "NEED_MORE_INFO", priority, resolution.ifBlank { null }, null, null, null, null, false) },
                                enabled = !isProcessing
                            ) { Text("Cần bổ sung thông tin") }
                        }
                        Button(
                            onClick = { onUpdate(id, "APPROVED", priority, resolution.ifBlank { null }, null, null, null, null, false) },
                            enabled = !isProcessing
                        ) { Text("Duyệt khiếu nại") }
                        OutlinedButton(
                            onClick = { onUpdate(id, "REJECTED", priority, resolution.ifBlank { null }, null, null, null, null, false) },
                            enabled = !isProcessing
                        ) { Text("Từ chối") }
                    }
                }

                "APPROVED" -> {
                    Text("Hoàn tiền & Hoàn tất", fontWeight = FontWeight.SemiBold)

                    val alreadyRefunded = complaint.refundStatus in setOf("REFUNDED", "REFUND_PROCESSING")

                    if (alreadyRefunded) {
                        Surface(shape = RoundedCornerShape(10.dp), color = MaterialTheme.colorScheme.primaryContainer) {
                            Column(Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text("Hoàn tiền: ${refundStatusLabel(complaint.refundStatus)}", fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
                                complaint.refundAmount?.let { Text("Số tiền: ${formatCurrency(it)}", style = MaterialTheme.typography.bodySmall) }
                                complaint.refundTransactionId?.let { Text("Mã GD: $it", style = MaterialTheme.typography.bodySmall) }
                                complaint.refundMethod?.let { Text("Phương thức: ${refundMethodLabel(it)}", style = MaterialTheme.typography.bodySmall) }
                            }
                        }
                    } else {
                        Text("Số tiền hoàn (VND)", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                        OutlinedTextField(
                            value = refundAmountText,
                            onValueChange = { onRefundAmountChange(it.filter { ch -> ch.isDigit() || ch == '.' }) },
                            label = { Text("Nhập số tiền") },
                            modifier = Modifier.fillMaxWidth(),
                            trailingIcon = {
                                complaint.orderTotal?.let { total ->
                                    if (refundAmountText.isBlank()) {
                                        TextButton(
                                            onClick = { onRefundAmountChange(total.toLong().toString()) },
                                            contentPadding = PaddingValues(horizontal = 6.dp)
                                        ) { Text("Điền đủ", style = MaterialTheme.typography.labelSmall) }
                                    }
                                }
                            }
                        )

                        Text("Phương thức hoàn", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            listOf(
                                "ORIGINAL_PAYMENT" to "Ví/cổng cũ",
                                "BANK_TRANSFER" to "Chuyển khoản",
                                "CASH" to "Tiền mặt",
                                "POINTS" to "Điểm thưởng",
                                "OTHER" to "Khác"
                            ).forEach { (v, l) ->
                                FilterPill(label = l, selected = refundMethod == v, onClick = { onRefundMethodChange(v) })
                            }
                        }

                        if (refundMethod == "ORIGINAL_PAYMENT") {
                            Surface(shape = RoundedCornerShape(8.dp), color = MaterialTheme.colorScheme.primaryContainer) {
                                Text(
                                    "Hệ thống sẽ tự động gọi API hoàn tiền qua ví/cổng ban đầu (MoMo/ZaloPay).",
                                    modifier = Modifier.padding(10.dp),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        } else {
                            OutlinedTextField(
                                value = refundTransactionId,
                                onValueChange = onRefundTransactionIdChange,
                                label = { Text("Mã giao dịch hoàn (tùy chọn)") },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Checkbox(
                                checked = restoreStock,
                                onCheckedChange = onRestoreStockChange
                            )
                            Spacer(Modifier.width(4.dp))
                            Column {
                                Text("Khách đã trả lại hàng vật lý", style = MaterialTheme.typography.bodyMedium)
                                Text(
                                    "Tích nếu hàng được hoàn về kho (nhầm hàng, lỗi giao). Sẽ cộng lại tồn kho và đánh dấu đơn là Hoàn trả.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        val hasAmount = refundAmountText.toDoubleOrNull()?.let { it > 0 } == true
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                            if (hasAmount) {
                                if (refundMethod == "ORIGINAL_PAYMENT") {
                                    Button(
                                        onClick = {
                                            onUpdate(id, "APPROVED", priority, resolution.ifBlank { null },
                                                refundAmountText.toDoubleOrNull(), "REFUNDED", "ORIGINAL_PAYMENT", null, restoreStock)
                                        },
                                        enabled = !isProcessing,
                                        modifier = Modifier.weight(1f)
                                    ) { Text("Hoàn tiền qua cổng") }
                                } else {
                                    OutlinedButton(
                                        onClick = {
                                            onUpdate(id, "APPROVED", priority, resolution.ifBlank { null },
                                                refundAmountText.toDoubleOrNull(), "REFUNDED",
                                                refundMethod, refundTransactionId.ifBlank { null }, restoreStock)
                                        },
                                        enabled = !isProcessing,
                                        modifier = Modifier.weight(1f)
                                    ) { Text("Xác nhận đã hoàn tiền") }
                                }
                            }
                            OutlinedButton(
                                onClick = { onUpdate(id, "RESOLVED", priority, resolution.ifBlank { null }, null, null, null, null, restoreStock) },
                                enabled = !isProcessing,
                                modifier = if (hasAmount) Modifier else Modifier.fillMaxWidth()
                            ) { Text(if (hasAmount) "Hoàn tất" else "Hoàn tất (không hoàn tiền)") }
                        }
                    }

                    if (alreadyRefunded) {
                        Button(
                            onClick = { onUpdate(id, "RESOLVED", priority, resolution.ifBlank { null }, null, null, null, null, false) },
                            enabled = !isProcessing,
                            modifier = Modifier.fillMaxWidth()
                        ) { Text("Hoàn tất khiếu nại") }
                    }
                }

                "RESOLVED" -> {
                    Surface(shape = RoundedCornerShape(10.dp), color = MaterialTheme.colorScheme.primaryContainer) {
                        Column(Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("Khiếu nại đã hoàn tất", fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
                            complaint.resolvedAt?.let { Text("Đóng lúc: ${formatUtcToVnDateTime(it)}", style = MaterialTheme.typography.bodySmall) }
                            complaint.resolution?.takeIf { it.isNotBlank() }?.let { Text("Hướng xử lý: $it", style = MaterialTheme.typography.bodySmall) }
                        }
                    }
                }

                else -> {
                    Surface(shape = RoundedCornerShape(10.dp), color = MaterialTheme.colorScheme.errorContainer) {
                        Column(Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                if (complaint.status == "REJECTED") "Khiếu nại đã bị từ chối" else "Khiếu nại đã bị hủy",
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.error
                            )
                            complaint.resolution?.takeIf { it.isNotBlank() }?.let {
                                Text("Lý do: $it", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onErrorContainer)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ComplaintDetailDialog(
    complaint: OperationsComplaintDto,
    isLoading: Boolean,
    isSendingMessage: Boolean,
    processingId: String?,
    onDismiss: () -> Unit,
    onRefresh: () -> Unit,
    onUpdate: (String, String, String, String?, Double?, String?, String?, String?, Boolean) -> Unit,
    onSendMessage: (String, String, Boolean) -> Unit
) {
    var resolution by remember(complaint.id, complaint.updatedAt) { mutableStateOf(complaint.resolution.orEmpty()) }
    var priority by remember(complaint.id, complaint.updatedAt) { mutableStateOf(complaint.priority) }
    var refundAmountText by remember(complaint.id, complaint.updatedAt) {
        mutableStateOf(complaint.refundAmount?.let { editableAmount(it) }.orEmpty())
    }
    var refundMethod by remember(complaint.id, complaint.updatedAt) {
        mutableStateOf(complaint.refundMethod ?: "ORIGINAL_PAYMENT")
    }
    var refundTransactionId by remember(complaint.id, complaint.updatedAt) {
        mutableStateOf(complaint.refundTransactionId.orEmpty())
    }
    var restoreStock by remember(complaint.id) { mutableStateOf(false) }
    var reply by remember(complaint.id, complaint.messages.size) { mutableStateOf("") }
    var internalOnly by remember(complaint.id) { mutableStateOf(false) }

    val isClosed = complaint.status in setOf("RESOLVED", "REJECTED", "CANCELLED")
    val isProcessing = processingId == complaint.id

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.widthIn(min = 720.dp, max = 1080.dp),
        title = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            "${complaint.complaintCode} — ${complaint.title}",
                            fontWeight = FontWeight.Bold,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            "${complaint.userName ?: "Khách ${complaint.userId.takeLast(8)}"} · ${complaintTypeLabel(complaint.type)} · ${formatVnDate(complaint.createdAt)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                        if (isLoading) CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                        ComplaintStatusBadge(complaint.status)
                        ComplaintPriorityBadge(complaint.priority)
                    }
                }
                ComplaintStatusStepper(status = complaint.status)
            }
        },
        text = {
            LazyColumn(
                modifier = Modifier.heightIn(max = 680.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Info card
                item {
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(horizontalArrangement = Arrangement.spacedBy(20.dp), modifier = Modifier.fillMaxWidth()) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Đơn hàng", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                                    Text(complaint.orderId, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                }
                                complaint.productName?.takeIf { it.isNotBlank() }?.let { pName ->
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("Sản phẩm", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                                        Text(pName, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium, maxLines = 2, overflow = TextOverflow.Ellipsis)
                                    }
                                }
                                Column(modifier = Modifier.weight(0.8f)) {
                                    Text("Cập nhật", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                                    Text(formatUtcToVnDateTime(complaint.updatedAt), style = MaterialTheme.typography.bodySmall)
                                }
                            }
                            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                            Text(complaint.description, style = MaterialTheme.typography.bodyMedium)
                            complaint.resolution?.takeIf { it.isNotBlank() }?.let { res ->
                                Surface(shape = RoundedCornerShape(8.dp), color = MaterialTheme.colorScheme.primaryContainer) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 8.dp),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text("Hướng xử lý:", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                                        Text(res, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.onPrimaryContainer)
                                    }
                                }
                            }
                            // Refund summary (read-only if set)
                            if (complaint.refundStatus != "NONE") {
                                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                                Row(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("Hoàn tiền", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                                        Text(
                                            refundStatusLabel(complaint.refundStatus),
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.SemiBold,
                                            color = when (complaint.refundStatus) {
                                                "REFUNDED" -> MaterialTheme.colorScheme.secondary
                                                "REFUND_PROCESSING" -> MaterialTheme.colorScheme.tertiary
                                                "APPROVED" -> MaterialTheme.colorScheme.primary
                                                "REJECTED", "REFUND_FAILED" -> MaterialTheme.colorScheme.error
                                                else -> MaterialTheme.colorScheme.onSurface
                                            }
                                        )
                                    }
                                    complaint.refundAmount?.let { amt ->
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text("Số tiền", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                                            Text(formatCurrency(amt), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                    complaint.refundMethod?.let { method ->
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text("Phương thức", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                                            Text(refundMethodLabel(method), style = MaterialTheme.typography.bodySmall)
                                        }
                                    }
                                }
                                complaint.refundTransactionId?.let { txnId ->
                                    Text("Mã GD hoàn: $txnId", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                                }
                            }
                        }
                    }
                }

                // Context-aware action zone
                item {
                    ComplaintActionZone(
                        complaint = complaint,
                        priority = priority,
                        resolution = resolution,
                        refundAmountText = refundAmountText,
                        refundMethod = refundMethod,
                        refundTransactionId = refundTransactionId,
                        restoreStock = restoreStock,
                        isProcessing = isProcessing,
                        onPriorityChange = { priority = it },
                        onResolutionChange = { resolution = it },
                        onRefundAmountChange = { refundAmountText = it },
                        onRefundMethodChange = { refundMethod = it },
                        onRefundTransactionIdChange = { refundTransactionId = it },
                        onRestoreStockChange = { restoreStock = it },
                        onUpdate = onUpdate
                    )
                }

                // Attachments
                if (complaint.attachments.isNotEmpty()) {
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("File minh chứng (${complaint.attachments.size})", fontWeight = FontWeight.SemiBold)
                            complaint.attachments.forEach { ComplaintAttachmentRow(it) }
                        }
                    }
                }

                // Timeline
                item { ComplaintTimelineSection(complaint.events) }

                // Messages
                item {
                    Text("Trao đổi (${complaint.messages.size})", fontWeight = FontWeight.SemiBold)
                }
                if (complaint.messages.isEmpty()) {
                    item {
                        Text("Chưa có trao đổi nào.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                    }
                } else {
                    items(complaint.messages, key = { it.id }) { ComplaintMessageRow(it) }
                }

                // Reply section (hide for closed complaints)
                if (!isClosed) {
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = reply,
                                onValueChange = { reply = it },
                                label = { Text("Phản hồi cho khách hàng") },
                                minLines = 3,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Checkbox(checked = internalOnly, onCheckedChange = { internalOnly = it })
                                Text("Ghi chú nội bộ, không gửi cho khách")
                            }
                            Button(
                                onClick = { onSendMessage(complaint.id, reply, internalOnly); reply = "" },
                                enabled = reply.isNotBlank() && !isSendingMessage,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(if (isSendingMessage) "Đang gửi..." else "Gửi phản hồi")
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onRefresh) { Text("Tải lại") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Đóng") }
        }
    )
}

@Composable
private fun ComplaintTimelineSection(events: List<OperationsComplaintEventDto>) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Timeline xử lý (${events.size})", fontWeight = FontWeight.SemiBold)
        if (events.isEmpty()) {
            Text("Chưa có sự kiện xử lý.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
        } else {
            events.forEach { event -> ComplaintEventRow(event) }
        }
    }
}

@Composable
private fun ComplaintEventRow(event: OperationsComplaintEventDto) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(event.title, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                Text(formatUtcToVnDateTime(event.createdAt), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
            }
            Text(
                "${event.actorName ?: event.actorRole ?: "Hệ thống"} · ${event.eventType}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary
            )
            event.description?.takeIf { it.isNotBlank() }?.let { Text(it) }
            if (!event.fromStatus.isNullOrBlank() || !event.toStatus.isNullOrBlank()) {
                Text(
                    "Trạng thái: ${event.fromStatus ?: "—"} → ${event.toStatus ?: "—"}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            event.dueAt?.takeIf { it.isNotBlank() }?.let {
                Text(
                    "SLA trước: ${formatUtcToVnDateTime(it)}",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun ComplaintAttachmentRow(attachment: OperationsComplaintAttachmentDto) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    attachment.fileUrl.substringAfterLast('/').substringBefore('?'),
                    fontWeight = FontWeight.SemiBold
                )
                Text("${attachment.fileType} | ${formatUtcToVnDateTime(attachment.createdAt)}", style = MaterialTheme.typography.bodySmall)
            }
            OutlinedButton(onClick = { openExternalUrl(attachment.fileUrl) }) { Text("Mở") }
        }
    }
}

@Composable
private fun ComplaintMessageRow(message: OperationsComplaintMessageDto) {
    val isCustomer = message.senderRole == "USER"
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isCustomer) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.primaryContainer
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    "${message.senderName ?: message.senderUserId.takeLast(8)} (${message.senderRole})",
                    fontWeight = FontWeight.SemiBold
                )
                Text(formatUtcToVnDateTime(message.createdAt), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
            }
            if (message.isInternal) {
                Text("Ghi chú nội bộ", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }
            Text(message.message)
        }
    }
}

@Composable
private fun RewardsTab(
    redemptions: List<OperationsRewardRedemptionDto>,
    processingId: String?,
    onUpdateRedemption: (String, String, String?) -> Unit,
    onAdjustPoints: (String, Int, String) -> Unit
) {
    LazyColumn(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            PointAdjustmentCard(onAdjustPoints = onAdjustPoints)
        }

        item {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Yêu cầu đổi điểm",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    "${redemptions.size} yêu cầu",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
            Text(
                "Voucher: tự phát mã khi duyệt, user dùng ngay ở checkout. Quà vật lý: duyệt → gửi → hoàn tất thủ công.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.outline
            )
        }

        if (redemptions.isEmpty()) {
            item {
                Text(
                    "Chưa có yêu cầu đổi điểm nào.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }

        items(redemptions, key = { it.id }) { redemption ->
            RedemptionCard(
                redemption = redemption,
                processingId = processingId,
                onUpdateRedemption = onUpdateRedemption
            )
        }
    }
}

@Composable
private fun PointAdjustmentCard(onAdjustPoints: (String, Int, String) -> Unit) {
    var userId by remember { mutableStateOf("") }
    var points by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Điều chỉnh điểm thưởng", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            Text(
                "Nhập số dương để cộng điểm, số âm để trừ điểm. Lý do sẽ ghi vào lịch sử điểm của user.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.outline
            )
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = userId,
                    onValueChange = { userId = it },
                    label = { Text("User ID") },
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = points,
                    onValueChange = { points = it.filter { ch -> ch == '-' || ch.isDigit() } },
                    label = { Text("Số điểm (+/-)") },
                    modifier = Modifier.weight(0.5f)
                )
            }
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Lý do ghi nhận") },
                minLines = 2,
                modifier = Modifier.fillMaxWidth()
            )
            Button(
                onClick = {
                    onAdjustPoints(userId, points.toIntOrNull() ?: 0, description)
                    userId = ""; points = ""; description = ""
                },
                enabled = userId.isNotBlank() && (points.toIntOrNull() ?: 0) != 0 && description.isNotBlank(),
                modifier = Modifier.fillMaxWidth()
            ) { Text("Ghi nhận điều chỉnh điểm") }
        }
    }
}

@Composable
private fun RedemptionCard(
    redemption: OperationsRewardRedemptionDto,
    processingId: String?,
    onUpdateRedemption: (String, String, String?) -> Unit
) {
    val isVoucher = redemption.rewardType == "VOUCHER"
    val customerLabel = redemption.userName?.takeIf { it.isNotBlank() }
        ?: "Khách ${redemption.userId.takeLast(8)}"

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(redemption.productName, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(
                        if (isVoucher) "Voucher giảm giá" else "Quà vật lý",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Text(
                    redemption.status,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold,
                    style = MaterialTheme.typography.labelMedium
                )
            }
            Text(
                "$customerLabel · SL: ${redemption.quantity} · ${redemption.pointsUsed} điểm",
                style = MaterialTheme.typography.bodyMedium
            )
            if (isVoucher) {
                redemption.issuedVoucherCode?.let {
                    Text("Mã đã phát: $it", fontWeight = FontWeight.Medium)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    redemption.voucherIssuedAt?.let {
                        Text("Phát: ${formatVnDateTime(it)}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                    }
                    redemption.voucherUsedAt?.let {
                        Text("Dùng: ${formatVnDateTime(it)}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                    }
                    redemption.redeemedOrderId?.let {
                        Text("Đơn: ${it.takeLast(8)}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                    }
                }
            }
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = {
                        onUpdateRedemption(
                            redemption.id, "APPROVED",
                            if (isVoucher) "Duyệt voucher và phát mã riêng" else "Duyệt quà đổi điểm"
                        )
                    },
                    enabled = processingId != redemption.id && redemption.status == "PROCESSING"
                ) { Text(if (isVoucher) "Duyệt & phát mã" else "Duyệt") }

                if (!isVoucher) {
                    OutlinedButton(
                        onClick = { onUpdateRedemption(redemption.id, "SHIPPED", "Đã gửi quà đổi điểm") },
                        enabled = processingId != redemption.id && redemption.status in setOf("APPROVED", "SHIPPED")
                    ) { Text("Đã gửi") }
                    OutlinedButton(
                        onClick = { onUpdateRedemption(redemption.id, "DELIVERED", "User đã nhận quà đổi điểm") },
                        enabled = processingId != redemption.id && redemption.status in setOf("APPROVED", "SHIPPED")
                    ) { Text("Hoàn tất") }
                }

                OutlinedButton(
                    onClick = { onUpdateRedemption(redemption.id, "CANCELLED", "Cửa hàng hủy yêu cầu đổi điểm") },
                    enabled = processingId != redemption.id && redemption.status !in setOf("DELIVERED", "USED", "CANCELLED")
                ) { Text("Hủy") }
            }
        }
    }
}

private fun openExternalUrl(url: String) {
    runCatching {
        if (Desktop.isDesktopSupported()) {
            Desktop.getDesktop().browse(URI(url))
        }
    }
}

private fun formatCurrency(value: Double): String = "%,.0f đ".format(value).replace(",", ".")

private fun editableAmount(value: Double): String =
    if (value % 1.0 == 0.0) value.toLong().toString() else value.toString()

private fun refundStatusLabel(status: String): String = when (status.uppercase()) {
    "REQUESTED" -> "Chờ hoàn"
    "APPROVED" -> "Đã duyệt hoàn"
    "REFUND_PROCESSING" -> "Đang xử lý hoàn"
    "REFUNDED" -> "Đã hoàn tiền"
    "REJECTED" -> "Từ chối hoàn"
    "REFUND_FAILED" -> "Hoàn tiền thất bại"
    else -> "Không hoàn"
}

private fun refundMethodLabel(method: String): String = when (method.uppercase()) {
    "ORIGINAL_PAYMENT" -> "Ví/cổng thanh toán ban đầu"
    "BANK_TRANSFER" -> "Chuyển khoản"
    "CASH" -> "Tiền mặt"
    "POINTS" -> "Điểm thưởng"
    else -> "Khác"
}

private fun complaintTypeLabel(type: String): String = when (type.uppercase()) {
    "MISSING_ITEM" -> "Thiếu hàng"
    "WRONG_ITEM" -> "Sai hàng"
    "DAMAGED" -> "Hàng hỏng/vỡ"
    "COUNTERFEIT" -> "Nghi hàng giả"
    "EXPIRED" -> "Hết hạn"
    "PAYMENT" -> "Thanh toán"
    "REFUND" -> "Hoàn tiền"
    else -> "Khác"
}
