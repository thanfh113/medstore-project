package com.example.nhathuoc.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.nhathuoc.data.model.*
import com.example.nhathuoc.data.repository.OrderRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * ViewModel for order management and history
 */
class OrderViewModel(
    private val orderRepository: OrderRepository
) : ViewModel() {

    // Filters
    val statusFilter = MutableStateFlow<String?>(null) // "PENDING", "CONFIRMED", "SHIPPED", "DELIVERED", "CANCELLED"
    val searchQuery = MutableStateFlow("")
    val currentPage = MutableStateFlow(1)

    // UI States
    private val _ordersState = MutableStateFlow<UiState<List<OrderDto>>>(UiState.Idle)
    val ordersState: StateFlow<UiState<List<OrderDto>>> = _ordersState.asStateFlow()

    private val _orderDetailState = MutableStateFlow<UiState<OrderDto>>(UiState.Idle)
    val orderDetailState: StateFlow<UiState<OrderDto>> = _orderDetailState.asStateFlow()

    private val _cancelOrderState = MutableStateFlow<UiState<String>>(UiState.Idle)
    val cancelOrderState: StateFlow<UiState<String>> = _cancelOrderState.asStateFlow()

    // Pagination
    val hasMoreOrders = MutableStateFlow(false)
    val totalOrders = MutableStateFlow(0)

    // Derived states
    val isLoading: StateFlow<Boolean> = ordersState
        .map { it is UiState.Loading }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val isEmpty: StateFlow<Boolean> = ordersState
        .map { it is UiState.Success && (it as UiState.Success).data.isEmpty() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    // Load orders when filters change
    init {
        combine(
            statusFilter,
            searchQuery,
            currentPage
        ) { _, _, _ -> Unit }
            .debounce(300)
            .onEach { loadOrders() }
            .launchIn(viewModelScope)
    }

    /**
     * Load user's orders with filters
     */
    fun loadOrders(resetPage: Boolean = true) {
        if (resetPage) {
            currentPage.value = 1
        }

        viewModelScope.launch {
            _ordersState.value = UiState.Loading

            when (val result = orderRepository.getOrders(
                status = statusFilter.value,
                search = searchQuery.value.ifEmpty { null },
                page = currentPage.value,
                limit = 10
            )) {
                is NetworkResult.Success -> {
                    _ordersState.value = UiState.Success(result.data.orders)
                    totalOrders.value = result.data.totalCount
                    hasMoreOrders.value = result.data.hasMore
                }
                is NetworkResult.Error -> {
                    _ordersState.value = UiState.Error(result.message)
                }
                is NetworkResult.Exception -> {
                    _ordersState.value = UiState.Error(
                        result.e.message ?: "Lỗi khi tải đơn hàng"
                    )
                }
            }
        }
    }

    /**
     * Get order details by ID
     */
    fun getOrderDetails(orderId: String) {
        viewModelScope.launch {
            _orderDetailState.value = UiState.Loading

            when (val result = orderRepository.getOrderById(orderId)) {
                is NetworkResult.Success -> {
                    _orderDetailState.value = UiState.Success(result.data)
                }
                is NetworkResult.Error -> {
                    _orderDetailState.value = UiState.Error(result.message)
                }
                is NetworkResult.Exception -> {
                    _orderDetailState.value = UiState.Error(
                        result.e.message ?: "Lỗi khi tải chi tiết đơn hàng"
                    )
                }
            }
        }
    }

    /**
     * Cancel an order
     */
    fun cancelOrder(orderId: String) {
        viewModelScope.launch {
            _cancelOrderState.value = UiState.Loading

            when (val result = orderRepository.cancelOrder(orderId)) {
                is NetworkResult.Success -> {
                    _cancelOrderState.value = UiState.Success("Đơn hàng đã được hủy")
                    // Refresh order list
                    loadOrders()
                }
                is NetworkResult.Error -> {
                    _cancelOrderState.value = UiState.Error(result.message)
                }
                is NetworkResult.Exception -> {
                    _cancelOrderState.value = UiState.Error(
                        result.e.message ?: "Lỗi khi hủy đơn hàng"
                    )
                }
            }
        }
    }

    /**
     * Filter by status
     */
    fun filterByStatus(status: String?) {
        statusFilter.value = status
        loadOrders(resetPage = true)
    }

    /**
     * Search orders
     */
    fun searchOrders(query: String) {
        searchQuery.value = query
        loadOrders(resetPage = true)
    }

    /**
     * Load more orders
     */
    fun loadMore() {
        if (hasMoreOrders.value && ordersState.value !is UiState.Loading) {
            currentPage.value++
        }
    }

    /**
     * Clear cancel state
     */
    fun clearCancelState() {
        _cancelOrderState.value = UiState.Idle
    }

    /**
     * Clear detail state
     */
    fun clearDetailState() {
        _orderDetailState.value = UiState.Idle
    }

    /**
     * Retry loading
     */
    fun retry() {
        loadOrders(resetPage = true)
    }
}

/**
 * Factory for OrderViewModel
 */
class OrderViewModelFactory(
    private val orderRepository: OrderRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(OrderViewModel::class.java)) {
            return OrderViewModel(orderRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
