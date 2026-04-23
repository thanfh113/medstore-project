package com.example.nhathuoc.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nhathuoc.data.model.*
import com.example.nhathuoc.data.repository.OrderRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class OrderViewModel @Inject constructor(
    private val orderRepository: OrderRepository
) : ViewModel() {
    private val _orderState = MutableStateFlow<UiState<OrderDto>>(UiState.Idle)
    val orderState: StateFlow<UiState<OrderDto>> = _orderState.asStateFlow()

    private val _ordersListState = MutableStateFlow<UiState<OrderListResponse>>(UiState.Idle)
    val ordersListState: StateFlow<UiState<OrderListResponse>> = _ordersListState.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun getOrderById(orderId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _orderState.value = UiState.Loading
            when (val result = orderRepository.getOrderById(orderId)) {
                is NetworkResult.Success -> _orderState.value = UiState.Success(result.data)
                is NetworkResult.Error -> _orderState.value = UiState.Error("Loi: ${result.message}")
                is NetworkResult.Exception -> _orderState.value = UiState.Error("Loi ket noi")
            }
            _isLoading.value = false
        }
    }

    fun getOrders(status: String? = null, page: Int = 1) {
        viewModelScope.launch {
            _isLoading.value = true
            _ordersListState.value = UiState.Loading
            when (val result = orderRepository.getOrders(status = status, page = page)) {
                is NetworkResult.Success -> _ordersListState.value = UiState.Success(result.data)
                is NetworkResult.Error -> _ordersListState.value = UiState.Error("Loi: ${result.message}")
                is NetworkResult.Exception -> _ordersListState.value = UiState.Error("Loi ket noi")
            }
            _isLoading.value = false
        }
    }

    fun cancelOrder(orderId: String, reason: String) {
        viewModelScope.launch {
            _isLoading.value = true
            when (val result = orderRepository.cancelOrder(orderId, reason)) {
                is NetworkResult.Success -> {
                    when (val refreshed = orderRepository.getOrderById(orderId)) {
                        is NetworkResult.Success -> _orderState.value = UiState.Success(refreshed.data)
                        is NetworkResult.Error -> _orderState.value = UiState.Error("Da huy don nhung khong tai lai duoc: ${refreshed.message}")
                        is NetworkResult.Exception -> _orderState.value = UiState.Error("Da huy don nhung mat ket noi khi tai lai")
                    }
                }
                is NetworkResult.Error -> _orderState.value = UiState.Error("Loi: ${result.message}")
                is NetworkResult.Exception -> _orderState.value = UiState.Error("Loi ket noi")
            }
            _isLoading.value = false
        }
    }

    fun clearState() {
        _orderState.value = UiState.Idle
        _ordersListState.value = UiState.Idle
    }
}
