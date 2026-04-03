package com.example.nhathuoc.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nhathuoc.data.model.*
import com.example.nhathuoc.data.repository.ProductRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

sealed class UiState<out T> {
    object Idle : UiState<Nothing>()
    object Loading : UiState<Nothing>()
    data class Success<T>(val data: T) : UiState<T>()
    data class Error(val message: String) : UiState<Nothing>()
}

class ProductListViewModel(
    private val productRepository: ProductRepository = ProductRepository()
) : ViewModel() {

    private val _state = MutableStateFlow<UiState<ProductListResponse>>(UiState.Idle)
    val state: StateFlow<UiState<ProductListResponse>> = _state.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow<String?>(null)
    val selectedCategory: StateFlow<String?> = _selectedCategory.asStateFlow()

    private val _priceRange = MutableStateFlow(0.0..1000000.0)
    val priceRange: StateFlow<ClosedFloatingPointRange<Double>> = _priceRange.asStateFlow()

    private val _sortBy = MutableStateFlow<String?>(null)
    val sortBy: StateFlow<String?> = _sortBy.asStateFlow()

    private val _currentPage = MutableStateFlow(1)
    val currentPage: StateFlow<Int> = _currentPage.asStateFlow()

    private val _allProducts = MutableStateFlow<List<ProductDto>>(emptyList())
    val allProducts: StateFlow<List<ProductDto>> = _allProducts.asStateFlow()

    private val _hasMore = MutableStateFlow(false)
    val hasMore: StateFlow<Boolean> = _hasMore.asStateFlow()

    init {
        loadInitialProducts()
    }

    private fun loadInitialProducts() {
        viewModelScope.launch {
            _state.value = UiState.Loading
            when (val result = productRepository.getProducts(page = 1, limit = 20)) {
                is NetworkResult.Success -> {
                    _allProducts.value = result.data.products
                    _hasMore.value = result.data.pagination.hasNext
                    _state.value = UiState.Success(result.data)
                }
                is NetworkResult.Error -> {
                    _state.value = UiState.Error(result.message)
                }
                is NetworkResult.Exception -> {
                    _state.value = UiState.Error("Lỗi kết nối")
                }
            }
        }
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
        _currentPage.value = 1
        performSearch()
    }

    fun setCategory(category: String?) {
        _selectedCategory.value = category
        _currentPage.value = 1
        performSearch()
    }

    fun setPriceRange(min: Double, max: Double) {
        _priceRange.value = min..max
        _currentPage.value = 1
        performSearch()
    }

    fun setSortBy(sort: String?) {
        _sortBy.value = sort
        _currentPage.value = 1
        performSearch()
    }

    fun resetFilters() {
        _searchQuery.value = ""
        _selectedCategory.value = null
        _priceRange.value = 0.0..1000000.0
        _sortBy.value = null
        _currentPage.value = 1
        loadInitialProducts()
    }

    private fun performSearch() {
        viewModelScope.launch {
            _state.value = UiState.Loading
            val minPrice = if (_priceRange.value.start > 0) _priceRange.value.start else null
            val maxPrice = if (_priceRange.value.endInclusive < 1000000) _priceRange.value.endInclusive else null

            when (val result = productRepository.getProducts(
                category = _selectedCategory.value,
                minPrice = minPrice,
                maxPrice = maxPrice,
                sortBy = _sortBy.value,
                page = _currentPage.value,
                limit = 20
            )) {
                is NetworkResult.Success -> {
                    _allProducts.value = if (_currentPage.value == 1) {
                        result.data.products
                    } else {
                        _allProducts.value + result.data.products
                    }
                    _hasMore.value = result.data.pagination.hasNext
                    _state.value = UiState.Success(result.data)
                }
                is NetworkResult.Error -> {
                    _state.value = UiState.Error(result.message)
                }
                is NetworkResult.Exception -> {
                    _state.value = UiState.Error("Lỗi kết nối")
                }
            }
        }
    }

    fun loadMoreProducts() {
        if (!_hasMore.value) return
        _currentPage.value += 1
        performSearch()
    }

    fun clearState() {
        _state.value = UiState.Idle
    }
}
