package org.example.project.presentation.viewmodels

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.example.project.data.repositories.SettingsRepository
import org.example.project.data.repositories.ShopSettingsRequest

data class SettingsUiState(
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val name: String = "",
    val description: String = "",
    val logoUrl: String = "",
    val licenseNumber: String = "",
    val expiryAlertDays: String = "30",
    val storeId: String = "",
    val isApproved: Boolean = false,
    val successMessage: String? = null,
    val error: String? = null
)

class SettingsViewModel(
    private val repository: SettingsRepository
) {
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        loadSettings()
    }

    fun loadSettings() {
        scope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, successMessage = null) }
            repository.getSettings().fold(
                onSuccess = { settings ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            name = settings.name,
                            description = settings.description.orEmpty(),
                            logoUrl = settings.logoUrl.orEmpty(),
                            licenseNumber = settings.licenseNumber.orEmpty(),
                            expiryAlertDays = settings.expiryAlertDays.toString(),
                            storeId = settings.id,
                            isApproved = settings.isApproved
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = error.message ?: "Không thể tải cấu hình cửa hàng"
                        )
                    }
                }
            )
        }
    }

    fun updateName(value: String) = _uiState.update { it.copy(name = value) }
    fun updateDescription(value: String) = _uiState.update { it.copy(description = value) }
    fun updateLogoUrl(value: String) = _uiState.update { it.copy(logoUrl = value) }
    fun updateLicenseNumber(value: String) = _uiState.update { it.copy(licenseNumber = value) }
    fun updateExpiryAlertDays(value: String) = _uiState.update { it.copy(expiryAlertDays = value) }

    fun saveSettings() {
        scope.launch {
            val snapshot = uiState.value
            val expiryAlertDays = snapshot.expiryAlertDays.toIntOrNull()
            if (snapshot.name.isBlank()) {
                _uiState.update { it.copy(error = "Tên cửa hàng không được để trống") }
                return@launch
            }
            if (expiryAlertDays == null || expiryAlertDays <= 0) {
                _uiState.update { it.copy(error = "Số ngày cảnh báo hạn dùng phải lớn hơn 0") }
                return@launch
            }

            _uiState.update { it.copy(isSaving = true, error = null, successMessage = null) }
            repository.updateSettings(
                ShopSettingsRequest(
                    name = snapshot.name,
                    description = snapshot.description.ifBlank { null },
                    logoUrl = snapshot.logoUrl.ifBlank { null },
                    licenseNumber = snapshot.licenseNumber.ifBlank { null },
                    expiryAlertDays = expiryAlertDays
                )
            ).fold(
                onSuccess = {
                    _uiState.update { state ->
                        state.copy(
                            isSaving = false,
                            successMessage = "Đã cập nhật cấu hình cửa hàng"
                        )
                    }
                    loadSettings()
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            error = error.message ?: "Không thể lưu cấu hình cửa hàng"
                        )
                    }
                }
            )
        }
    }
}
