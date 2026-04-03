package com.example.nhathuoc.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.nhathuoc.data.model.CartDto
import com.example.nhathuoc.data.model.AddCartResponse
import com.example.nhathuoc.data.model.NetworkResult
import com.example.nhathuoc.data.model.UiState
import com.example.nhathuoc.data.repository.CartRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import android.util.Log

class CartViewModel(
    private val cartRepository: CartRepository
) : ViewModel() {

    private val _cartState = MutableStateFlow<UiState<CartDto>>(UiState.Idle)
    val cartState: StateFlow<UiState<CartDto>> = _cartState.asStateFlow()

    private val _addToCartState = MutableStateFlow<UiState<AddCartResponse>>(UiState.Idle)
    val addToCartState: StateFlow<UiState<AddCartResponse>> = _addToCartState.asStateFlow()

    private val _removeItemState = MutableStateFlow<UiState<String>>(UiState.Idle)
    val removeItemState: StateFlow<UiState<String>> = _removeItemState.asStateFlow()

    /**
     * Load user's shopping cart
     */
    fun loadCart() {
        Log.d("CartViewModel", "📦 loadCart() called")
        viewModelScope.launch {
            _cartState.value = UiState.Loading
            Log.d("CartViewModel", "⏳ Loading cart...")

            when (val result = cartRepository.getCart()) {
                is NetworkResult.Success -> {
                    Log.d("CartViewModel", "✅ Cart loaded: ${result.data.items.size} items")
                    _cartState.value = UiState.Success(result.data)
                }
                is NetworkResult.Error -> {
                    Log.e("CartViewModel", "❌ Cart error: ${result.message}")
                    _cartState.value = UiState.Error(result.message)
                }
                is NetworkResult.Exception -> {
                    Log.e("CartViewModel", "❌ Cart exception: ${result.e.message}")
                    _cartState.value = UiState.Error(
                        result.e.message ?: "Lỗi tải giỏ hàng"
                    )
                }
            }
        }
    }

    /**
     * Add product to cart
     */
    fun addToCart(productId: String, quantity: Int = 1, unit: String = "Hộp") {
        Log.d("CartViewModel", "🛒 addToCart() called: productId=$productId, qty=$quantity, unit=$unit")
        viewModelScope.launch {
            _addToCartState.value = UiState.Loading
            Log.d("CartViewModel", "⏳ Adding to cart...")

            when (val result = cartRepository.addToCart(productId, quantity, unit)) {
                is NetworkResult.Success -> {
                    Log.d("CartViewModel", "✅ Added to cart successfully")
                    _addToCartState.value = UiState.Success(result.data)
                    // Reload cart after adding
                    loadCart()
                }
                is NetworkResult.Error -> {
                    Log.e("CartViewModel", "❌ Add to cart error: ${result.message}")
                    _addToCartState.value = UiState.Error(result.message)
                }
                is NetworkResult.Exception -> {
                    Log.e("CartViewModel", "❌ Add to cart exception: ${result.e.message}")
                    _addToCartState.value = UiState.Error(
                        result.e.message ?: "Lỗi thêm vào giỏ hàng"
                    )
                }
            }
        }
    }

    /**
     * Update cart item quantity or unit
     */
    fun updateCartItem(itemId: String, quantity: Int, unit: String) {
        Log.d("CartViewModel", "📝 updateCartItem() called: itemId=$itemId, qty=$quantity, unit=$unit")
        viewModelScope.launch {
            _cartState.value = UiState.Loading

            when (val result = cartRepository.updateCartItem(itemId, quantity, unit)) {
                is NetworkResult.Success -> {
                    Log.d("CartViewModel", "✅ Cart item updated")
                    _cartState.value = UiState.Success(result.data.cart)
                }
                is NetworkResult.Error -> {
                    Log.e("CartViewModel", "❌ Update error: ${result.message}")
                    _cartState.value = UiState.Error(result.message)
                }
                is NetworkResult.Exception -> {
                    Log.e("CartViewModel", "❌ Update exception: ${result.e.message}")
                    _cartState.value = UiState.Error(result.e.message ?: "Lỗi cập nhật giỏ hàng")
                }
            }
        }
    }

    /**
     * Remove item from cart
     */
    fun removeCartItem(itemId: String) {
        Log.d("CartViewModel", "🗑️ removeCartItem() called: itemId=$itemId")
        viewModelScope.launch {
            _removeItemState.value = UiState.Loading

            when (val result = cartRepository.removeCartItem(itemId)) {
                is NetworkResult.Success -> {
                    Log.d("CartViewModel", "✅ Item removed from cart")
                    _removeItemState.value = UiState.Success(itemId)
                    // Reload cart after removing
                    loadCart()
                }
                is NetworkResult.Error -> {
                    Log.e("CartViewModel", "❌ Remove error: ${result.message}")
                    _removeItemState.value = UiState.Error(result.message)
                }
                is NetworkResult.Exception -> {
                    Log.e("CartViewModel", "❌ Remove exception: ${result.e.message}")
                    _removeItemState.value = UiState.Error(result.e.message ?: "Lỗi xóa khỏi giỏ hàng")
                }
            }
        }
    }

    /**
     * Clear add to cart state
     */
    fun clearAddToCartState() {
        Log.d("CartViewModel", "🔄 Clearing add to cart state")
        _addToCartState.value = UiState.Idle
    }

    /**
     * Clear cart state
     */
    fun clearCartState() {
        Log.d("CartViewModel", "🔄 Clearing cart state")
        _cartState.value = UiState.Idle
    }
}

/**
 * Factory for CartViewModel
 */
class CartViewModelFactory(
    private val cartRepository: CartRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return CartViewModel(cartRepository) as T
    }
}
