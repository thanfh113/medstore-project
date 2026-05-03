package com.example.nhathuoc.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nhathuoc.data.model.NetworkResult
import com.example.nhathuoc.data.model.PointTransactionDto
import com.example.nhathuoc.data.model.RewardAccountDto
import com.example.nhathuoc.data.model.RewardRedemptionHistoryDto
import com.example.nhathuoc.data.model.RewardProductDto
import com.example.nhathuoc.data.model.RewardVoucherDto
import com.example.nhathuoc.data.model.RedeemRequest
import com.example.nhathuoc.data.model.UiState
import com.example.nhathuoc.data.repository.RewardRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class RewardViewModel @Inject constructor(
    private val rewardRepository: RewardRepository
) : ViewModel() {
    private val _accountState = MutableStateFlow<UiState<RewardAccountDto>>(UiState.Idle)
    val accountState: StateFlow<UiState<RewardAccountDto>> = _accountState.asStateFlow()

    private val _productsState = MutableStateFlow<UiState<List<RewardProductDto>>>(UiState.Idle)
    val productsState: StateFlow<UiState<List<RewardProductDto>>> = _productsState.asStateFlow()

    private val _transactionsState = MutableStateFlow<UiState<List<PointTransactionDto>>>(UiState.Idle)
    val transactionsState: StateFlow<UiState<List<PointTransactionDto>>> = _transactionsState.asStateFlow()

    private val _redemptionsState = MutableStateFlow<UiState<List<RewardRedemptionHistoryDto>>>(UiState.Idle)
    val redemptionsState: StateFlow<UiState<List<RewardRedemptionHistoryDto>>> = _redemptionsState.asStateFlow()

    private val _vouchersState = MutableStateFlow<UiState<List<RewardVoucherDto>>>(UiState.Idle)
    val vouchersState: StateFlow<UiState<List<RewardVoucherDto>>> = _vouchersState.asStateFlow()

    private val _redeemState = MutableStateFlow<UiState<String>>(UiState.Idle)
    val redeemState: StateFlow<UiState<String>> = _redeemState.asStateFlow()

    private val _currentCategory = MutableStateFlow<String?>(null)
    val currentCategory: StateFlow<String?> = _currentCategory.asStateFlow()

    fun loadRewardAccount() {
        Log.d("RewardVM", "Loading reward account")
        viewModelScope.launch {
            _accountState.value = UiState.Loading
            when (val result = rewardRepository.getRewardAccount()) {
                is NetworkResult.Success -> _accountState.value = UiState.Success(result.data)
                is NetworkResult.Error -> _accountState.value = UiState.Error(result.message)
                is NetworkResult.Exception -> _accountState.value = UiState.Error(result.e.message ?: "Unknown error")
            }
        }
    }

    fun loadRewardProducts(category: String? = null, page: Int = 1, limit: Int = 20) {
        viewModelScope.launch {
            _productsState.value = UiState.Loading
            when (val result = rewardRepository.getRewardProducts(category = category, page = page, limit = limit)) {
                is NetworkResult.Success -> {
                    _productsState.value = UiState.Success(result.data.products)
                    _currentCategory.value = category
                }
                is NetworkResult.Error -> _productsState.value = UiState.Error(result.message)
                is NetworkResult.Exception -> _productsState.value = UiState.Error(result.e.message ?: "Unknown error")
            }
        }
    }

    fun loadRewardTransactions(limit: Int = 50) {
        viewModelScope.launch {
            _transactionsState.value = UiState.Loading
            when (val result = rewardRepository.getRewardTransactions(limit)) {
                is NetworkResult.Success -> _transactionsState.value = UiState.Success(result.data)
                is NetworkResult.Error -> _transactionsState.value = UiState.Error(result.message)
                is NetworkResult.Exception -> _transactionsState.value = UiState.Error(result.e.message ?: "Unknown error")
            }
        }
    }

    fun loadRewardRedemptions() {
        viewModelScope.launch {
            _redemptionsState.value = UiState.Loading
            when (val result = rewardRepository.getRewardRedemptions()) {
                is NetworkResult.Success -> _redemptionsState.value = UiState.Success(result.data)
                is NetworkResult.Error -> _redemptionsState.value = UiState.Error(result.message)
                is NetworkResult.Exception -> _redemptionsState.value = UiState.Error(result.e.message ?: "Unknown error")
            }
        }
    }

    fun loadRewardVouchers() {
        viewModelScope.launch {
            _vouchersState.value = UiState.Loading
            when (val result = rewardRepository.getRewardVouchers()) {
                is NetworkResult.Success -> _vouchersState.value = UiState.Success(result.data)
                is NetworkResult.Error -> _vouchersState.value = UiState.Error(result.message)
                is NetworkResult.Exception -> _vouchersState.value = UiState.Error(result.e.message ?: "Unknown error")
            }
        }
    }

    fun redeemProduct(rewardProductId: String, quantity: Int = 1) {
        viewModelScope.launch {
            _redeemState.value = UiState.Loading
            when (val result = rewardRepository.redeem(RedeemRequest(rewardProductId, quantity))) {
                is NetworkResult.Success -> {
                    _redeemState.value = UiState.Success(result.data.message)
                    loadRewardAccount()
                    loadRewardTransactions()
                    loadRewardRedemptions()
                    loadRewardVouchers()
                }
                is NetworkResult.Error -> _redeemState.value = UiState.Error(result.message)
                is NetworkResult.Exception -> _redeemState.value = UiState.Error(result.e.message ?: "Unknown error")
            }
        }
    }

    fun clearRedeemState() {
        _redeemState.value = UiState.Idle
    }
}
