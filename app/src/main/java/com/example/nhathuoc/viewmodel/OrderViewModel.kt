package com.example.nhathuoc.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nhathuoc.data.model.*
import com.example.nhathuoc.data.repository.FileUploadRepository
import com.example.nhathuoc.data.repository.OrderRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class OrderViewModel @Inject constructor(
    private val orderRepository: OrderRepository,
    private val fileUploadRepository: FileUploadRepository
) : ViewModel() {
    private val _orderState = MutableStateFlow<UiState<OrderDto>>(UiState.Idle)
    val orderState: StateFlow<UiState<OrderDto>> = _orderState.asStateFlow()

    private val _ordersListState = MutableStateFlow<UiState<OrderListResponse>>(UiState.Idle)
    val ordersListState: StateFlow<UiState<OrderListResponse>> = _ordersListState.asStateFlow()

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

    fun clearState() {
        _orderState.value = UiState.Idle
        _ordersListState.value = UiState.Idle
        _complaintState.value = UiState.Idle
        _complaintsListState.value = UiState.Idle
        _complaintDetailState.value = UiState.Idle
        _complaintMessageState.value = UiState.Idle
        _addAttachmentsState.value = UiState.Idle
        _requestRefundState.value = UiState.Idle
    }
}
