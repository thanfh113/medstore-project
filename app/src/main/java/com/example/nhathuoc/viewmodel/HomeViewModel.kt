package com.example.nhathuoc.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nhathuoc.data.model.BannerDto
import com.example.nhathuoc.data.model.NetworkResult
import com.example.nhathuoc.data.model.ProductDto
import com.example.nhathuoc.data.remote.ApiService
import com.example.nhathuoc.data.repository.ProductRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val productRepository: ProductRepository,
    private val apiService: ApiService
) : ViewModel() {

    // ── Flash Sale ──────────────────────────────────────────────────────────
    private val _flashSaleProducts = MutableStateFlow<List<ProductDto>>(emptyList())
    val flashSaleProducts: StateFlow<List<ProductDto>> = _flashSaleProducts.asStateFlow()

    private val _flashSaleLoading = MutableStateFlow(false)
    val flashSaleLoading: StateFlow<Boolean> = _flashSaleLoading.asStateFlow()

    // ── Best Sellers ────────────────────────────────────────────────────────
    private val _bestSellerProducts = MutableStateFlow<List<ProductDto>>(emptyList())
    val bestSellerProducts: StateFlow<List<ProductDto>> = _bestSellerProducts.asStateFlow()

    private val _bestSellerLoading = MutableStateFlow(false)
    val bestSellerLoading: StateFlow<Boolean> = _bestSellerLoading.asStateFlow()

    // ── Banners ─────────────────────────────────────────────────────────────
    private val _banners = MutableStateFlow<List<BannerDto>>(emptyList())
    val banners: StateFlow<List<BannerDto>> = _banners.asStateFlow()

    // ── Error ────────────────────────────────────────────────────────────────
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    init {
        loadHomeData()
    }

    fun loadHomeData() {
        loadFlashSale()
        loadBestSellers()
        loadBanners()
    }

    private fun loadFlashSale() {
        viewModelScope.launch {
            _flashSaleLoading.value = true
            when (val result = productRepository.getFlashSaleProducts()) {
                is NetworkResult.Success -> {
                    _flashSaleProducts.value = result.data.products
                }
                is NetworkResult.Error -> {
                    _errorMessage.value = result.message
                    // Keep existing list (could be empty or previously loaded)
                }
                is NetworkResult.Exception -> {
                    _errorMessage.value = "Không thể kết nối máy chủ"
                }
            }
            _flashSaleLoading.value = false
        }
    }

    private fun loadBestSellers() {
        viewModelScope.launch {
            _bestSellerLoading.value = true
            when (val result = productRepository.getBestSellers(period = "week")) {
                is NetworkResult.Success -> {
                    _bestSellerProducts.value = result.data.products
                }
                is NetworkResult.Error -> {
                    _errorMessage.value = result.message
                }
                is NetworkResult.Exception -> {
                    _errorMessage.value = "Không thể kết nối máy chủ"
                }
            }
            _bestSellerLoading.value = false
        }
    }

    private fun loadBanners() {
        viewModelScope.launch {
            try {
                val response = apiService.getBanners(isActive = true)
                if (response.isSuccessful) {
                    _banners.value = response.body()?.data ?: emptyList()
                }
            } catch (_: Exception) {
                // Banner load failure is non-critical – fallback to static UI
            }
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }
}
