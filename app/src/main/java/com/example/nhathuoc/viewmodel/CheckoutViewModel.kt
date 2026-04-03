package com.example.nhathuoc.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nhathuoc.data.model.*
import com.example.nhathuoc.data.repository.OrderRepository
import com.example.nhathuoc.data.repository.CartRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class CheckoutState(
    val items: List<OrderItemDto> = emptyList(),
    val selectedAddressId: String? = null,
    val addresses: List<UserAddress> = emptyList(),
    val pickupType: String = "DELIVERY", // DELIVERY or PICKUP
    val branchId: String? = null,
    val paymentMethod: String = "COD", // COD, VNPAY, MOMO
    val pointsToUse: Int = 0,
    val note: String = "",
    val subtotal: Double = 0.0,
    val shipping: Double = 0.0,
    val discount: Double = 0.0,
    val tax: Double = 0.0,
    val total: Double = 0.0,
    val isLoading: Boolean = false,
    val error: String? = null
)

class CheckoutViewModel(
    private val orderRepository: OrderRepository = OrderRepository(),
    private val cartRepository: CartRepository = CartRepository()
) : ViewModel() {

    private val _checkoutState = MutableStateFlow(CheckoutState())
    val checkoutState: StateFlow<CheckoutState> = _checkoutState.asStateFlow()

    private val _currentStep = MutableStateFlow(0) // 0: address, 1: delivery, 2: payment, 3: confirm
    val currentStep: StateFlow<Int> = _currentStep.asStateFlow()

    private val _paymentUrlEvent = MutableStateFlow<String?>(null)
    val paymentUrlEvent: StateFlow<String?> = _paymentUrlEvent.asStateFlow()

    private val _orderCreatedEvent = MutableStateFlow<OrderDto?>(null)
    val orderCreatedEvent: StateFlow<OrderDto?> = _orderCreatedEvent.asStateFlow()

    init {
        loadCartAndAddresses()
    }

    private fun loadCartAndAddresses() {
        viewModelScope.launch {
            _checkoutState.value = _checkoutState.value.copy(isLoading = true)

            // TODO: Load cart items from CartRepository
            // TODO: Load user addresses from API

            _checkoutState.value = _checkoutState.value.copy(isLoading = false)
        }
    }

    fun selectAddress(addressId: String) {
        _checkoutState.value = _checkoutState.value.copy(selectedAddressId = addressId)
        calculateTotals()
    }

    fun setDeliveryType(type: String, branchId: String? = null) {
        _checkoutState.value = _checkoutState.value.copy(
            pickupType = type,
            branchId = branchId
        )
        calculateTotals()
    }

    fun setPaymentMethod(method: String) {
        _checkoutState.value = _checkoutState.value.copy(paymentMethod = method)
    }

    fun setRewardPoints(points: Int) {
        _checkoutState.value = _checkoutState.value.copy(pointsToUse = points)
        calculateTotals()
    }

    fun setNote(note: String) {
        _checkoutState.value = _checkoutState.value.copy(note = note)
    }

    fun nextStep() {
        val newStep = (_currentStep.value + 1).coerceAtMost(3)
        _currentStep.value = newStep
    }

    fun previousStep() {
        val newStep = (_currentStep.value - 1).coerceAtLeast(0)
        _currentStep.value = newStep
    }

    private fun calculateTotals() {
        val state = _checkoutState.value
        val subtotal = state.items.sumOf { it.price.toDouble() * it.quantity }
        val discount = if (subtotal > 1000000) subtotal * 0.05 else 0.0
        val shipping = if (subtotal <= 500000) 30000.0 else 0.0
        val tax = subtotal * 0.1
        val rewardDiscount = state.pointsToUse * 1000.0
        val total = subtotal - discount + shipping + tax - rewardDiscount

        _checkoutState.value = state.copy(
            subtotal = subtotal,
            discount = discount,
            shipping = shipping,
            tax = tax,
            total = total.coerceAtLeast(0.0)
        )
    }

    fun createOrder() {
        viewModelScope.launch {
            val state = _checkoutState.value
            if (state.selectedAddressId == null && state.pickupType == "DELIVERY") {
                _checkoutState.value = state.copy(error = "Vui lòng chọn địa chỉ giao hàng")
                return@launch
            }

            _checkoutState.value = state.copy(isLoading = true, error = null)

            val request = PlaceOrderRequest(
                items = state.items.map { PlaceOrderItem(it.productId, it.quantity) },
                paymentMethod = state.paymentMethod,
                pickupType = state.pickupType,
                shippingAddressId = state.selectedAddressId,
                branchId = state.branchId,
                note = state.note,
                pointsToUse = state.pointsToUse
            )

            val result = orderRepository.placeOrder(request)

            when (result) {
                is NetworkResult.Success -> {
                    _orderCreatedEvent.value = result.data.order
                    _checkoutState.value = state.copy(isLoading = false)

                    // If online payment, get payment URL
                    if (state.paymentMethod in listOf("VNPAY", "MOMO")) {
                        initiatePayment(result.data.order.id)
                    }
                }
                is NetworkResult.Error -> {
                    _checkoutState.value = state.copy(
                        isLoading = false,
                        error = result.message
                    )
                }
                is NetworkResult.Exception -> {
                    _checkoutState.value = state.copy(
                        isLoading = false,
                        error = "Lỗi kết nối: ${result.e.message}"
                    )
                }
            }
        }
    }

    private fun initiatePayment(orderId: String) {
        viewModelScope.launch {
            val state = _checkoutState.value
            val method = state.paymentMethod

            // TODO: Call payment API to get payment URL
            // When received, emit it through _paymentUrlEvent

            // Example:
            // val paymentUrl = paymentRepository.getPaymentUrl(orderId, method)
            // _paymentUrlEvent.value = paymentUrl
        }
    }

    fun onPaymentComplete() {
        _currentStep.value = 3 // Move to confirmation
    }

    fun clearState() {
        _checkoutState.value = CheckoutState()
        _currentStep.value = 0
        _paymentUrlEvent.value = null
        _orderCreatedEvent.value = null
    }
}
