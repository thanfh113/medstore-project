package com.example.nhathuoc.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nhathuoc.data.model.BannerDto
import com.example.nhathuoc.data.model.NetworkResult
import com.example.nhathuoc.data.model.OrderDto
import com.example.nhathuoc.data.model.ProductDto
import com.example.nhathuoc.data.remote.ApiService
import com.example.nhathuoc.data.repository.OrderRepository
import com.example.nhathuoc.data.repository.ProductRepository
import com.example.nhathuoc.data.repository.RewardRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val productRepository: ProductRepository,
    private val apiService: ApiService,
    private val rewardRepository: RewardRepository,
    private val orderRepository: OrderRepository
) : ViewModel() {

    private val _flashSaleProducts = MutableStateFlow<List<ProductDto>>(emptyList())
    val flashSaleProducts: StateFlow<List<ProductDto>> = _flashSaleProducts.asStateFlow()

    private val _flashSaleLoading = MutableStateFlow(false)
    val flashSaleLoading: StateFlow<Boolean> = _flashSaleLoading.asStateFlow()

    private val _bestSellerProducts = MutableStateFlow<List<ProductDto>>(emptyList())
    val bestSellerProducts: StateFlow<List<ProductDto>> = _bestSellerProducts.asStateFlow()

    private val _bestSellerLoading = MutableStateFlow(false)
    val bestSellerLoading: StateFlow<Boolean> = _bestSellerLoading.asStateFlow()

    private val _banners = MutableStateFlow<List<BannerDto>>(emptyList())
    val banners: StateFlow<List<BannerDto>> = _banners.asStateFlow()

    private val _rewardPoints = MutableStateFlow(0)
    val rewardPoints: StateFlow<Int> = _rewardPoints.asStateFlow()

    private val _recentOrders = MutableStateFlow<List<OrderDto>>(emptyList())
    val recentOrders: StateFlow<List<OrderDto>> = _recentOrders.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    init {
        loadHomeData()
    }

    fun loadHomeData() {
        loadFlashSale()
        loadBestSellers()
        loadBanners()
        loadRewardAccount()
        loadRecentOrders()
    }

    private fun loadFlashSale() {
        viewModelScope.launch {
            _flashSaleLoading.value = true
            when (val result = productRepository.getFlashSaleProducts()) {
                is NetworkResult.Success -> _flashSaleProducts.value = result.data.products
                is NetworkResult.Error -> _errorMessage.value = result.message
                is NetworkResult.Exception -> _errorMessage.value = "Không thể kết nối máy chủ"
            }
            _flashSaleLoading.value = false
        }
    }

    private fun loadBestSellers() {
        viewModelScope.launch {
            _bestSellerLoading.value = true
            when (val result = productRepository.getBestSellers(period = "week")) {
                is NetworkResult.Success -> _bestSellerProducts.value = result.data.products
                is NetworkResult.Error -> _errorMessage.value = result.message
                is NetworkResult.Exception -> _errorMessage.value = "Không thể kết nối máy chủ"
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
            } catch (_: Exception) {}
        }
    }

    fun refreshRewardAccount() {
        viewModelScope.launch {
            when (val result = rewardRepository.getRewardAccount()) {
                is NetworkResult.Success -> _rewardPoints.value = result.data.availablePoints
                else -> Unit
            }
        }
    }

    private fun loadRewardAccount() {
        refreshRewardAccount()
    }

    private fun loadRecentOrders() {
        viewModelScope.launch {
            when (val result = orderRepository.getOrders(page = 1, limit = 5)) {
                is NetworkResult.Success -> _recentOrders.value = result.data.orders
                else -> Unit
            }
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }
}
