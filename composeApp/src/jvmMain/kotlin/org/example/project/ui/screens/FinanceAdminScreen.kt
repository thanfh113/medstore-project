package org.example.project.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import org.example.project.data.repositories.FinanceSummaryDto
import org.example.project.presentation.viewmodels.FinanceDashboardViewModel
import org.example.project.presentation.viewmodels.FinanceUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FinanceAdminScreen(viewModel: FinanceDashboardViewModel) {
    val state by viewModel.uiState.collectAsState()
    var retriedAuth by remember { mutableStateOf(false) }
    var exportMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) { viewModel.loadSummary() }

    LaunchedEffect(state) {
        val current = state
        if (current is FinanceUiState.Error && !retriedAuth) {
            retriedAuth = true
            delay(700)
            viewModel.loadSummary()
        } else if (current !is FinanceUiState.Error) {
            retriedAuth = false
        }
    }

    LaunchedEffect(exportMessage) {
        if (exportMessage != null) {
            delay(3500)
            exportMessage = null
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Báo cáo tài chính", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        snackbarHost = {
            exportMessage?.let { msg ->
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
                    Snackbar(modifier = Modifier.padding(16.dp)) { Text(msg) }
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ── Action bar ──────────────────────────────────────────────
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = { viewModel.loadSummary() },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Tải lại")
                }
                Button(
                    onClick = {
                        val data = (state as? FinanceUiState.Success)?.data ?: return@Button
                        val csv = buildFinanceCsv(data)
                        val dialog = java.awt.FileDialog(null as java.awt.Frame?, "Lưu báo cáo CSV", java.awt.FileDialog.SAVE)
                        dialog.file = "bao_cao_tai_chinh.csv"
                        dialog.isVisible = true
                        val dir = dialog.directory
                        val file = dialog.file
                        if (dir != null && file != null) {
                            val fn = if (file.endsWith(".csv")) file else "$file.csv"
                            runCatching {
                                val bomCsv = "﻿$csv"
                                java.io.File(dir, fn).writeText(bomCsv, Charsets.UTF_8)
                            }
                                .onSuccess { exportMessage = "✓ Đã xuất: $fn" }
                                .onFailure { exportMessage = "Xuất thất bại: ${it.message}" }
                        }
                    },
                    enabled = state is FinanceUiState.Success,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Xuất CSV")
                }
            }

            // ── States ──────────────────────────────────────────────────
            when (val ui = state) {
                is FinanceUiState.Loading -> {
                    Box(modifier = Modifier.fillMaxWidth().height(240.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                is FinanceUiState.Error -> {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = ui.message,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
                is FinanceUiState.Success -> {
                    val d = ui.data

                    // ── Revenue ────────────────────────────────────────
                    FinanceSectionLabel("Doanh thu")
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        FinanceCard(
                            title = "Doanh thu gộp",
                            value = formatVND(d.grossRevenue),
                            valueColor = Color(0xFF1B5E20),
                            modifier = Modifier.weight(1f)
                        )
                        FinanceCard(
                            title = "Kênh online",
                            value = formatVND(d.onlineRevenue),
                            valueColor = Color(0xFF1565C0),
                            modifier = Modifier.weight(1f)
                        )
                        FinanceCard(
                            title = "Kênh POS",
                            value = formatVND(d.posRevenue),
                            valueColor = Color(0xFF6A1B9A),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // ── Cost & profit ──────────────────────────────────
                    FinanceSectionLabel("Chi phí & Lợi nhuận")
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        FinanceCard(
                            title = "Chiết khấu",
                            value = formatVND(d.totalDiscount),
                            valueColor = Color(0xFFE65100),
                            modifier = Modifier.weight(1f)
                        )
                        FinanceCard(
                            title = "Chi phí",
                            value = formatVND(d.totalExpenses),
                            valueColor = Color(0xFFB71C1C),
                            modifier = Modifier.weight(1f)
                        )
                        FinanceCard(
                            title = "Lợi nhuận thuần",
                            value = formatVND(d.netProfit),
                            valueColor = if (d.netProfit >= 0) Color(0xFF1B5E20) else Color(0xFFB71C1C),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // ── Order stats ────────────────────────────────────
                    FinanceSectionLabel("Thống kê")
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        FinanceStatCard(
                            label = "Đơn thành công",
                            count = d.successfulOrderCount.toString(),
                            modifier = Modifier.weight(1f)
                        )
                        FinanceStatCard(
                            label = "Phiếu chi",
                            count = d.expenseCount.toString(),
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun FinanceSectionLabel(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        fontWeight = FontWeight.SemiBold
    )
}

@Composable
private fun FinanceCard(title: String, value: String, valueColor: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(text = title, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(text = value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = valueColor)
        }
    }
}

@Composable
private fun FinanceStatCard(label: String, count: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(text = label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(text = count, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        }
    }
}

fun formatVND(value: Double): String = "%,.0f đ".format(value).replace(",", ".")

private fun csvMoney(value: Double): String = value.toLong().toString()

private fun buildFinanceCsv(d: FinanceSummaryDto): String = buildString {
    val now = java.time.LocalDateTime.now()
    val dateStr = "%02d/%02d/%04d %02d:%02d".format(
        now.dayOfMonth, now.monthValue, now.year, now.hour, now.minute
    )

    appendLine("BÁO CÁO TÀI CHÍNH VẬT TƯ Y TẾ")
    appendLine("Ngày xuất,${dateStr}")
    appendLine()

    appendLine("=== DOANH THU ===")
    appendLine("Chỉ số,Giá trị (VNĐ)")
    appendLine("Doanh thu gộp,${csvMoney(d.grossRevenue)}")
    appendLine("Kênh online,${csvMoney(d.onlineRevenue)}")
    appendLine("Kênh POS,${csvMoney(d.posRevenue)}")
    appendLine()

    appendLine("=== CHI PHÍ & CHIẾT KHẤU ===")
    appendLine("Chỉ số,Giá trị (VNĐ)")
    appendLine("Tổng chiết khấu,${csvMoney(d.totalDiscount)}")
    appendLine("Tổng chi phí,${csvMoney(d.totalExpenses)}")
    appendLine()

    appendLine("=== LỢI NHUẬN ===")
    appendLine("Chỉ số,Giá trị (VNĐ)")
    appendLine("Lợi nhuận thuần,${csvMoney(d.netProfit)}")
    appendLine()

    appendLine("=== ĐƠN HÀNG ===")
    appendLine("Chỉ số,Số lượng")
    appendLine("Đơn hàng thành công,${d.successfulOrderCount}")
    appendLine("Phiếu chi,${d.expenseCount}")
}
