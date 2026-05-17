package org.example.project.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.example.project.data.repositories.AdminRewardProductDto
import org.example.project.data.repositories.CouponDto
import org.example.project.presentation.viewmodels.CouponAdminUiState
import org.example.project.presentation.viewmodels.CouponAdminViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CouponManagementScreen(viewModel: CouponAdminViewModel) {
    val state by viewModel.uiState.collectAsState()
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Mã giảm giá", "Quà đổi điểm")
    val discountOptions = listOf("PERCENT" to "Giảm theo %", "FIXED_AMOUNT" to "Giảm tiền cố định", "FREESHIP" to "Freeship (Miễn phí vận chuyển)")
    val rewardTypeOptions = listOf("VOUCHER" to "Voucher giảm giá", "ITEM" to "Quà vật lý")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Quản lý khuyến mãi & đổi điểm", fontWeight = FontWeight.Bold) },
                actions = {
                    state.successMessage?.let {
                        Text(it, color = MaterialTheme.colorScheme.primary, fontSize = 12.sp, modifier = Modifier.padding(end = 4.dp), maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                    state.error?.let {
                        Text(it, color = MaterialTheme.colorScheme.error, fontSize = 12.sp, modifier = Modifier.padding(end = 4.dp), maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                    IconButton(onClick = viewModel::loadData, enabled = !state.isLoading) {
                        if (state.isLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                        } else {
                            Icon(Icons.Default.Refresh, contentDescription = "Tải lại")
                        }
                    }
                },
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
            TabRow(selectedTabIndex = selectedTab) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title) }
                    )
                }
            }

            when (selectedTab) {
                0 -> CouponTab(state = state, discountOptions = discountOptions, viewModel = viewModel)
                1 -> RewardProductTab(state = state, rewardTypeOptions = rewardTypeOptions, viewModel = viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CouponTab(
    state: CouponAdminUiState,
    discountOptions: List<Pair<String, String>>,
    viewModel: CouponAdminViewModel
) {
    Row(modifier = Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        Column(
            modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(0.dp)
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
        }

        LazyColumn(
            modifier = Modifier.weight(1.2f),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Danh sách (${state.coupons.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    val templateCount = state.coupons.count { it.isRewardVoucherTemplate }
                    if (templateCount > 0) {
                        Text(
                            "$templateCount template voucher",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            }
            if (state.coupons.isEmpty()) {
                item {
                    Text(
                        "Chưa có coupon nào. Điền form bên trái để tạo mới.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
            items(state.coupons, key = { it.code }) { coupon ->
                CouponItemCard(
                    coupon = coupon,
                    onEdit = viewModel::editCoupon,
                    onToggleActive = viewModel::toggleCouponFromList
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RewardProductTab(
    state: CouponAdminUiState,
    rewardTypeOptions: List<Pair<String, String>>,
    viewModel: CouponAdminViewModel
) {
    Row(modifier = Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        Column(
            modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
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

        LazyColumn(
            modifier = Modifier.weight(1.2f),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        "Catalog đổi điểm (${state.rewardProducts.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        "Voucher tự phát mã khi duyệt. Quà vật lý cần admin xử lý thủ công.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
            if (state.rewardProducts.isEmpty()) {
                item {
                    Text(
                        "Chưa có quà đổi điểm nào.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
            items(state.rewardProducts, key = { it.name }) { reward ->
                RewardProductItemCard(
                    reward = reward,
                    onEdit = viewModel::editRewardProduct,
                    onToggleActive = viewModel::toggleRewardProductFromList
                )
            }
        }
    }
}

@Composable
private fun CouponItemCard(
    coupon: CouponDto,
    onEdit: (CouponDto) -> Unit,
    onToggleActive: (CouponDto) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(coupon.code, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                    Text(
                        coupon.name,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Text(
                    if (coupon.isActive) "Đang dùng" else "Tạm tắt",
                    color = if (coupon.isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
            coupon.description?.takeIf { it.isNotBlank() }?.let {
                Text(
                    it,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                AssistChip(
                    onClick = {},
                    label = {
                        Text(
                            when (coupon.discountType) {
                                "PERCENT" -> "Giảm ${coupon.discountValue}%"
                                "FREESHIP" -> "Freeship"
                                else -> "Giảm ${formatCouponVnd(coupon.discountValue)}"
                            }
                        )
                    }
                )
                coupon.minOrderTotal?.let {
                    AssistChip(onClick = {}, label = { Text("Đơn min ${formatCouponVnd(it)}") })
                }
                coupon.maxDiscountAmount?.let {
                    AssistChip(onClick = {}, label = { Text("Max ${formatCouponVnd(it)}") })
                }
                coupon.usageLimit?.let {
                    AssistChip(onClick = {}, label = { Text("Giới hạn $it lần") })
                }
                coupon.usagePerUserLimit?.let {
                    AssistChip(onClick = {}, label = { Text("Mỗi user $it") })
                }
                if (coupon.isRewardVoucherTemplate) {
                    AssistChip(onClick = {}, label = { Text("Template voucher") })
                }
            }
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Đã dùng: ${coupon.usedCount} lần",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = { onEdit(coupon) }) { Text("Sửa") }
                    OutlinedButton(onClick = { onToggleActive(coupon) }) {
                        Text(if (coupon.isActive) "Tắt" else "Bật")
                    }
                }
            }
        }
    }
}

@Composable
private fun RewardProductItemCard(
    reward: AdminRewardProductDto,
    onEdit: (AdminRewardProductDto) -> Unit,
    onToggleActive: (AdminRewardProductDto) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        reward.name,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleSmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        if (reward.rewardType == "VOUCHER") "Voucher giảm giá" else "Quà vật lý",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Text(
                    if (reward.isActive) "Đang hoạt động" else "Tạm tắt",
                    color = if (reward.isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
            reward.description?.takeIf { it.isNotBlank() }?.let {
                Text(
                    it,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                AssistChip(onClick = {}, label = { Text("${reward.pointCost} điểm") })
                AssistChip(onClick = {}, label = { Text("Tồn: ${reward.stock}") })
                reward.category?.takeIf { it.isNotBlank() }?.let {
                    AssistChip(onClick = {}, label = { Text(it) })
                }
            }
            if (reward.rewardType == "VOUCHER") {
                Text(
                    "Template: ${reward.couponCode ?: "Chưa gắn"}${reward.couponName?.let { " ($it)" } ?: ""}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            reward.terms?.takeIf { it.isNotBlank() }?.let {
                Text(
                    "Điều kiện: $it",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(
                    reward.updatedAt?.takeIf { it.isNotBlank() }?.let { "Cập nhật: ${it.take(16)}" } ?: "",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = { onEdit(reward) }) { Text("Sửa") }
                    OutlinedButton(onClick = { onToggleActive(reward) }) {
                        Text(if (reward.isActive) "Tắt" else "Bật")
                    }
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
                Text(
                    if (state.editingCouponId != null) "Sửa coupon template" else "Tạo coupon template",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Text(
                "Mã template định nghĩa điều kiện giảm giá. Nếu đánh dấu làm template voucher, reward product có thể gắn vào để phát mã riêng cho từng user.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.outline
            )

            OutlinedTextField(
                value = state.code,
                onValueChange = onCodeChange,
                label = { Text("Mã template") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = state.name,
                onValueChange = onNameChange,
                label = { Text("Tên chương trình") },
                modifier = Modifier.fillMaxWidth()
            )
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
                            onClick = { onDiscountTypeChange(value); expanded = false }
                        )
                    }
                }
            }

            if (state.discountType != "FREESHIP") {
                OutlinedTextField(
                    value = state.discountValue,
                    onValueChange = onDiscountValueChange,
                    label = { Text(if (state.discountType == "PERCENT") "Giá trị giảm (%)" else "Giá trị giảm (VND)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )
            }
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
                label = { Text("Giảm tối đa (VND)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = state.usageLimit,
                onValueChange = onUsageLimitChange,
                label = { Text("Tổng lượt dùng (để trống = không giới hạn)") },
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
                            state.editingCouponId != null -> "Lưu thay đổi"
                            else -> "Tạo coupon"
                        }
                    )
                }
                if (state.editingCouponId != null) {
                    OutlinedButton(onClick = onCancelEdit, enabled = !state.isSubmittingCoupon) {
                        Text("Hủy")
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
            Text(
                if (state.editingRewardProductId != null) "Sửa quà đổi điểm" else "Tạo quà đổi điểm",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                "Voucher: gắn coupon template để backend phát mã riêng khi admin duyệt. Quà vật lý: xử lý thủ công qua tab Đổi điểm.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.outline
            )

            ExposedDropdownMenuBox(expanded = rewardTypeExpanded, onExpandedChange = { rewardTypeExpanded = it }) {
                OutlinedTextField(
                    value = rewardTypeOptions.firstOrNull { it.first == state.rewardType }?.second ?: state.rewardType,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Loại quà") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = rewardTypeExpanded) },
                    modifier = Modifier.fillMaxWidth().menuAnchor()
                )
                ExposedDropdownMenu(expanded = rewardTypeExpanded, onDismissRequest = { rewardTypeExpanded = false }) {
                    rewardTypeOptions.forEach { (value, label) ->
                        DropdownMenuItem(
                            text = { Text(label) },
                            onClick = { onRewardTypeChange(value); rewardTypeExpanded = false }
                        )
                    }
                }
            }

            OutlinedTextField(
                value = state.rewardName,
                onValueChange = onRewardNameChange,
                label = { Text("Tên quà") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = state.rewardDescription,
                onValueChange = onRewardDescriptionChange,
                label = { Text("Mô tả") },
                minLines = 2,
                modifier = Modifier.fillMaxWidth()
            )

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = state.rewardPointCost,
                    onValueChange = onRewardPointCostChange,
                    label = { Text("Điểm cần đổi") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = state.rewardStock,
                    onValueChange = onRewardStockChange,
                    label = { Text("Số lượng tồn") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = state.rewardCategory,
                    onValueChange = onRewardCategoryChange,
                    label = { Text("Nhóm") },
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = state.rewardPriceText,
                    onValueChange = onRewardPriceTextChange,
                    label = { Text("Nhãn giá trị") },
                    modifier = Modifier.weight(1f)
                )
            }

            if (state.rewardType == "VOUCHER") {
                ExposedDropdownMenuBox(expanded = couponExpanded, onExpandedChange = { couponExpanded = it }) {
                    OutlinedTextField(
                        value = state.rewardCouponCode,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Coupon template gắn với voucher") },
                        placeholder = { Text(if (templateCoupons.isEmpty()) "Chưa có template nào" else "Chọn template") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = couponExpanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    ExposedDropdownMenu(expanded = couponExpanded, onDismissRequest = { couponExpanded = false }) {
                        if (templateCoupons.isEmpty()) {
                            DropdownMenuItem(
                                text = { Text("Chưa có template voucher nào") },
                                onClick = { couponExpanded = false }
                            )
                        }
                        templateCoupons.forEach { coupon ->
                            DropdownMenuItem(
                                text = { Text("${coupon.code} — ${coupon.name}") },
                                onClick = { onRewardCouponCodeChange(coupon.code); couponExpanded = false }
                            )
                        }
                    }
                }
            }

            OutlinedTextField(
                value = state.rewardTerms,
                onValueChange = onRewardTermsChange,
                label = { Text("Điều kiện áp dụng (hiển thị cho user)") },
                minLines = 2,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = state.rewardImageUrl,
                onValueChange = onRewardImageUrlChange,
                label = { Text("URL ảnh đại diện") },
                modifier = Modifier.fillMaxWidth()
            )
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
                            state.editingRewardProductId != null -> "Lưu thay đổi"
                            else -> "Tạo quà đổi điểm"
                        }
                    )
                }
                if (state.editingRewardProductId != null) {
                    OutlinedButton(onClick = onCancelEdit, enabled = !state.isSubmittingRewardProduct) {
                        Text("Hủy")
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

private fun formatCouponVnd(amount: Double): String {
    return "%.0f đ".format(amount).replace(Regex("\\B(?=(\\d{3})+(?!\\d))"), ".")
}
