package org.example.project.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.example.project.presentation.viewmodels.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(viewModel: SettingsViewModel) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Cài đặt cửa hàng", fontWeight = FontWeight.Bold) },
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        "Màn này hiện phù hợp nhất với DB/backend mới: cấu hình hồ sơ cửa hàng và ngưỡng cảnh báo hạn dùng. " +
                            "Thiết lập cổng thanh toán, domain public, webhook vẫn nên quản lý bằng env/deploy thay vì form admin.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text("Store ID: ${uiState.storeId.ifBlank { "default-store" }}")
                    Text("Trạng thái duyệt: ${if (uiState.isApproved) "Đã duyệt" else "Chưa duyệt"}")
                }
            }

            when {
                uiState.isLoading -> CircularProgressIndicator()
                else -> {
                    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            OutlinedTextField(
                                value = uiState.name,
                                onValueChange = viewModel::updateName,
                                label = { Text("Tên cửa hàng") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            OutlinedTextField(
                                value = uiState.description,
                                onValueChange = viewModel::updateDescription,
                                label = { Text("Mô tả") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            OutlinedTextField(
                                value = uiState.licenseNumber,
                                onValueChange = viewModel::updateLicenseNumber,
                                label = { Text("Số giấy phép") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            OutlinedTextField(
                                value = uiState.logoUrl,
                                onValueChange = viewModel::updateLogoUrl,
                                label = { Text("Logo URL") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            OutlinedTextField(
                                value = uiState.expiryAlertDays,
                                onValueChange = viewModel::updateExpiryAlertDays,
                                label = { Text("Cảnh báo hạn dùng trước (ngày)") },
                                modifier = Modifier.fillMaxWidth()
                            )

                            uiState.successMessage?.let { Text(it, color = MaterialTheme.colorScheme.primary) }
                            uiState.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }

                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                Button(onClick = viewModel::loadSettings, enabled = !uiState.isSaving) {
                                    Text("Tải lại")
                                }
                                Button(onClick = viewModel::saveSettings, enabled = !uiState.isSaving) {
                                    Text(if (uiState.isSaving) "Đang lưu..." else "Lưu cấu hình")
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}
