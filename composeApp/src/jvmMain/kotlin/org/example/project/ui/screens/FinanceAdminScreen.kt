package org.example.project.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
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
import org.example.project.data.repositories.FinanceCategoryReportDto
import org.example.project.data.repositories.FinanceProductReportDto
import org.example.project.data.repositories.FinanceSummaryDto
import org.example.project.data.repositories.FinanceTimeBreakdownDto
import org.example.project.data.repositories.TopProductDto
import org.example.project.presentation.viewmodels.FinanceDashboardViewModel
import org.example.project.presentation.viewmodels.FinancePeriod
import org.example.project.presentation.viewmodels.FinanceUiState

private enum class FinanceReportType(val label: String) {
    TIME("Theo thời gian"),
    CATEGORY("Theo loại sản phẩm"),
    PRODUCT("Từng sản phẩm & tồn kho"),
    TOP("Top mặt hàng bán chạy")
}

private enum class SortDirection(val arrow: String) {
    ASC("↑"),
    DESC("↓");

    fun toggled(): SortDirection = if (this == ASC) DESC else ASC
}

private data class SortState<T>(val key: T, val direction: SortDirection)

private enum class TimeReportSort { PERIOD, ORDER_COUNT, SUCCESSFUL_ORDER_COUNT, QUANTITY_SOLD, GROSS_REVENUE, NET_PROFIT }
private enum class CategoryReportSort { NAME, STOCK, QUANTITY_SOLD, REVENUE, COST, NET_PROFIT }
private enum class ProductReportSort { NAME, CATEGORY, STOCK, QUANTITY_SOLD, REVENUE, NET_PROFIT }
private enum class TopReportSort { NAME, QUANTITY_SOLD, REVENUE }
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FinanceAdminScreen(viewModel: FinanceDashboardViewModel) {
    val state by viewModel.uiState.collectAsState()
    val selectedPeriod by viewModel.selectedPeriod.collectAsState()
    var retriedAuth by remember { mutableStateOf(false) }
    var exportMessage by remember { mutableStateOf<String?>(null) }
    var exportMenuExpanded by remember { mutableStateOf(false) }

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
                        Text(
                            it,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(end = 4.dp),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Box {
                        IconButton(
                            onClick = { exportMenuExpanded = true },
                            enabled = state is FinanceUiState.Success
                        ) {
                            Icon(Icons.Default.Download, contentDescription = "Xuất báo cáo")
                        }
                        DropdownMenu(
                            expanded = exportMenuExpanded,
                            onDismissRequest = { exportMenuExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Mở báo cáo HTML") },
                                onClick = {
                                    exportMenuExpanded = false
                                    val data = (state as? FinanceUiState.Success)?.data
                                    if (data != null) {
                                        val html = buildFinanceHtml(data)
                                        val dialog = java.awt.FileDialog(null as java.awt.Frame?, "Lưu báo cáo HTML", java.awt.FileDialog.SAVE)
                                        dialog.file = "bao_cao_tai_chinh_${selectedPeriod.key.lowercase()}.html"
                                        dialog.isVisible = true
                                        val dir = dialog.directory; val file = dialog.file
                                        if (dir != null && file != null) {
                                            val fn = if (file.endsWith(".html")) file else "$file.html"
                                            runCatching { java.io.File(dir, fn).also { it.writeText(html, Charsets.UTF_8) } }
                                                .onSuccess { f -> exportMessage = "Đã xuất: $fn"; runCatching { java.awt.Desktop.getDesktop().browse(f.toURI()) } }
                                                .onFailure { exportMessage = "Xuất báo cáo thất bại" }
                                        }
                                    }
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Xuất Excel (.xlsx)") },
                                onClick = {
                                    exportMenuExpanded = false
                                    val data = (state as? FinanceUiState.Success)?.data
                                    if (data != null) {
                                        val bytes = runCatching { buildFinanceXlsx(data) }
                                            .onFailure { exportMessage = "Lỗi tạo Excel" }
                                            .getOrNull()
                                        if (bytes != null) {
                                            val dialog = java.awt.FileDialog(null as java.awt.Frame?, "Lưu báo cáo Excel", java.awt.FileDialog.SAVE)
                                            dialog.file = "bao_cao_tai_chinh_${selectedPeriod.key.lowercase()}.xlsx"
                                            dialog.isVisible = true
                                            val dir = dialog.directory; val file = dialog.file
                                            if (dir != null && file != null) {
                                                val fn = if (file.endsWith(".xlsx")) file else "$file.xlsx"
                                                runCatching { java.io.File(dir, fn).also { it.writeBytes(bytes) } }
                                                    .onSuccess { f -> exportMessage = "Đã xuất: $fn"; runCatching { java.awt.Desktop.getDesktop().open(f) } }
                                                    .onFailure { exportMessage = "Xuất Excel thất bại" }
                                            }
                                        }
                                    }
                                }
                            )
                        }
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
                FinancePeriod.entries.forEach { period ->
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
                                text = "Không tải được dữ liệu tài chính. Vui lòng kiểm tra kết nối hệ thống.",
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    }
                    is FinanceUiState.Success -> FinanceReportContent(ui.data)
                }
            }
        }
    }
}

@Composable
private fun FinanceReportContent(d: FinanceSummaryDto) {
    var selectedReport by remember { mutableStateOf(FinanceReportType.TIME) }
    var timeSort by remember { mutableStateOf(SortState(TimeReportSort.PERIOD, SortDirection.ASC)) }
    var categorySort by remember { mutableStateOf(SortState(CategoryReportSort.REVENUE, SortDirection.DESC)) }
    var productSort by remember { mutableStateOf(SortState(ProductReportSort.QUANTITY_SOLD, SortDirection.DESC)) }
    var topSort by remember { mutableStateOf(SortState(TopReportSort.QUANTITY_SOLD, SortDirection.DESC)) }
    FinanceSectionLabel("Kỳ báo cáo: ${d.periodLabel}")

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
        FinanceCard("Hoàn tiền", formatVND(d.totalRefunds), Color(0xFF6A1B9A), Modifier.weight(1f))
        FinanceCard(
            "Lợi nhuận thuần",
            formatVND(d.netProfit),
            if (d.netProfit >= 0) Color(0xFF1B5E20) else Color(0xFFB71C1C),
            Modifier.weight(1f)
        )
    }

    FinanceSectionLabel("Thống kê đơn hàng")
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        FinanceStatCard("Tổng đơn hàng", d.totalOrderCount.toString(), Modifier.weight(1f))
        FinanceStatCard("Đơn thành công", d.successfulOrderCount.toString(), Modifier.weight(1f))
        FinanceStatCard("Đơn hủy", d.cancelledOrderCount.toString(), Modifier.weight(1f))
        FinanceStatCard("Đã hoàn tiền", d.refundedOrderCount.toString(), Modifier.weight(1f))
    }
    if (d.returnedOrderCount > 0) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            FinanceStatCard("Đơn trả hàng", d.returnedOrderCount.toString(), Modifier.weight(1f))
            Spacer(Modifier.weight(3f))
        }
    }

    ReportTypeSelector(selectedReport = selectedReport, onSelect = { selectedReport = it })

    when (selectedReport) {
        FinanceReportType.TIME -> TimeBreakdownTable(
            items = sortedTimeBreakdown(d.timeBreakdown, timeSort),
            sort = timeSort,
            onSort = { key, defaultDirection -> timeSort = nextSort(timeSort, key, defaultDirection) }
        )
        FinanceReportType.CATEGORY -> CategoryReportTable(
            items = sortedCategoryReports(d.categoryReports, categorySort),
            sort = categorySort,
            onSort = { key, defaultDirection -> categorySort = nextSort(categorySort, key, defaultDirection) }
        )
        FinanceReportType.PRODUCT -> ProductReportTable(
            items = sortedProductReports(d.productReports, productSort),
            sort = productSort,
            onSort = { key, defaultDirection -> productSort = nextSort(productSort, key, defaultDirection) }
        )
        FinanceReportType.TOP -> TopSellingProductsTable(
            products = sortedTopProducts(d.topSellingProducts, topSort),
            sort = topSort,
            onSort = { key, defaultDirection -> topSort = nextSort(topSort, key, defaultDirection) }
        )
    }
}

@Composable
private fun ReportTypeSelector(selectedReport: FinanceReportType, onSelect: (FinanceReportType) -> Unit) {
    var expanded by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        FinanceSectionLabel("Loại báo cáo")
        Box {
            OutlinedButton(onClick = { expanded = true }) {
                Text(selectedReport.label)
                Spacer(Modifier.width(8.dp))
                Icon(Icons.Default.ArrowDropDown, contentDescription = null)
            }
            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                FinanceReportType.entries.forEach { reportType ->
                    DropdownMenuItem(
                        text = { Text(reportType.label) },
                        onClick = {
                            onSelect(reportType)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}
@Composable
private fun TimeBreakdownTable(
    items: List<FinanceTimeBreakdownDto>,
    sort: SortState<TimeReportSort>,
    onSort: (TimeReportSort, SortDirection) -> Unit
) {
    FinanceTableCard(title = "Báo cáo theo thời gian") {
        FinanceTableHeader {
            FinanceSortHeader("Thời gian", Modifier.weight(1.2f), sort.key == TimeReportSort.PERIOD, sort.direction) {
                onSort(TimeReportSort.PERIOD, SortDirection.ASC)
            }
            FinanceSortHeader("Tổng đơn", Modifier.width(72.dp), sort.key == TimeReportSort.ORDER_COUNT, sort.direction) {
                onSort(TimeReportSort.ORDER_COUNT, SortDirection.DESC)
            }
            FinanceSortHeader("Đơn đạt", Modifier.width(72.dp), sort.key == TimeReportSort.SUCCESSFUL_ORDER_COUNT, sort.direction) {
                onSort(TimeReportSort.SUCCESSFUL_ORDER_COUNT, SortDirection.DESC)
            }
            FinanceSortHeader("SL bán", Modifier.width(72.dp), sort.key == TimeReportSort.QUANTITY_SOLD, sort.direction) {
                onSort(TimeReportSort.QUANTITY_SOLD, SortDirection.DESC)
            }
            FinanceSortHeader("Doanh thu", Modifier.width(118.dp), sort.key == TimeReportSort.GROSS_REVENUE, sort.direction) {
                onSort(TimeReportSort.GROSS_REVENUE, SortDirection.DESC)
            }
            FinanceSortHeader("Lãi gộp", Modifier.width(110.dp), sort.key == TimeReportSort.NET_PROFIT, sort.direction) {
                onSort(TimeReportSort.NET_PROFIT, SortDirection.DESC)
            }
        }
        if (items.isEmpty()) {
            EmptyReportRow("Chưa có dữ liệu theo thời gian")
        } else {
            items.forEachIndexed { index, item ->
                FinanceTableRow(index) {
                    FinanceTextCell(item.label, Modifier.weight(1.2f))
                    FinanceTextCell(item.orderCount.toString(), Modifier.width(72.dp))
                    FinanceTextCell(item.successfulOrderCount.toString(), Modifier.width(72.dp))
                    FinanceTextCell(item.quantitySold.toString(), Modifier.width(72.dp), color = MaterialTheme.colorScheme.primary)
                    FinanceTextCell(formatVND(item.grossRevenue), Modifier.width(118.dp), color = Color(0xFF1B5E20))
                    FinanceTextCell(formatVND(item.netProfit), Modifier.width(110.dp), color = profitColor(item.netProfit))
                }
            }
        }
    }
}
@Composable
private fun CategoryReportTable(
    items: List<FinanceCategoryReportDto>,
    sort: SortState<CategoryReportSort>,
    onSort: (CategoryReportSort, SortDirection) -> Unit
) {
    FinanceTableCard(title = "Báo cáo theo loại sản phẩm") {
        FinanceTableHeader {
            FinanceSortHeader("Loại sản phẩm", Modifier.weight(1f), sort.key == CategoryReportSort.NAME, sort.direction) {
                onSort(CategoryReportSort.NAME, SortDirection.ASC)
            }
            FinanceSortHeader("Tồn kho", Modifier.width(76.dp), sort.key == CategoryReportSort.STOCK, sort.direction) {
                onSort(CategoryReportSort.STOCK, SortDirection.DESC)
            }
            FinanceSortHeader("Đã bán", Modifier.width(76.dp), sort.key == CategoryReportSort.QUANTITY_SOLD, sort.direction) {
                onSort(CategoryReportSort.QUANTITY_SOLD, SortDirection.DESC)
            }
            FinanceSortHeader("Doanh thu", Modifier.width(124.dp), sort.key == CategoryReportSort.REVENUE, sort.direction) {
                onSort(CategoryReportSort.REVENUE, SortDirection.DESC)
            }
            FinanceSortHeader("Giá vốn", Modifier.width(116.dp), sort.key == CategoryReportSort.COST, sort.direction) {
                onSort(CategoryReportSort.COST, SortDirection.DESC)
            }
            FinanceSortHeader("Lãi gộp", Modifier.width(116.dp), sort.key == CategoryReportSort.NET_PROFIT, sort.direction) {
                onSort(CategoryReportSort.NET_PROFIT, SortDirection.DESC)
            }
        }
        if (items.isEmpty()) {
            EmptyReportRow("Chưa có dữ liệu loại sản phẩm")
        } else {
            items.forEachIndexed { index, item ->
                FinanceTableRow(index) {
                    FinanceTextCell(item.categoryName, Modifier.weight(1f))
                    FinanceTextCell(item.stockQuantity.toString(), Modifier.width(76.dp))
                    FinanceTextCell(item.quantitySold.toString(), Modifier.width(76.dp), color = MaterialTheme.colorScheme.primary)
                    FinanceTextCell(formatVND(item.revenue), Modifier.width(124.dp), color = Color(0xFF1B5E20))
                    FinanceTextCell(formatVND(item.cost), Modifier.width(116.dp))
                    FinanceTextCell(formatVND(item.netProfit), Modifier.width(116.dp), color = profitColor(item.netProfit))
                }
            }
        }
    }
}
@Composable
private fun ProductReportTable(
    items: List<FinanceProductReportDto>,
    sort: SortState<ProductReportSort>,
    onSort: (ProductReportSort, SortDirection) -> Unit
) {
    FinanceTableCard(title = "Báo cáo từng sản phẩm & tồn kho") {
        FinanceTableHeader {
            FinanceSortHeader("Sản phẩm", Modifier.weight(1.3f), sort.key == ProductReportSort.NAME, sort.direction) {
                onSort(ProductReportSort.NAME, SortDirection.ASC)
            }
            FinanceSortHeader("Loại", Modifier.weight(0.9f), sort.key == ProductReportSort.CATEGORY, sort.direction) {
                onSort(ProductReportSort.CATEGORY, SortDirection.ASC)
            }
            FinanceSortHeader("Tồn", Modifier.width(58.dp), sort.key == ProductReportSort.STOCK, sort.direction) {
                onSort(ProductReportSort.STOCK, SortDirection.DESC)
            }
            FinanceSortHeader("Bán", Modifier.width(58.dp), sort.key == ProductReportSort.QUANTITY_SOLD, sort.direction) {
                onSort(ProductReportSort.QUANTITY_SOLD, SortDirection.DESC)
            }
            FinanceSortHeader("Doanh thu", Modifier.width(118.dp), sort.key == ProductReportSort.REVENUE, sort.direction) {
                onSort(ProductReportSort.REVENUE, SortDirection.DESC)
            }
            FinanceSortHeader("Lãi gộp", Modifier.width(110.dp), sort.key == ProductReportSort.NET_PROFIT, sort.direction) {
                onSort(ProductReportSort.NET_PROFIT, SortDirection.DESC)
            }
        }
        if (items.isEmpty()) {
            EmptyReportRow("Chưa có dữ liệu từng sản phẩm")
        } else {
            items.forEachIndexed { index, item ->
                FinanceTableRow(index) {
                    FinanceTextCell(item.productName, Modifier.weight(1.3f))
                    FinanceTextCell(item.categoryName, Modifier.weight(0.9f))
                    FinanceTextCell(item.stockQuantity.toString(), Modifier.width(58.dp))
                    FinanceTextCell(item.quantitySold.toString(), Modifier.width(58.dp), color = MaterialTheme.colorScheme.primary)
                    FinanceTextCell(formatVND(item.revenue), Modifier.width(118.dp), color = Color(0xFF1B5E20))
                    FinanceTextCell(formatVND(item.netProfit), Modifier.width(110.dp), color = profitColor(item.netProfit))
                }
            }
        }
    }
}
@Composable
private fun TopSellingProductsTable(
    products: List<TopProductDto>,
    sort: SortState<TopReportSort>,
    onSort: (TopReportSort, SortDirection) -> Unit
) {
    FinanceTableCard(title = "Top mặt hàng bán chạy", leadingIcon = true) {
        FinanceTableHeader {
            FinanceTextCell("#", Modifier.width(34.dp), bold = true)
            FinanceSortHeader("Tên sản phẩm", Modifier.weight(1f), sort.key == TopReportSort.NAME, sort.direction) {
                onSort(TopReportSort.NAME, SortDirection.ASC)
            }
            FinanceSortHeader("Số lượng", Modifier.width(78.dp), sort.key == TopReportSort.QUANTITY_SOLD, sort.direction) {
                onSort(TopReportSort.QUANTITY_SOLD, SortDirection.DESC)
            }
            FinanceSortHeader("Doanh thu", Modifier.width(124.dp), sort.key == TopReportSort.REVENUE, sort.direction) {
                onSort(TopReportSort.REVENUE, SortDirection.DESC)
            }
        }
        if (products.isEmpty()) {
            EmptyReportRow("Chưa có dữ liệu bán chạy")
        } else {
            products.forEachIndexed { index, product ->
                FinanceTableRow(index) {
                    FinanceTextCell("${index + 1}", Modifier.width(34.dp), bold = index < 3)
                    FinanceTextCell(product.productName, Modifier.weight(1f), bold = index < 3)
                    FinanceTextCell(product.quantitySold.toString(), Modifier.width(78.dp), color = MaterialTheme.colorScheme.primary)
                    FinanceTextCell(formatVND(product.revenue), Modifier.width(124.dp), color = Color(0xFF1B5E20))
                }
            }
        }
    }
}
@Composable
private fun FinanceSortHeader(
    text: String,
    modifier: Modifier = Modifier,
    active: Boolean,
    direction: SortDirection,
    onClick: () -> Unit
) {
    Row(
        modifier = modifier
            .clickable(onClick = onClick)
            .padding(end = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = if (active) "$text ${direction.arrow}" else text,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.SemiBold,
            color = if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun EmptyReportRow(message: String) {
    FinanceTableRow(0) {
        FinanceTextCell(
            message,
            Modifier.weight(1f),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
@Composable
private fun FinanceTableCard(
    title: String,
    leadingIcon: Boolean = false,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(0.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (leadingIcon) {
                    Icon(Icons.Default.TrendingUp, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                }
                Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            }
            Spacer(Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
private fun FinanceTableHeader(content: @Composable RowScope.() -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        content = content
    )
}

@Composable
private fun FinanceTableRow(index: Int, content: @Composable RowScope.() -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (index % 2 == 0) MaterialTheme.colorScheme.background else Color.Transparent)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        content = content
    )
    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
}

@Composable
private fun FinanceTextCell(
    text: String,
    modifier: Modifier = Modifier,
    bold: Boolean = false,
    color: Color = MaterialTheme.colorScheme.onSurface
) {
    Text(
        text = text,
        modifier = modifier.padding(end = 8.dp),
        style = MaterialTheme.typography.bodySmall,
        fontWeight = if (bold) FontWeight.SemiBold else FontWeight.Normal,
        color = color,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
    )
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
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(10.dp)
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
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(10.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(text = label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(text = count, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        }
    }
}

private fun profitColor(value: Double): Color = if (value >= 0) Color(0xFF1B5E20) else Color(0xFFB71C1C)

fun formatVND(value: Double): String = "%,.0f đ".format(value).replace(",", ".")

private fun <T> nextSort(current: SortState<T>, key: T, defaultDirection: SortDirection): SortState<T> {
    return if (current.key == key) {
        current.copy(direction = current.direction.toggled())
    } else {
        SortState(key, defaultDirection)
    }
}

private fun <T> applyDirection(items: List<T>, comparator: Comparator<T>, direction: SortDirection): List<T> {
    return if (direction == SortDirection.ASC) items.sortedWith(comparator) else items.sortedWith(comparator.reversed())
}

private fun sortedTimeBreakdown(
    items: List<FinanceTimeBreakdownDto>,
    sort: SortState<TimeReportSort>
): List<FinanceTimeBreakdownDto> {
    val comparator = when (sort.key) {
        TimeReportSort.PERIOD -> compareBy<FinanceTimeBreakdownDto> { it.sortKey }
        TimeReportSort.ORDER_COUNT -> compareBy { it.orderCount }
        TimeReportSort.SUCCESSFUL_ORDER_COUNT -> compareBy { it.successfulOrderCount }
        TimeReportSort.QUANTITY_SOLD -> compareBy { it.quantitySold }
        TimeReportSort.GROSS_REVENUE -> compareBy { it.grossRevenue }
        TimeReportSort.NET_PROFIT -> compareBy { it.netProfit }
    }
    return applyDirection(items, comparator, sort.direction)
}

private fun sortedCategoryReports(
    items: List<FinanceCategoryReportDto>,
    sort: SortState<CategoryReportSort>
): List<FinanceCategoryReportDto> {
    val comparator = when (sort.key) {
        CategoryReportSort.NAME -> compareBy<FinanceCategoryReportDto> { it.categoryName.lowercase() }
        CategoryReportSort.STOCK -> compareBy { it.stockQuantity }
        CategoryReportSort.QUANTITY_SOLD -> compareBy { it.quantitySold }
        CategoryReportSort.REVENUE -> compareBy { it.revenue }
        CategoryReportSort.COST -> compareBy { it.cost }
        CategoryReportSort.NET_PROFIT -> compareBy { it.netProfit }
    }
    return applyDirection(items, comparator, sort.direction)
}

private fun sortedProductReports(
    items: List<FinanceProductReportDto>,
    sort: SortState<ProductReportSort>
): List<FinanceProductReportDto> {
    val comparator = when (sort.key) {
        ProductReportSort.NAME -> compareBy<FinanceProductReportDto> { it.productName.lowercase() }
        ProductReportSort.CATEGORY -> compareBy { it.categoryName.lowercase() }
        ProductReportSort.STOCK -> compareBy { it.stockQuantity }
        ProductReportSort.QUANTITY_SOLD -> compareBy { it.quantitySold }
        ProductReportSort.REVENUE -> compareBy { it.revenue }
        ProductReportSort.NET_PROFIT -> compareBy { it.netProfit }
    }
    return applyDirection(items, comparator, sort.direction)
}

private fun sortedTopProducts(
    items: List<TopProductDto>,
    sort: SortState<TopReportSort>
): List<TopProductDto> {
    val comparator = when (sort.key) {
        TopReportSort.NAME -> compareBy<TopProductDto> { it.productName.lowercase() }
        TopReportSort.QUANTITY_SOLD -> compareBy { it.quantitySold }
        TopReportSort.REVENUE -> compareBy { it.revenue }
    }
    return applyDirection(items, comparator, sort.direction)
}

private fun buildFinanceHtml(d: FinanceSummaryDto): String {
    val now = java.time.LocalDateTime.now()
    val dateStr = "%02d/%02d/%04d %02d:%02d".format(now.dayOfMonth, now.monthValue, now.year, now.hour, now.minute)
    fun h(s: String) = s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
    fun money(v: Double) = "%,.0f đ".format(v).replace(",", ".")
    fun profitCls(v: Double) = if (v >= 0) "pos" else "neg"

    return buildString {
        append("""<!DOCTYPE html>
<html lang="vi">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width,initial-scale=1">
<title>Báo cáo tài chính — ${h(d.periodLabel)}</title>
<style>
*{box-sizing:border-box;margin:0;padding:0}
body{font-family:'Segoe UI',system-ui,sans-serif;background:#f0f4f8;color:#1a202c;-webkit-print-color-adjust:exact;print-color-adjust:exact}
.wrap{max-width:1100px;margin:0 auto;padding:32px 24px}
.header{background:linear-gradient(135deg,#155d30 0%,#1f8c4b 60%,#27ae60 100%);color:#fff;border-radius:16px;padding:28px 36px;margin-bottom:28px;display:flex;justify-content:space-between;align-items:flex-start;gap:16px}
.header h1{font-size:22px;font-weight:700;letter-spacing:-.3px;margin-bottom:4px}
.header p{font-size:12.5px;opacity:.8}
.badges{display:flex;flex-direction:column;align-items:flex-end;gap:6px;flex-shrink:0}
.badge{background:rgba(255,255,255,.18);border:1px solid rgba(255,255,255,.28);border-radius:20px;padding:4px 13px;font-size:11.5px;font-weight:500;white-space:nowrap}
.sec{font-size:10.5px;font-weight:700;color:#718096;text-transform:uppercase;letter-spacing:.8px;margin:22px 0 9px}
.cards{display:grid;grid-template-columns:repeat(4,1fr);gap:11px}
.card{background:#fff;border-radius:12px;padding:16px 18px;box-shadow:0 1px 4px rgba(0,0,0,.07);border-top:3px solid}
.card-label{font-size:11px;color:#718096;margin-bottom:7px;font-weight:500}
.card-value{font-size:16px;font-weight:700;line-height:1.2}
.stat-cards{display:grid;grid-template-columns:repeat(4,1fr);gap:11px}
.stat-card{background:#fff;border-radius:12px;padding:16px;box-shadow:0 1px 4px rgba(0,0,0,.07);text-align:center}
.stat-n{font-size:30px;font-weight:800;line-height:1;margin-bottom:5px}
.stat-l{font-size:11px;color:#718096;font-weight:500}
.tcard{background:#fff;border-radius:12px;box-shadow:0 1px 4px rgba(0,0,0,.07);padding:20px 22px;margin-top:12px;overflow:hidden}
.tcard h3{font-size:13.5px;font-weight:600;color:#2d3748;margin-bottom:14px;padding-bottom:10px;border-bottom:2px solid #f0f4f8;display:flex;align-items:center;gap:8px}
.dot{width:9px;height:9px;border-radius:50%;display:inline-block;flex-shrink:0}
table{width:100%;border-collapse:collapse;font-size:12.5px}
thead th{padding:8px 10px;text-align:left;font-size:10.5px;font-weight:700;color:#4a5568;background:#f7fafc;border-bottom:2px solid #e2e8f0;white-space:nowrap}
tbody td{padding:9px 10px;border-bottom:1px solid #f0f4f8;vertical-align:middle}
tbody tr:last-child td{border-bottom:none}
tbody tr:hover td{background:#fafbfc}
.r{text-align:right;font-variant-numeric:tabular-nums}
.c{text-align:center}
.b{font-weight:600}
.pos{color:#276749;font-weight:600}
.neg{color:#c53030;font-weight:600}
.pri{color:#2b6cb0;font-weight:600}
.muted{color:#718096;font-size:11.5px}
.rank{width:36px;text-align:center;font-weight:700;font-size:14px}
.g1{color:#d69e2e}.g2{color:#a0aec0}.g3{color:#c05621}
.footer{text-align:center;font-size:11px;color:#a0aec0;margin-top:28px;padding-top:14px;border-top:1px solid #e2e8f0}
@media print{body{background:#fff}.wrap{padding:8px}}
</style>
</head>
<body>
<div class="wrap">
<div class="header">
  <div>
    <h1>Báo cáo Tài chính</h1>
    <p>Nhà thuốc Medstore</p>
  </div>
  <div class="badges">
    <span class="badge">📅 ${h(d.periodLabel)}</span>
    <span class="badge">⏱ $dateStr</span>
  </div>
</div>
""")

        append("""<p class="sec">Doanh thu</p>
<div class="cards">
  <div class="card" style="border-top-color:#276749"><div class="card-label">Doanh thu gộp</div><div class="card-value" style="color:#276749">${money(d.grossRevenue)}</div></div>
  <div class="card" style="border-top-color:#2b6cb0"><div class="card-label">Kênh online</div><div class="card-value" style="color:#2b6cb0">${money(d.onlineRevenue)}</div></div>
  <div class="card" style="border-top-color:#6b46c1"><div class="card-label">Kênh POS</div><div class="card-value" style="color:#6b46c1">${money(d.posRevenue)}</div></div>
  <div class="card" style="border-top-color:#285e61"><div class="card-label">Giá trị TB/đơn</div><div class="card-value" style="color:#285e61">${money(d.averageOrderValue)}</div></div>
</div>
<p class="sec">Chi phí &amp; Lợi nhuận</p>
<div class="cards">
  <div class="card" style="border-top-color:#c05621"><div class="card-label">Chiết khấu</div><div class="card-value" style="color:#c05621">${money(d.totalDiscount)}</div></div>
  <div class="card" style="border-top-color:#c53030"><div class="card-label">Chi phí vốn</div><div class="card-value" style="color:#c53030">${money(d.totalExpenses)}</div></div>
  <div class="card" style="border-top-color:#c53030"><div class="card-label">Hoàn tiền</div><div class="card-value" style="color:#c53030">${money(d.totalRefunds)}</div></div>
  <div class="card" style="border-top-color:${if (d.netProfit >= 0) "#276749" else "#c53030"}"><div class="card-label">Lợi nhuận thuần</div><div class="card-value ${profitCls(d.netProfit)}">${money(d.netProfit)}</div></div>
</div>
<p class="sec">Thống kê đơn hàng</p>
<div class="stat-cards">
  <div class="stat-card"><div class="stat-n">${d.totalOrderCount}</div><div class="stat-l">Tổng đơn hàng</div></div>
  <div class="stat-card"><div class="stat-n pos">${d.successfulOrderCount}</div><div class="stat-l">Đơn thành công</div></div>
  <div class="stat-card"><div class="stat-n neg">${d.cancelledOrderCount}</div><div class="stat-l">Đơn hủy</div></div>
  <div class="stat-card"><div class="stat-n" style="color:#c05621">${d.refundedOrderCount}</div><div class="stat-l">Hoàn tiền</div></div>
</div>
""")

        if (d.timeBreakdown.isNotEmpty()) {
            append("""<p class="sec">Theo thời gian</p>
<div class="tcard">
<h3><span class="dot" style="background:#276749"></span>Báo cáo theo thời gian</h3>
<table><thead><tr>
  <th>Thời gian</th><th class="c">Tổng đơn</th><th class="c">Đơn đạt</th><th class="c">SL bán</th><th class="r">Doanh thu</th><th class="r">Lãi gộp</th>
</tr></thead><tbody>
""")
            d.timeBreakdown.sortedBy { it.sortKey }.forEach { item ->
                append("""<tr>
  <td class="b">${h(item.label)}</td>
  <td class="c">${item.orderCount}</td>
  <td class="c pri">${item.successfulOrderCount}</td>
  <td class="c b pri">${item.quantitySold}</td>
  <td class="r">${money(item.grossRevenue)}</td>
  <td class="r ${profitCls(item.netProfit)}">${money(item.netProfit)}</td>
</tr>""")
            }
            append("</tbody></table></div>\n")
        }

        if (d.categoryReports.isNotEmpty()) {
            append("""<p class="sec">Theo loại sản phẩm</p>
<div class="tcard">
<h3><span class="dot" style="background:#2b6cb0"></span>Báo cáo theo loại sản phẩm</h3>
<table><thead><tr>
  <th>Loại sản phẩm</th><th class="c">Tồn kho</th><th class="c">Đã bán</th><th class="r">Doanh thu</th><th class="r">Giá vốn</th><th class="r">Lãi gộp</th>
</tr></thead><tbody>
""")
            d.categoryReports.sortedByDescending { it.revenue }.forEach { item ->
                append("""<tr>
  <td class="b">${h(item.categoryName)}</td>
  <td class="c">${item.stockQuantity}</td>
  <td class="c b pri">${item.quantitySold}</td>
  <td class="r">${money(item.revenue)}</td>
  <td class="r">${money(item.cost)}</td>
  <td class="r ${profitCls(item.netProfit)}">${money(item.netProfit)}</td>
</tr>""")
            }
            append("</tbody></table></div>\n")
        }

        if (d.productReports.isNotEmpty()) {
            append("""<p class="sec">Từng sản phẩm &amp; tồn kho</p>
<div class="tcard">
<h3><span class="dot" style="background:#6b46c1"></span>Báo cáo từng sản phẩm</h3>
<table><thead><tr>
  <th>Sản phẩm</th><th>Loại</th><th class="c">Tồn</th><th class="c">Bán</th><th class="r">Doanh thu</th><th class="r">Lãi gộp</th>
</tr></thead><tbody>
""")
            d.productReports.sortedByDescending { it.revenue }.forEach { item ->
                append("""<tr>
  <td>${h(item.productName)}</td>
  <td class="muted">${h(item.categoryName)}</td>
  <td class="c">${item.stockQuantity}</td>
  <td class="c b pri">${item.quantitySold}</td>
  <td class="r">${money(item.revenue)}</td>
  <td class="r ${profitCls(item.netProfit)}">${money(item.netProfit)}</td>
</tr>""")
            }
            append("</tbody></table></div>\n")
        }

        if (d.topSellingProducts.isNotEmpty()) {
            append("""<p class="sec">Top mặt hàng bán chạy</p>
<div class="tcard">
<h3><span class="dot" style="background:#c05621"></span>Top sản phẩm bán chạy</h3>
<table><thead><tr>
  <th class="rank">#</th><th>Tên sản phẩm</th><th class="c">Số lượng</th><th class="r">Doanh thu</th>
</tr></thead><tbody>
""")
            d.topSellingProducts.forEachIndexed { i, p ->
                val rankCls = when (i) { 0 -> "g1"; 1 -> "g2"; 2 -> "g3"; else -> "" }
                val boldCls = if (i < 3) " b" else ""
                append("""<tr>
  <td class="rank $rankCls">${i + 1}</td>
  <td class="$boldCls">${h(p.productName)}</td>
  <td class="c b pri">${p.quantitySold}</td>
  <td class="r">${money(p.revenue)}</td>
</tr>""")
            }
            append("</tbody></table></div>\n")
        }

        append("""<div class="footer">Báo cáo được tạo tự động lúc $dateStr · Medstore Desktop</div>
</div></body></html>""")
    }
}
