package org.example.project.presentation.viewmodels

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.example.project.data.local.InventorySyncAuditRecord
import org.example.project.data.repositories.SyncRepository

data class SyncInventoryAuditItem(
    val id: String,
    val productId: String,
    val productName: String,
    val beforeStock: Int,
    val serverStock: Int,
    val policy: String,
    val reason: String,
    val sourceDeviceId: String?,
    val createdAtEpochMs: Long,
    val resolvedBySyncAtEpochMs: Long?
)

data class SyncInventoryAuditUiState(
    val isLoading: Boolean = false,
    val audits: List<SyncInventoryAuditItem> = emptyList()
)

class SyncInventoryAuditViewModel(
    private val syncRepository: SyncRepository
) {
    private val _uiState = MutableStateFlow(SyncInventoryAuditUiState())
    val uiState: StateFlow<SyncInventoryAuditUiState> = _uiState.asStateFlow()

    fun loadAudits() {
        _uiState.update { it.copy(isLoading = true) }
        val productsById = syncRepository.loadCachedProducts().associateBy { it.id }
        val rows = syncRepository.getInventorySyncAudits(limit = 500)
            .map { it.toUi(productsById[it.productId]?.name ?: it.productId) }
        _uiState.update {
            it.copy(
                isLoading = false,
                audits = rows
            )
        }
    }

    private fun InventorySyncAuditRecord.toUi(productName: String): SyncInventoryAuditItem {
        return SyncInventoryAuditItem(
            id = id,
            productId = productId,
            productName = productName,
            beforeStock = beforeStock,
            serverStock = serverStock,
            policy = policy,
            reason = reason,
            sourceDeviceId = sourceDeviceId,
            createdAtEpochMs = createdAtEpochMs,
            resolvedBySyncAtEpochMs = resolvedBySyncAtEpochMs
        )
    }
}


