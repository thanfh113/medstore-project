package com.example.nhathuoc.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nhathuoc.data.model.AddCartRequest
import com.example.nhathuoc.data.model.CartDto
import com.example.nhathuoc.data.model.CartItemDto
import com.example.nhathuoc.data.model.NetworkResult
import com.example.nhathuoc.data.model.UiState
import com.example.nhathuoc.data.repository.CartRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CartUiState(
    val items: List<CartItemDto> = emptyList(),
    val totalItems: Int = 0,
    val totalPrice: Double = 0.0,
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class CartViewModel @Inject constructor(
    private val repository: CartRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(CartUiState())
    val uiState: StateFlow<CartUiState> = _uiState.asStateFlow()

    private val _cartState = MutableStateFlow<UiState<CartDto>>(UiState.Idle)
    val cartState: StateFlow<UiState<CartDto>> = _cartState.asStateFlow()

    private val _removeItemState = MutableStateFlow<UiState<Unit>>(UiState.Idle)
    val removeItemState: StateFlow<UiState<Unit>> = _removeItemState.asStateFlow()

    init {
        loadCart()
    }

    fun loadCart() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            _cartState.value = UiState.Loading
            when (val result = repository.getCart()) {
                is NetworkResult.Success -> {
                    val cart = result.data.data
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            items = cart.items,
                            totalItems = cart.totalItems,
                            totalPrice = cart.totalAmount,
                            error = null
                        )
                    }
                    _cartState.value = UiState.Success(cart)
                }
                is NetworkResult.Error -> {
                    _uiState.update { it.copy(isLoading = false, error = result.message) }
                    _cartState.value = UiState.Error(result.message)
                }
                is NetworkResult.Exception -> {
                    val message = result.e.localizedMessage ?: "Loi ket noi may chu"
                    _uiState.update { it.copy(isLoading = false, error = message) }
                    _cartState.value = UiState.Error(message, result.e)
                }
            }
        }
    }

    fun addToCart(productId: String, quantity: Int, unit: String = "Cái") {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (val result = repository.addToCart(AddCartRequest(productId, quantity, unit))) {
                is NetworkResult.Success -> {
                    loadCart()
                }
                is NetworkResult.Error -> {
                    _uiState.update { it.copy(isLoading = false, error = result.message) }
                }
                is NetworkResult.Exception -> {
                    val message = result.e.localizedMessage ?: "Loi them vao gio hang"
                    _uiState.update { it.copy(isLoading = false, error = message) }
                }
            }
        }
    }

    fun updateQuantity(itemId: String, newQuantity: Int) {
        if (newQuantity <= 0) {
            removeItem(itemId)
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (val result = repository.updateCartItem(itemId, newQuantity)) {
                is NetworkResult.Success -> {
                    loadCart()
                }
                is NetworkResult.Error -> {
                    _uiState.update { it.copy(isLoading = false, error = result.message) }
                }
                is NetworkResult.Exception -> {
                    _uiState.update { it.copy(isLoading = false, error = result.e.localizedMessage ?: "Loi ket noi may chu") }
                }
            }
        }
    }

    fun updateCartItem(itemId: String, newQuantity: Int, unit: String? = null) {
        updateQuantity(itemId, newQuantity)
    }

    fun removeItem(itemId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            _removeItemState.value = UiState.Loading
            when (val result = repository.removeCartItem(itemId)) {
                is NetworkResult.Success -> {
                    _removeItemState.value = UiState.Success(Unit)
                    loadCart()
                }
                is NetworkResult.Error -> {
                    _uiState.update { it.copy(isLoading = false, error = result.message) }
                    _removeItemState.value = UiState.Error(result.message)
                }
                is NetworkResult.Exception -> {
                    val message = result.e.localizedMessage ?: "Loi ket noi may chu"
                    _uiState.update { it.copy(isLoading = false, error = message) }
                    _removeItemState.value = UiState.Error(message, result.e)
                }
            }
        }
    }

    fun removeCartItem(itemId: String) {
        removeItem(itemId)
    }

    fun clearCart() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (val result = repository.clearCart()) {
                is NetworkResult.Success -> {
                    loadCart()
                }
                is NetworkResult.Error -> {
                    _uiState.update { it.copy(isLoading = false, error = result.message) }
                }
                is NetworkResult.Exception -> {
                    _uiState.update { it.copy(isLoading = false, error = result.e.localizedMessage ?: "Loi ket noi may chu") }
                }
            }
        }
    }
}
