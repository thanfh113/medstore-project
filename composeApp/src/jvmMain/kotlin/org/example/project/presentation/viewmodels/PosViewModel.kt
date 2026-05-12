package org.example.project.presentation.viewmodels

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.example.project.data.models.Product
import org.example.project.data.repositories.CouponAdminRepository
import org.example.project.data.repositories.PosOrderItemRequest
import org.example.project.data.repositories.CreatePosOrderRequest
import org.example.project.data.repositories.DesktopOrderRepository
import org.example.project.data.repositories.PosRepository
import org.example.project.data.repositories.ProductRepository
import org.example.project.printing.PosReceiptData
import org.example.project.printing.ReceiptPdfArchiver
import java.io.File

data class PosCartItem(
    val product: Product,
    val quantity: Int
)

data class PosUiState(
    val isLoading: Boolean = false,
    val isSubmitting: Boolean = false,
    val products: List<Product> = emptyList(),
    val filteredProducts: List<Product> = emptyList(),
    val searchQuery: String = "",
    val cart: List<PosCartItem> = emptyList(),
    val couponCode: String = "",
    val appliedDiscount: Double = 0.0,
    val paymentMethod: String = "CASH",
    // ...existing code...
    val customerCode: String = "",                      // Mã khách hàng (để tích điểm)
    val currentPaymentStep: Int = 0,                    // 0: Products, 1: Checkout, 2: Processing
    val activeOrderId: String? = null,
    val activeOrderCode: String? = null,
    val activeOrderStatus: String? = null,
    val activeOrderPaymentMethod: String? = null,
    val activeOrderPaymentStatus: String? = null,
    val activeOrderTotal: Double? = null,
    val cashReceivedInput: String = "",
    val gatewayPaymentUrl: String? = null,
    val gatewayQrContent: String? = null,
    val gatewayPaymentReference: String? = null,
    val gatewayPaidAt: String? = null,
    val isGatewayWebViewVisible: Boolean = false,
    val lastCompletedOrderId: String? = null,
    val lastArchivedInvoicePath: String? = null,
    val isArchivingInvoice: Boolean = false,
    val isPollingPayment: Boolean = false,
    val isChangingPaymentMethod: Boolean = false,
    val successMessage: String? = null,
    val error: String? = null
) {
    val subtotal: Double get() = cart.sumOf { it.product.price * it.quantity }
    val total: Double get() = (subtotal - appliedDiscount).coerceAtLeast(0.0)
    val cashReceivedAmount: Double? get() = cashReceivedInput.toDoubleOrNull()
    val cashChangePreview: Double?
        get() {
            val orderTotal = activeOrderTotal ?: return null
            val received = cashReceivedAmount ?: return null
            return (received - orderTotal).takeIf { it >= 0.0 }
        }
    val isAwaitingCashConfirmation: Boolean
        get() = activeOrderId != null && activeOrderPaymentMethod == "CASH" && activeOrderPaymentStatus == "UNPAID"
    val isAwaitingGatewayPayment: Boolean
        get() = activeOrderId != null &&
            activeOrderPaymentMethod != null &&
            activeOrderPaymentMethod != "CASH" &&
            activeOrderPaymentStatus == "PENDING"
    val hasActiveOrder: Boolean get() = activeOrderId != null
}

class PosViewModel(
    private val productRepository: ProductRepository,
    private val posRepository: PosRepository,
    private val orderRepository: DesktopOrderRepository,
    private val couponRepository: CouponAdminRepository,
    private val receiptArchiver: ReceiptPdfArchiver = ReceiptPdfArchiver()
) {
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private var paymentPollingJob: Job? = null

    private val _uiState = MutableStateFlow(PosUiState())
    val uiState: StateFlow<PosUiState> = _uiState.asStateFlow()

    init {
        loadProducts()
    }

    fun loadProducts() {
        scope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            productRepository.getAllProducts().fold(
                onSuccess = { products ->
                    _uiState.update { state ->
                        state.copy(
                            isLoading = false,
                            products = products,
                            filteredProducts = filterProducts(products, state.searchQuery)
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update { it.copy(isLoading = false, error = error.message ?: "Không thể tải sản phẩm") }
                }
            )
        }
    }

    fun search(query: String) {
        _uiState.update { state ->
            state.copy(
                searchQuery = query,
                filteredProducts = filterProducts(state.products, query)
            )
        }
    }

    fun addToCart(product: Product, quantity: Int = 1) {
        _uiState.update { state ->
            if (product.stockQuantity <= 0) {
                return@update state.copy(error = "Sản phẩm đã hết hàng")
            }
            if (quantity <= 0) {
                return@update state.copy(error = "Số lượng phải lớn hơn 0")
            }
            val existing = state.cart.firstOrNull { it.product.id == product.id }
            val updated = if (existing == null) {
                if (quantity > product.stockQuantity) {
                    return@update state.copy(error = "Số lượng vượt quá tồn kho")
                }
                state.cart + PosCartItem(product, quantity)
            } else {
                val nextQuantity = existing.quantity + quantity
                if (nextQuantity > product.stockQuantity) {
                    return@update state.copy(error = "Số lượng trong giỏ đã bằng tồn kho")
                }
                state.cart.map {
                    if (it.product.id == product.id) it.copy(quantity = nextQuantity) else it
                }
            }
            state.copy(cart = updated, error = null)
        }
    }

    fun updateQuantity(productId: String, quantity: Int) {
        _uiState.update { state ->
            val current = state.cart.firstOrNull { it.product.id == productId }
            if (current != null && quantity > current.product.stockQuantity) {
                return@update state.copy(error = "Số lượng vượt quá tồn kho")
            }
            val updated = state.cart.mapNotNull {
                if (it.product.id != productId) return@mapNotNull it
                if (quantity <= 0) null else it.copy(quantity = quantity)
            }
            state.copy(cart = updated, error = null)
        }
    }

    fun removeFromCart(productId: String) {
        _uiState.update { state ->
            state.copy(cart = state.cart.filterNot { it.product.id == productId })
        }
    }

    fun setCouponCode(code: String) {
        _uiState.update { it.copy(couponCode = code) }
    }

    fun setPaymentMethod(method: String) {
        _uiState.update { it.copy(paymentMethod = method.uppercase()) }
    }

    fun setCustomerCode(code: String) {
        _uiState.update { it.copy(customerCode = code) }
    }

    fun openCheckoutReview() {
        val state = _uiState.value
        if (state.cart.isEmpty()) {
            _uiState.update { it.copy(error = "Giỏ hàng trống") }
            return
        }
        _uiState.update { it.copy(currentPaymentStep = 1, error = null) }
    }

    fun submitCheckout() {
        val state = _uiState.value
        if (state.cart.isEmpty()) {
            _uiState.update { it.copy(error = "Giỏ hàng trống") }
            return
        }
        _uiState.update { it.copy(currentPaymentStep = 3, error = null) }
        createPosOrder()
    }

    // Payment Wizard Methods
    fun nextStep() {
        val state = _uiState.value
        when (state.currentPaymentStep) {
            0 -> { // Products → Confirm
                if (state.cart.isEmpty()) {
                    _uiState.update { it.copy(error = "Giỏ hàng trống") }
                    return
                }
                _uiState.update { it.copy(currentPaymentStep = 1, error = null) }
            }
            1 -> { // Confirm → Payment Method
                _uiState.update { it.copy(currentPaymentStep = 2, error = null) }
            }
            2 -> { // Payment Method → QR/Cash
                _uiState.update { it.copy(currentPaymentStep = 3, error = null) }
                createPosOrder()
            }
        }
    }

    fun previousStep() {
        val state = _uiState.value
        if (state.currentPaymentStep > 0) {
            _uiState.update { it.copy(currentPaymentStep = state.currentPaymentStep - 1) }
        }
    }

    fun backToCheckoutFromPendingPayment() {
        paymentPollingJob?.cancel()
        _uiState.update { state ->
            state.copy(
                currentPaymentStep = 0,
                // Keep order info so user can re-initiate with a different payment method
                isChangingPaymentMethod = state.activeOrderId != null,
                cashReceivedInput = "",
                gatewayPaymentUrl = null,
                gatewayQrContent = null,
                gatewayPaymentReference = null,
                gatewayPaidAt = null,
                isGatewayWebViewVisible = false,
                isPollingPayment = false,
                isSubmitting = false,
                successMessage = null,
                error = null
            )
        }
    }

    fun backToCheckoutFromCash() {
        paymentPollingJob?.cancel()
        _uiState.update {
            it.copy(
                currentPaymentStep = 0,
                activeOrderId = null,
                activeOrderCode = null,
                activeOrderStatus = null,
                activeOrderPaymentMethod = null,
                activeOrderPaymentStatus = null,
                activeOrderTotal = null,
                isChangingPaymentMethod = false,
                cashReceivedInput = "",
                gatewayPaymentUrl = null,
                gatewayQrContent = null,
                gatewayPaymentReference = null,
                gatewayPaidAt = null,
                isGatewayWebViewVisible = false,
                isPollingPayment = false,
                isSubmitting = false,
                successMessage = null,
                error = null
            )
        }
    }

    fun changePaymentMethodForActiveOrder() {
        val state = _uiState.value
        val orderId = state.activeOrderId
        if (orderId == null) {
            submitCheckout()
            return
        }
        if (state.paymentMethod == "CASH") {
            _uiState.update { it.copy(error = "Đơn đang chờ thanh toán điện tử. Không thể đổi sang tiền mặt. Hủy đơn và tạo đơn mới nếu muốn thanh toán tiền mặt.") }
            return
        }
        _uiState.update { it.copy(isChangingPaymentMethod = false, currentPaymentStep = 3, error = null) }
        initGatewayPaymentForOrder(orderId, state.paymentMethod)
    }

    fun resetPaymentFlow() {
        paymentPollingJob?.cancel()
        _uiState.update {
            it.copy(
                currentPaymentStep = 0,
                paymentMethod = "CASH",
                customerCode = "",
                cashReceivedInput = "",
                activeOrderId = null,
                activeOrderCode = null,
                activeOrderStatus = null,
                activeOrderPaymentMethod = null,
                activeOrderPaymentStatus = null,
                activeOrderTotal = null,
                isChangingPaymentMethod = false,
                gatewayPaymentUrl = null,
                gatewayQrContent = null,
                gatewayPaymentReference = null,
                gatewayPaidAt = null,
                isGatewayWebViewVisible = false,
                isPollingPayment = false,
                isSubmitting = false,
                successMessage = null,
                error = null
            )
        }
    }

    fun setCashReceivedInput(value: String) {
        _uiState.update { it.copy(cashReceivedInput = value, error = null) }
    }

    fun applyCoupon() {
        scope.launch {
            val state = _uiState.value
            if (state.couponCode.isBlank()) {
                _uiState.update { it.copy(error = "Nhập mã giảm giá") }
                return@launch
            }
            if (state.subtotal <= 0.0) {
                _uiState.update { it.copy(error = "Giỏ hàng trống") }
                return@launch
            }

            couponRepository.validateCoupon(
                code = state.couponCode,
                orderTotal = state.subtotal,
                userId = null
            ).fold(
                onSuccess = { data ->
                    _uiState.update {
                        it.copy(
                            appliedDiscount = data.discountAmount,
                            successMessage = "Áp dụng coupon thành công",
                            error = null
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update { it.copy(error = error.message ?: "Coupon không hợp lệ") }
                }
            )
        }
    }

    fun createPosOrder() {
        scope.launch {
            val state = _uiState.value
            if (state.cart.isEmpty()) {
                _uiState.update { it.copy(error = "Giỏ hàng trống") }
                return@launch
            }
            if (state.hasActiveOrder) {
                _uiState.update { it.copy(error = "Đang có đơn POS chưa hoàn tất") }
                return@launch
            }

            _uiState.update { it.copy(isSubmitting = true, error = null, successMessage = null) }
            val request = CreatePosOrderRequest(
                items = state.cart.map {
                    PosOrderItemRequest(productId = it.product.id, quantity = it.quantity, unit = it.product.unit)
                },
                customerId = state.customerCode.trim().takeIf { it.isNotBlank() },
                paymentMethod = state.paymentMethod,
                couponCode = state.couponCode.takeIf { it.isNotBlank() }
            )

            posRepository.createOrder(request).fold(
                onSuccess = { result ->
                    val isCashOrder = result.paymentStatus == "UNPAID"
                    _uiState.update {
                        it.copy(
                            isSubmitting = false,
                            activeOrderId = result.id,
                            activeOrderCode = result.orderCode,
                            activeOrderStatus = result.status,
                            activeOrderPaymentMethod = result.paymentMethod,
                            activeOrderPaymentStatus = result.paymentStatus,
                            activeOrderTotal = result.total,
                            cashReceivedInput = "",
                            gatewayPaymentUrl = null,
                            gatewayQrContent = null,
                            gatewayPaymentReference = null,
                            gatewayPaidAt = null,
                            isGatewayWebViewVisible = false,
                            isPollingPayment = false,
                            successMessage = if (isCashOrder) {
                                "Tạo đơn POS thành công: ${result.orderCode}. Vui lòng xác nhận tiền mặt."
                            } else {
                                "Tạo đơn POS thành công: ${result.orderCode}. Đang tạo QR thanh toán ${result.paymentMethod}."
                            },
                            error = null,
                            cart = if (isCashOrder) it.cart else emptyList(),
                            couponCode = if (isCashOrder) it.couponCode else "",
                            appliedDiscount = if (isCashOrder) it.appliedDiscount else 0.0
                        )
                    }
                    if (isCashOrder) {
                        paymentPollingJob?.cancel()
                    } else {
                        initGatewayPaymentForOrder(result.id, result.paymentMethod)
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isSubmitting = false,
                            error = error.message ?: "Không thể tạo đơn POS. Vui lòng kiểm tra kết nối backend."
                        )
                    }
                }
            )
        }
    }

    fun confirmCashOrder() {
        scope.launch {
            val state = _uiState.value
            val orderId = state.activeOrderId
            if (orderId.isNullOrBlank()) {
                _uiState.update { it.copy(error = "Chua co don POS de xac nhan") }
                return@launch
            }

            val cashReceived = state.cashReceivedAmount
                ?: return@launch _uiState.update { it.copy(error = "Nhap so tien khach dua") }
            val total = state.activeOrderTotal ?: 0.0
            if (cashReceived < total) {
                _uiState.update { it.copy(error = "So tien khach dua nho hon tong don") }
                return@launch
            }

            _uiState.update { it.copy(isSubmitting = true, error = null) }
            posRepository.confirmCash(orderId, cashReceived).fold(
                onSuccess = { result ->
                    val archiveResult = archiveCompletedReceipt(
                        orderId = orderId,
                        cashReceived = result.cashReceived,
                        cashChange = result.cashChange
                    )
                    val archivedFile = archiveResult.getOrNull()
                    val archiveError = archiveResult.exceptionOrNull()?.message
                    _uiState.update {
                        it.copy(
                            isSubmitting = false,
                            currentPaymentStep = 3,
                            successMessage = buildCompletionMessage(
                                orderCode = state.activeOrderCode ?: result.id,
                                paymentLabel = "tien mat",
                                archivedFile = archivedFile
                            ),
                            error = archiveError?.let { message ->
                                "Thanh toán thành công nhưng không lưu được hóa đơn PDF: $message"
                            },
                            cart = emptyList(),
                            couponCode = "",
                            appliedDiscount = 0.0,
                            activeOrderId = orderId,
                            activeOrderCode = state.activeOrderCode ?: result.id,
                            activeOrderStatus = result.status,
                            activeOrderPaymentMethod = result.paymentMethod,
                            activeOrderPaymentStatus = result.paymentStatus,
                            activeOrderTotal = total,
                            lastCompletedOrderId = orderId,
                            lastArchivedInvoicePath = archivedFile?.absolutePath,
                            cashReceivedInput = "",
                            gatewayPaymentUrl = null,
                            gatewayQrContent = null,
                            gatewayPaymentReference = null,
                            gatewayPaidAt = null,
                            isGatewayWebViewVisible = false,
                            isPollingPayment = false
                        )
                    }
                    paymentPollingJob?.cancel()
                    loadProducts()
                },
                onFailure = { error ->
                    _uiState.update { it.copy(isSubmitting = false, error = error.message ?: "Không thể xác nhận tiền mặt") }
                }
            )
        }
    }

    fun initPendingGatewayPayment() {
        val state = _uiState.value
        val orderId = state.activeOrderId ?: return
        val paymentMethod = state.activeOrderPaymentMethod ?: return
        if (paymentMethod == "CASH") return
        _uiState.update { it.copy(currentPaymentStep = 3) }
        initGatewayPaymentForOrder(orderId, paymentMethod)
    }

    fun refreshPendingPaymentStatus() {
        val orderId = _uiState.value.activeOrderId ?: return
        scope.launch {
            refreshOrderStatus(orderId)
        }
    }

    fun openGatewayPaymentPage() {
        val paymentUrl = _uiState.value.gatewayPaymentUrl
            ?.takeIf { it.isNotBlank() }
            ?: return _uiState.update { it.copy(error = "Chua co trang thanh toan de mo") }

        _uiState.update {
            it.copy(
                gatewayPaymentUrl = paymentUrl,
                isGatewayWebViewVisible = true,
                successMessage = "Da mo trang thanh toan ${it.activeOrderPaymentMethod ?: "gateway"} cho don ${it.activeOrderCode ?: "-"}",
                error = null
            )
        }
    }

    fun closeGatewayPaymentPage() {
        _uiState.update { it.copy(isGatewayWebViewVisible = false) }
    }

    fun handleGatewayPageNavigation(url: String) {
        val normalizedUrl = url.lowercase()
        if (!normalizedUrl.contains("/api/v1/payments/pos-return")) return

        _uiState.update {
            it.copy(
                successMessage = "Da quay ve trang ket qua, dang doi cap nhat thanh toan cho don ${it.activeOrderCode ?: "-"}",
                error = null
            )
        }
        refreshPendingPaymentStatus()
    }

    fun clearMessage() {
        _uiState.update { it.copy(successMessage = null, error = null) }
    }

    fun openInvoiceFolder() {
        val invoiceFile = _uiState.value.lastArchivedInvoicePath?.let(::File)
        receiptArchiver.openInvoiceDirectory(invoiceFile).fold(
            onSuccess = { directory ->
                _uiState.update {
                    it.copy(
                        successMessage = "Da mo thu muc hoa don: ${directory.absolutePath}",
                        error = null
                    )
                }
            },
            onFailure = { error ->
                _uiState.update { it.copy(error = error.message ?: "Không thể mở thư mục hóa đơn") }
            }
        )
    }

    fun exportLastInvoicePdf() {
        val orderId = _uiState.value.lastCompletedOrderId
            ?: return _uiState.update { it.copy(error = "Chua co hoa don nao de xuat lai") }

        scope.launch {
            _uiState.update { it.copy(isArchivingInvoice = true, error = null) }
            val result = archiveCompletedReceipt(orderId)
            val archivedFile = result.getOrNull()
            val archiveError = result.exceptionOrNull()?.message
            _uiState.update {
                it.copy(
                    isArchivingInvoice = false,
                    lastArchivedInvoicePath = archivedFile?.absolutePath ?: it.lastArchivedInvoicePath,
                    successMessage = archivedFile?.let { file ->
                        "Da xuat lai hoa don PDF: ${file.absolutePath}"
                    } ?: it.successMessage,
                    error = archiveError ?: it.error
                )
            }
        }
    }

    private fun initGatewayPaymentForOrder(orderId: String, paymentMethod: String) {
        paymentPollingJob?.cancel()
        scope.launch {
            _uiState.update { it.copy(isSubmitting = true, error = null, isGatewayWebViewVisible = false) }
            posRepository.initPayment(orderId, paymentMethod).fold(
                onSuccess = { payment ->
                    _uiState.update {
                        it.copy(
                            isSubmitting = false,
                            activeOrderId = payment.id,
                            activeOrderCode = payment.orderCode,
                            activeOrderStatus = payment.status,
                            activeOrderPaymentMethod = payment.paymentMethod,
                            activeOrderPaymentStatus = payment.paymentStatus,
                            activeOrderTotal = payment.total,
                            gatewayPaymentUrl = payment.paymentUrl,
                            gatewayQrContent = payment.qrContent,
                            gatewayPaymentReference = payment.paymentReference,
                            gatewayPaidAt = null,
                            isGatewayWebViewVisible = payment.paymentUrl.isNotBlank(),
                            isPollingPayment = true,
                            successMessage = if (payment.paymentUrl.isNullOrBlank()) {
                                "Đang chờ khách quét QR ${payment.paymentMethod} cho đơn ${payment.orderCode}"
                            } else {
                                "Đang mở trang thanh toán ${payment.paymentMethod} cho đơn ${payment.orderCode}"
                            },
                            error = null
                        )
                    }
                    startPaymentPolling(orderId)
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isSubmitting = false,
                            isGatewayWebViewVisible = false,
                            isPollingPayment = false,
                            error = error.message ?: "Không thể tạo QR thanh toán POS"
                        )
                    }
                }
            )
        }
    }

    private fun startPaymentPolling(orderId: String) {
        paymentPollingJob?.cancel()
        paymentPollingJob = scope.launch {
            while (true) {
                delay(3000)
                val state = _uiState.value
                if (state.activeOrderId != orderId || !state.isAwaitingGatewayPayment) {
                    break
                }
                refreshOrderStatus(orderId, silentOnFailure = true)
            }
        }
    }

    private suspend fun refreshOrderStatus(
        orderId: String,
        silentOnFailure: Boolean = false
    ) {
        posRepository.getOrderStatus(orderId).fold(
            onSuccess = { order ->
                val completed = order.paymentStatus == "COMPLETED"
                if (completed) {
                    paymentPollingJob?.cancel()
                    val archiveResult = archiveCompletedReceipt(
                        orderId = order.id,
                        cashReceived = order.cashReceived,
                        cashChange = order.cashChange,
                        paymentReference = order.paymentReference,
                        paidAt = order.paidAt
                    )
                    val archivedFile = archiveResult.getOrNull()
                    val archiveError = archiveResult.exceptionOrNull()?.message
                    _uiState.update {
                        it.copy(
                            isSubmitting = false,
                            cart = emptyList(),
                            couponCode = "",
                            appliedDiscount = 0.0,
                            activeOrderId = order.id,
                            activeOrderCode = order.orderCode,
                            activeOrderStatus = order.status,
                            activeOrderPaymentMethod = order.paymentMethod,
                            activeOrderPaymentStatus = order.paymentStatus,
                            activeOrderTotal = order.total,
                            lastCompletedOrderId = order.id,
                            lastArchivedInvoicePath = archivedFile?.absolutePath,
                            cashReceivedInput = "",
                            gatewayPaymentUrl = null,
                            gatewayQrContent = null,
                            gatewayPaymentReference = order.paymentReference,
                            gatewayPaidAt = order.paidAt,
                            isGatewayWebViewVisible = false,
                            isPollingPayment = false,
                            successMessage = buildCompletionMessage(
                                orderCode = order.orderCode,
                                paymentLabel = order.paymentMethod ?: "dien tu",
                                archivedFile = archivedFile
                            ),
                            error = archiveError?.let { message ->
                                "Thanh toán thành công nhưng không lưu được hóa đơn PDF: $message"
                            }
                        )
                    }
                    loadProducts()
                } else {
                    _uiState.update {
                        it.copy(
                            activeOrderStatus = order.status,
                            activeOrderPaymentMethod = order.paymentMethod ?: it.activeOrderPaymentMethod,
                            activeOrderPaymentStatus = order.paymentStatus,
                            activeOrderTotal = order.total,
                            gatewayPaymentReference = order.paymentReference ?: it.gatewayPaymentReference,
                            gatewayPaidAt = order.paidAt,
                            isGatewayWebViewVisible = if (order.paymentStatus == "PENDING") it.isGatewayWebViewVisible else false,
                            isPollingPayment = order.paymentStatus == "PENDING",
                            error = if (silentOnFailure) it.error else null
                        )
                    }
                }
            },
            onFailure = { error ->
                if (!silentOnFailure) {
                    _uiState.update {
                        it.copy(
                            isPollingPayment = false,
                            error = error.message ?: "Không thể tải trạng thái thanh toán POS"
                        )
                    }
                }
            }
        )
    }

    private fun filterProducts(products: List<Product>, query: String): List<Product> {
        if (query.isBlank()) return products
        return products.filter {
            it.name.contains(query, ignoreCase = true) ||
                it.sku.orEmpty().contains(query, ignoreCase = true) ||
                it.manufacturer.contains(query, ignoreCase = true)
        }
    }

    private suspend fun archiveCompletedReceipt(
        orderId: String,
        cashReceived: Double? = null,
        cashChange: Double? = null,
        paymentReference: String? = null,
        paidAt: String? = null
    ): Result<File> {
        return orderRepository.getOrderDetail(orderId).fold(
            onSuccess = { detail ->
                receiptArchiver.archiveReceipt(
                    PosReceiptData(
                        order = detail,
                        cashReceived = cashReceived,
                        cashChange = cashChange,
                        paymentReference = paymentReference,
                        paidAt = paidAt
                    )
                )
            },
            onFailure = { error ->
                Result.failure(
                    IllegalStateException(error.message ?: "Không thể tải chi tiết đơn để lưu hóa đơn PDF")
                )
            }
        )
    }

    private fun buildCompletionMessage(
        orderCode: String,
        paymentLabel: String,
        archivedFile: File?
    ): String {
        return if (archivedFile != null) {
            "Đơn POS $orderCode đã hoàn tất thanh toán $paymentLabel. PDF: ${archivedFile.absolutePath}"
        } else {
            "Đơn POS $orderCode đã hoàn tất thanh toán $paymentLabel."
        }
    }
}
