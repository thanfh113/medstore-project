package org.example.project.presentation.viewmodels

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.example.project.data.repositories.CouponAdminRepository
import org.example.project.data.repositories.CouponCreateRequest
import org.example.project.data.repositories.CouponDto

data class CouponAdminUiState(
    val isLoading: Boolean = false,
    val isSubmitting: Boolean = false,
    val coupons: List<CouponDto> = emptyList(),
    val code: String = "",
    val name: String = "",
    val discountType: String = "PERCENT",
    val discountValue: String = "",
    val minOrderTotal: String = "",
    val maxDiscountAmount: String = "",
    val usageLimit: String = "",
    val usagePerUserLimit: String = "",
    val successMessage: String? = null,
    val error: String? = null
)

class CouponAdminViewModel(
    private val repository: CouponAdminRepository
) {
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    private val _uiState = MutableStateFlow(CouponAdminUiState())
    val uiState: StateFlow<CouponAdminUiState> = _uiState.asStateFlow()

    init {
        loadCoupons()
    }

    fun loadCoupons() {
        scope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            repository.getCoupons().fold(
                onSuccess = { list ->
                    _uiState.update { it.copy(isLoading = false, coupons = list, error = null) }
                },
                onFailure = { error ->
                    _uiState.update { it.copy(isLoading = false, error = error.message ?: "Không thể tải danh sách coupon") }
                }
            )
        }
    }

    fun updateCode(value: String) = _uiState.update { it.copy(code = value) }
    fun updateName(value: String) = _uiState.update { it.copy(name = value) }
    fun updateDiscountType(value: String) = _uiState.update { it.copy(discountType = value.uppercase()) }
    fun updateDiscountValue(value: String) = _uiState.update { it.copy(discountValue = value) }
    fun updateMinOrder(value: String) = _uiState.update { it.copy(minOrderTotal = value) }
    fun updateMaxDiscount(value: String) = _uiState.update { it.copy(maxDiscountAmount = value) }
    fun updateUsageLimit(value: String) = _uiState.update { it.copy(usageLimit = value) }
    fun updateUsagePerUser(value: String) = _uiState.update { it.copy(usagePerUserLimit = value) }

    fun createCoupon() {
        scope.launch {
            val state = _uiState.value
            if (state.code.isBlank() || state.name.isBlank()) {
                _uiState.update { it.copy(error = "Nhập mã và tên coupon") }
                return@launch
            }
            val discountValue = state.discountValue.toDoubleOrNull()
            if (discountValue == null || discountValue <= 0.0) {
                _uiState.update { it.copy(error = "Giá trị giảm phải lớn hơn 0") }
                return@launch
            }

            _uiState.update { it.copy(isSubmitting = true, error = null, successMessage = null) }
            val request = CouponCreateRequest(
                code = state.code,
                name = state.name,
                discountType = state.discountType,
                discountValue = discountValue,
                minOrderTotal = state.minOrderTotal.toDoubleOrNull(),
                maxDiscountAmount = state.maxDiscountAmount.toDoubleOrNull(),
                usageLimit = state.usageLimit.toIntOrNull(),
                usagePerUserLimit = state.usagePerUserLimit.toIntOrNull(),
                isActive = true
            )

            repository.createCoupon(request).fold(
                onSuccess = {
                    _uiState.update {
                        it.copy(
                            isSubmitting = false,
                            successMessage = "Tạo coupon thành công",
                            error = null,
                            code = "",
                            name = "",
                            discountValue = "",
                            minOrderTotal = "",
                            maxDiscountAmount = "",
                            usageLimit = "",
                            usagePerUserLimit = ""
                        )
                    }
                    loadCoupons()
                },
                onFailure = { error ->
                    _uiState.update { it.copy(isSubmitting = false, error = error.message ?: "Không thể tạo coupon") }
                }
            )
        }
    }

    fun clearMessage() {
        _uiState.update { it.copy(successMessage = null, error = null) }
    }
}

