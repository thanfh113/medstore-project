package org.example.project.presentation.viewmodels

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.example.project.data.repositories.AdminRewardProductDto
import org.example.project.data.repositories.AdminRewardProductUpsertRequest
import org.example.project.data.repositories.CouponAdminRepository
import org.example.project.data.repositories.CouponCreateRequest
import org.example.project.data.repositories.CouponDto

data class CouponAdminUiState(
    val isLoading: Boolean = false,
    val isSubmittingCoupon: Boolean = false,
    val isSubmittingRewardProduct: Boolean = false,
    val coupons: List<CouponDto> = emptyList(),
    val rewardProducts: List<AdminRewardProductDto> = emptyList(),
    val editingCouponId: String? = null,
    val editingRewardProductId: String? = null,
    val code: String = "",
    val name: String = "",
    val description: String = "",
    val discountType: String = "PERCENT",
    val discountValue: String = "",
    val minOrderTotal: String = "",
    val maxDiscountAmount: String = "",
    val usageLimit: String = "",
    val usagePerUserLimit: String = "1",
    val couponIsActive: Boolean = true,
    val isRewardVoucherTemplate: Boolean = false,
    val rewardName: String = "",
    val rewardDescription: String = "",
    val rewardImageUrl: String = "",
    val rewardPointCost: String = "",
    val rewardStock: String = "",
    val rewardType: String = "VOUCHER",
    val rewardCategory: String = "VOUCHER",
    val rewardCouponCode: String = "",
    val rewardTerms: String = "",
    val rewardPriceText: String = "",
    val rewardSortOrder: String = "0",
    val rewardIsActive: Boolean = true,
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
        loadData()
    }

    fun loadData() {
        scope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            var coupons: List<CouponDto> = emptyList()
            var rewardProducts: List<AdminRewardProductDto> = emptyList()
            var firstError: String? = null

            repository.getCoupons().fold(
                onSuccess = { coupons = it },
                onFailure = { firstError = it.message ?: "Khong the tai coupon" }
            )

            repository.getRewardProducts().fold(
                onSuccess = { rewardProducts = it },
                onFailure = { if (firstError == null) firstError = it.message ?: "Khong the tai reward products" }
            )

            _uiState.update { state ->
                val selectedCouponCode = state.rewardCouponCode
                    .takeIf { code -> coupons.any { it.code.equals(code, ignoreCase = true) && it.isRewardVoucherTemplate } }
                    ?: coupons.firstOrNull { it.isRewardVoucherTemplate }?.code.orEmpty()
                state.copy(
                    isLoading = false,
                    coupons = coupons,
                    rewardProducts = rewardProducts,
                    rewardCouponCode = if (state.rewardType == "VOUCHER") selectedCouponCode else state.rewardCouponCode,
                    error = firstError
                )
            }
        }
    }

    fun updateCode(value: String) = _uiState.update { it.copy(code = value.uppercase(), error = null, successMessage = null) }
    fun updateName(value: String) = _uiState.update { it.copy(name = value, error = null, successMessage = null) }
    fun updateDescription(value: String) = _uiState.update { it.copy(description = value, error = null, successMessage = null) }
    fun updateDiscountType(value: String) = _uiState.update { it.copy(discountType = value.uppercase(), error = null, successMessage = null) }
    fun updateDiscountValue(value: String) = _uiState.update { it.copy(discountValue = value, error = null, successMessage = null) }
    fun updateMinOrder(value: String) = _uiState.update { it.copy(minOrderTotal = value, error = null, successMessage = null) }
    fun updateMaxDiscount(value: String) = _uiState.update { it.copy(maxDiscountAmount = value, error = null, successMessage = null) }
    fun updateUsageLimit(value: String) = _uiState.update { it.copy(usageLimit = value, error = null, successMessage = null) }
    fun updateUsagePerUser(value: String) = _uiState.update { it.copy(usagePerUserLimit = value, error = null, successMessage = null) }
    fun toggleCouponIsActive(enabled: Boolean) = _uiState.update { it.copy(couponIsActive = enabled, error = null, successMessage = null) }
    fun toggleRewardVoucherTemplate(enabled: Boolean) = _uiState.update {
        it.copy(
            isRewardVoucherTemplate = enabled,
            usagePerUserLimit = if (enabled && it.usagePerUserLimit.isBlank()) "1" else it.usagePerUserLimit,
            error = null,
            successMessage = null
        )
    }

    fun updateRewardName(value: String) = _uiState.update { it.copy(rewardName = value, error = null, successMessage = null) }
    fun updateRewardDescription(value: String) = _uiState.update { it.copy(rewardDescription = value, error = null, successMessage = null) }
    fun updateRewardImageUrl(value: String) = _uiState.update { it.copy(rewardImageUrl = value, error = null, successMessage = null) }
    fun updateRewardPointCost(value: String) = _uiState.update { it.copy(rewardPointCost = value.filter(Char::isDigit), error = null, successMessage = null) }
    fun updateRewardStock(value: String) = _uiState.update { it.copy(rewardStock = value.filter(Char::isDigit), error = null, successMessage = null) }
    fun updateRewardType(value: String) = _uiState.update {
        val normalized = value.uppercase()
        it.copy(
            rewardType = normalized,
            rewardCategory = if (normalized == "VOUCHER") "VOUCHER" else it.rewardCategory,
            rewardCouponCode = if (normalized == "VOUCHER") it.rewardCouponCode else "",
            error = null,
            successMessage = null
        )
    }
    fun updateRewardCategory(value: String) = _uiState.update { it.copy(rewardCategory = value, error = null, successMessage = null) }
    fun updateRewardCouponCode(value: String) = _uiState.update { it.copy(rewardCouponCode = value.uppercase(), error = null, successMessage = null) }
    fun updateRewardTerms(value: String) = _uiState.update { it.copy(rewardTerms = value, error = null, successMessage = null) }
    fun updateRewardPriceText(value: String) = _uiState.update { it.copy(rewardPriceText = value, error = null, successMessage = null) }
    fun updateRewardSortOrder(value: String) = _uiState.update {
        it.copy(
            rewardSortOrder = buildString {
                value.forEach { ch ->
                    if (ch == '-' || ch.isDigit()) append(ch)
                }
            },
            error = null,
            successMessage = null
        )
    }
    fun toggleRewardIsActive(enabled: Boolean) = _uiState.update { it.copy(rewardIsActive = enabled, error = null, successMessage = null) }

    fun createCoupon() {
        submitCoupon()
    }

    fun submitCoupon() {
        scope.launch {
            val state = _uiState.value
            if (state.code.isBlank() || state.name.isBlank()) {
                _uiState.update { it.copy(error = "Nhap ma va ten coupon") }
                return@launch
            }

            val discountValue = state.discountValue.toDoubleOrNull()
            if (discountValue == null || discountValue <= 0.0) {
                _uiState.update { it.copy(error = "Gia tri giam phai lon hon 0") }
                return@launch
            }

            _uiState.update { it.copy(isSubmittingCoupon = true, error = null, successMessage = null) }

            val request = CouponCreateRequest(
                code = state.code.trim(),
                name = state.name.trim(),
                description = state.description.trim().ifBlank { null },
                discountType = state.discountType,
                discountValue = discountValue,
                minOrderTotal = state.minOrderTotal.toDoubleOrNull(),
                maxDiscountAmount = state.maxDiscountAmount.toDoubleOrNull(),
                usageLimit = state.usageLimit.toIntOrNull(),
                usagePerUserLimit = state.usagePerUserLimit.toIntOrNull(),
                isActive = state.couponIsActive,
                isRewardVoucherTemplate = state.isRewardVoucherTemplate
            )

            val result = state.editingCouponId?.let { repository.updateCoupon(it, request).map { updated -> updated.id } }
                ?: repository.createCoupon(request)

            result.fold(
                onSuccess = {
                    _uiState.update {
                        it.copy(
                            isSubmittingCoupon = false,
                            editingCouponId = null,
                            code = "",
                            name = "",
                            description = "",
                            discountValue = "",
                            minOrderTotal = "",
                            maxDiscountAmount = "",
                            usageLimit = "",
                            usagePerUserLimit = if (state.isRewardVoucherTemplate) "1" else "",
                            couponIsActive = true,
                            isRewardVoucherTemplate = false,
                            successMessage = if (state.editingCouponId == null) {
                                "Da tao coupon/template moi"
                            } else {
                                "Da cap nhat coupon/template"
                            }
                        )
                    }
                    loadData()
                },
                onFailure = { error ->
                    _uiState.update { it.copy(isSubmittingCoupon = false, error = error.message ?: "Khong the tao coupon") }
                }
            )
        }
    }

    fun editCoupon(coupon: CouponDto) {
        _uiState.update {
            it.copy(
                editingCouponId = coupon.id,
                code = coupon.code,
                name = coupon.name,
                description = coupon.description.orEmpty(),
                discountType = coupon.discountType,
                discountValue = coupon.discountValue.toPlainInput(),
                minOrderTotal = coupon.minOrderTotal.toPlainInput(),
                maxDiscountAmount = coupon.maxDiscountAmount.toPlainInput(),
                usageLimit = coupon.usageLimit?.toString().orEmpty(),
                usagePerUserLimit = coupon.usagePerUserLimit?.toString().orEmpty(),
                couponIsActive = coupon.isActive,
                isRewardVoucherTemplate = coupon.isRewardVoucherTemplate,
                error = null,
                successMessage = null
            )
        }
    }

    fun cancelCouponEdit() {
        _uiState.update {
            it.copy(
                editingCouponId = null,
                code = "",
                name = "",
                description = "",
                discountValue = "",
                minOrderTotal = "",
                maxDiscountAmount = "",
                usageLimit = "",
                usagePerUserLimit = "1",
                couponIsActive = true,
                isRewardVoucherTemplate = false,
                error = null,
                successMessage = null
            )
        }
    }

    fun toggleCouponFromList(coupon: CouponDto) {
        scope.launch {
            _uiState.update { it.copy(isSubmittingCoupon = true, error = null, successMessage = null) }
            val request = CouponCreateRequest(
                code = coupon.code,
                name = coupon.name,
                description = coupon.description,
                discountType = coupon.discountType,
                discountValue = coupon.discountValue,
                minOrderTotal = coupon.minOrderTotal,
                maxDiscountAmount = coupon.maxDiscountAmount,
                usageLimit = coupon.usageLimit,
                usagePerUserLimit = coupon.usagePerUserLimit,
                isActive = !coupon.isActive,
                isRewardVoucherTemplate = coupon.isRewardVoucherTemplate
            )

            repository.updateCoupon(coupon.id, request).fold(
                onSuccess = {
                    _uiState.update {
                        it.copy(
                            isSubmittingCoupon = false,
                            successMessage = if (coupon.isActive) "Da tam tat coupon" else "Da bat coupon"
                        )
                    }
                    loadData()
                },
                onFailure = { error ->
                    _uiState.update { it.copy(isSubmittingCoupon = false, error = error.message ?: "Khong the cap nhat coupon") }
                }
            )
        }
    }

    fun createRewardProduct() {
        submitRewardProduct()
    }

    fun submitRewardProduct() {
        scope.launch {
            val state = _uiState.value
            if (state.rewardName.isBlank()) {
                _uiState.update { it.copy(error = "Nhap ten reward product") }
                return@launch
            }

            val pointCost = state.rewardPointCost.toIntOrNull()
            if (pointCost == null || pointCost <= 0) {
                _uiState.update { it.copy(error = "Point cost phai lon hon 0") }
                return@launch
            }

            val stock = state.rewardStock.toIntOrNull()
            if (stock == null || stock < 0) {
                _uiState.update { it.copy(error = "Stock khong hop le") }
                return@launch
            }

            if (state.rewardType == "VOUCHER" && state.rewardCouponCode.isBlank()) {
                _uiState.update { it.copy(error = "Voucher reward phai gan coupon template") }
                return@launch
            }

            _uiState.update { it.copy(isSubmittingRewardProduct = true, error = null, successMessage = null) }

            val request = AdminRewardProductUpsertRequest(
                name = state.rewardName.trim(),
                description = state.rewardDescription.trim().ifBlank { null },
                imageUrl = state.rewardImageUrl.trim().ifBlank { null },
                pointCost = pointCost,
                stock = stock,
                rewardType = state.rewardType,
                category = state.rewardCategory.trim().ifBlank {
                    if (state.rewardType == "VOUCHER") "VOUCHER" else null
                },
                couponCode = state.rewardCouponCode.trim().ifBlank { null },
                terms = state.rewardTerms.trim().ifBlank { null },
                priceText = state.rewardPriceText.trim().ifBlank { null },
                isActive = state.rewardIsActive,
                sortOrder = state.rewardSortOrder.toIntOrNull() ?: 0
            )

            val result = state.editingRewardProductId?.let { repository.updateRewardProduct(it, request) }
                ?: repository.createRewardProduct(request)

            result.fold(
                onSuccess = {
                    _uiState.update {
                        it.copy(
                            isSubmittingRewardProduct = false,
                            editingRewardProductId = null,
                            rewardName = "",
                            rewardDescription = "",
                            rewardImageUrl = "",
                            rewardPointCost = "",
                            rewardStock = "",
                            rewardCouponCode = "",
                            rewardTerms = "",
                            rewardPriceText = "",
                            rewardSortOrder = "0",
                            rewardIsActive = true,
                            successMessage = if (state.editingRewardProductId == null) {
                                "Da tao reward product"
                            } else {
                                "Da cap nhat reward product"
                            }
                        )
                    }
                    loadData()
                },
                onFailure = { error ->
                    _uiState.update { it.copy(isSubmittingRewardProduct = false, error = error.message ?: "Khong the tao reward product") }
                }
            )
        }
    }

    fun editRewardProduct(product: AdminRewardProductDto) {
        _uiState.update {
            it.copy(
                editingRewardProductId = product.id,
                rewardName = product.name,
                rewardDescription = product.description.orEmpty(),
                rewardImageUrl = product.imageUrl.orEmpty(),
                rewardPointCost = product.pointCost.toString(),
                rewardStock = product.stock.toString(),
                rewardType = product.rewardType,
                rewardCategory = product.category.orEmpty(),
                rewardCouponCode = product.couponCode.orEmpty(),
                rewardTerms = product.terms.orEmpty(),
                rewardPriceText = product.priceText.orEmpty(),
                rewardSortOrder = product.sortOrder.toString(),
                rewardIsActive = product.isActive,
                error = null,
                successMessage = null
            )
        }
    }

    fun cancelRewardProductEdit() {
        _uiState.update {
            it.copy(
                editingRewardProductId = null,
                rewardName = "",
                rewardDescription = "",
                rewardImageUrl = "",
                rewardPointCost = "",
                rewardStock = "",
                rewardType = "VOUCHER",
                rewardCategory = "VOUCHER",
                rewardCouponCode = "",
                rewardTerms = "",
                rewardPriceText = "",
                rewardSortOrder = "0",
                rewardIsActive = true,
                error = null,
                successMessage = null
            )
        }
    }

    fun toggleRewardProductFromList(product: AdminRewardProductDto) {
        scope.launch {
            _uiState.update { it.copy(isSubmittingRewardProduct = true, error = null, successMessage = null) }
            val request = AdminRewardProductUpsertRequest(
                name = product.name,
                description = product.description,
                imageUrl = product.imageUrl,
                pointCost = product.pointCost,
                stock = product.stock,
                rewardType = product.rewardType,
                category = product.category,
                couponCode = product.couponCode,
                terms = product.terms,
                priceText = product.priceText,
                isActive = !product.isActive,
                sortOrder = product.sortOrder
            )

            repository.updateRewardProduct(product.id, request).fold(
                onSuccess = {
                    _uiState.update {
                        it.copy(
                            isSubmittingRewardProduct = false,
                            successMessage = if (product.isActive) "Da tam tat reward product" else "Da bat reward product"
                        )
                    }
                    loadData()
                },
                onFailure = { error ->
                    _uiState.update { it.copy(isSubmittingRewardProduct = false, error = error.message ?: "Khong the cap nhat reward product") }
                }
            )
        }
    }

    fun clearMessage() {
        _uiState.update { it.copy(successMessage = null, error = null) }
    }
}

private fun Double?.toPlainInput(): String {
    val value = this ?: return ""
    return if (value % 1.0 == 0.0) value.toLong().toString() else value.toString()
}
