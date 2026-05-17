package org.example.project.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import org.example.project.data.repositories.FinanceSummaryDto
import org.example.project.data.repositories.TopProductDto
import org.example.project.presentation.viewmodels.FinanceDashboardViewModel
import org.example.project.presentation.viewmodels.FinancePeriod
import org.example.project.presentation.viewmodels.FinanceUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FinanceAdminScreen(viewModel: FinanceDashboardViewModel) {
    val state by viewModel.uiState.collectAsState()
    val selectedPeriod by viewModel.selectedPeriod.collectAsState()
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
                actions = {
                    exportMessage?.let {
                        Text(it, fontSize = 12.sp, modifier = Modifier.padding(end = 4.dp), maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                    IconButton(
                        onClick = {
                            val data = (state as? FinanceUiState.Success)?.data ?: return@IconButton
                            val csv = buildFinanceCsv(data)
                            val dialog = java.awt.FileDialog(null as java.awt.Frame?, "Lưu báo cáo CSV", java.awt.FileDialog.SAVE)
                            dialog.file = "bao_cao_tai_chinh.csv"
                            dialog.isVisible = true
                            val dir = dialog.directory
                            val file = dialog.file
                            if (dir != null && file != null) {
                                val fn = if (file.endsWith(".csv")) file else "$file.csv"
                                runCatching {
                                    java.io.File(dir, fn).writeText("﻿$csv", Charsets.UTF_8)
                                }
                                    .onSuccess { exportMessage = "✓ Đã xuất: $fn" }
                                    .onFailure { exportMessage = "Xuất thất bại: ${it.message}" }
                            }
                        },
                        enabled = state is FinanceUiState.Success
                    ) {
                        Icon(Icons.Default.Download, contentDescription = "Xuất CSV")
                    }
                    IconButton(
                        onClick = viewModel::loadSummary,
                        enabled = state !is FinanceUiState.Loading
                    ) {
                        if (state is FinanceUiState.Loading) {
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
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            TabRow(
                selectedTabIndex = FinancePeriod.entries.indexOf(selectedPeriod),
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary
            ) {
                FinancePeriod.entries.forEachIndexed { index, period ->
                    Tab(
                        selected = selectedPeriod == period,
                        onClick = { viewModel.selectPeriod(period) },
                        text = { Text(period.label) }
                    )
                }
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
            when (val ui = state) {
                is FinanceUiState.Loading -> {
                    Box(modifier = Modifier.fillMaxWidth().height(300.dp), contentAlignment = Alignment.Center) {
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

                    FinanceSectionLabel("Doanh thu")
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        FinanceCard("Doanh thu gộp", formatVND(d.grossRevenue), Color(0xFF1B5E20), Modifier.weight(1f))
                        FinanceCard("Kênh online", formatVND(d.onlineRevenue), Color(0xFF1565C0), Modifier.weight(1f))
                        FinanceCard("Kênh POS", formatVND(d.posRevenue), Color(0xFF6A1B9A), Modifier.weight(1f))
                        FinanceCard("Giá trị TB/đơn", formatVND(d.averageOrderValue), Color(0xFF00695C), Modifier.weight(1f))
                    }

                    FinanceSectionLabel("Chi phí & Lợi nhuận")
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        FinanceCard("Chiết khấu", formatVND(d.totalDiscount), Color(0xFFE65100), Modifier.weight(1f))
                        FinanceCard("Chi phí vốn", formatVND(d.totalExpenses), Color(0xFFB71C1C), Modifier.weight(1f))
                        FinanceCard(
                            "Lợi nhuận thuần",
                            formatVND(d.netProfit),
                            if (d.netProfit >= 0) Color(0xFF1B5E20) else Color(0xFFB71C1C),
                            Modifier.weight(1f)
                        )
                        Spacer(Modifier.weight(1f))
                    }

                    FinanceSectionLabel("Thống kê đơn hàng")
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        FinanceStatCard("Tổng đơn hàng", d.totalOrderCount.toString(), Modifier.weight(1f))
                        FinanceStatCard("Đơn thành công", d.successfulOrderCount.toString(), Modifier.weight(1f))
                        FinanceStatCard("Đơn hủy", d.cancelledOrderCount.toString(), Modifier.weight(1f))
                        Spacer(Modifier.weight(1f))
                    }

                    if (d.topSellingProducts.isNotEmpty()) {
                        TopSellingProductsTable(d.topSellingProducts)
                    }
                }
            }
        }
    }
}
}

@Composable
private fun TopSellingProductsTable(products: List<TopProductDto>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(0.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Default.TrendingUp, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                Text("Top mặt hàng bán chạy", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            }

            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Text("#", modifier = Modifier.width(28.dp), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("Tên sản phẩm", modifier = Modifier.weight(1f), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("Số lượng", modifier = Modifier.width(70.dp), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("Doanh thu", modifier = Modifier.width(120.dp), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            products.forEachIndexed { index, product ->
                val isEven = index % 2 == 0
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(if (isEven) MaterialTheme.colorScheme.background else Color.Transparent)
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "${index + 1}",
                        modifier = Modifier.width(28.dp),
                        style = MaterialTheme.typography.bodySmall,
                        color = if (index == 0) Color(0xFFFFAB00) else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = if (index < 3) FontWeight.Bold else FontWeight.Normal
                    )
                    Text(
                        product.productName,
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = if (index < 3) FontWeight.SemiBold else FontWeight.Normal,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        "${product.quantitySold}",
                        modifier = Modifier.width(70.dp),
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        formatVND(product.revenue),
                        modifier = Modifier.width(120.dp),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF1B5E20)
                    )
                }
                if (index < products.lastIndex) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
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
    val dateStr = "%02d/%02d/%04d %02d:%02d".format(now.dayOfMonth, now.monthValue, now.year, now.hour, now.minute)

    appendLine("BÁO CÁO TÀI CHÍNH VẬT TƯ Y TẾ")
    appendLine("Ngày xuất,${dateStr}")
    appendLine()

    appendLine("=== DOANH THU ===")
    appendLine("Chỉ số,Giá trị (VNĐ)")
    appendLine("Doanh thu gộp,${csvMoney(d.grossRevenue)}")
    appendLine("Kênh online,${csvMoney(d.onlineRevenue)}")
    appendLine("Kênh POS,${csvMoney(d.posRevenue)}")
    appendLine("Giá trị trung bình/đơn,${csvMoney(d.averageOrderValue)}")
    appendLine()

    appendLine("=== CHI PHÍ & CHIẾT KHẤU ===")
    appendLine("Chỉ số,Giá trị (VNĐ)")
    appendLine("Tổng chiết khấu,${csvMoney(d.totalDiscount)}")
    appendLine("Tổng chi phí vốn,${csvMoney(d.totalExpenses)}")
    appendLine()

    appendLine("=== LỢI NHUẬN ===")
    appendLine("Chỉ số,Giá trị (VNĐ)")
    appendLine("Lợi nhuận thuần,${csvMoney(d.netProfit)}")
    appendLine()

    appendLine("=== ĐƠN HÀNG ===")
    appendLine("Chỉ số,Số lượng")
    appendLine("Tổng đơn hàng,${d.totalOrderCount}")
    appendLine("Đơn thành công,${d.successfulOrderCount}")
    appendLine("Đơn hủy,${d.cancelledOrderCount}")
    appendLine()

    if (d.topSellingProducts.isNotEmpty()) {
        appendLine("=== TOP MẶT HÀNG BÁN CHẠY ===")
        appendLine("STT,Tên sản phẩm,Số lượng bán,Doanh thu (VNĐ)")
        d.topSellingProducts.forEachIndexed { i, p ->
            appendLine("${i + 1},\"${p.productName}\",${p.quantitySold},${csvMoney(p.revenue)}")
        }
    }
}
