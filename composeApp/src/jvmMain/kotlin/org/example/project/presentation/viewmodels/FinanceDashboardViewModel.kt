package org.example.project.presentation.viewmodels

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.example.project.data.repositories.FinanceRepository
import org.example.project.data.repositories.FinanceSummaryDto

sealed class FinanceUiState {
    data object Loading : FinanceUiState()
    data class Success(val data: FinanceSummaryDto) : FinanceUiState()
    data class Error(val message: String) : FinanceUiState()
}

enum class FinancePeriod(val label: String, val key: String) {
    TODAY("Hôm nay", "TODAY"),
    WEEK("Tuần này", "WEEK"),
    MONTH("Tháng này", "MONTH"),
    YEAR("Năm này", "YEAR"),
    ALL("Tất cả", "ALL")
}

class FinanceDashboardViewModel(
    private val repository: FinanceRepository
) {
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    private val _uiState = MutableStateFlow<FinanceUiState>(FinanceUiState.Loading)
    val uiState: StateFlow<FinanceUiState> = _uiState.asStateFlow()

    private val _selectedPeriod = MutableStateFlow(FinancePeriod.MONTH)
    val selectedPeriod: StateFlow<FinancePeriod> = _selectedPeriod.asStateFlow()

    init {
        loadSummary()
    }

    fun selectPeriod(period: FinancePeriod) {
        _selectedPeriod.value = period
        loadSummary()
    }

    fun loadSummary() {
        scope.launch {
            _uiState.value = FinanceUiState.Loading
            repository.getFinanceSummary(_selectedPeriod.value.key).fold(
                onSuccess = { _uiState.value = FinanceUiState.Success(it) },
                onFailure = { _uiState.value = FinanceUiState.Error(it.message ?: "Khong the tai du lieu tai chinh") }
            )
        }
    }
}
