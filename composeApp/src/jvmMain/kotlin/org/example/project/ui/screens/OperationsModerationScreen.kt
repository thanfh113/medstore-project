package org.example.project.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import org.example.project.data.repositories.OperationsComplaintAttachmentDto
import org.example.project.data.repositories.OperationsComplaintDto
import org.example.project.data.repositories.OperationsComplaintEventDto
import org.example.project.data.repositories.OperationsComplaintMessageDto
import org.example.project.data.repositories.OperationsReviewAttachmentDto
import org.example.project.data.repositories.OperationsReviewReportDto
import org.example.project.data.repositories.OperationsReviewDto
import org.example.project.data.repositories.OperationsRewardRedemptionDto
import org.example.project.presentation.viewmodels.OperationsViewModel
import java.awt.Desktop
import java.net.URI

private val reviewStatusFilters = listOf(
    null to "Tất cả",
    "VISIBLE" to "Đang hiện",
    "HIDDEN" to "Đã ẩn",
    "REMOVED" to "Đã gỡ"
)

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
    "RESOLVED" to "Đã xong",
    "REJECTED" to "Từ chối"
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Vận hành khách hàng", fontWeight = FontWeight.Bold) },
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

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                Button(onClick = viewModel::loadAll, enabled = !state.isLoading) {
                    Text(if (state.isLoading) "Đang tải..." else "Tải lại")
                }
                state.successMessage?.let {
                    Text(it, color = MaterialTheme.colorScheme.primary)
                }
                state.error?.let {
                    Text(it, color = MaterialTheme.colorScheme.error)
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
private fun FilterRow(
    title: String,
    options: List<Pair<String?, String>>,
    selected: String?,
    onSelected: (String?) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(title, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.labelLarge)
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            options.forEach { (value, label) ->
                FilterPill(label = label, selected = selected == value, onClick = { onSelected(value) })
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
        "RESOLVED" -> "Hoàn tất" to MaterialTheme.colorScheme.secondary
        "REJECTED" -> "Từ chối" to MaterialTheme.colorScheme.outline
        else -> status to MaterialTheme.colorScheme.outline
    }
    Text(label, color = color, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
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
    onHandleReport: (String, String, String?, String?) -> Unit,
    onModerate: (String, String, String?) -> Unit
) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxSize()) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                FilterRow(
                    title = "Báo cáo đánh giá",
                    options = reviewReportStatusFilters,
                    selected = reportStatusFilter,
                    onSelected = onReportStatusFilterChange
                )
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
        item {
            Spacer(Modifier.height(4.dp))
            FilterRow(
                title = "Lọc trạng thái đánh giá",
                options = reviewStatusFilters,
                selected = statusFilter,
                onSelected = onStatusFilterChange
            )
        }
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
                        Text(review.status, color = MaterialTheme.colorScheme.primary)
                    }
                    Text("Sản phẩm: ${review.productId}", style = MaterialTheme.typography.bodySmall)
                    review.title?.takeIf { it.isNotBlank() }?.let { Text(it, fontWeight = FontWeight.SemiBold) }
                    review.comment?.takeIf { it.isNotBlank() }?.let {
                        Text(it, maxLines = 3, overflow = TextOverflow.Ellipsis)
                    }
                    Text(
                        "Báo cáo: ${review.reportCount} · File: ${review.attachments.size} · Mua thật: ${if (review.isVerifiedPurchase) "Có" else "Không"} · ${review.createdAt.take(16)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
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
                Text(report.status, color = MaterialTheme.colorScheme.primary)
            }
            report.note?.takeIf { it.isNotBlank() }?.let { Text("Ghi chú: $it") }
            Text(
                "Báo cáo lúc: ${report.createdAt.take(16)} · Review: ${report.reviewId.takeLast(8)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.outline
            )
            review?.let {
                Text(
                    "${it.rating}/5 sao — ${it.userName ?: it.userId.takeLast(8)} | ${it.status}",
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
                    "${report.reason} | ${report.status} | ${report.createdAt.take(16)}",
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
                            Text("Đã xử lý lúc: ${it.take(16)} bởi ${report.handledBy?.takeLast(8) ?: "N/A"}")
                        }
                    }
                }

                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Review gốc", fontWeight = FontWeight.SemiBold)
                        if (review == null) {
                            Text("Review gốc không còn tồn tại", color = MaterialTheme.colorScheme.error)
                        } else {
                            Text("${review.rating}/5 sao — ${review.userName ?: review.userId.takeLast(8)} | ${review.status}")
                            Text("Sản phẩm: ${review.productId} | Đơn: ${review.orderId?.takeLast(8) ?: "N/A"}")
                            review.title?.takeIf { it.isNotBlank() }?.let { Text(it, fontWeight = FontWeight.SemiBold) }
                            review.comment?.takeIf { it.isNotBlank() }?.let { Text(it) }
                            Text("Mua thật: ${if (review.isVerifiedPurchase) "Có" else "Không"} | Báo cáo: ${review.reportCount} | ${review.createdAt.take(16)}")
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
                Text("${attachment.fileType} | ${attachment.createdAt.take(16)}", style = MaterialTheme.typography.bodySmall)
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
    onUpdate: (String, String, String, String?, Double?, String?, String?, String?) -> Unit
) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxSize()) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterRow("Trạng thái", complaintStatusFilters, statusFilter, onStatusFilterChange)
                FilterRow("Mức ưu tiên", complaintPriorityFilters, priorityFilter, onPriorityFilterChange)
                FilterRow("Loại khiếu nại", complaintTypeFilters, typeFilter, onTypeFilterChange)
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
    onUpdate: (String, String, String, String?, Double?, String?, String?, String?) -> Unit
) {
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
                Text(
                    "${complaint.complaintCode} — ${complaint.title}",
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ComplaintStatusBadge(complaint.status)
                    ComplaintPriorityBadge(complaint.priority)
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    complaint.userName ?: "Khách ${complaint.userId.takeLast(8)}",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text("·", color = MaterialTheme.colorScheme.outline)
                Text(
                    "Đơn ${complaint.orderId.takeLast(8)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.outline
                )
                Text("·", color = MaterialTheme.colorScheme.outline)
                Text(
                    complaint.type,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.outline
                )
            }
            complaint.description.takeIf { it.isNotBlank() }?.let {
                Text(
                    it,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                "${complaint.attachments.size} file · ${complaint.messages.size} tin nhắn · ${complaint.createdAt.take(16)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.outline
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { onOpenDetail(complaint.id) },
                    enabled = processingId != complaint.id
                ) { Text("Xem chi tiết") }

                if (complaint.status == "OPEN" || complaint.status == "NEED_MORE_INFO") {
                    OutlinedButton(
                        onClick = {
                            onUpdate(
                                complaint.id, "IN_REVIEW", complaint.priority,
                                complaint.resolution, complaint.refundAmount,
                                complaint.refundStatus, complaint.refundMethod,
                                complaint.refundTransactionId
                            )
                        },
                        enabled = processingId != complaint.id
                    ) { Text("Nhận xử lý") }
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
    onUpdate: (String, String, String, String?, Double?, String?, String?, String?) -> Unit,
    onSendMessage: (String, String, Boolean) -> Unit
) {
    var resolution by remember(complaint.id, complaint.updatedAt) { mutableStateOf(complaint.resolution.orEmpty()) }
    var priority by remember(complaint.id, complaint.updatedAt) { mutableStateOf(complaint.priority) }
    var refundAmountText by remember(complaint.id, complaint.updatedAt) {
        mutableStateOf(complaint.refundAmount?.let { editableAmount(it) }.orEmpty())
    }
    var refundStatus by remember(complaint.id, complaint.updatedAt) { mutableStateOf(complaint.refundStatus) }
    var refundMethod by remember(complaint.id, complaint.updatedAt) {
        mutableStateOf(complaint.refundMethod ?: "ORIGINAL_PAYMENT")
    }
    var refundTransactionId by remember(complaint.id, complaint.updatedAt) {
        mutableStateOf(complaint.refundTransactionId.orEmpty())
    }
    var reply by remember(complaint.id, complaint.messages.size) { mutableStateOf("") }
    var internalOnly by remember(complaint.id) { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.widthIn(min = 720.dp, max = 1040.dp),
        title = {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("${complaint.complaintCode} — ${complaint.title}", fontWeight = FontWeight.Bold)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    ComplaintStatusBadge(complaint.status)
                    Text("·", color = MaterialTheme.colorScheme.outline)
                    ComplaintPriorityBadge(complaint.priority)
                    Text("·", color = MaterialTheme.colorScheme.outline)
                    Text(
                        complaint.userName ?: "Khách ${complaint.userId.takeLast(8)}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        },
        text = {
            LazyColumn(
                modifier = Modifier.heightIn(max = 640.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Thông tin ticket", fontWeight = FontWeight.SemiBold)
                        Text("Đơn: ${complaint.orderId} | SP: ${complaint.productName ?: "Toàn đơn"}")
                        Text("Loại: ${complaint.type} | Tạo: ${complaint.createdAt.take(16)} | Cập nhật: ${complaint.updatedAt.take(16)}")
                        if (complaint.refundStatus != "NONE" || complaint.refundAmount != null) {
                            Text(
                                "Hoàn tiền: ${refundStatusLabel(complaint.refundStatus)}" +
                                    (complaint.refundMethod?.let { " · ${refundMethodLabel(it)}" } ?: "") +
                                    (complaint.refundedAt?.let { " · Đã hoàn: ${it.take(16)}" } ?: ""),
                                color = MaterialTheme.colorScheme.primary
                            )
                            complaint.refundTransactionId?.let {
                                Text("Mã giao dịch hoàn: $it", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                        complaint.refundAmount?.let { Text("Số tiền hoàn: ${formatCurrency(it)}") }
                        Text(complaint.description)
                        complaint.resolution?.takeIf { it.isNotBlank() }?.let {
                            Text("Hướng xử lý: $it", color = MaterialTheme.colorScheme.primary)
                        }
                        if (isLoading) {
                            Text("Đang tải chi tiết...", color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }

                if (complaint.attachments.isNotEmpty()) {
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("File minh chứng (${complaint.attachments.size})", fontWeight = FontWeight.SemiBold)
                            complaint.attachments.forEach { ComplaintAttachmentRow(it) }
                        }
                    }
                }

                item {
                    ComplaintTimelineSection(complaint.events)
                }

                item {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Xử lý nội bộ", fontWeight = FontWeight.SemiBold)

                        Text("Mức ưu tiên", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.outline)
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("LOW" to "Thấp", "NORMAL" to "Thường", "HIGH" to "Cao", "URGENT" to "Gấp").forEach { (value, label) ->
                                FilterPill(label = label, selected = priority == value, onClick = { priority = value })
                            }
                        }

                        OutlinedTextField(
                            value = resolution,
                            onValueChange = { resolution = it },
                            label = { Text("Hướng xử lý / ghi chú nội bộ") },
                            minLines = 2,
                            modifier = Modifier.fillMaxWidth()
                        )

                        HorizontalDivider()
                        Text("Hoàn tiền / đổi trả", fontWeight = FontWeight.SemiBold)

                        Text("Trạng thái hoàn", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.outline)
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf(
                                "NONE" to "Không hoàn",
                                "REQUESTED" to "Chờ hoàn",
                                "APPROVED" to "Duyệt hoàn",
                                "REFUNDED" to "Đã hoàn",
                                "REJECTED" to "Từ chối hoàn"
                            ).forEach { (value, label) ->
                                FilterPill(label = label, selected = refundStatus == value, onClick = { refundStatus = value })
                            }
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                value = refundAmountText,
                                onValueChange = { refundAmountText = it.filter { ch -> ch.isDigit() || ch == '.' } },
                                label = { Text("Số tiền hoàn (VND)") },
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = refundTransactionId,
                                onValueChange = { refundTransactionId = it },
                                label = { Text("Mã giao dịch hoàn") },
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Text("Phương thức hoàn", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.outline)
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf(
                                "ORIGINAL_PAYMENT" to "Ví/cổng cũ",
                                "BANK_TRANSFER" to "Chuyển khoản",
                                "CASH" to "Tiền mặt",
                                "POINTS" to "Điểm thưởng",
                                "OTHER" to "Khác"
                            ).forEach { (value, label) ->
                                FilterPill(label = label, selected = refundMethod == value, onClick = { refundMethod = value })
                            }
                        }

                        HorizontalDivider()
                        Text("Cập nhật trạng thái", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.outline)
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf(
                                "IN_REVIEW" to "Đang xử lý",
                                "NEED_MORE_INFO" to "Cần bổ sung",
                                "APPROVED" to "Duyệt",
                                "RESOLVED" to "Hoàn tất",
                                "REJECTED" to "Từ chối"
                            ).forEach { (status, label) ->
                                OutlinedButton(
                                    onClick = {
                                        val hasRefund = refundStatus != "NONE" || refundAmountText.isNotBlank()
                                        onUpdate(
                                            complaint.id, status, priority, resolution,
                                            if (hasRefund) refundAmountText.toDoubleOrNull() else null,
                                            if (hasRefund) refundStatus else null,
                                            if (hasRefund && refundStatus != "NONE") refundMethod else null,
                                            if (hasRefund && refundStatus != "NONE") refundTransactionId.ifBlank { null } else null
                                        )
                                    },
                                    enabled = processingId != complaint.id
                                ) { Text(label) }
                            }
                        }
                    }
                }

                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Trao đổi (${complaint.messages.size})", fontWeight = FontWeight.SemiBold)
                        if (complaint.messages.isEmpty()) {
                            Text("Chưa có trao đổi nào.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                        }
                    }
                }

                items(complaint.messages, key = { it.id }) { message ->
                    ComplaintMessageRow(message)
                }

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
                Text(event.createdAt.take(16), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
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
                    "SLA trước: ${it.take(16)}",
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
                Text("${attachment.fileType} | ${attachment.createdAt.take(16)}", style = MaterialTheme.typography.bodySmall)
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
                Text(message.createdAt.take(16), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
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
                        Text("Phát: ${it.take(16)}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                    }
                    redemption.voucherUsedAt?.let {
                        Text("Dùng: ${it.take(16)}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
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
    "REFUNDED" -> "Đã hoàn"
    "REJECTED" -> "Từ chối hoàn"
    else -> "Không hoàn"
}

private fun refundMethodLabel(method: String): String = when (method.uppercase()) {
    "ORIGINAL_PAYMENT" -> "Ví/cổng thanh toán ban đầu"
    "BANK_TRANSFER" -> "Chuyển khoản"
    "CASH" -> "Tiền mặt"
    "POINTS" -> "Điểm thưởng"
    else -> "Khác"
}
