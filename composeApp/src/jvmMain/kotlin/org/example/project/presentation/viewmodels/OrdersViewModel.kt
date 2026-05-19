package org.example.project.presentation.viewmodels

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.example.project.data.repositories.DesktopOrderRepository
import org.example.project.data.repositories.InternalOrderDetailDto
import org.example.project.data.repositories.InternalOrderItemDto
import org.example.project.data.repositories.InternalOrderSummaryDto
import org.example.project.printing.PosReceiptData
import org.example.project.printing.ReceiptPdfArchiver
import java.io.File

enum class OrderStatus(val displayName: String, val backendValue: String) {
    PENDING("Chờ xử lý", "PENDING"),
    PROCESSING("Đang xử lý", "PROCESSING"),
    SHIPPING("Đang giao", "SHIPPING"),
    DELIVERED("Hoàn thành", "DELIVERED"),
    CANCELLED("Đã hủy", "CANCELLED"),
    RETURNED("Trả hàng", "RETURNED"),
    REFUNDED("Hoàn tiền", "");  // display-only: DELIVERED + paymentStatus=REFUNDED, không có backend status riêng

    companion object {
        fun fromBackend(value: String?): OrderStatus {
            return entries.firstOrNull { it.backendValue.isNotBlank() && it.backendValue == value?.uppercase() } ?: PENDING
        }
    }
}

enum class OrderChannel(val displayName: String, val backendValue: String) {
    POS("Đơn tại quầy", "POS"),
    ONLINE("Đơn online", "ONLINE");

    companion object {
        fun fromBackend(value: String?): OrderChannel {
            return entries.firstOrNull { it.backendValue == value?.uppercase() } ?: ONLINE
        }
    }
}

data class OrderItemDto(
    val id: String,
    val name: String,
    val price: Double,
    val quantity: Int,
    val unit: String
)

data class OrderDto(
    val id: String,
    val orderCode: String,
    val channel: OrderChannel,
    val customerName: String,
    val customerPhone: String,
    val address: String,
    val total: Double,
    val status: OrderStatus,
    val paymentMethod: String,
    val paymentStatus: String,
    val cashierName: String? = null,
    val cashReceived: Double? = null,
    val cashChange: Double? = null,
    val paymentReference: String? = null,
    val paidAt: String? = null,
    val createdAt: String,
    val items: List<OrderItemDto>
)

data class OrdersUiState(
    val isLoading: Boolean = false,
    val orders: List<OrderDto> = emptyList(),
    val filteredOrders: List<OrderDto> = emptyList(),
    val selectedOrder: OrderDto? = null,
    val selectedOrderDetail: InternalOrderDetailDto? = null,
    val searchQuery: String = "",
    val selectedStatus: OrderStatus? = null,
    val selectedChannel: OrderChannel? = null,
    val error: String? = null,
    val isUpdatingStatus: Boolean = false,
    val isExportingInvoice: Boolean = false,
    val lastArchivedInvoicePath: String? = null,
    val invoiceMessage: String? = null
)

class OrdersViewModel(
    private val orderRepository: DesktopOrderRepository,
    private val receiptArchiver: ReceiptPdfArchiver = ReceiptPdfArchiver()
) {
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    private val _uiState = MutableStateFlow(OrdersUiState())
    val uiState: StateFlow<OrdersUiState> = _uiState.asStateFlow()

    init {
        loadOrders()
    }

    fun loadOrders() {
        scope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val status = _uiState.value.selectedStatus?.backendValue?.ifBlank { null }
            val channel = _uiState.value.selectedChannel?.backendValue
            val result = orderRepository.getOrders(status, channel)
            result.onSuccess { orders ->
                val mapped = orders.map { it.toOrderSummary() }
                _uiState.update { state ->
                    val selected = state.selectedOrder?.id?.let { selectedId ->
                        mapped.firstOrNull { it.id == selectedId } ?: state.selectedOrder
                    }
                    val next = state.copy(
                        isLoading = false,
                        orders = mapped,
                        selectedOrder = selected,
                        selectedOrderDetail = state.selectedOrderDetail?.takeIf { detail -> detail.id == selected?.id },
                        error = null
                    )
                    next.copy(filteredOrders = applyFilter(next.orders, next.searchQuery, next.selectedStatus, next.selectedChannel))
                }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = error.message ?: "Không thể tải danh sách đơn hàng"
                    )
                }
            }
        }
    }

    fun filterOrders(query: String, status: OrderStatus?, channel: OrderChannel?) {
        _uiState.update { state ->
            val previewFiltered = applyFilter(state.orders, query, status, channel)
            state.copy(
                searchQuery = query,
                selectedStatus = status,
                selectedChannel = channel,
                filteredOrders = previewFiltered,
                selectedOrder = state.selectedOrder?.takeIf { selected ->
                    applyFilter(listOf(selected), query, status, channel).isNotEmpty()
                },
                selectedOrderDetail = state.selectedOrderDetail?.takeIf { detail ->
                    state.selectedOrder?.let { selected ->
                        detail.id == selected.id && applyFilter(listOf(selected), query, status, channel).isNotEmpty()
                    } == true
                }
            )
        }
    }

    fun selectOrder(order: OrderDto) {
        scope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val result = orderRepository.getOrderDetail(order.id)
            result.onSuccess { detail ->
                _uiState.update { state ->
                    state.copy(
                        isLoading = false,
                        selectedOrder = detail.toOrderDetail(),
                        selectedOrderDetail = detail,
                        error = null
                    )
                }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = error.message ?: "Không thể tải chi tiết đơn hàng"
                    )
                }
            }
        }
    }

    fun updateOrderStatus(orderId: String, newStatus: OrderStatus) {
        scope.launch {
            _uiState.update { it.copy(isUpdatingStatus = true, error = null) }
            val result = orderRepository.updateOrderStatus(orderId, newStatus.backendValue)
            result.onSuccess {
                _uiState.update { state ->
                    val updatedOrders = state.orders.map { order ->
                        if (order.id == orderId) order.copy(status = newStatus) else order
                    }
                    val updatedSelected = state.selectedOrder?.takeIf { it.id == orderId }?.copy(status = newStatus)
                        ?: state.selectedOrder
                    val next = state.copy(
                        isUpdatingStatus = false,
                        orders = updatedOrders,
                        selectedOrder = updatedSelected,
                        error = null
                    )
                    next.copy(filteredOrders = applyFilter(next.orders, next.searchQuery, next.selectedStatus, next.selectedChannel))
                }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        isUpdatingStatus = false,
                        error = error.message ?: "Không thể cập nhật trạng thái đơn hàng"
                    )
                }
            }
        }
    }

    fun exportSelectedInvoicePdf() {
        val detail = _uiState.value.selectedOrderDetail
            ?: return _uiState.update { it.copy(error = "Chưa tải chi tiết đơn hàng để xuất PDF") }

        scope.launch {
            _uiState.update { it.copy(isExportingInvoice = true, error = null, invoiceMessage = null) }
            val result = receiptArchiver.archiveReceipt(PosReceiptData(order = detail))
            val archivedFile = result.getOrNull()
            val archiveError = result.exceptionOrNull()?.message
            _uiState.update {
                it.copy(
                    isExportingInvoice = false,
                    lastArchivedInvoicePath = archivedFile?.absolutePath ?: it.lastArchivedInvoicePath,
                    invoiceMessage = archivedFile?.let { file ->
                        "Đã xuất hóa đơn PDF: ${file.absolutePath}"
                    },
                    error = archiveError ?: it.error
                )
            }
        }
    }

    fun openInvoiceFolder() {
        val invoiceFile = _uiState.value.lastArchivedInvoicePath?.let(::File)
        receiptArchiver.openInvoiceDirectory(invoiceFile).fold(
            onSuccess = { directory ->
                _uiState.update {
                    it.copy(
                        invoiceMessage = "Đã mở thư mục hóa đơn: ${directory.absolutePath}",
                        error = null
                    )
                }
            },
            onFailure = { error ->
                _uiState.update { it.copy(error = error.message ?: "Không thể mở thư mục hóa đơn") }
            }
        )
    }

    fun availableStatuses(selectedChannel: OrderChannel?): List<OrderStatus> {
        return when (selectedChannel) {
            OrderChannel.POS -> listOf(
                OrderStatus.PENDING,
                OrderStatus.DELIVERED,
                OrderStatus.CANCELLED,
                OrderStatus.RETURNED
            )
            OrderChannel.ONLINE, null -> listOf(
                OrderStatus.PENDING,
                OrderStatus.PROCESSING,
                OrderStatus.SHIPPING,
                OrderStatus.DELIVERED,
                OrderStatus.CANCELLED,
                OrderStatus.RETURNED,
                OrderStatus.REFUNDED
            )
        }
    }

    private fun applyFilter(
        orders: List<OrderDto>,
        query: String,
        status: OrderStatus?,
        channel: OrderChannel?
    ): List<OrderDto> {
        return orders.filter { order ->
            val matchQuery = query.isBlank() ||
                order.orderCode.contains(query, ignoreCase = true) ||
                order.customerName.contains(query, ignoreCase = true) ||
                order.customerPhone.contains(query, ignoreCase = true)
            val matchStatus = status == null || order.status == status
            val matchChannel = channel == null || order.channel == channel
            matchQuery && matchStatus && matchChannel
        }
    }

    private fun InternalOrderSummaryDto.toOrderSummary(): OrderDto {
        val walkIn = customerId == "WALK_IN"
        val channel = OrderChannel.fromBackend(orderChannel)
        return OrderDto(
            id = id,
            orderCode = orderCode,
            channel = channel,
            customerName = customerName ?: if (walkIn) "Khách tại quầy" else customerId,
            customerPhone = customerPhone ?: if (walkIn) "Khách mua trực tiếp" else "",
            address = note ?: "",
            total = total ?: 0.0,
            status = normalizeStatus(channel, status, paymentStatus),
            paymentMethod = paymentMethod ?: "UNKNOWN",
            paymentStatus = paymentStatus,
            cashierName = null,
            cashReceived = null,
            cashChange = null,
            paymentReference = null,
            paidAt = null,
            createdAt = createdAt,
            items = emptyList()
        )
    }

    private fun InternalOrderDetailDto.toOrderDetail(): OrderDto {
        val walkIn = customerId == "WALK_IN"
        val channel = OrderChannel.fromBackend(orderChannel)
        val fullAddress = listOfNotNull(address, ward, district, province)
            .filter { it.isNotBlank() }
            .joinToString(", ")
        return OrderDto(
            id = id,
            orderCode = orderCode,
            channel = channel,
            customerName = customerName ?: if (walkIn) "Khách tại quầy" else customerId,
            customerPhone = customerPhone ?: if (walkIn) "Khách mua trực tiếp" else "",
            address = fullAddress,
            total = total ?: 0.0,
            status = normalizeStatus(channel, status, paymentStatus),
            paymentMethod = paymentMethod ?: "UNKNOWN",
            paymentStatus = paymentStatus,
            cashierName = cashierName,
            cashReceived = cashReceived,
            cashChange = cashChange,
            paymentReference = paymentReference,
            paidAt = paidAt,
            createdAt = createdAt,
            items = items.map { it.toItemDto() }
        )
    }

    private fun InternalOrderItemDto.toItemDto(): OrderItemDto {
        return OrderItemDto(
            id = id,
            name = name,
            price = price,
            quantity = quantity,
            unit = unit
        )
    }

    private fun normalizeStatus(channel: OrderChannel, rawStatus: String?, paymentStatus: String? = null): OrderStatus {
        val mapped = OrderStatus.fromBackend(rawStatus)
        return if (channel == OrderChannel.POS) {
            when (mapped) {
                OrderStatus.DELIVERED -> OrderStatus.DELIVERED
                OrderStatus.CANCELLED -> OrderStatus.CANCELLED
                OrderStatus.RETURNED -> OrderStatus.RETURNED
                else -> OrderStatus.PENDING
            }
        } else {
            // Phân biệt hoàn tiền (không trả hàng) vs trả hàng vật lý (status=RETURNED trong DB)
            if (mapped == OrderStatus.DELIVERED && paymentStatus in setOf("REFUNDED", "PARTIALLY_REFUNDED", "REFUND_PROCESSING")) {
                OrderStatus.REFUNDED
            } else {
                mapped
            }
        }
    }
}
