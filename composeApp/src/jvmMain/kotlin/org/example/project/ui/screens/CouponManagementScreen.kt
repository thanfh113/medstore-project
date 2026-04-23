package org.example.project.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import org.example.project.presentation.viewmodels.CouponAdminViewModel
import androidx.compose.foundation.text.KeyboardOptions

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CouponManagementScreen(viewModel: CouponAdminViewModel) {
    val state by viewModel.uiState.collectAsState()
    val discountOptions = listOf(
        "PERCENT" to "Giảm theo phần trăm",
        "FIXED_AMOUNT" to "Giảm số tiền cố định"
    )
    var expanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Quản lý mã giảm giá", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.primary
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Tạo mã giảm giá mới", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)

                    OutlinedTextField(
                        value = state.code,
                        onValueChange = viewModel::updateCode,
                        label = { Text("Mã coupon") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = state.name,
                        onValueChange = viewModel::updateName,
                        label = { Text("Tên chương trình") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = it }
                    ) {
                        OutlinedTextField(
                            value = discountOptions.firstOrNull { it.first == state.discountType }?.second ?: state.discountType,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Loại giảm giá") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            discountOptions.forEach { (value, label) ->
                                DropdownMenuItem(
                                    text = { Text(label) },
                                    onClick = {
                                        viewModel.updateDiscountType(value)
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = state.discountValue,
                        onValueChange = viewModel::updateDiscountValue,
                        label = {
                            Text(
                                if (state.discountType == "PERCENT") "Giá trị giảm (%)"
                                else "Giá trị giảm (đ)"
                            )
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = state.minOrderTotal,
                        onValueChange = viewModel::updateMinOrder,
                        label = { Text("Đơn tối thiểu (đ)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = state.maxDiscountAmount,
                        onValueChange = viewModel::updateMaxDiscount,
                        label = { Text("Giảm tối đa (đ, để trống nếu không giới hạn)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = state.usageLimit,
                        onValueChange = viewModel::updateUsageLimit,
                        label = { Text("Tổng lượt sử dụng") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = state.usagePerUserLimit,
                        onValueChange = viewModel::updateUsagePerUser,
                        label = { Text("Giới hạn mỗi người dùng") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = viewModel::createCoupon,
                        enabled = !state.isSubmitting,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(if (state.isSubmitting) "Đang tạo..." else "Tạo mã giảm giá")
                    }

                    state.successMessage?.let {
                        Text(it, color = MaterialTheme.colorScheme.primary)
                    }
                    state.error?.let {
                        Text(it, color = MaterialTheme.colorScheme.error)
                    }
                }
            }

            Card(
                modifier = Modifier.weight(1.2f),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Danh sách mã giảm giá", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(8.dp))

                    if (state.coupons.isEmpty()) {
                        Text("Chưa có coupon nào")
                    } else {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            items(state.coupons) { coupon ->
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp)
                                ) {
                                    Text("${coupon.code} - ${coupon.name}", fontWeight = FontWeight.Medium)
                                    Text(
                                        if (coupon.discountType == "PERCENT") {
                                            "Giảm ${coupon.discountValue}%"
                                        } else {
                                            "Giảm ${formatCouponVnd(coupon.discountValue)}"
                                        }
                                    )
                                    Text("Đã dùng: ${coupon.usedCount} | Trạng thái: ${if (coupon.isActive) "Đang áp dụng" else "Tạm tắt"}")
                                }
                                HorizontalDivider()
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun formatCouponVnd(amount: Double): String {
    return "%.0f đ".format(amount).replace(Regex("\\B(?=(\\d{3})+(?!\\d))"), ".")
}
