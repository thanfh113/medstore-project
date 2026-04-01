package com.example.nhathuoc.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.nhathuoc.data.model.*
import com.example.nhathuoc.data.repository.ProductRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * ViewModel for product listing with search, filter, and pagination
 */
class ProductListViewModel(
    private val productRepository: ProductRepository
) : ViewModel() {

    // Search and filter inputs
    val searchQuery = MutableStateFlow("")
    val selectedCategory = MutableStateFlow<String?>(null)
    val minPrice = MutableStateFlow(0.0)
    val maxPrice = MutableStateFlow(100_000_000.0)
    val sortBy = MutableStateFlow<String?>(null) // "price_asc", "price_desc", "newest"
    val currentPage = MutableStateFlow(1)

    // UI States
    private val _productsState = MutableStateFlow<UiState<List<ProductDto>>>(UiState.Idle)
    val productsState: StateFlow<UiState<List<ProductDto>>> = _productsState.asStateFlow()

    private val _categoriesState = MutableStateFlow<UiState<List<CategoryDto>>>(UiState.Idle)
    val categoriesState: StateFlow<UiState<List<CategoryDto>>> = _categoriesState.asStateFlow()

    // Pagination and count
    val hasMoreProducts = MutableStateFlow(false)
    val totalCount = MutableStateFlow(0)

    // Derived states
    val isLoading: StateFlow<Boolean> = productsState
        .map { it is UiState.Loading }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val isEmpty: StateFlow<Boolean> = combine(
        productsState, searchQuery
    ) { state, query ->
        (state is UiState.Success && (state as UiState.Success).data.isEmpty()) ||
        (query.isNotEmpty() && state is UiState.Success && (state as UiState.Success).data.isEmpty())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    // Triggered load when search or filters change
    init {
        loadCategories()

        combine(
            searchQuery,
            selectedCategory,
            minPrice,
            maxPrice,
            sortBy,
            currentPage
        ) { _, _, _, _, _, _ -> Unit }
            .debounce(300) // Debounce search input
            .onEach { searchProducts() }
            .launchIn(viewModelScope)
    }

    /**
     * Load product categories for filter dropdown
     */
    fun loadCategories() {
        viewModelScope.launch {
            _categoriesState.value = UiState.Loading

            when (val result = productRepository.getCategories()) {
                is NetworkResult.Success -> {
                    _categoriesState.value = UiState.Success(result.data)
                }
                is NetworkResult.Error -> {
                    _categoriesState.value = UiState.Error(result.message)
                }
                is NetworkResult.Exception -> {
                    _categoriesState.value = UiState.Error(
                        result.e.message ?: "Lỗi khi tải danh mục"
                    )
                }
            }
        }
    }

    /**
     * Search products with current filters
     */
    fun searchProducts(resetPage: Boolean = true) {
        if (resetPage) {
            currentPage.value = 1
        }

        viewModelScope.launch {
            _productsState.value = UiState.Loading

            try {
                val result = productRepository.getProducts(
                    search = searchQuery.value.ifEmpty { null },
                    category = selectedCategory.value,
                    minPrice = if (minPrice.value > 0) minPrice.value else null,
                    maxPrice = if (maxPrice.value < 100_000_000) maxPrice.value else null,
                    sortBy = sortBy.value,
                    page = currentPage.value,
                    limit = 20
                )

                when (result) {
                    is NetworkResult.Success -> {
                        _productsState.value = UiState.Success(result.data.products)
                        totalCount.value = result.data.totalCount
                        hasMoreProducts.value = result.data.hasMore

                    }
                    is NetworkResult.Error -> {
                        _productsState.value = UiState.Error(result.message)
                    }
                    is NetworkResult.Exception -> {
                        _productsState.value = UiState.Error(
                            result.e.message ?: "Lỗi khi tìm kiếm sản phẩm"
                        )
                    }
                }
            } catch (e: Exception) {
                _productsState.value = UiState.Error(
                    e.message ?: "Lỗi không xác định"
                )
            }
        }
    }

    /**
     * Load more products (pagination)
     */
    fun loadMore() {
        if (hasMoreProducts.value && productsState.value !is UiState.Loading) {
            currentPage.value++
            viewModelScope.launch {
                try {
                    val result = productRepository.getProducts(
                        search = searchQuery.value.ifEmpty { null },
                        category = selectedCategory.value,
                        minPrice = if (minPrice.value > 0) minPrice.value else null,
                        maxPrice = if (maxPrice.value < 100_000_000) maxPrice.value else null,
                        sortBy = sortBy.value,
                        page = currentPage.value,
                        limit = 20
                    )

                    when (result) {
                        is NetworkResult.Success -> {
                            val currentList = (productsState.value as? UiState.Success)?.data ?: emptyList()
                            _productsState.value = UiState.Success(
                                currentList + result.data.products
                            )
                            hasMoreProducts.value = result.data.hasMore
                        }
                        else -> {
                            currentPage.value-- // Revert page increment
                        }
                    }
                } catch (e: Exception) {
                    currentPage.value-- // Revert page increment
                }
            }
        }
    }

    /**
     * Reset all filters and search
     */
    fun resetFilters() {
        searchQuery.value = ""
        selectedCategory.value = null
        minPrice.value = 0.0
        maxPrice.value = 100_000_000.0
        sortBy.value = null
        currentPage.value = 1
    }

    /**
     * Update sort preference
     */
    fun setSortBy(sort: String?) {
        sortBy.value = sort
        searchProducts(resetPage = true)
    }

    /**
     * Update price range
     */
    fun setPriceRange(min: Double, max: Double) {
        minPrice.value = min
        maxPrice.value = max
        searchProducts(resetPage = true)
    }

    /**
     * Update category filter
     */
    fun setCategory(category: String?) {
        selectedCategory.value = category
        searchProducts(resetPage = true)
    }

    /**
     * Retry loading on error
     */
    fun retry() {
        searchProducts(resetPage = true)
    }
}

/**
 * Factory for ProductListViewModel
 */
class ProductListViewModelFactory(
    private val productRepository: ProductRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProductListViewModel::class.java)) {
            return ProductListViewModel(productRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
