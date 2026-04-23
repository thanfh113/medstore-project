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

class FinanceDashboardViewModel(
    private val repository: FinanceRepository
) {
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    private val _uiState = MutableStateFlow<FinanceUiState>(FinanceUiState.Loading)
    val uiState: StateFlow<FinanceUiState> = _uiState.asStateFlow()

    init {
        loadSummary()
    }

    fun loadSummary() {
        scope.launch {
            _uiState.value = FinanceUiState.Loading
            repository.getFinanceSummary().fold(
                onSuccess = { _uiState.value = FinanceUiState.Success(it) },
                onFailure = { _uiState.value = FinanceUiState.Error(it.message ?: "Khong the tai du lieu tai chinh") }
            )
        }
    }
}

