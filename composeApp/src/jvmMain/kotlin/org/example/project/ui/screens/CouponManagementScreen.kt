package org.example.project.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import org.example.project.data.repositories.AdminRewardProductDto
import org.example.project.data.repositories.CouponDto
import org.example.project.presentation.viewmodels.CouponAdminUiState
import org.example.project.presentation.viewmodels.CouponAdminViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CouponManagementScreen(viewModel: CouponAdminViewModel) {
    val state by viewModel.uiState.collectAsState()
    val discountOptions = listOf(
        "PERCENT" to "Giảm theo %",
        "FIXED_AMOUNT" to "Giảm tiền cố định"
    )
    val rewardTypeOptions = listOf(
        "VOUCHER" to "Voucher giảm giá",
        "ITEM" to "Quà vật lý"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Quản lý coupon và đổi điểm", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.primary
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(onClick = viewModel::loadData, enabled = !state.isLoading) {
                    Text(if (state.isLoading) "Đang tải..." else "Tải lại")
                }
                state.successMessage?.let { Text(it, color = MaterialTheme.colorScheme.primary) }
                state.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            }

            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    CouponTemplateFormCard(
                        state = state,
                        discountOptions = discountOptions,
                        onSubmit = viewModel::submitCoupon,
                        onCancelEdit = viewModel::cancelCouponEdit,
                        onCodeChange = viewModel::updateCode,
                        onNameChange = viewModel::updateName,
                        onDescriptionChange = viewModel::updateDescription,
                        onDiscountTypeChange = viewModel::updateDiscountType,
                        onDiscountValueChange = viewModel::updateDiscountValue,
                        onMinOrderChange = viewModel::updateMinOrder,
                        onMaxDiscountChange = viewModel::updateMaxDiscount,
                        onUsageLimitChange = viewModel::updateUsageLimit,
                        onUsagePerUserChange = viewModel::updateUsagePerUser,
                        onToggleActive = viewModel::toggleCouponIsActive,
                        onToggleTemplate = viewModel::toggleRewardVoucherTemplate
                    )

                    RewardProductFormCard(
                        state = state,
                        rewardTypeOptions = rewardTypeOptions,
                        templateCoupons = state.coupons.filter { it.isRewardVoucherTemplate },
                        onSubmit = viewModel::submitRewardProduct,
                        onCancelEdit = viewModel::cancelRewardProductEdit,
                        onRewardNameChange = viewModel::updateRewardName,
                        onRewardDescriptionChange = viewModel::updateRewardDescription,
                        onRewardImageUrlChange = viewModel::updateRewardImageUrl,
                        onRewardPointCostChange = viewModel::updateRewardPointCost,
                        onRewardStockChange = viewModel::updateRewardStock,
                        onRewardTypeChange = viewModel::updateRewardType,
                        onRewardCategoryChange = viewModel::updateRewardCategory,
                        onRewardCouponCodeChange = viewModel::updateRewardCouponCode,
                        onRewardTermsChange = viewModel::updateRewardTerms,
                        onRewardPriceTextChange = viewModel::updateRewardPriceText,
                        onRewardSortOrderChange = viewModel::updateRewardSortOrder,
                        onToggleRewardActive = viewModel::toggleRewardIsActive
                    )
                }

                Column(
                    modifier = Modifier
                        .weight(1.15f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    CouponTemplateListCard(
                        coupons = state.coupons,
                        onEdit = viewModel::editCoupon,
                        onToggleActive = viewModel::toggleCouponFromList
                    )
                    RewardProductListCard(
                        rewardProducts = state.rewardProducts,
                        onEdit = viewModel::editRewardProduct,
                        onToggleActive = viewModel::toggleRewardProductFromList
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CouponTemplateFormCard(
    state: CouponAdminUiState,
    discountOptions: List<Pair<String, String>>,
    onSubmit: () -> Unit,
    onCancelEdit: () -> Unit,
    onCodeChange: (String) -> Unit,
    onNameChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onDiscountTypeChange: (String) -> Unit,
    onDiscountValueChange: (String) -> Unit,
    onMinOrderChange: (String) -> Unit,
    onMaxDiscountChange: (String) -> Unit,
    onUsageLimitChange: (String) -> Unit,
    onUsagePerUserChange: (String) -> Unit,
    onToggleActive: (Boolean) -> Unit,
    onToggleTemplate: (Boolean) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("Coupon template / Mẫu giảm giá", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                state.editingCouponId?.let { AssistChip(onClick = {}, label = { Text("Đang sửa") }) }
            }
            Text(
                "Đây là khuôn điều kiện giảm giá: kiểu giảm, đơn tối thiểu, giới hạn lượt dùng. Reward voucher sẽ sinh mã riêng cho từng user khi admin duyệt đổi điểm.",
                style = MaterialTheme.typography.bodyMedium
            )

            OutlinedTextField(state.code, onCodeChange, label = { Text("Mã template") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(state.name, onNameChange, label = { Text("Tên chương trình") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(
                value = state.description,
                onValueChange = onDescriptionChange,
                label = { Text("Mô tả ngắn") },
                minLines = 2,
                modifier = Modifier.fillMaxWidth()
            )

            ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
                OutlinedTextField(
                    value = discountOptions.firstOrNull { it.first == state.discountType }?.second ?: state.discountType,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Loại giảm giá") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier.fillMaxWidth().menuAnchor()
                )
                ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    discountOptions.forEach { (value, label) ->
                        DropdownMenuItem(
                            text = { Text(label) },
                            onClick = {
                                onDiscountTypeChange(value)
                                expanded = false
                            }
                        )
                    }
                }
            }

            OutlinedTextField(
                value = state.discountValue,
                onValueChange = onDiscountValueChange,
                label = { Text(if (state.discountType == "PERCENT") "Giá trị giảm (%)" else "Giá trị giảm (VND)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = state.minOrderTotal,
                onValueChange = onMinOrderChange,
                label = { Text("Đơn tối thiểu để áp dụng") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = state.maxDiscountAmount,
                onValueChange = onMaxDiscountChange,
                label = { Text("Giảm tối đa") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = state.usageLimit,
                onValueChange = onUsageLimitChange,
                label = { Text("Tổng lượt dùng") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = state.usagePerUserLimit,
                onValueChange = onUsagePerUserChange,
                label = { Text("Giới hạn mỗi user") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            SwitchRow("Đang hoạt động", state.couponIsActive, onToggleActive)
            SwitchRow("Dùng làm template voucher đổi điểm", state.isRewardVoucherTemplate, onToggleTemplate)

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(onClick = onSubmit, enabled = !state.isSubmittingCoupon, modifier = Modifier.weight(1f)) {
                    Text(
                        when {
                            state.isSubmittingCoupon -> "Đang lưu..."
                            state.editingCouponId != null -> "Lưu coupon"
                            else -> "Tạo coupon template"
                        }
                    )
                }
                if (state.editingCouponId != null) {
                    OutlinedButton(onClick = onCancelEdit, enabled = !state.isSubmittingCoupon) {
                        Text("Hủy sửa")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RewardProductFormCard(
    state: CouponAdminUiState,
    rewardTypeOptions: List<Pair<String, String>>,
    templateCoupons: List<CouponDto>,
    onSubmit: () -> Unit,
    onCancelEdit: () -> Unit,
    onRewardNameChange: (String) -> Unit,
    onRewardDescriptionChange: (String) -> Unit,
    onRewardImageUrlChange: (String) -> Unit,
    onRewardPointCostChange: (String) -> Unit,
    onRewardStockChange: (String) -> Unit,
    onRewardTypeChange: (String) -> Unit,
    onRewardCategoryChange: (String) -> Unit,
    onRewardCouponCodeChange: (String) -> Unit,
    onRewardTermsChange: (String) -> Unit,
    onRewardPriceTextChange: (String) -> Unit,
    onRewardSortOrderChange: (String) -> Unit,
    onToggleRewardActive: (Boolean) -> Unit
) {
    var rewardTypeExpanded by remember { mutableStateOf(false) }
    var couponExpanded by remember { mutableStateOf(false) }

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("Reward product / Quà đổi điểm", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                state.editingRewardProductId?.let { AssistChip(onClick = {}, label = { Text("Đang sửa") }) }
            }
            Text(
                "Reward product là thứ user đổi bằng điểm. Với loại Voucher, hãy gắn coupon template để backend phát mã riêng khi admin duyệt.",
                style = MaterialTheme.typography.bodyMedium
            )

            ExposedDropdownMenuBox(expanded = rewardTypeExpanded, onExpandedChange = { rewardTypeExpanded = it }) {
                OutlinedTextField(
                    value = rewardTypeOptions.firstOrNull { it.first == state.rewardType }?.second ?: state.rewardType,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Loại reward") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = rewardTypeExpanded) },
                    modifier = Modifier.fillMaxWidth().menuAnchor()
                )
                ExposedDropdownMenu(expanded = rewardTypeExpanded, onDismissRequest = { rewardTypeExpanded = false }) {
                    rewardTypeOptions.forEach { (value, label) ->
                        DropdownMenuItem(
                            text = { Text(label) },
                            onClick = {
                                onRewardTypeChange(value)
                                rewardTypeExpanded = false
                            }
                        )
                    }
                }
            }

            OutlinedTextField(state.rewardName, onRewardNameChange, label = { Text("Tên reward") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(
                value = state.rewardDescription,
                onValueChange = onRewardDescriptionChange,
                label = { Text("Mô tả reward") },
                minLines = 2,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = state.rewardPointCost,
                onValueChange = onRewardPointCostChange,
                label = { Text("Số điểm cần đổi") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = state.rewardStock,
                onValueChange = onRewardStockChange,
                label = { Text("Số lượng phát hành / tồn") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(state.rewardCategory, onRewardCategoryChange, label = { Text("Nhóm reward") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(state.rewardPriceText, onRewardPriceTextChange, label = { Text("Nhãn giá trị hiển thị") }, modifier = Modifier.fillMaxWidth())

            if (state.rewardType == "VOUCHER") {
                ExposedDropdownMenuBox(expanded = couponExpanded, onExpandedChange = { couponExpanded = it }) {
                    OutlinedTextField(
                        value = state.rewardCouponCode,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Coupon template gắn với reward") },
                        placeholder = { Text("Chọn coupon template") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = couponExpanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    ExposedDropdownMenu(expanded = couponExpanded, onDismissRequest = { couponExpanded = false }) {
                        templateCoupons.forEach { coupon ->
                            DropdownMenuItem(
                                text = { Text("${coupon.code} - ${coupon.name}") },
                                onClick = {
                                    onRewardCouponCodeChange(coupon.code)
                                    couponExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            OutlinedTextField(
                value = state.rewardTerms,
                onValueChange = onRewardTermsChange,
                label = { Text("Điều kiện hiển thị cho user") },
                minLines = 2,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(state.rewardImageUrl, onRewardImageUrlChange, label = { Text("Ảnh reward") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(
                value = state.rewardSortOrder,
                onValueChange = onRewardSortOrderChange,
                label = { Text("Thứ tự hiển thị") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            SwitchRow("Đang hoạt động", state.rewardIsActive, onToggleRewardActive)

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(onClick = onSubmit, enabled = !state.isSubmittingRewardProduct, modifier = Modifier.weight(1f)) {
                    Text(
                        when {
                            state.isSubmittingRewardProduct -> "Đang lưu..."
                            state.editingRewardProductId != null -> "Lưu reward product"
                            else -> "Tạo reward product"
                        }
                    )
                }
                if (state.editingRewardProductId != null) {
                    OutlinedButton(onClick = onCancelEdit, enabled = !state.isSubmittingRewardProduct) {
                        Text("Hủy sửa")
                    }
                }
            }
        }
    }
}

@Composable
private fun SwitchRow(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, fontWeight = FontWeight.Medium)
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun CouponTemplateListCard(
    coupons: List<CouponDto>,
    onEdit: (CouponDto) -> Unit,
    onToggleActive: (CouponDto) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Danh sách coupon template", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            if (coupons.isEmpty()) {
                Text("Chưa có coupon nào")
            } else {
                coupons.forEach { coupon ->
                    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("${coupon.code} - ${coupon.name}", fontWeight = FontWeight.Medium)
                            Text(if (coupon.isActive) "Đang áp dụng" else "Tạm tắt", color = MaterialTheme.colorScheme.primary)
                        }
                        coupon.description?.takeIf { it.isNotBlank() }?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            AssistChip(onClick = {}, label = {
                                Text(
                                    if (coupon.discountType == "PERCENT") {
                                        "Giảm ${coupon.discountValue}%"
                                    } else {
                                        "Giảm ${formatCouponVnd(coupon.discountValue)}"
                                    }
                                )
                            })
                            coupon.minOrderTotal?.let { AssistChip(onClick = {}, label = { Text("Đơn tối thiểu ${formatCouponVnd(it)}") }) }
                            coupon.maxDiscountAmount?.let { AssistChip(onClick = {}, label = { Text("Max ${formatCouponVnd(it)}") }) }
                            coupon.usageLimit?.let { AssistChip(onClick = {}, label = { Text("Limit $it") }) }
                            coupon.usagePerUserLimit?.let { AssistChip(onClick = {}, label = { Text("Mỗi user $it") }) }
                            if (coupon.isRewardVoucherTemplate) {
                                AssistChip(onClick = {}, label = { Text("Template đổi điểm") })
                            }
                        }
                        Text("Đã dùng: ${coupon.usedCount}", style = MaterialTheme.typography.bodySmall)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(onClick = { onEdit(coupon) }) { Text("Sửa") }
                            OutlinedButton(onClick = { onToggleActive(coupon) }) { Text(if (coupon.isActive) "Tắt" else "Bật") }
                        }
                        HorizontalDivider()
                    }
                }
            }
        }
    }
}

@Composable
private fun RewardProductListCard(
    rewardProducts: List<AdminRewardProductDto>,
    onEdit: (AdminRewardProductDto) -> Unit,
    onToggleActive: (AdminRewardProductDto) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Catalog đổi điểm", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text(
                "Voucher đổi điểm dùng stock làm số lượng phát hành. Điều kiện đơn tối thiểu nằm ở coupon template gắn với reward.",
                style = MaterialTheme.typography.bodySmall
            )
            if (rewardProducts.isEmpty()) {
                Text("Chưa có reward product nào")
            } else {
                rewardProducts.forEach { reward ->
                    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(reward.name, fontWeight = FontWeight.Medium)
                            Text("${reward.pointCost} điểm", color = MaterialTheme.colorScheme.primary)
                        }
                        reward.description?.takeIf { it.isNotBlank() }?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            AssistChip(onClick = {}, label = { Text(if (reward.rewardType == "VOUCHER") "Voucher" else "Quà vật lý") })
                            AssistChip(onClick = {}, label = { Text("Tồn ${reward.stock}") })
                            AssistChip(onClick = {}, label = { Text(if (reward.isActive) "Đang hoạt động" else "Tạm tắt") })
                            reward.category?.takeIf { it.isNotBlank() }?.let { AssistChip(onClick = {}, label = { Text(it) }) }
                        }
                        if (reward.rewardType == "VOUCHER") {
                            Text(
                                "Coupon gắn: ${reward.couponCode ?: "-"}${reward.couponName?.let { " - $it" } ?: ""}",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        reward.terms?.takeIf { it.isNotBlank() }?.let { Text("Điều kiện: $it", style = MaterialTheme.typography.bodySmall) }
                        reward.updatedAt?.takeIf { it.isNotBlank() }?.let { Text("Cập nhật: ${it.take(16)}", style = MaterialTheme.typography.bodySmall) }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(onClick = { onEdit(reward) }) { Text("Sửa") }
                            OutlinedButton(onClick = { onToggleActive(reward) }) { Text(if (reward.isActive) "Tắt" else "Bật") }
                        }
                        HorizontalDivider()
                    }
                }
            }
        }
    }
}

private fun formatCouponVnd(amount: Double): String {
    return "%.0f đ".format(amount).replace(Regex("\\B(?=(\\d{3})+(?!\\d))"), ".")
}
