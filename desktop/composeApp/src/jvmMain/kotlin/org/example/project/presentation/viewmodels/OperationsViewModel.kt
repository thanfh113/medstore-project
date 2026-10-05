package org.example.project.presentation.viewmodels

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.example.project.data.repositories.OperationsComplaintDto
import org.example.project.data.repositories.OperationsRepository
import org.example.project.data.repositories.OperationsReviewReportDto
import org.example.project.data.repositories.OperationsReviewDto
import org.example.project.data.repositories.OperationsRewardRedemptionDto
import org.example.project.data.repositories.UpdateComplaintRequest

data class OperationsUiState(
    val isLoading: Boolean = false,
    val selectedTab: Int = 0,
    val reviews: List<OperationsReviewDto> = emptyList(),
    val reviewReports: List<OperationsReviewReportDto> = emptyList(),
    val complaints: List<OperationsComplaintDto> = emptyList(),
    val selectedComplaint: OperationsComplaintDto? = null,
    val redemptions: List<OperationsRewardRedemptionDto> = emptyList(),
    val reviewStatusFilter: String? = null,
    val reviewReportStatusFilter: String? = "OPEN",
    val complaintStatusFilter: String? = null,
    val complaintPriorityFilter: String? = null,
    val complaintTypeFilter: String? = null,
    val isComplaintDetailLoading: Boolean = false,
    val isSendingComplaintMessage: Boolean = false,
    val processingId: String? = null,
    val error: String? = null,
    val successMessage: String? = null
)

class OperationsViewModel(
    private val repository: OperationsRepository
) {
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private val _uiState = MutableStateFlow(OperationsUiState())
    val uiState: StateFlow<OperationsUiState> = _uiState.asStateFlow()

    fun selectTab(index: Int) {
        _uiState.update { it.copy(selectedTab = index) }
    }

    fun loadAll() {
        loadReviews()
        loadReviewReports()
        loadComplaints()
        loadRedemptions()
    }

    fun loadReviews() {
        scope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            repository.getReviews(_uiState.value.reviewStatusFilter).fold(
                onSuccess = { data -> _uiState.update { it.copy(isLoading = false, reviews = data) } },
                onFailure = { error -> _uiState.update { it.copy(isLoading = false, error = error.message) } }
            )
        }
    }

    fun loadReviewReports() {
        scope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            repository.getReviewReports(_uiState.value.reviewReportStatusFilter).fold(
                onSuccess = { data -> _uiState.update { it.copy(isLoading = false, reviewReports = data) } },
                onFailure = { error -> _uiState.update { it.copy(isLoading = false, error = error.message) } }
            )
        }
    }

    fun loadComplaints() {
        scope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val current = _uiState.value
            repository.getComplaints(
                status = current.complaintStatusFilter,
                priority = current.complaintPriorityFilter,
                type = current.complaintTypeFilter
            ).fold(
                onSuccess = { data -> _uiState.update { it.copy(isLoading = false, complaints = data) } },
                onFailure = { error -> _uiState.update { it.copy(isLoading = false, error = error.message) } }
            )
        }
    }

    fun loadRedemptions() {
        scope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            repository.getRewardRedemptions().fold(
                onSuccess = { data -> _uiState.update { it.copy(isLoading = false, redemptions = data) } },
                onFailure = { error -> _uiState.update { it.copy(isLoading = false, error = error.message) } }
            )
        }
    }

    fun moderateReview(reviewId: String, status: String, reason: String? = null) {
        scope.launch {
            _uiState.update { it.copy(processingId = reviewId, error = null) }
            repository.moderateReview(reviewId, status, reason).fold(
                onSuccess = {
                    _uiState.update { state -> state.copy(processingId = null, successMessage = "Da cap nhat danh gia") }
                    loadReviews()
                },
                onFailure = { error -> _uiState.update { it.copy(processingId = null, error = error.message) } }
            )
        }
    }

    fun setReviewStatusFilter(status: String?) {
        _uiState.update { it.copy(reviewStatusFilter = status) }
        loadReviews()
    }

    fun setReviewReportStatusFilter(status: String?) {
        _uiState.update { it.copy(reviewReportStatusFilter = status) }
        loadReviewReports()
    }

    fun updateReviewReport(
        reportId: String,
        reportStatus: String,
        reviewStatus: String?,
        hiddenReason: String? = null
    ) {
        scope.launch {
            _uiState.update { it.copy(processingId = reportId, error = null) }
            repository.updateReviewReport(
                id = reportId,
                status = reportStatus,
                reviewStatus = reviewStatus,
                hiddenReason = hiddenReason
            ).fold(
                onSuccess = {
                    _uiState.update { state ->
                        state.copy(
                            processingId = null,
                            successMessage = "Đã xử lý báo cáo đánh giá"
                        )
                    }
                    loadReviewReports()
                    loadReviews()
                },
                onFailure = { error -> _uiState.update { it.copy(processingId = null, error = error.message) } }
            )
        }
    }

    fun setComplaintStatusFilter(status: String?) {
        _uiState.update { it.copy(complaintStatusFilter = status) }
        loadComplaints()
    }

    fun setComplaintPriorityFilter(priority: String?) {
        _uiState.update { it.copy(complaintPriorityFilter = priority) }
        loadComplaints()
    }

    fun setComplaintTypeFilter(type: String?) {
        _uiState.update { it.copy(complaintTypeFilter = type) }
        loadComplaints()
    }

    fun updateComplaint(
        complaintId: String,
        status: String,
        priority: String,
        resolution: String? = null,
        refundAmount: Double? = null,
        refundStatus: String? = null,
        refundMethod: String? = null,
        refundTransactionId: String? = null,
        restoreStock: Boolean = false
    ) {
        scope.launch {
            _uiState.update { it.copy(processingId = complaintId, error = null) }
            repository.updateComplaint(
                complaintId,
                UpdateComplaintRequest(
                    status = status,
                    priority = priority,
                    resolution = resolution,
                    refundAmount = refundAmount,
                    refundStatus = refundStatus,
                    refundMethod = refundMethod,
                    refundTransactionId = refundTransactionId,
                    restoreStock = restoreStock
                )
            ).fold(
                onSuccess = {
                    _uiState.update { state ->
                        state.copy(
                            processingId = null,
                            selectedComplaint = if (state.selectedComplaint?.id == complaintId) it else state.selectedComplaint,
                            successMessage = "Đã cập nhật khiếu nại"
                        )
                    }
                    loadComplaints()
                },
                onFailure = { error -> _uiState.update { it.copy(processingId = null, error = error.message) } }
            )
        }
    }

    fun openComplaintDetail(complaintId: String) {
        scope.launch {
            val cached = _uiState.value.complaints.firstOrNull { it.id == complaintId }
            _uiState.update {
                it.copy(
                    selectedComplaint = cached ?: it.selectedComplaint,
                    isComplaintDetailLoading = true,
                    error = null
                )
            }
            // Nếu đang REFUND_PROCESSING → thử sync ZaloPay trước khi load lại
            val currentRefundStatus = cached?.refundStatus ?: _uiState.value.selectedComplaint?.refundStatus
            if (currentRefundStatus == "REFUND_PROCESSING") {
                repository.syncComplaintRefund(complaintId)
            }
            repository.getComplaint(complaintId).fold(
                onSuccess = { complaint ->
                    _uiState.update {
                        it.copy(
                            selectedComplaint = complaint,
                            isComplaintDetailLoading = false
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isComplaintDetailLoading = false,
                            error = error.message
                        )
                    }
                }
            )
        }
    }

    fun closeComplaintDetail() {
        _uiState.update { it.copy(selectedComplaint = null, isComplaintDetailLoading = false, isSendingComplaintMessage = false) }
    }

    fun sendComplaintMessage(complaintId: String, message: String, isInternal: Boolean = false) {
        val normalizedMessage = message.trim()
        if (normalizedMessage.isBlank()) return
        scope.launch {
            _uiState.update { it.copy(isSendingComplaintMessage = true, error = null) }
            repository.sendComplaintMessage(
                id = complaintId,
                message = normalizedMessage,
                isInternal = isInternal
            ).fold(
                onSuccess = {
                    _uiState.update { state ->
                        val current = state.selectedComplaint
                        val updatedComplaint = if (current?.id == complaintId) {
                            current.copy(messages = current.messages + it)
                        } else {
                            current
                        }
                        state.copy(
                            selectedComplaint = updatedComplaint,
                            isSendingComplaintMessage = false,
                            successMessage = "Đã gửi phản hồi"
                        )
                    }
                    openComplaintDetail(complaintId)
                },
                onFailure = { error ->
                    _uiState.update { it.copy(isSendingComplaintMessage = false, error = error.message) }
                }
            )
        }
    }

    fun updateRedemption(redemptionId: String, status: String, note: String? = null) {
        scope.launch {
            _uiState.update { it.copy(processingId = redemptionId, error = null) }
            repository.updateRewardRedemption(redemptionId, status, note).fold(
                onSuccess = {
                    _uiState.update { state -> state.copy(processingId = null, successMessage = "Da cap nhat doi diem") }
                    loadRedemptions()
                },
                onFailure = { error -> _uiState.update { it.copy(processingId = null, error = error.message) } }
            )
        }
    }

    fun adjustPoints(userId: String, points: Int, description: String) {
        scope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            repository.adjustPoints(userId, points, description).fold(
                onSuccess = {
                    _uiState.update { state -> state.copy(isLoading = false, successMessage = "Da dieu chinh diem") }
                },
                onFailure = { error -> _uiState.update { it.copy(isLoading = false, error = error.message) } }
            )
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(error = null, successMessage = null) }
    }
}
