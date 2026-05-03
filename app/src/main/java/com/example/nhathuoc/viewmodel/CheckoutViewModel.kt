package com.example.nhathuoc.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nhathuoc.data.model.AddAddressRequest
import com.example.nhathuoc.data.model.CartItemDto
import com.example.nhathuoc.data.model.CheckoutOrderSummaryDto
import com.example.nhathuoc.data.model.CheckoutRequest
import com.example.nhathuoc.data.model.CodPaymentRequest
import com.example.nhathuoc.data.model.NetworkResult
import com.example.nhathuoc.data.model.PaymentInitRequest
import com.example.nhathuoc.data.model.PaymentStatusDto
import com.example.nhathuoc.data.model.RewardVoucherDto
import com.example.nhathuoc.data.model.UserAddress
import com.example.nhathuoc.data.repository.AddressRepository
import com.example.nhathuoc.data.repository.CartRepository
import com.example.nhathuoc.data.repository.CheckoutRepository
import com.example.nhathuoc.data.repository.RewardRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlin.math.floor
import kotlin.math.min
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private const val PAYMENT_RETURN_URL = "nhathuoc://payment-return"

data class CheckoutState(
    val addresses: List<UserAddress> = emptyList(),
    val selectedAddressId: String? = null,
    val cartItems: List<CartItemDto> = emptyList(),
    val totalItems: Int = 0,
    val paymentMethod: String = "COD",
    val promoCode: String = "",
    val selectedVoucherCode: String? = null,
    val availableVouchers: List<RewardVoucherDto> = emptyList(),
    val note: String = "",
    val pointsInput: String = "",
    val useRewardPoints: Boolean = false,
    val subtotal: Double = 0.0,
    val cartDiscount: Double = 0.0,
    val promoDiscount: Double = 0.0,
    val discount: Double = 0.0,
    val shipping: Double = 0.0,
    val tax: Double = 0.0,
    val pointsToUse: Int = 0,
    val availableRewardPoints: Int = 0,
    val maxUsableRewardPoints: Int = 0,
    val estimatedRewardPoints: Int = 0,
    val total: Double = 0.0,
    val isLoading: Boolean = false,
    val error: String? = null,
    val paymentStatus: PaymentStatusDto? = null
)

@HiltViewModel
class CheckoutViewModel @Inject constructor(
    private val checkoutRepository: CheckoutRepository,
    private val addressRepository: AddressRepository,
    private val cartRepository: CartRepository,
    private val rewardRepository: RewardRepository
) : ViewModel() {
    private val _checkoutState = MutableStateFlow(CheckoutState())
    val checkoutState: StateFlow<CheckoutState> = _checkoutState.asStateFlow()

    private val _orderCreatedEvent = MutableStateFlow<CheckoutOrderSummaryDto?>(null)
    val orderCreatedEvent: StateFlow<CheckoutOrderSummaryDto?> = _orderCreatedEvent.asStateFlow()

    private val _paymentUrlEvent = MutableStateFlow<String?>(null)
    val paymentUrlEvent: StateFlow<String?> = _paymentUrlEvent.asStateFlow()

    init {
        loadCheckoutData()
    }

    fun loadCheckoutData() {
        viewModelScope.launch {
            _checkoutState.update { it.copy(isLoading = true, error = null) }

            var firstError: String? = null

            when (val result = addressRepository.getUserAddresses()) {
                is NetworkResult.Success -> {
                    _checkoutState.update { state ->
                        val addresses = result.data
                        val selectedAddressId = state.selectedAddressId
                            ?.takeIf { currentId -> addresses.any { it.id == currentId } }
                            ?: addresses.firstOrNull { it.isDefault }?.id
                            ?: addresses.firstOrNull()?.id
                        state.copy(
                            addresses = addresses,
                            selectedAddressId = selectedAddressId
                        )
                    }
                }
                is NetworkResult.Error -> {
                    firstError = result.message
                }
                is NetworkResult.Exception -> {
                    firstError = result.e.localizedMessage ?: "Không thể tải địa chỉ giao hàng"
                }
            }

            when (val result = cartRepository.getCart()) {
                is NetworkResult.Success -> {
                    val cart = result.data.data
                    val estimatedRewardPoints = cart.items.sumOf { item ->
                        (item.product?.rewardPoints ?: 0) * item.quantity
                    }
                    _checkoutState.update { state ->
                        state.copy(
                            cartItems = cart.items,
                            totalItems = cart.totalItems,
                            subtotal = cart.subtotal,
                            cartDiscount = cart.discount,
                            discount = cart.discount,
                            estimatedRewardPoints = estimatedRewardPoints,
                            pointsInput = "",
                            pointsToUse = 0
                        )
                    }
                }
                is NetworkResult.Error -> {
                    if (firstError == null) {
                        firstError = result.message
                    }
                }
                is NetworkResult.Exception -> {
                    if (firstError == null) {
                        firstError = result.e.localizedMessage ?: "Không thể tải giỏ hàng"
                    }
                }
            }

            when (val result = rewardRepository.getRewardAccount()) {
                is NetworkResult.Success -> {
                    _checkoutState.update { state ->
                        state.copy(
                            availableRewardPoints = result.data.availablePoints
                        )
                    }
                }
                is NetworkResult.Error -> {
                    if (firstError == null) {
                        firstError = result.message
                    }
                }
                is NetworkResult.Exception -> {
                    if (firstError == null) {
                        firstError = result.e.localizedMessage ?: "Không thể tải điểm thưởng"
                    }
                }
            }

            when (val result = rewardRepository.getRewardVouchers()) {
                is NetworkResult.Success -> {
                    _checkoutState.update { state ->
                        val vouchers = result.data
                        val selectedVoucherCode = state.selectedVoucherCode
                            ?.takeIf { selected -> vouchers.any { it.code.equals(selected, ignoreCase = true) } }
                            ?: state.promoCode.trim()
                                .takeIf { it.isNotBlank() && vouchers.any { voucher -> voucher.code.equals(it, ignoreCase = true) } }
                        state.copy(
                            availableVouchers = vouchers,
                            selectedVoucherCode = selectedVoucherCode
                        )
                    }
                }
                is NetworkResult.Error -> {
                    if (firstError == null) {
                        firstError = result.message
                    }
                }
                is NetworkResult.Exception -> {
                    if (firstError == null) {
                        firstError = result.e.localizedMessage ?: "Không thể tải voucher đã đổi"
                    }
                }
            }

            recalculateTotals()
            _checkoutState.update { it.copy(isLoading = false, error = firstError) }
        }
    }

    fun selectAddress(id: String) {
        _checkoutState.update { it.copy(selectedAddressId = id) }
    }

    fun addAddress(request: AddAddressRequest) {
        viewModelScope.launch {
            _checkoutState.update { it.copy(isLoading = true, error = null) }
            when (val result = addressRepository.addAddress(request)) {
                is NetworkResult.Success -> {
                    val address = result.data.address
                    _checkoutState.update { state ->
                        state.copy(
                            addresses = listOf(address) + state.addresses.filterNot { it.id == address.id },
                            selectedAddressId = address.id,
                            isLoading = false
                        )
                    }
                }
                is NetworkResult.Error -> {
                    _checkoutState.update { it.copy(isLoading = false, error = result.message) }
                }
                is NetworkResult.Exception -> {
                    _checkoutState.update {
                        it.copy(
                            isLoading = false,
                            error = result.e.localizedMessage ?: "Không thể thêm địa chỉ"
                        )
                    }
                }
            }
        }
    }

    fun setPaymentMethod(method: String) {
        _checkoutState.update {
            if (it.paymentMethod == method) it else it.copy(paymentMethod = method, paymentStatus = null)
        }
    }

    fun setPromoCode(promoCode: String) {
        _checkoutState.update { state ->
            val normalized = promoCode.trim()
            val matchedVoucherCode = state.availableVouchers
                .firstOrNull { it.code.equals(normalized, ignoreCase = true) }
                ?.code
            state.copy(
                promoCode = promoCode,
                selectedVoucherCode = matchedVoucherCode
            )
        }
        recalculateTotals()
    }

    fun selectVoucher(code: String?) {
        _checkoutState.update { state ->
            val normalized = code?.trim()?.takeIf { it.isNotBlank() }
            val nextSelected = if (
                normalized != null &&
                state.selectedVoucherCode.equals(normalized, ignoreCase = true)
            ) {
                null
            } else {
                normalized
            }
            state.copy(
                selectedVoucherCode = nextSelected,
                promoCode = nextSelected.orEmpty()
            )
        }
        recalculateTotals()
    }

    fun setNote(note: String) {
        _checkoutState.update { it.copy(note = note) }
    }

    fun setPointsInput(value: String) {
        val digitsOnly = value.filter(Char::isDigit)
        _checkoutState.update {
            it.copy(
                pointsInput = digitsOnly,
                useRewardPoints = false
            )
        }
        recalculateTotals()
    }

    fun toggleUseRewardPoints(enabled: Boolean) {
        _checkoutState.update {
            it.copy(
                useRewardPoints = enabled,
                pointsInput = if (enabled) it.maxUsableRewardPoints.toString() else ""
            )
        }
        recalculateTotals()
    }

    fun clearError() {
        _checkoutState.update { it.copy(error = null) }
    }

    fun clearPaymentProgress() {
        _checkoutState.update { it.copy(paymentStatus = null, error = null) }
    }

    fun consumePaymentUrl() {
        _paymentUrlEvent.value = null
    }

    fun consumeOrderCreatedEvent() {
        _orderCreatedEvent.value = null
    }

    private fun recalculateTotals() {
        _checkoutState.update { state ->
            val selectedVoucher = findSelectedVoucher(state)
            val promoDiscount = selectedVoucher
                ?.let { estimateVoucherDiscount(it, state.subtotal) }
                ?: 0.0
            val combinedDiscount = state.cartDiscount + promoDiscount
            val maxUsableRewardPoints = calculateMaxUsableRewardPoints(
                subtotal = state.subtotal,
                discount = combinedDiscount,
                availablePoints = state.availableRewardPoints
            )
            val requestedPoints = state.pointsInput.toIntOrNull()?.coerceAtLeast(0) ?: 0
            val appliedPoints = if (state.useRewardPoints) {
                maxUsableRewardPoints
            } else {
                requestedPoints.coerceAtMost(maxUsableRewardPoints)
            }
            val pointsValue = appliedPoints * 1000.0
            val shipping = if (state.subtotal >= 500_000.0 || state.subtotal <= 0.0) 0.0 else 30_000.0
            val taxableAmount = (state.subtotal - combinedDiscount - pointsValue).coerceAtLeast(0.0)
            val tax = taxableAmount * 0.10
            state.copy(
                pointsInput = if (state.useRewardPoints && appliedPoints > 0) appliedPoints.toString() else state.pointsInput,
                promoDiscount = promoDiscount,
                discount = combinedDiscount,
                shipping = shipping,
                tax = tax,
                pointsToUse = appliedPoints,
                maxUsableRewardPoints = maxUsableRewardPoints,
                total = (taxableAmount + shipping + tax).coerceAtLeast(0.0)
            )
        }
    }

    private fun calculateMaxUsableRewardPoints(
        subtotal: Double,
        discount: Double,
        availablePoints: Int
    ): Int {
        if (subtotal <= 0.0 || availablePoints <= 0) return 0
        val applicableValue = (subtotal - discount).coerceAtLeast(0.0)
        val orderCap = floor(applicableValue / 1000.0).toInt().coerceAtLeast(0)
        return min(availablePoints, orderCap)
    }

    fun createOrder(returnUrl: String = PAYMENT_RETURN_URL) {
        val currentState = _checkoutState.value
        if (currentState.selectedAddressId == null) {
            _checkoutState.update { it.copy(error = "Vui lòng chọn địa chỉ giao hàng") }
            return
        }
        if (currentState.cartItems.isEmpty()) {
            _checkoutState.update { it.copy(error = "Giỏ hàng của bạn đang trống") }
            return
        }

        viewModelScope.launch {
            _checkoutState.update { it.copy(isLoading = true, error = null) }
            val request = CheckoutRequest(
                addressId = currentState.selectedAddressId,
                pickupType = "DELIVERY",
                paymentMethod = currentState.paymentMethod,
                rewardPointsToUse = currentState.pointsToUse,
                promoCode = resolveActivePromoCode(currentState),
                notes = currentState.note.ifBlank { null }
            )

            when (val result = checkoutRepository.checkout(request)) {
                is NetworkResult.Success -> {
                    val response = result.data
                    val order = response.data
                    if (!response.success || order == null) {
                        _checkoutState.update {
                            it.copy(
                                isLoading = false,
                                error = response.message.ifBlank { "Không thể tạo đơn hàng" }
                            )
                        }
                        return@launch
                    }

                    _orderCreatedEvent.value = order
                    val paymentReturnUrl = buildPaymentReturnUrl(order.id, returnUrl)
                    when (currentState.paymentMethod) {
                        "MOMO" -> initiateOnlinePayment(order.id, paymentReturnUrl, "MOMO")
                        "VNPAY" -> initiateOnlinePayment(order.id, paymentReturnUrl, "VNPAY")
                        "ZALOPAY" -> initiateOnlinePayment(order.id, paymentReturnUrl, "ZALOPAY")
                        else -> createCodPayment(order.id)
                    }
                }
                is NetworkResult.Error -> {
                    _checkoutState.update { it.copy(isLoading = false, error = result.message) }
                }
                is NetworkResult.Exception -> {
                    _checkoutState.update { it.copy(isLoading = false, error = result.e.localizedMessage ?: "Lỗi tạo đơn hàng") }
                }
            }
        }
    }

    fun resumeOnlinePayment(orderId: String, returnUrl: String = PAYMENT_RETURN_URL) {
        val method = _checkoutState.value.paymentMethod
        if (method == "COD") {
            _checkoutState.update { it.copy(error = "Phương thức hiện tại không cần mở cổng thanh toán") }
            return
        }

        viewModelScope.launch {
            _checkoutState.update { it.copy(isLoading = true, error = null) }
            initiateOnlinePayment(orderId, buildPaymentReturnUrl(orderId, returnUrl), method)
        }
    }

    private suspend fun initiateOnlinePayment(orderId: String, returnUrl: String, method: String) {
        val request = PaymentInitRequest(orderId = orderId, returnUrl = returnUrl)
        val result = when (method) {
            "MOMO" -> checkoutRepository.initMomoPayment(request)
            "VNPAY" -> checkoutRepository.initVnPayPayment(request)
            "ZALOPAY" -> checkoutRepository.initZaloPayPayment(request)
            else -> null
        }
        
        if (result == null) {
            _checkoutState.update { it.copy(isLoading = false, error = "Phương thức thanh toán không được hỗ trợ") }
            return
        }
        
        when (result) {
            is NetworkResult.Success -> {
                _checkoutState.update { it.copy(isLoading = false) }
                val paymentData = result.data.data
                _paymentUrlEvent.value = paymentData.deeplink?.takeIf { it.isNotBlank() }
                    ?: paymentData.paymentUrl
            }
            is NetworkResult.Error -> {
                _checkoutState.update { it.copy(isLoading = false, error = result.message) }
            }
            is NetworkResult.Exception -> {
                _checkoutState.update { it.copy(isLoading = false, error = result.e.localizedMessage ?: "Không thể khởi tạo thanh toán") }
            }
        }
    }

    private suspend fun createCodPayment(orderId: String) {
        when (val result = checkoutRepository.createCodPayment(CodPaymentRequest(orderId))) {
            is NetworkResult.Success -> {
                _checkoutState.update { it.copy(isLoading = false) }
            }
            is NetworkResult.Error -> {
                _checkoutState.update { it.copy(isLoading = false, error = result.message) }
            }
            is NetworkResult.Exception -> {
                _checkoutState.update { it.copy(isLoading = false, error = result.e.localizedMessage ?: "Không thể tạo thanh toán COD") }
            }
        }
    }

    fun refreshPaymentStatus(orderId: String, showErrorOnFailure: Boolean = false) {
        viewModelScope.launch {
            when (val result = checkoutRepository.getPaymentStatus(orderId)) {
                is NetworkResult.Success -> {
                    _checkoutState.update { it.copy(paymentStatus = result.data.data) }
                }
                is NetworkResult.Error -> {
                    if (showErrorOnFailure) {
                        _checkoutState.update { it.copy(error = result.message) }
                    }
                }
                is NetworkResult.Exception -> {
                    if (showErrorOnFailure) {
                        _checkoutState.update {
                            it.copy(error = result.e.localizedMessage ?: "Không thể kiểm tra trạng thái thanh toán")
                        }
                    }
                }
            }
        }
    }

    fun pollPaymentStatus(orderId: String, maxAttempts: Int = 5) {
        viewModelScope.launch {
            repeat(maxAttempts) { attempt ->
                when (val result = checkoutRepository.getPaymentStatus(orderId)) {
                    is NetworkResult.Success -> {
                        val status = result.data.data
                        _checkoutState.update { it.copy(paymentStatus = status) }
                        if (status.status == "COMPLETED") {
                            return@launch
                        }
                    }
                    is NetworkResult.Error -> {
                        if (attempt == maxAttempts - 1) {
                            _checkoutState.update { it.copy(error = result.message) }
                        }
                    }
                    is NetworkResult.Exception -> {
                        if (attempt == maxAttempts - 1) {
                            _checkoutState.update {
                                it.copy(error = result.e.localizedMessage ?: "Không thể kiểm tra trạng thái thanh toán")
                            }
                        }
                    }
                }
                delay(1500)
            }
        }
    }

    private fun buildPaymentReturnUrl(orderId: String, baseReturnUrl: String): String {
        val cleanBase = baseReturnUrl.ifBlank { PAYMENT_RETURN_URL }
        val separator = if (cleanBase.contains("?")) "&" else "?"
        return "${cleanBase}${separator}orderId=$orderId"
    }

    private fun resolveActivePromoCode(state: CheckoutState): String? {
        return findSelectedVoucher(state)?.code ?: state.promoCode.trim().ifBlank { null }
    }

    private fun findSelectedVoucher(state: CheckoutState): RewardVoucherDto? {
        val activeCode = state.selectedVoucherCode?.takeIf { it.isNotBlank() }
            ?: state.promoCode.trim().ifBlank { null }
        return activeCode?.let { code ->
            state.availableVouchers.firstOrNull { it.code.equals(code, ignoreCase = true) }
        }
    }

    private fun estimateVoucherDiscount(voucher: RewardVoucherDto, subtotal: Double): Double {
        if (subtotal <= 0.0) return 0.0
        val minOrderTotal = voucher.minOrderTotal ?: 0.0
        if (subtotal < minOrderTotal) return 0.0

        val rawDiscount = when (voucher.discountType.uppercase()) {
            "PERCENT" -> subtotal * (voucher.discountValue / 100.0)
            else -> voucher.discountValue
        }
        val cappedDiscount = voucher.maxDiscountAmount?.let { maxAmount ->
            min(rawDiscount, maxAmount)
        } ?: rawDiscount
        return cappedDiscount.coerceIn(0.0, subtotal)
    }
}
