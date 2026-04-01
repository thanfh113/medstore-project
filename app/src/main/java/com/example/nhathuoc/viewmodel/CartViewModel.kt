package com.example.nhathuoc.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.nhathuoc.data.model.*
import com.example.nhathuoc.data.repository.CartRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * ViewModel for shopping cart operations
 * Manages cart state, synchronization with backend, and calculations
 */
class CartViewModel(
    private val cartRepository: CartRepository
) : ViewModel() {

    // Cart data state
    private val _cartState = MutableStateFlow<UiState<CartDto>>(UiState.Idle)
    val cartState: StateFlow<UiState<CartDto>> = _cartState.asStateFlow()

    // Add to cart operation state
    private val _addToCartState = MutableStateFlow<UiState<CartItemDto>>(UiState.Idle)
    val addToCartState: StateFlow<UiState<CartItemDto>> = _addToCartState.asStateFlow()

    // Update cart item state
    private val _updateCartState = MutableStateFlow<UiState<CartItemDto>>(UiState.Idle)
    val updateCartState: StateFlow<UiState<CartItemDto>> = _updateCartState.asStateFlow()

    // Remove from cart state
    private val _removeFromCartState = MutableStateFlow<UiState<String>>(UiState.Idle)
    val removeFromCartState: StateFlow<UiState<String>> = _removeFromCartState.asStateFlow()

    // Derived flows
    val cartItems: StateFlow<List<CartItemDto>> = cartState
        .map { state ->
            if (state is UiState.Success) state.data.items else emptyList()
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalItems: StateFlow<Int> = cartState
        .map { state ->
            if (state is UiState.Success) state.data.items.sumOf { it.quantity } else 0
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val subtotal: StateFlow<Double> = cartState
        .map { state ->
            if (state is UiState.Success) {
                state.data.items.sumOf { (it.price ?: 0.0) * it.quantity }
            } else 0.0
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val totalPrice: StateFlow<Double> = cartState
        .map { state ->
            if (state is UiState.Success) state.data.totalPrice else 0.0
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val isEmpty: StateFlow<Boolean> = cartItems
        .map { it.isEmpty() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val isLoadingCart: StateFlow<Boolean> = cartState
        .map { it is UiState.Loading }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    init {
        loadCart()
    }

    /**
     * Load cart data from backend
     */
    fun loadCart() {
        viewModelScope.launch {
            _cartState.value = UiState.Loading

            when (val result = cartRepository.getCart()) {
                is NetworkResult.Success -> {
                    _cartState.value = UiState.Success(result.data)
                }
                is NetworkResult.Error -> {
                    _cartState.value = UiState.Error(result.message)
                }
                is NetworkResult.Exception -> {
                    _cartState.value = UiState.Error(
                        result.e.message ?: "Lỗi khi tải giỏ hàng"
                    )
                }
            }
        }
    }

    /**
     * Add product to cart
     */
    fun addToCart(productId: String, quantity: Int = 1) {
        viewModelScope.launch {
            _addToCartState.value = UiState.Loading

            when (val result = cartRepository.addToCart(productId, quantity)) {
                is NetworkResult.Success -> {
                    _addToCartState.value = UiState.Success(result.data)
                    // Refresh cart
                    loadCart()
                }
                is NetworkResult.Error -> {
                    _addToCartState.value = UiState.Error(result.message)
                }
                is NetworkResult.Exception -> {
                    _addToCartState.value = UiState.Error(
                        result.e.message ?: "Lỗi khi thêm vào giỏ"
                    )
                }
            }
        }
    }

    /**
     * Update quantity of cart item
     */
    fun updateQuantity(itemId: String, quantity: Int) {
        viewModelScope.launch {
            _updateCartState.value = UiState.Loading

            when (val result = cartRepository.updateCartItem(itemId, quantity)) {
                is NetworkResult.Success -> {
                    _updateCartState.value = UiState.Success(result.data)
                    // Refresh cart
                    loadCart()
                }
                is NetworkResult.Error -> {
                    _updateCartState.value = UiState.Error(result.message)
                }
                is NetworkResult.Exception -> {
                    _updateCartState.value = UiState.Error(
                        result.e.message ?: "Lỗi khi cập nhật giỏ"
                    )
                }
            }
        }
    }

    /**
     * Remove item from cart
     */
    fun removeFromCart(itemId: String) {
        viewModelScope.launch {
            _removeFromCartState.value = UiState.Loading

            when (val result = cartRepository.removeFromCart(itemId)) {
                is NetworkResult.Success -> {
                    _removeFromCartState.value = UiState.Success("Đã xóa khỏi giỏ hàng")
                    // Refresh cart
                    loadCart()
                }
                is NetworkResult.Error -> {
                    _removeFromCartState.value = UiState.Error(result.message)
                }
                is NetworkResult.Exception -> {
                    _removeFromCartState.value = UiState.Error(
                        result.e.message ?: "Lỗi khi xóa từ giỏ"
                    )
                }
            }
        }
    }

    /**
     * Clear entire cart
     */
    fun clearCart() {
        viewModelScope.launch {
            _cartState.value = UiState.Loading

            when (val result = cartRepository.clearCart()) {
                is NetworkResult.Success -> {
                    _cartState.value = UiState.Success(result.data)
                }
                is NetworkResult.Error -> {
                    _cartState.value = UiState.Error(result.message)
                }
                is NetworkResult.Exception -> {
                    _cartState.value = UiState.Error(
                        result.e.message ?: "Lỗi khi xóa giỏ hàng"
                    )
                }
            }
        }
    }

    /**
     * Clear add to cart state for UI flow
     */
    fun clearAddToCartState() {
        _addToCartState.value = UiState.Idle
    }

    /**
     * Clear update state for UI flow
     */
    fun clearUpdateState() {
        _updateCartState.value = UiState.Idle
    }

    /**
     * Clear remove state for UI flow
     */
    fun clearRemoveState() {
        _removeFromCartState.value = UiState.Idle
    }
}

/**
 * Factory for CartViewModel
 */
class CartViewModelFactory(
    private val cartRepository: CartRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CartViewModel::class.java)) {
            return CartViewModel(cartRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
