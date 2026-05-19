package com.example.nhathuoc.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nhathuoc.data.model.*
import com.example.nhathuoc.data.repository.FileUploadRepository
import com.example.nhathuoc.data.repository.OrderRepository
import com.example.nhathuoc.data.repository.ProductRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class OrderViewModel @Inject constructor(
    private val orderRepository: OrderRepository,
    private val fileUploadRepository: FileUploadRepository,
    private val productRepository: ProductRepository
) : ViewModel() {
    private val _orderState = MutableStateFlow<UiState<OrderDto>>(UiState.Idle)
    val orderState: StateFlow<UiState<OrderDto>> = _orderState.asStateFlow()

    private val _ordersListState = MutableStateFlow<UiState<OrderListResponse>>(UiState.Idle)
    val ordersListState: StateFlow<UiState<OrderListResponse>> = _ordersListState.asStateFlow()

    private val _posOrdersListState = MutableStateFlow<UiState<OrderListResponse>>(UiState.Idle)
    val posOrdersListState: StateFlow<UiState<OrderListResponse>> = _posOrdersListState.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _complaintState = MutableStateFlow<UiState<ComplaintDto>>(UiState.Idle)
    val complaintState: StateFlow<UiState<ComplaintDto>> = _complaintState.asStateFlow()

    private val _complaintsListState = MutableStateFlow<UiState<List<ComplaintDto>>>(UiState.Idle)
    val complaintsListState: StateFlow<UiState<List<ComplaintDto>>> = _complaintsListState.asStateFlow()

    private val _complaintDetailState = MutableStateFlow<UiState<ComplaintDto>>(UiState.Idle)
    val complaintDetailState: StateFlow<UiState<ComplaintDto>> = _complaintDetailState.asStateFlow()

    private val _complaintMessageState = MutableStateFlow<UiState<Unit>>(UiState.Idle)
    val complaintMessageState: StateFlow<UiState<Unit>> = _complaintMessageState.asStateFlow()

    private val _addAttachmentsState = MutableStateFlow<UiState<ComplaintDto>>(UiState.Idle)
    val addAttachmentsState: StateFlow<UiState<ComplaintDto>> = _addAttachmentsState.asStateFlow()

    private val _requestRefundState = MutableStateFlow<UiState<ComplaintDto>>(UiState.Idle)
    val requestRefundState: StateFlow<UiState<ComplaintDto>> = _requestRefundState.asStateFlow()

    private val _batchReviewState = MutableStateFlow<UiState<Unit>>(UiState.Idle)
    val batchReviewState: StateFlow<UiState<Unit>> = _batchReviewState.asStateFlow()

    fun getOrderById(orderId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _orderState.value = UiState.Loading
            when (val result = orderRepository.getOrderById(orderId)) {
                is NetworkResult.Success -> _orderState.value = UiState.Success(result.data)
                is NetworkResult.Error -> _orderState.value = UiState.Error("Lỗi: ${result.message}")
                is NetworkResult.Exception -> _orderState.value = UiState.Error("Lỗi kết nối")
            }
            _isLoading.value = false
        }
    }

    fun getPosOrders() {
        viewModelScope.launch {
            _posOrdersListState.value = UiState.Loading
            when (val result = orderRepository.getPosOrders()) {
                is NetworkResult.Success -> _posOrdersListState.value = UiState.Success(result.data)
                is NetworkResult.Error -> _posOrdersListState.value = UiState.Error("Lỗi: ${result.message}")
                is NetworkResult.Exception -> _posOrdersListState.value = UiState.Error("Lỗi kết nối")
            }
        }
    }

    fun getOrders(status: String? = null, page: Int = 1) {
        viewModelScope.launch {
            _isLoading.value = true
            _ordersListState.value = UiState.Loading
            when (val result = orderRepository.getOrders(status = status, page = page)) {
                is NetworkResult.Success -> _ordersListState.value = UiState.Success(result.data)
                is NetworkResult.Error -> _ordersListState.value = UiState.Error("Lỗi: ${result.message}")
                is NetworkResult.Exception -> _ordersListState.value = UiState.Error("Lỗi kết nối")
            }
            _isLoading.value = false
        }
    }

    fun cancelOrder(orderId: String, reason: String) {
        viewModelScope.launch {
            _isLoading.value = true
            when (val result = orderRepository.cancelOrder(orderId, reason)) {
                is NetworkResult.Success -> {
                    when (val refreshed = orderRepository.getOrderById(orderId)) {
                        is NetworkResult.Success -> _orderState.value = UiState.Success(refreshed.data)
                        is NetworkResult.Error -> _orderState.value = UiState.Error("Đã hủy đơn nhưng không tải lại được: ${refreshed.message}")
                        is NetworkResult.Exception -> _orderState.value = UiState.Error("Đã hủy đơn nhưng mất kết nối khi tải lại")
                    }
                }
                is NetworkResult.Error -> _orderState.value = UiState.Error("Lỗi: ${result.message}")
                is NetworkResult.Exception -> _orderState.value = UiState.Error("Lỗi kết nối")
            }
            _isLoading.value = false
        }
    }

    fun confirmOrderReceived(orderId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            when (val result = orderRepository.confirmOrderReceived(orderId)) {
                is NetworkResult.Success -> {
                    when (val refreshed = orderRepository.getOrderById(orderId)) {
                        is NetworkResult.Success -> _orderState.value = UiState.Success(refreshed.data)
                        is NetworkResult.Error -> _orderState.value = UiState.Error("Đã xác nhận nhận hàng nhưng không tải lại được: ${refreshed.message}")
                        is NetworkResult.Exception -> _orderState.value = UiState.Error("Đã xác nhận nhận hàng nhưng mất kết nối khi tải lại")
                    }
                }
                is NetworkResult.Error -> _orderState.value = UiState.Error("Lỗi: ${result.message}")
                is NetworkResult.Exception -> _orderState.value = UiState.Error("Lỗi kết nối")
            }
            _isLoading.value = false
        }
    }

    fun createComplaint(
        orderId: String,
        type: String,
        title: String,
        description: String,
        orderItemId: String? = null,
        productId: String? = null,
        attachmentUris: List<Uri> = emptyList()
    ) {
        viewModelScope.launch {
            _complaintState.value = UiState.Loading
            val attachments = if (attachmentUris.isNotEmpty()) {
                when (val uploadResult = fileUploadRepository.uploadEvidenceFiles(attachmentUris)) {
                    is NetworkResult.Success -> uploadResult.data.map { file ->
                        ComplaintAttachmentInput(
                            fileUrl = file.fileUrl,
                            fileType = file.fileType,
                            publicId = file.publicId
                        )
                    }
                    is NetworkResult.Error -> {
                        _complaintState.value = UiState.Error("Lỗi upload file: ${uploadResult.message}")
                        return@launch
                    }
                    is NetworkResult.Exception -> {
                        _complaintState.value = UiState.Error(uploadResult.e.message ?: "Không thể upload file")
                        return@launch
                    }
                }
            } else {
                emptyList()
            }
            val request = CreateComplaintRequest(
                orderId = orderId,
                orderItemId = orderItemId,
                productId = productId,
                type = type,
                title = title.trim(),
                description = description.trim(),
                attachments = attachments
            )
            when (val result = orderRepository.createComplaint(request)) {
                is NetworkResult.Success -> {
                    _complaintState.value = UiState.Success(result.data)
                    getComplaints()
                }
                is NetworkResult.Error -> _complaintState.value = UiState.Error("Lỗi: ${result.message}")
                is NetworkResult.Exception -> _complaintState.value = UiState.Error("Lỗi kết nối")
            }
        }
    }

    fun getComplaints() {
        viewModelScope.launch {
            _complaintsListState.value = UiState.Loading
            when (val result = orderRepository.getComplaints()) {
                is NetworkResult.Success -> _complaintsListState.value = UiState.Success(result.data)
                is NetworkResult.Error -> _complaintsListState.value = UiState.Error("Lỗi: ${result.message}")
                is NetworkResult.Exception -> _complaintsListState.value = UiState.Error("Lỗi kết nối")
            }
        }
    }

    fun getComplaintById(complaintId: String) {
        viewModelScope.launch {
            _complaintDetailState.value = UiState.Loading
            when (val result = orderRepository.getComplaintById(complaintId)) {
                is NetworkResult.Success -> _complaintDetailState.value = UiState.Success(result.data)
                is NetworkResult.Error -> _complaintDetailState.value = UiState.Error("Lỗi: ${result.message}")
                is NetworkResult.Exception -> _complaintDetailState.value = UiState.Error("Lỗi kết nối")
            }
        }
    }

    fun sendComplaintMessage(complaintId: String, message: String) {
        viewModelScope.launch {
            _complaintMessageState.value = UiState.Loading
            when (val result = orderRepository.sendComplaintMessage(complaintId, message)) {
                is NetworkResult.Success -> {
                    _complaintMessageState.value = UiState.Success(Unit)
                    getComplaintById(complaintId)
                }
                is NetworkResult.Error -> _complaintMessageState.value = UiState.Error("Lỗi: ${result.message}")
                is NetworkResult.Exception -> _complaintMessageState.value = UiState.Error("Lỗi kết nối")
            }
        }
    }

    fun addComplaintAttachments(complaintId: String, uris: List<Uri>) {
        viewModelScope.launch {
            _addAttachmentsState.value = UiState.Loading
            when (val uploadResult = fileUploadRepository.uploadEvidenceFiles(uris)) {
                is NetworkResult.Success -> {
                    val attachments = uploadResult.data.map {
                        ComplaintAttachmentInput(fileUrl = it.fileUrl, fileType = it.fileType, publicId = it.publicId)
                    }
                    when (val result = orderRepository.addComplaintAttachments(complaintId, attachments)) {
                        is NetworkResult.Success -> {
                            _addAttachmentsState.value = UiState.Success(result.data)
                            _complaintDetailState.value = UiState.Success(result.data)
                        }
                        is NetworkResult.Error -> _addAttachmentsState.value = UiState.Error("Lỗi: ${result.message}")
                        is NetworkResult.Exception -> _addAttachmentsState.value = UiState.Error("Lỗi kết nối")
                    }
                }
                is NetworkResult.Error -> _addAttachmentsState.value = UiState.Error("Lỗi upload: ${uploadResult.message}")
                is NetworkResult.Exception -> _addAttachmentsState.value = UiState.Error(uploadResult.e.message ?: "Không thể upload file")
            }
        }
    }

    fun requestRefundForComplaint(complaintId: String) {
        viewModelScope.launch {
            _requestRefundState.value = UiState.Loading
            when (val result = orderRepository.requestRefundForComplaint(complaintId)) {
                is NetworkResult.Success -> {
                    _requestRefundState.value = UiState.Success(result.data)
                    _complaintDetailState.value = UiState.Success(result.data)
                }
                is NetworkResult.Error -> _requestRefundState.value = UiState.Error("Lỗi: ${result.message}")
                is NetworkResult.Exception -> _requestRefundState.value = UiState.Error("Lỗi kết nối")
            }
        }
    }

    fun clearComplaintMessageState() {
        _complaintMessageState.value = UiState.Idle
    }

    fun clearComplaintState() {
        _complaintState.value = UiState.Idle
    }

    fun clearAddAttachmentsState() {
        _addAttachmentsState.value = UiState.Idle
    }

    fun clearRequestRefundState() {
        _requestRefundState.value = UiState.Idle
    }

    fun submitBatchReviews(
        items: List<OrderItemDto>,
        ratings: Map<String, Int>,
        titles: Map<String, String> = emptyMap(),
        comments: Map<String, String>,
        attachmentUris: Map<String, List<Uri>> = emptyMap()
    ) {
        val toReview = items.filter { (ratings[it.id] ?: 0) > 0 }
        if (toReview.isEmpty()) return
        viewModelScope.launch {
            _batchReviewState.value = UiState.Loading
            for (item in toReview) {
                val rating = ratings[item.id] ?: continue
                val uris = attachmentUris[item.id].orEmpty()
                val attachments = if (uris.isNotEmpty()) {
                    when (val uploadResult = fileUploadRepository.uploadReviewImages(uris)) {
                        is NetworkResult.Success -> uploadResult.data.mapIndexed { index, file ->
                            com.example.nhathuoc.data.model.ReviewAttachmentInput(
                                fileUrl = file.fileUrl,
                                fileType = file.fileType,
                                publicId = file.publicId,
                                sortOrder = index
                            )
                        }
                        is NetworkResult.Error -> {
                            _batchReviewState.value = UiState.Error("Lỗi upload ảnh: ${uploadResult.message}")
                            return@launch
                        }
                        is NetworkResult.Exception -> {
                            _batchReviewState.value = UiState.Error(uploadResult.e.message ?: "Không thể upload ảnh")
                            return@launch
                        }
                    }
                } else emptyList()
                val request = CreateReviewRequest(
                    orderId = item.orderId,
                    orderItemId = item.id,
                    rating = rating,
                    title = titles[item.id]?.trim()?.ifBlank { null },
                    comment = comments[item.id]?.trim()?.ifBlank { null },
                    attachments = attachments
                )
                when (val result = productRepository.createProductReview(item.productId, request)) {
                    is NetworkResult.Error -> {
                        _batchReviewState.value = UiState.Error(result.message)
                        return@launch
                    }
                    is NetworkResult.Exception -> {
                        _batchReviewState.value = UiState.Error(result.e.message ?: "Lỗi không xác định")
                        return@launch
                    }
                    else -> Unit
                }
            }
            _batchReviewState.value = UiState.Success(Unit)
        }
    }

    fun clearBatchReviewState() {
        _batchReviewState.value = UiState.Idle
    }

    fun clearState() {
        _orderState.value = UiState.Idle
        _ordersListState.value = UiState.Idle
        _complaintState.value = UiState.Idle
        _complaintsListState.value = UiState.Idle
        _complaintDetailState.value = UiState.Idle
        _complaintMessageState.value = UiState.Idle
        _addAttachmentsState.value = UiState.Idle
        _requestRefundState.value = UiState.Idle
        _batchReviewState.value = UiState.Idle
    }
}
