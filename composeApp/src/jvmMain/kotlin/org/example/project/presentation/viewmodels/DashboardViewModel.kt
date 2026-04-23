package org.example.project.presentation.viewmodels

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.example.project.data.repositories.DesktopDashboardRepository
import org.example.project.data.repositories.InternalDashboardDto

data class DashboardStatsDto(
    val totalRevenue: Double,
    val totalOrders: Int,
    val totalProducts: Int,
    val totalCustomers: Int,
    val pendingOrders: Int,
    val today: DashboardPeriodStatsDto,
    val month: DashboardPeriodStatsDto,
    val recentOrders: List<RecentOrderDto>
)

data class DashboardPeriodStatsDto(
    val revenue: Double,
    val orderCount: Int,
    val completedOrderCount: Int,
    val pendingOrderCount: Int,
    val posRevenue: Double,
    val posOrderCount: Int,
    val posCompletedOrderCount: Int,
    val onlineRevenue: Double,
    val onlineOrderCount: Int,
    val onlineCompletedOrderCount: Int
)

data class RecentOrderDto(
    val orderId: String,
    val orderCode: String,
    val customerName: String,
    val total: Double,
    val status: String,
    val paymentStatus: String,
    val orderChannel: String,
    val createdAt: String
)

sealed class DashboardUiState {
    data object Loading : DashboardUiState()
    data class Success(val data: DashboardStatsDto) : DashboardUiState()
    data class Error(val message: String) : DashboardUiState()
}

class DashboardViewModel(
    private val dashboardRepository: DesktopDashboardRepository
) {
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    private val _uiState = MutableStateFlow<DashboardUiState>(DashboardUiState.Loading)
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        fetchDashboardData()
    }

    fun fetchDashboardData() {
        scope.launch {
            _uiState.value = DashboardUiState.Loading
            val result = dashboardRepository.getDashboard()
            result.onSuccess { dto ->
                _uiState.value = DashboardUiState.Success(dto.toUi())
            }.onFailure { error ->
                _uiState.value = DashboardUiState.Error(error.message ?: "Không thể tải dashboard")
            }
        }
    }

    private fun InternalDashboardDto.toUi(): DashboardStatsDto {
        return DashboardStatsDto(
            totalRevenue = totalRevenue,
            totalOrders = totalOrders,
            totalProducts = totalProducts,
            totalCustomers = totalCustomers,
            pendingOrders = pendingOrders,
            today = DashboardPeriodStatsDto(
                revenue = today.revenue,
                orderCount = today.orderCount,
                completedOrderCount = today.completedOrderCount,
                pendingOrderCount = today.pendingOrderCount,
                posRevenue = today.posRevenue,
                posOrderCount = today.posOrderCount,
                posCompletedOrderCount = today.posCompletedOrderCount,
                onlineRevenue = today.onlineRevenue,
                onlineOrderCount = today.onlineOrderCount,
                onlineCompletedOrderCount = today.onlineCompletedOrderCount
            ),
            month = DashboardPeriodStatsDto(
                revenue = month.revenue,
                orderCount = month.orderCount,
                completedOrderCount = month.completedOrderCount,
                pendingOrderCount = month.pendingOrderCount,
                posRevenue = month.posRevenue,
                posOrderCount = month.posOrderCount,
                posCompletedOrderCount = month.posCompletedOrderCount,
                onlineRevenue = month.onlineRevenue,
                onlineOrderCount = month.onlineOrderCount,
                onlineCompletedOrderCount = month.onlineCompletedOrderCount
            ),
            recentOrders = recentOrders.map {
                RecentOrderDto(
                    orderId = it.orderId,
                    orderCode = it.orderCode,
                    customerName = it.customerName,
                    total = it.total,
                    status = it.status,
                    paymentStatus = it.paymentStatus,
                    orderChannel = it.orderChannel,
                    createdAt = it.createdAt
                )
            }
        )
    }
}
