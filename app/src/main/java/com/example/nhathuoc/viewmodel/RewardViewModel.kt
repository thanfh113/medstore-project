package com.example.nhathuoc.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.nhathuoc.data.model.NetworkResult
import com.example.nhathuoc.data.model.RewardAccountDto
import com.example.nhathuoc.data.model.RewardProductDto
import com.example.nhathuoc.data.model.RedeemRequest
import com.example.nhathuoc.data.model.UiState
import com.example.nhathuoc.data.repository.RewardRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class RewardViewModel(
    private val rewardRepository: RewardRepository
) : ViewModel() {

    // Reward account state
    private val _accountState = MutableStateFlow<UiState<RewardAccountDto>>(UiState.Idle)
    val accountState: StateFlow<UiState<RewardAccountDto>> = _accountState.asStateFlow()

    // Reward products list
    private val _productsState = MutableStateFlow<UiState<List<RewardProductDto>>>(UiState.Idle)
    val productsState: StateFlow<UiState<List<RewardProductDto>>> = _productsState.asStateFlow()

    // Redeem action state
    private val _redeemState = MutableStateFlow<UiState<String>>(UiState.Idle)
    val redeemState: StateFlow<UiState<String>> = _redeemState.asStateFlow()

    private val _currentCategory = MutableStateFlow<String?>(null)
    val currentCategory: StateFlow<String?> = _currentCategory.asStateFlow()

    /**
     * Load user reward account info (points, tier, history)
     */
    fun loadRewardAccount() {
        Log.d("RewardVM", "💎 Loading reward account...")
        viewModelScope.launch {
            _accountState.value = UiState.Loading

            when (val result = rewardRepository.getRewardAccount()) {
                is NetworkResult.Success -> {
                    val account = result.data
                    _accountState.value = UiState.Success(account)
                    Log.d("RewardVM", "✅ Account: ${account.availablePoints} points, tier: ${account.tier}")
                }
                is NetworkResult.Error -> {
                    Log.e("RewardVM", "❌ Error: ${result.message}")
                    _accountState.value = UiState.Error(result.message)
                }
                is NetworkResult.Exception -> {
                    Log.e("RewardVM", "❌ Exception: ${result.e.message}")
                    _accountState.value = UiState.Error(result.e.message ?: "Unknown error")
                }
            }
        }
    }

    /**
     * Load reward products available for redemption
     */
    fun loadRewardProducts(
        category: String? = null,
        page: Int = 1,
        limit: Int = 20
    ) {
        Log.d("RewardVM", "🎁 Loading reward products, category: $category")
        viewModelScope.launch {
            _productsState.value = UiState.Loading

            when (val result = rewardRepository.getRewardProducts(
                category = category,
                page = page,
                limit = limit
            )) {
                is NetworkResult.Success -> {
                    val products = result.data.products
                    if (products.isNotEmpty()) {
                        _productsState.value = UiState.Success(products)
                        _currentCategory.value = category
                        Log.d("RewardVM", "✅ Loaded ${products.size} reward products")
                    } else {
                        _productsState.value = UiState.Success(emptyList())
                        Log.d("RewardVM", "⚠️ No products available")
                    }
                }
                is NetworkResult.Error -> {
                    Log.e("RewardVM", "❌ Error: ${result.message}")
                    _productsState.value = UiState.Error(result.message)
                }
                is NetworkResult.Exception -> {
                    Log.e("RewardVM", "❌ Exception: ${result.e.message}")
                    _productsState.value = UiState.Error(result.e.message ?: "Unknown error")
                }
            }
        }
    }

    /**
     * Redeem reward product using points
     */
    fun redeemProduct(
        rewardProductId: String,
        quantity: Int = 1
    ) {
        Log.d("RewardVM", "🔄 Redeeming product: $rewardProductId, qty: $quantity")
        viewModelScope.launch {
            _redeemState.value = UiState.Loading

            val request = RedeemRequest(
                rewardProductId = rewardProductId,
                quantity = quantity
            )

            when (val result = rewardRepository.redeem(request)) {
                is NetworkResult.Success -> {
                    val response = result.data
                    _redeemState.value = UiState.Success(response.message)
                    // Reload account to reflect new points
                    loadRewardAccount()
                    Log.d("RewardVM", "✅ Redeem success: ${response.message}")
                }
                is NetworkResult.Error -> {
                    Log.e("RewardVM", "❌ Error: ${result.message}")
                    _redeemState.value = UiState.Error(result.message)
                }
                is NetworkResult.Exception -> {
                    Log.e("RewardVM", "❌ Exception: ${result.e.message}")
                    _redeemState.value = UiState.Error(result.e.message ?: "Unknown error")
                }
            }
        }
    }

    /**
     * Clear redeem state after showing success/failure
     */
    fun clearRedeemState() {
        _redeemState.value = UiState.Idle
    }
}

class RewardViewModelFactory(
    private val rewardRepository: RewardRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return RewardViewModel(rewardRepository) as T
    }
}
