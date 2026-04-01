package com.example.nhathuoc.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.nhathuoc.data.model.*
import com.example.nhathuoc.data.repository.ProductRepository
import com.example.nhathuoc.data.repository.BannerRepository
import com.example.nhathuoc.data.repository.CategoryRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.async

/**
 * ViewModel for Home screen operations
 * Handles banners, flash sales, best sellers, categories using repository pattern
 */
class HomeViewModel(
    private val productRepository: ProductRepository,
    private val bannerRepository: BannerRepository,
    private val categoryRepository: CategoryRepository
) : ViewModel() {

    // UI State for banners
    private val _bannersState = MutableStateFlow<UiState<List<BannerDto>>>(UiState.Idle)
    val bannersState: StateFlow<UiState<List<BannerDto>>> = _bannersState.asStateFlow()

    // UI State for flash sale products
    private val _flashSaleState = MutableStateFlow<UiState<FlashSaleResponse>>(UiState.Idle)
    val flashSaleState: StateFlow<UiState<FlashSaleResponse>> = _flashSaleState.asStateFlow()

    // UI State for best sellers
    private val _bestSellersState = MutableStateFlow<UiState<BestSellersResponse>>(UiState.Idle)
    val bestSellersState: StateFlow<UiState<BestSellersResponse>> = _bestSellersState.asStateFlow()

    // UI State for product categories
    private val _categoriesState = MutableStateFlow<UiState<List<CategoryDto>>>(UiState.Idle)
    val categoriesState: StateFlow<UiState<List<CategoryDto>>> = _categoriesState.asStateFlow()

    // UI State for featured products
    private val _featuredProductsState = MutableStateFlow<UiState<ProductListResponse>>(UiState.Idle)
    val featuredProductsState: StateFlow<UiState<ProductListResponse>> = _featuredProductsState.asStateFlow()

    // Combined loading state for home screen
    val isLoading: StateFlow<Boolean> = combine(
        _bannersState,
        _flashSaleState,
        _bestSellersState,
        _categoriesState
    ) { banners, flashSale, bestSellers, categories ->
        banners is UiState.Loading ||
        flashSale is UiState.Loading ||
        bestSellers is UiState.Loading ||
        categories is UiState.Loading
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = false
    )

    init {
        loadHomeData()
    }

    // Load all home screen data in parallel
    fun loadHomeData() {
        // Launch all loading operations concurrently
        loadBanners()
        loadFlashSale()
        loadBestSellers()
        loadCategories()
        loadFeaturedProducts()
    }

    // Load banners
    fun loadBanners() {
        viewModelScope.launch {
            _bannersState.value = UiState.Loading

            when (val result = bannerRepository.getActiveBanners(position = "home_hero")) {
                is NetworkResult.Success -> {
                    _bannersState.value = UiState.Success(result.data)
                }
                is NetworkResult.Error -> {
                    _bannersState.value = UiState.Error(result.message)
                }
                is NetworkResult.Exception -> {
                    _bannersState.value = UiState.Error(
                        result.e.message ?: "Có lỗi xảy ra khi tải banner"
                    )
                }
            }
        }
    }

    // Load flash sale products
    fun loadFlashSale() {
        viewModelScope.launch {
            _flashSaleState.value = UiState.Loading

            when (val result = productRepository.getFlashSaleProducts()) {
                is NetworkResult.Success -> {
                    _flashSaleState.value = UiState.Success(result.data)
                }
                is NetworkResult.Error -> {
                    _flashSaleState.value = UiState.Error(result.message)
                }
                is NetworkResult.Exception -> {
                    _flashSaleState.value = UiState.Error(
                        result.e.message ?: "Có lỗi xảy ra khi tải flash sale"
                    )
                }
            }
        }
    }

    // Load best seller products
    fun loadBestSellers() {
        viewModelScope.launch {
            _bestSellersState.value = UiState.Loading

            when (val result = productRepository.getBestSellers()) {
                is NetworkResult.Success -> {
                    _bestSellersState.value = UiState.Success(result.data)
                }
                is NetworkResult.Error -> {
                    _bestSellersState.value = UiState.Error(result.message)
                }
                is NetworkResult.Exception -> {
                    _bestSellersState.value = UiState.Error(
                        result.e.message ?: "Có lỗi xảy ra khi tải sản phẩm bán chạy"
                    )
                }
            }
        }
    }

    // Load featured products
    fun loadFeaturedProducts() {
        viewModelScope.launch {
            _featuredProductsState.value = UiState.Loading

            when (val result = productRepository.getProducts(
                sortBy = "featured",
                limit = 10
            )) {
                is NetworkResult.Success -> {
                    _featuredProductsState.value = UiState.Success(result.data)
                }
                is NetworkResult.Error -> {
                    _featuredProductsState.value = UiState.Error(result.message)
                }
                is NetworkResult.Exception -> {
                    _featuredProductsState.value = UiState.Error(
                        result.e.message ?: "Có lỗi xảy ra khi tải sản phẩm nổi bật"
                    )
                }
            }
        }
    }

    // Load categories from API
    fun loadCategories() {
        viewModelScope.launch {
            _categoriesState.value = UiState.Loading

            when (val result = categoryRepository.getTopLevelCategories()) {
                is NetworkResult.Success -> {
                    _categoriesState.value = UiState.Success(result.data)
                }
                is NetworkResult.Error -> {
                    _categoriesState.value = UiState.Error(result.message)
                }
                is NetworkResult.Exception -> {
                    _categoriesState.value = UiState.Error(
                        result.e.message ?: "Có lỗi xảy ra khi tải danh mục"
                    )
                }
            }
        }
    }

    // Refresh all home data
    fun refreshHomeData() {
        loadHomeData()
    }

    // Clear states
    fun clearBannersState() {
        _bannersState.value = UiState.Idle
    }

    fun clearFlashSaleState() {
        _flashSaleState.value = UiState.Idle
    }

    fun clearBestSellersState() {
        _bestSellersState.value = UiState.Idle
    }

    fun clearCategoriesState() {
        _categoriesState.value = UiState.Idle
    }
}

/**
 * ViewModelFactory for HomeViewModel
 */
class HomeViewModelFactory(
    private val productRepository: ProductRepository,
    private val bannerRepository: BannerRepository,
    private val categoryRepository: CategoryRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            return HomeViewModel(productRepository, bannerRepository, categoryRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}