package com.example.nhathuoc.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.nhathuoc.data.model.*
import com.example.nhathuoc.data.repository.OrderRepository
import com.example.nhathuoc.data.repository.AuthRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * ViewModel for Checkout screen operations
 * Handles checkout flow, address management, and order placement
 * Follows existing patterns from HomeViewModel
 */
class CheckoutViewModel(
    private val orderRepository: OrderRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    // Checkout state
    private val _checkoutState = MutableStateFlow(CheckoutUiState())
    val checkoutState: StateFlow<CheckoutUiState> = _checkoutState.asStateFlow()

    // User addresses
    private val _addressesState = MutableStateFlow<UiState<List<CheckoutAddress>>>(UiState.Idle)
    val addressesState: StateFlow<UiState<List<CheckoutAddress>>> = _addressesState.asStateFlow()

    // Available reward points
    private val _pointsState = MutableStateFlow<UiState<Int>>(UiState.Idle)
    val pointsState: StateFlow<UiState<Int>> = _pointsState.asStateFlow()

    // Order placement state
    private val _orderPlacementState = MutableStateFlow<UiState<PlaceOrderResponse>>(UiState.Idle)
    val orderPlacementState: StateFlow<UiState<PlaceOrderResponse>> = _orderPlacementState.asStateFlow()

    // Combined loading state
    val isLoading: StateFlow<Boolean> = combine(
        _addressesState,
        _pointsState,
        _orderPlacementState
    ) { addresses, points, orderPlacement ->
        addresses is UiState.Loading ||
        points is UiState.Loading ||
        orderPlacement is UiState.Loading
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = false
    )

    init {
        loadCheckoutData()
    }

    // Load initial data for checkout
    private fun loadCheckoutData() {
        loadUserAddresses()
        loadAvailablePoints()
    }

    // Update current checkout step
    fun updateCurrentStep(step: Int) {
        _checkoutState.value = _checkoutState.value.copy(currentStep = step)
    }

    // Update selected address
    fun updateSelectedAddress(address: CheckoutAddress?) {
        _checkoutState.value = _checkoutState.value.copy(selectedAddress = address)
    }

    // Update pickup type
    fun updatePickupType(pickupType: PickupType) {
        _checkoutState.value = _checkoutState.value.copy(pickupType = pickupType)
    }

    // Update payment method
    fun updatePaymentMethod(paymentMethod: PaymentMethod) {
        _checkoutState.value = _checkoutState.value.copy(paymentMethod = paymentMethod)
    }

    // Update note
    fun updateNote(note: String) {
        _checkoutState.value = _checkoutState.value.copy(note = note)
    }

    // Update points to use
    fun updatePointsToUse(points: Int, maxPoints: Int) {
        val validPoints = maxOf(0, minOf(points, maxPoints))
        _checkoutState.value = _checkoutState.value.copy(pointsToUse = validPoints)
    }

    // Load user addresses from API
    fun loadUserAddresses() {
        viewModelScope.launch {
            _addressesState.value = UiState.Loading

            // TODO: Implement when UserRepository/API is ready
            // For now, use mock data
            try {
                // Simulate API call
                kotlinx.coroutines.delay(500)

                // Mock addresses for now
                val mockAddresses = listOf(
                    CheckoutAddress(
                        id = "1",
                        userId = "current_user",
                        fullName = "Nguyễn Văn A",
                        phone = "0123456789",
                        address = "123 Đường ABC, Phường XYZ",
                        district = "Quận 1",
                        city = "TP. Hồ Chí Minh",
                        type = AddressType.HOME.value,
                        isDefault = true,
                        createdAt = "",
                        updatedAt = ""
                    ),
                    CheckoutAddress(
                        id = "2",
                        userId = "current_user",
                        fullName = "Nguyễn Văn A",
                        phone = "0123456789",
                        address = "456 Đường DEF, Phường UVW",
                        district = "Quận 3",
                        city = "TP. Hồ Chí Minh",
                        type = AddressType.OFFICE.value,
                        isDefault = false,
                        createdAt = "",
                        updatedAt = ""
                    )
                )

                _addressesState.value = UiState.Success(mockAddresses)

                // Set default address if none selected
                if (_checkoutState.value.selectedAddress == null) {
                    val defaultAddress = mockAddresses.firstOrNull { it.isDefault }
                        ?: mockAddresses.firstOrNull()
                    _checkoutState.value = _checkoutState.value.copy(selectedAddress = defaultAddress)
                }

            } catch (e: Exception) {
                _addressesState.value = UiState.Error(
                    e.message ?: "Có lỗi xảy ra khi tải địa chỉ"
                )
            }

            // TODO: Replace with actual API call when ready
            /*
            when (val result = userRepository.getUserAddresses()) {
                is NetworkResult.Success -> {
                    _addressesState.value = UiState.Success(result.data)

                    // Set default address if none selected
                    if (_checkoutState.value.selectedAddress == null) {
                        val defaultAddress = result.data.firstOrNull { it.isDefault }
                            ?: result.data.firstOrNull()
                        _checkoutState.value = _checkoutState.value.copy(selectedAddress = defaultAddress)
                    }
                }
                is NetworkResult.Error -> {
                    _addressesState.value = UiState.Error(result.message)
                }
                is NetworkResult.Exception -> {
                    _addressesState.value = UiState.Error(
                        result.e.message ?: "Có lỗi xảy ra khi tải địa chỉ"
                    )
                }
            }
            */
        }
    }

    // Load available reward points
    fun loadAvailablePoints() {
        viewModelScope.launch {
            _pointsState.value = UiState.Loading

            try {
                // Simulate API call
                kotlinx.coroutines.delay(300)

                // Mock points for now
                _pointsState.value = UiState.Success(246)

            } catch (e: Exception) {
                _pointsState.value = UiState.Error(
                    e.message ?: "Có lỗi xảy ra khi tải điểm thưởng"
                )
            }

            // TODO: Replace with actual API call when ready
            /*
            when (val result = userRepository.getUserPoints()) {
                is NetworkResult.Success -> {
                    _pointsState.value = UiState.Success(result.data.availablePoints)
                }
                is NetworkResult.Error -> {
                    _pointsState.value = UiState.Error(result.message)
                }
                is NetworkResult.Exception -> {
                    _pointsState.value = UiState.Error(
                        result.e.message ?: "Có lỗi xảy ra khi tải điểm thưởng"
                    )
                }
            }
            */
        }
    }

    // Place order
    fun placeOrder(cartItems: List<CartItemForOrder>) {
        viewModelScope.launch {
            _orderPlacementState.value = UiState.Loading

            val currentState = _checkoutState.value

            try {
                val orderRequest = PlaceOrderRequest(
                    items = cartItems.map { item ->
                        PlaceOrderItem(
                            productId = item.productId,
                            quantity = item.quantity
                        )
                    },
                    paymentMethod = currentState.paymentMethod.value,
                    pickupType = currentState.pickupType.value,
                    shippingAddressId = if (currentState.pickupType == PickupType.DELIVERY) {
                        currentState.selectedAddress?.id
                    } else null,
                    branchId = if (currentState.pickupType == PickupType.STORE_PICKUP) {
                        // TODO: Get selected branch ID
                        "default_branch"
                    } else null,
                    note = currentState.note.ifBlank { null },
                    pointsToUse = currentState.pointsToUse
                )

                when (val result = orderRepository.placeOrder(orderRequest)) {
                    is NetworkResult.Success -> {
                        _orderPlacementState.value = UiState.Success(result.data)
                    }
                    is NetworkResult.Error -> {
                        _orderPlacementState.value = UiState.Error(result.message)
                    }
                    is NetworkResult.Exception -> {
                        _orderPlacementState.value = UiState.Error(
                            result.e.message ?: "Có lỗi xảy ra khi đặt hàng"
                        )
                    }
                }
            } catch (e: Exception) {
                _orderPlacementState.value = UiState.Error(
                    e.message ?: "Có lỗi xảy ra khi đặt hàng"
                )
            }
        }
    }

    // Calculate order totals
    fun calculateOrderTotals(cartItems: List<CartItemForOrder>): OrderTotals {
        val subtotal = cartItems.sumOf { it.price * it.quantity }
        val shippingFee = if (_checkoutState.value.pickupType == PickupType.DELIVERY) 30000.0 else 0.0
        val pointValue = _checkoutState.value.pointsToUse * 10.0 // 1 point = 10đ
        val discount = pointValue
        val total = subtotal + shippingFee - discount

        return OrderTotals(
            subtotal = subtotal,
            shippingFee = shippingFee,
            discount = discount,
            total = total
        )
    }

    // Validate current step
    fun canProceedFromStep(step: Int): Boolean {
        val currentState = _checkoutState.value
        return when (step) {
            0 -> currentState.selectedAddress != null || currentState.pickupType == PickupType.STORE_PICKUP
            1, 2, 3 -> true
            else -> false
        }
    }

    // Reset checkout state
    fun resetCheckoutState() {
        _checkoutState.value = CheckoutUiState()
        _orderPlacementState.value = UiState.Idle
    }

    // Clear order placement state
    fun clearOrderPlacementState() {
        _orderPlacementState.value = UiState.Idle
    }
}

/**
 * UI State for checkout flow
 */
data class CheckoutUiState(
    val currentStep: Int = 0,
    val selectedAddress: CheckoutAddress? = null,
    val pickupType: PickupType = PickupType.DELIVERY,
    val paymentMethod: PaymentMethod = PaymentMethod.COD,
    val note: String = "",
    val pointsToUse: Int = 0
)

/**
 * Checkout address data class
 */
data class CheckoutAddress(
    val id: String,
    val userId: String,
    val fullName: String,
    val phone: String,
    val address: String,
    val district: String,
    val city: String,
    val type: String, // AddressType value
    val isDefault: Boolean = false,
    val createdAt: String,
    val updatedAt: String
)

/**
 * Cart item for order placement
 */
data class CartItemForOrder(
    val productId: String,
    val quantity: Int,
    val price: Double
)

/**
 * Order totals calculation
 */
data class OrderTotals(
    val subtotal: Double,
    val shippingFee: Double,
    val discount: Double,
    val total: Double
)

/**
 * ViewModelFactory for CheckoutViewModel
 */
class CheckoutViewModelFactory(
    private val orderRepository: OrderRepository,
    private val authRepository: AuthRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CheckoutViewModel::class.java)) {
            return CheckoutViewModel(orderRepository, authRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}