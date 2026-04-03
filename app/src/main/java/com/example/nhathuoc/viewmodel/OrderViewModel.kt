package com.example.nhathuoc.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nhathuoc.data.model.*
import com.example.nhathuoc.data.repository.OrderRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class OrderViewModel(
    private val orderRepository: OrderRepository = OrderRepository()
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

            val result = orderRepository.getOrderById(orderId)

            when (result) {
                is NetworkResult.Success -> {
                    _orderState.value = UiState.Success(result.data)
                }
                is NetworkResult.Error -> {
                    _orderState.value = UiState.Error("Lỗi: ${result.message}")
                }
                is NetworkResult.Exception -> {
                    _orderState.value = UiState.Error("Lỗi kết nối")
                }
            }

            _isLoading.value = false
        }
    }

    fun getOrders(status: String? = null, page: Int = 1) {
        viewModelScope.launch {
            _isLoading.value = true
            _ordersListState.value = UiState.Loading

            val result = orderRepository.getOrders(status = status, page = page)

            when (result) {
                is NetworkResult.Success -> {
                    _ordersListState.value = UiState.Success(result.data)
                }
                is NetworkResult.Error -> {
                    _ordersListState.value = UiState.Error("Lỗi: ${result.message}")
                }
                is NetworkResult.Exception -> {
                    _ordersListState.value = UiState.Error("Lỗi kết nối")
                }
            }

            _isLoading.value = false
        }
    }

    fun cancelOrder(orderId: String, reason: String) {
        viewModelScope.launch {
            _isLoading.value = true

            val result = orderRepository.cancelOrder(orderId, reason)

            when (result) {
                is NetworkResult.Success -> {
                    _orderState.value = UiState.Success(result.data.order)
                }
                is NetworkResult.Error -> {
                    _orderState.value = UiState.Error("Lỗi: ${result.message}")
                }
                is NetworkResult.Exception -> {
                    _orderState.value = UiState.Error("Lỗi kết nối")
                }
            }

            _isLoading.value = false
        }
    }

    fun clearState() {
        _orderState.value = UiState.Idle
        _ordersListState.value = UiState.Idle
    }
}
