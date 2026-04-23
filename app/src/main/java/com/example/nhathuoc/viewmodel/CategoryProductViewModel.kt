package com.example.nhathuoc.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nhathuoc.data.model.NetworkResult
import com.example.nhathuoc.data.model.ProductDto
import com.example.nhathuoc.data.model.UiState
import com.example.nhathuoc.data.repository.ProductRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class CategoryProductViewModel @Inject constructor(
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

    fun loadProductsByCategory(categoryName: String, page: Int = 1, limit: Int = 20) {
        Log.d("CategoryProductVM", "Loading products for category: $categoryName")
        viewModelScope.launch {
            _state.value = UiState.Loading
            when (val result = productRepository.getProducts(category = categoryName, page = page, limit = limit)) {
                is NetworkResult.Success -> {
                    val products = result.data.products
                    _allProducts.value = products
                    _hasMore.value = result.data.pagination.page < result.data.pagination.totalPages
                    _state.value = UiState.Success(products)
                }
                is NetworkResult.Error -> _state.value = UiState.Error(result.message)
                is NetworkResult.Exception -> _state.value = UiState.Error(result.e.message ?: "Unknown error")
            }
        }
    }

    fun updateSort(sortBy: String) {
        _selectedSort.value = sortBy
        val current = _allProducts.value
        if (current.isNotEmpty()) {
            _allProducts.value = when (sortBy) {
                "price_asc" -> current.sortedBy { it.price }
                "price_desc" -> current.sortedByDescending { it.price }
                "name" -> current.sortedBy { it.name }
                else -> current
            }
        }
    }

    fun clearState() {
        _state.value = UiState.Idle
        _allProducts.value = emptyList()
    }
}
