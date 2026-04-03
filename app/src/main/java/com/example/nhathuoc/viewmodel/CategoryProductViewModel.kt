package com.example.nhathuoc.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.nhathuoc.data.model.NetworkResult
import com.example.nhathuoc.data.model.ProductDto
import com.example.nhathuoc.data.model.UiState
import com.example.nhathuoc.data.repository.ProductRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CategoryProductViewModel(
    private val productRepository: ProductRepository
) : ViewModel() {

    private val _state = MutableStateFlow<UiState<List<ProductDto>>>(UiState.Idle)
    val state: StateFlow<UiState<List<ProductDto>>> = _state.asStateFlow()

    private val _allProducts = MutableStateFlow<List<ProductDto>>(emptyList())
    val allProducts: StateFlow<List<ProductDto>> = _allProducts.asStateFlow()

    private val _selectedSort = MutableStateFlow("price_asc")
    val selectedSort: StateFlow<String> = _selectedSort.asStateFlow()

    private val _hasMore = MutableStateFlow(true)
    val hasMore: StateFlow<Boolean> = _hasMore.asStateFlow()

    fun loadProductsByCategory(
        categoryName: String,
        page: Int = 1,
        limit: Int = 20
    ) {
        Log.d("CategoryProductVM", "📦 Loading products for category: $categoryName")
        viewModelScope.launch {
            _state.value = UiState.Loading

            when (val result = productRepository.getProducts(
                category = categoryName,
                page = page,
                limit = limit
            )) {
                is NetworkResult.Success -> {
                    val products = result.data.products
                    _allProducts.value = products
                    _state.value = if (products.isNotEmpty()) {
                        _hasMore.value = result.data.pagination.page < result.data.pagination.totalPages
                        Log.d("CategoryProductVM", "✅ Loaded ${products.size} products")
                        UiState.Success(products)
                    } else {
                        _hasMore.value = false
                        Log.d("CategoryProductVM", "⚠️ No products found")
                        UiState.Success(emptyList())
                    }
                }
                is NetworkResult.Error -> {
                    Log.e("CategoryProductVM", "❌ Error: ${result.message}")
                    _state.value = UiState.Error(result.message)
                }
                is NetworkResult.Exception -> {
                    Log.e("CategoryProductVM", "❌ Exception: ${result.e.message}")
                    _state.value = UiState.Error(result.e.message ?: "Unknown error")
                }
            }
        }
    }

    fun updateSort(sortBy: String) {
        _selectedSort.value = sortBy
        val current = _allProducts.value
        if (current.isNotEmpty()) {
            val sorted = when (sortBy) {
                "price_asc" -> current.sortedBy { it.price }
                "price_desc" -> current.sortedByDescending { it.price }
                "name" -> current.sortedBy { it.name }
                "created_at" -> current
                else -> current
            }
            _allProducts.value = sorted
            Log.d("CategoryProductVM", "Sorted by: $sortBy")
        }
    }

    fun clearState() {
        _state.value = UiState.Idle
        _allProducts.value = emptyList()
    }
}

class CategoryProductViewModelFactory(
    private val productRepository: ProductRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return CategoryProductViewModel(productRepository) as T
    }
}
