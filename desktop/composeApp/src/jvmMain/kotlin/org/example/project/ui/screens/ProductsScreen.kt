package org.example.project.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.ui.window.DialogProperties
import coil3.compose.AsyncImage
import org.example.project.data.models.Product
import org.example.project.data.models.ProductCategory
import org.example.project.data.models.categoryDisplayPath
import org.example.project.data.models.selectedCategoryGroupId
import org.example.project.data.models.topLevelProductCategories
import org.example.project.data.repositories.ProductDeleteRequestDto
import org.example.project.presentation.viewmodels.ProductsViewModel
import org.example.project.presentation.viewmodels.StockReceiptDraft
import org.example.project.ui.components.ProductFormDialog
import org.example.project.ui.components.ProductDetailDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductsScreen(viewModel: ProductsViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val categoriesById = remember(uiState.categories) { uiState.categories.associateBy { it.id } }
    val canCreateProduct = uiState.categories.isNotEmpty() && !uiState.isLoading
    var productPendingDelete by remember { mutableStateOf<Product?>(null) }
    var selectedProductForStockReceipt by remember { mutableStateOf<Product?>(null) }
    var showBulkStockReceiptDialog by remember { mutableStateOf(false) }
    var selectedProductForPreview by remember { mutableStateOf<Product?>(null) }

    LaunchedEffect(Unit) {
        viewModel.refreshData()
    }

    if (uiState.showCreateDialog) {
        ProductFormDialog(
            product = uiState.selectedProduct,
            categories = uiState.categories,
            onSave = { product, newImageFiles ->
                if (uiState.selectedProduct == null) {
                    viewModel.createProduct(product, newImageFiles)
                } else {
                    viewModel.updateProduct(product, newImageFiles)
                }
            },
            onCancel = { viewModel.hideCreateDialog() },
            onUploadCertificate = { file -> viewModel.uploadCertificate(file) },
            onGenerateSku = { categoryId -> viewModel.generateSkuForCategory(categoryId) },
            isCreating = uiState.isCreating,
            isUpdating = uiState.isUpdating
        )
    }

    // Product-level stock receipt dialog. Stock is stored directly on products.
    if (selectedProductForStockReceipt != null) {
        StockReceiptDialog(
            product = selectedProductForStockReceipt!!,
            isLoading = uiState.isUpdating,
            onSave = { mfgDate, expDate, quantity, importPrice ->
                viewModel.addStockReceipt(
                    productId = selectedProductForStockReceipt!!.id,
                    mfgDate = mfgDate,
                    expDate = expDate,
                    quantity = quantity,
                    importPrice = importPrice
                )
                selectedProductForStockReceipt = null
            },
            onCancel = { selectedProductForStockReceipt = null }
        )
    }


    // Close dialog only after successful submission
    LaunchedEffect(uiState.successMessage) {
        if (uiState.successMessage != null && showBulkStockReceiptDialog) {
            showBulkStockReceiptDialog = false
        }
    }

    if (showBulkStockReceiptDialog) {
        BulkStockReceiptDialog(
            products = uiState.products,
            isLoading = uiState.isUpdating,
            serverError = uiState.error,
            onSave = { receipts -> viewModel.addStockReceipts(receipts) },
            onCancel = {
                viewModel.clearError()
                showBulkStockReceiptDialog = false
            }
        )
    }
    selectedProductForPreview?.let { product ->
        ProductDetailDialog(
            product = product,
            categoryName = categoriesById[product.categoryId]?.displayName,
            onDismiss = { selectedProductForPreview = null },
            formatVnd = ::formatVND
        )
    }

    productPendingDelete?.let { product ->
        val isAdmin = uiState.userRole == "ADMIN"
        AlertDialog(
            onDismissRequest = { if (uiState.isDeleting == null) productPendingDelete = null },
            title = { Text(if (isAdmin) "Xác nhận xóa sản phẩm" else "Gửi yêu cầu xóa") },
            text = {
                Text(
                    if (isAdmin) {
                        "Sản phẩm \"${product.name}\" sẽ bị xóa khỏi DB. Hành động này không hoàn tác."
                    } else {
                        "Yêu cầu xóa sản phẩm \"${product.name}\" sẽ được gửi đến ADMIN để phê duyệt."
                    }
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteProduct(product.id)
                        productPendingDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    enabled = uiState.isDeleting == null
                ) {
                    Text(if (isAdmin) "Xóa" else "Gửi yêu cầu")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { productPendingDelete = null },
                    enabled = uiState.isDeleting == null
                ) {
                    Text("Hủy")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Quản lý sản phẩm", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = viewModel::refreshData) {
                        Icon(Icons.Default.Refresh, contentDescription = "Tải lại")
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
                .padding(24.dp)
        ) {
            if (uiState.error != null && !showBulkStockReceiptDialog) {
                MessageBanner(
                    message = uiState.error!!,
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer,
                    onDismiss = { viewModel.clearError() }
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            if (uiState.successMessage != null) {
                MessageBanner(
                    message = uiState.successMessage!!,
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    onDismiss = { viewModel.clearSuccessMessage() }
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            SearchAndActionsRow(
                searchQuery = uiState.searchQuery,
                canCreateProduct = canCreateProduct,
                onSearchChange = viewModel::searchProducts,
                onCreateClick = { viewModel.showCreateDialog() },
                onBulkStockReceiptClick = {
                    viewModel.clearSuccessMessage()
                    viewModel.clearError()
                    showBulkStockReceiptDialog = true
                },
                canReceiveStock = uiState.products.isNotEmpty() && !uiState.isLoading
            )

            Spacer(modifier = Modifier.height(16.dp))

            FilterRow(
                categories = uiState.categories,
                selectedCategory = uiState.selectedCategory,
                onCategorySelected = viewModel::filterByCategory
            )

            Spacer(modifier = Modifier.height(16.dp))

            SummaryRow(
                totalProducts = uiState.products.size,
                filteredProducts = uiState.filteredProducts.size,
                categoriesLoaded = uiState.categories.size,
                canCreateProduct = canCreateProduct
            )

            if (uiState.userRole == "ADMIN" &&
                (uiState.isLoadingDeleteRequests || uiState.pendingDeleteRequests.isNotEmpty())
            ) {
                Spacer(modifier = Modifier.height(16.dp))
                DeleteRequestPanel(
                    requests = uiState.pendingDeleteRequests,
                    isLoading = uiState.isLoadingDeleteRequests,
                    reviewingId = uiState.reviewingDeleteRequestId,
                    onRefresh = viewModel::loadDeleteRequests,
                    onApprove = { viewModel.reviewDeleteRequest(it, true) },
                    onReject = { viewModel.reviewDeleteRequest(it, false) }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    TableHeader()

                    when {
                        uiState.isLoading -> {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator()
                            }
                        }

                        uiState.filteredProducts.isEmpty() -> {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f)
                            ) {
                                EmptyProductsState(
                                    hasCategories = uiState.categories.isNotEmpty(),
                                    hasSearchQuery = uiState.searchQuery.isNotBlank() || uiState.selectedCategory != null,
                                    onRetry = viewModel::refreshData
                                )
                            }
                        }

                        else -> {
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f)
                            ) {
                                itemsIndexed(uiState.filteredProducts) { index, product ->
                                    ProductRow(
                                        product = product,
                                        categoryName = categoryDisplayPath(uiState.categories, product.categoryId).ifBlank { product.categoryId },
                                        isEven = index % 2 == 0,
                                        isDeleting = uiState.isDeleting == product.id,
                                        onPreview = { selectedProductForPreview = product },
                                        onEdit = { viewModel.selectProduct(product) },
                                        onDelete = { productPendingDelete = product },
                                        onAddStockReceipt = { selectedProductForStockReceipt = product }
                                    )
                                    if (index < uiState.filteredProducts.lastIndex) {
                                        HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DeleteRequestPanel(
    requests: List<ProductDeleteRequestDto>,
    isLoading: Boolean,
    reviewingId: String?,
    onRefresh: () -> Unit,
    onApprove: (String) -> Unit,
    onReject: (String) -> Unit
) {
    val compactContainer = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = compactContainer),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Yêu cầu xóa chờ duyệt", fontWeight = FontWeight.Bold)
                TextButton(onClick = onRefresh, enabled = !isLoading) { Text("Tải lại") }
            }
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.width(20.dp))
            } else if (requests.isEmpty()) {
                Text(
                    "Không có yêu cầu đang chờ. Danh sách sản phẩm bên dưới là danh sách chính để quản lý và xem chi tiết.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 13.sp
                )
            } else {
                requests.take(5).forEach { request ->
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(request.productName, fontWeight = FontWeight.SemiBold)
                            Text(request.reason ?: "Không có lý do", style = MaterialTheme.typography.bodySmall)
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            TextButton(onClick = { onReject(request.id) }, enabled = reviewingId != request.id) { Text("Từ chối") }
                            Button(onClick = { onApprove(request.id) }, enabled = reviewingId != request.id) { Text("Duyệt") }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchAndActionsRow(
    searchQuery: String,
    canCreateProduct: Boolean,
    onSearchChange: (String) -> Unit,
    onCreateClick: () -> Unit,
    onBulkStockReceiptClick: () -> Unit,
    canReceiveStock: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchChange,
            placeholder = { Text("Tìm theo tên, mô tả, hãng, xuất xứ, SKU, số lưu hành") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            modifier = Modifier.weight(1f),
            singleLine = true,
            shape = RoundedCornerShape(8.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                focusedContainerColor = MaterialTheme.colorScheme.surface
            )
        )

        OutlinedButton(
            onClick = onBulkStockReceiptClick,
            enabled = canReceiveStock,
            shape = RoundedCornerShape(8.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(4.dp))
            Text("Nhập theo phiếu", fontWeight = FontWeight.Bold, fontSize = 11.sp)
        }
        Button(
            onClick = onCreateClick,
            enabled = canCreateProduct,
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
            shape = RoundedCornerShape(8.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(4.dp))
            Text("Thêm sản phẩm", fontWeight = FontWeight.Bold, fontSize = 11.sp)
        }
    }
}

@Composable
private fun FilterRow(
    categories: List<ProductCategory>,
    selectedCategory: String?,
    onCategorySelected: (String?) -> Unit
) {
    val topLevelCategories = remember(categories) { topLevelProductCategories(categories) }
    val selectedGroupId = remember(categories, selectedCategory) {
        selectedCategoryGroupId(categories, selectedCategory) ?: selectedCategory
    }
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        FilterDropdown(
            label = "Danh mục",
            value = topLevelCategories.firstOrNull { it.id == selectedGroupId }?.displayName ?: "Tất cả nhóm danh mục",
            options = topLevelCategories.map { it.displayName to it.id },
            onClear = { onCategorySelected(null) },
            onSelect = { onCategorySelected(it) },
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun SummaryRow(
    totalProducts: Int,
    filteredProducts: Int,
    categoriesLoaded: Int,
    canCreateProduct: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Đang hiển thị $filteredProducts / $totalProducts sản phẩm, $categoriesLoaded danh mục con/nhóm",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        if (!canCreateProduct) {
            Text(
                text = "Cần tải được danh mục trước khi tạo sản phẩm",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}

@Composable
private fun TableHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.horizontalGradient(
                    colors = listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.secondary)
                )
            )
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("", color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.width(70.dp), textAlign = TextAlign.Center)
        Text("Sản phẩm", color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.weight(3.3f))
        Text("Danh mục / Đơn vị", color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.weight(2.1f))
        Text("Giá / kho", color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1.6f))
        Text("Trạng thái", color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1.5f))
        Text(
            "Tác vụ",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun EmptyProductsState(
    hasCategories: Boolean,
    hasSearchQuery: Boolean,
    onRetry: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = when {
                    !hasCategories -> "Chưa tải được danh mục sản phẩm"
                    hasSearchQuery -> "Không tìm thấy sản phẩm phù hợp bộ lọc hiện tại"
                    else -> "Chưa có sản phẩm nào"
                },
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = if (hasSearchQuery) {
                    "Thử đổi từ khóa hoặc danh mục."
                } else {
                    "Tải lại dữ liệu hoặc tạo sản phẩm mới khi danh mục đã sẵn sàng."
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onRetry) {
                Text("Tải lại")
            }
        }
    }
}

@Composable
private fun MessageBanner(
    message: String,
    containerColor: Color,
    contentColor: Color,
    onDismiss: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = containerColor),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = message, color = contentColor, modifier = Modifier.weight(1f))
            TextButton(onClick = onDismiss) {
                Text("Đóng")
            }
        }
    }
}

@Composable
private fun ProductRow(
    product: Product,
    categoryName: String,
    isEven: Boolean,
    isDeleting: Boolean,
    onPreview: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onAddStockReceipt: () -> Unit
) {
    val bgColor = if (isEven) Color.Transparent else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
    val primaryImage = product.images.firstOrNull()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(bgColor)
            .clickable(onClick = onPreview)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Image Thumbnail
        Box(
            modifier = Modifier
                .size(58.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            if (primaryImage != null) {
                AsyncImage(
                    model = primaryImage.url,
                    contentDescription = product.name,
                    modifier = Modifier
                        .size(58.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
            } else {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "No image",
                    modifier = Modifier.size(32.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                )
            }
        }

        Column(
            modifier = Modifier
                .weight(3f)
                .padding(end = 12.dp)
        ) {
            Text(text = product.name, fontWeight = FontWeight.SemiBold, maxLines = 2, overflow = TextOverflow.Ellipsis)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = buildString {
                    append("Mã: ${product.id.take(8).uppercase()}")
                    product.sku?.takeIf { it.isNotBlank() }?.let { append(" • SKU: $it") }
                },
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (product.manufacturer.isNotBlank()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Hãng: ${product.manufacturer}",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Text(
                text = buildString {
                    append("Tồn kho: ${product.stockQuantity}")
                    if (product.origin.isNotBlank()) append(" • ${product.origin}")
                    append(" • ${product.unit}")
                },
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Column(
            modifier = Modifier
                .weight(1.7f)
                .padding(end = 12.dp)
        ) {
            Text(text = categoryName, maxLines = 2, overflow = TextOverflow.Ellipsis)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = product.riskClassification.displayName,
                color = MaterialTheme.colorScheme.primary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
        }

        Column(
            modifier = Modifier
                .weight(1.4f)
                .padding(end = 12.dp)
        ) {
            if (product.contactForPrice) {
                Text(
                    text = "Liên hệ",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.secondary
                )
            } else {
                Text(
                    text = formatVND(product.price),
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                product.originalPrice?.let {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Giá gốc: ${formatVND(it)}",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        Column(
            modifier = Modifier
                .weight(1.5f)
                .padding(end = 12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            val statusText = when {
                !product.isActive -> "Tạm dừng"
                product.stockQuantity <= 0 -> "Hết hàng"
                else -> "Đang bán"
            }
            val statusContainerColor = when {
                !product.isActive -> Color(0xFFFFEBEE)
                product.stockQuantity <= 0 -> Color(0xFFFFF3E0)
                else -> Color(0xFFE8F5E9)
            }
            val statusContentColor = when {
                !product.isActive -> Color(0xFFD32F2F)
                product.stockQuantity <= 0 -> Color(0xFFE65100)
                else -> Color(0xFF2E7D32)
            }
            StatusChip(
                text = statusText,
                containerColor = statusContainerColor,
                contentColor = statusContentColor
            )
        }

        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Visibility,
                contentDescription = "Chi tiết",
                tint = if (isDeleting) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .clickable(enabled = !isDeleting, onClick = onPreview)
                    .padding(4.dp)
            )
            Icon(
                Icons.Default.Edit,
                contentDescription = "Sửa",
                tint = if (isDeleting) MaterialTheme.colorScheme.outline else Color(0xFF2196F3),
                modifier = Modifier
                    .clickable(enabled = !isDeleting, onClick = onEdit)
                    .padding(4.dp)
            )
            Icon(
                Icons.Default.Add,
                contentDescription = "Nhập hàng",
                tint = if (isDeleting) MaterialTheme.colorScheme.outline else Color(0xFF4CAF50),
                modifier = Modifier
                    .clickable(enabled = !isDeleting, onClick = onAddStockReceipt)
                    .padding(4.dp)
            )
            if (isDeleting) {
                CircularProgressIndicator(modifier = Modifier.width(18.dp), strokeWidth = 2.dp)
            } else {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Xóa",
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier
                        .clickable(onClick = onDelete)
                        .padding(4.dp)
                )
            }
        }
    }
}

@Composable
private fun StatusChip(
    text: String,
    containerColor: Color,
    contentColor: Color
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = containerColor
    ) {
        Text(
            text = text,
            color = contentColor,
            fontSize = 12.sp,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun FilterDropdown(
    label: String,
    value: String,
    options: List<Pair<String, String>>,
    onClear: () -> Unit,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        OutlinedButton(
            onClick = { expanded = true },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(value, color = MaterialTheme.colorScheme.onSurface)
                }
                Text("Chọn", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
            }
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.heightIn(max = 320.dp)
        ) {
            DropdownMenuItem(
                text = { Text("Tất cả") },
                onClick = {
                    onClear()
                    expanded = false
                }
            )
            options.forEach { (displayName, id) ->
                DropdownMenuItem(
                    text = { Text(displayName) },
                    onClick = {
                        onSelect(id)
                        expanded = false
                    }
                )
            }
        }
    }
}


// ── Bulk Stock Receipt (Nhập hàng theo phiếu) ──────────────────────────────

private data class BulkStockReceiptLine(
    val id: Int,
    val productId: String = "",
    val quantity: String = "",
    val importPrice: String = "",
    val mfgDate: String = "",
    val expDate: String = ""
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BulkStockReceiptDialog(
    products: List<Product>,
    isLoading: Boolean,
    serverError: String? = null,
    onSave: (List<StockReceiptDraft>) -> Unit,
    onCancel: () -> Unit
) {
    var invoiceCode by remember { mutableStateOf("") }
    var rows by remember { mutableStateOf(listOf(BulkStockReceiptLine(id = 1))) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val displayError = serverError ?: errorMessage
    val productsById = remember(products) { products.associateBy { it.id } }

    val duplicateProductIds = remember(rows) {
        rows.filter { it.productId.isNotBlank() }
            .groupBy { it.productId }
            .filter { it.value.size > 1 }
            .keys
    }
    val estimatedTotal = remember(rows) {
        rows.sumOf { l -> (l.quantity.toIntOrNull() ?: 0) * (l.importPrice.toDoubleOrNull() ?: 0.0) }
    }

    BasicAlertDialog(
        onDismissRequest = { if (!isLoading) onCancel() },
        modifier = Modifier.width(860.dp),
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column {
                // ── Header ───────────────────────────────────────────────────────
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f))
                        .padding(start = 24.dp, end = 12.dp, top = 16.dp, bottom = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        Text(
                            "Nhập hàng theo phiếu",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            "Tồn kho được cộng trực tiếp — không tạo bảng dữ liệu riêng",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = onCancel, enabled = !isLoading) {
                        Icon(Icons.Default.Close, contentDescription = "Đóng", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                // ── Body (scrollable) ────────────────────────────────────────────
                Column(
                    modifier = Modifier
                        .heightIn(max = 530.dp)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 24.dp, vertical = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Invoice code + stats
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = invoiceCode,
                            onValueChange = { invoiceCode = it },
                            label = { Text("Mã phiếu nhập (tuỳ chọn)") },
                            placeholder = { Text("VD: PN-2026-001") },
                            singleLine = true,
                            enabled = !isLoading,
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                focusedLabelColor = MaterialTheme.colorScheme.primary
                            )
                        )
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                        ) {
                            Column(
                                modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                Text(
                                    "${rows.size} dòng",
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                if (estimatedTotal > 0.0) {
                                    Text(
                                        formatVND(estimatedTotal),
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        fontSize = 13.sp
                                    )
                                    Text("tạm tính", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }

                    // Error banner
                    displayError?.let { msg ->
                        Surface(shape = RoundedCornerShape(10.dp), color = MaterialTheme.colorScheme.errorContainer) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Warning, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                                Text(msg, color = MaterialTheme.colorScheme.onErrorContainer, fontSize = 13.sp)
                            }
                        }
                    }

                    // Table
                    Column {
                        // Column headers
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Sản phẩm *", Modifier.weight(1f), fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("SL *", Modifier.width(72.dp), fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
                            Text("Đơn giá nhập", Modifier.width(118.dp), fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
                            Text("NSX", Modifier.width(108.dp), fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
                            Text("HSD", Modifier.width(108.dp), fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
                            Spacer(Modifier.width(36.dp))
                        }
                        HorizontalDivider(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.25f), thickness = 1.5.dp)

                        rows.forEachIndexed { idx, line ->
                            BulkStockReceiptRow(
                                line = line,
                                products = products,
                                selectedProduct = productsById[line.productId],
                                isDuplicate = line.productId.isNotBlank() && line.productId in duplicateProductIds,
                                canRemove = rows.size > 1,
                                isLoading = isLoading,
                                onProductSelected = { pid ->
                                    rows = rows.map { if (it.id == line.id) it.copy(productId = pid) else it }
                                    errorMessage = null
                                },
                                onQuantityChange = { v ->
                                    rows = rows.map { if (it.id == line.id) it.copy(quantity = v.filter { c -> c.isDigit() }) else it }
                                    errorMessage = null
                                },
                                onImportPriceChange = { v ->
                                    rows = rows.map { if (it.id == line.id) it.copy(importPrice = sanitizeDecimalInput(v)) else it }
                                    errorMessage = null
                                },
                                onMfgDateChange = { v ->
                                    rows = rows.map { if (it.id == line.id) it.copy(mfgDate = v) else it }
                                    errorMessage = null
                                },
                                onExpDateChange = { v ->
                                    rows = rows.map { if (it.id == line.id) it.copy(expDate = v) else it }
                                    errorMessage = null
                                },
                                onRemove = {
                                    rows = rows.filterNot { it.id == line.id }
                                    errorMessage = null
                                }
                            )
                            if (idx < rows.lastIndex) {
                                HorizontalDivider(
                                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                                    modifier = Modifier.padding(vertical = 2.dp)
                                )
                            }
                        }
                    }

                    // Add row
                    Row {
                        OutlinedButton(
                            onClick = {
                                val nextId = (rows.maxOfOrNull { it.id } ?: 0) + 1
                                rows = rows + BulkStockReceiptLine(id = nextId)
                            },
                            enabled = !isLoading,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.Add, null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Thêm dòng")
                        }
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                // ── Footer ───────────────────────────────────────────────────────
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (duplicateProductIds.isNotEmpty()) {
                        Surface(shape = RoundedCornerShape(8.dp), color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.55f)) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Warning, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(13.dp))
                                Text("Có ${duplicateProductIds.size} sản phẩm trùng dòng", fontSize = 12.sp, color = MaterialTheme.colorScheme.onErrorContainer)
                            }
                        }
                        Spacer(Modifier.width(12.dp))
                    }
                    Spacer(Modifier.weight(1f))
                    TextButton(onClick = onCancel, enabled = !isLoading) { Text("Hủy") }
                    Spacer(Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val activeRows = rows.filter { l ->
                                l.productId.isNotBlank() || l.quantity.isNotBlank() ||
                                    l.importPrice.isNotBlank() || l.mfgDate.isNotBlank() || l.expDate.isNotBlank()
                            }
                            when {
                                products.isEmpty() -> errorMessage = "Chưa có sản phẩm để nhập kho"
                                activeRows.isEmpty() -> errorMessage = "Thêm ít nhất một dòng nhập kho"
                                activeRows.any { it.productId.isBlank() } -> errorMessage = "Mỗi dòng cần chọn sản phẩm"
                                activeRows.any { (it.quantity.toIntOrNull() ?: 0) <= 0 } -> errorMessage = "Số lượng nhập phải lớn hơn 0"
                                activeRows.any { it.importPrice.isNotBlank() && it.importPrice.toDoubleOrNull() == null } -> errorMessage = "Giá nhập không hợp lệ"
                                activeRows.any { !isValidCalendarDate(it.mfgDate) } -> errorMessage = "Ngày sản xuất không hợp lệ (định dạng: yyyy-MM-dd)"
                                activeRows.any { !isValidCalendarDate(it.expDate) } -> errorMessage = "Hạn sử dụng không hợp lệ (định dạng: yyyy-MM-dd)"
                                else -> {
                                    val note = invoiceCode.trim().takeIf { it.isNotBlank() }?.let { "Phiếu nhập: $it" }
                                    onSave(activeRows.map { l ->
                                        StockReceiptDraft(
                                            productId = l.productId,
                                            mfgDate = l.mfgDate.ifBlank { null },
                                            expDate = l.expDate.ifBlank { null },
                                            quantity = l.quantity.toInt(),
                                            importPrice = l.importPrice.toDoubleOrNull()?.takeIf { it > 0.0 },
                                            note = note
                                        )
                                    })
                                }
                            }
                        },
                        enabled = !isLoading
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary)
                            Spacer(Modifier.width(8.dp))
                            Text("Đang nhập kho...")
                        } else {
                            Text("Xác nhận nhập")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BulkStockReceiptRow(
    line: BulkStockReceiptLine,
    products: List<Product>,
    selectedProduct: Product?,
    isDuplicate: Boolean,
    canRemove: Boolean,
    isLoading: Boolean,
    onProductSelected: (String) -> Unit,
    onQuantityChange: (String) -> Unit,
    onImportPriceChange: (String) -> Unit,
    onMfgDateChange: (String) -> Unit,
    onExpDateChange: (String) -> Unit,
    onRemove: () -> Unit
) {
    val mfgInvalid = line.mfgDate.isNotBlank() && !isValidCalendarDate(line.mfgDate)
    val expInvalid = line.expDate.isNotBlank() && !isValidCalendarDate(line.expDate)
    val qtyInvalid = line.quantity.isNotBlank() && (line.quantity.toIntOrNull() ?: 0) <= 0
    val incoming = line.quantity.toIntOrNull() ?: 0

    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp, horizontal = 4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ProductStockDropdown(
                products = products,
                selectedProduct = selectedProduct,
                onProductSelected = onProductSelected,
                enabled = !isLoading,
                isDuplicate = isDuplicate,
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = line.quantity,
                onValueChange = onQuantityChange,
                label = { Text("SL", fontSize = 11.sp) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                enabled = !isLoading,
                isError = qtyInvalid,
                modifier = Modifier.width(72.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    focusedLabelColor = MaterialTheme.colorScheme.primary
                )
            )
            OutlinedTextField(
                value = line.importPrice,
                onValueChange = onImportPriceChange,
                label = { Text("Giá nhập", fontSize = 11.sp) },
                placeholder = { Text("0") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                enabled = !isLoading,
                modifier = Modifier.width(118.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    focusedLabelColor = MaterialTheme.colorScheme.primary
                )
            )
            OutlinedTextField(
                value = line.mfgDate,
                onValueChange = onMfgDateChange,
                label = { Text("NSX", fontSize = 11.sp) },
                placeholder = { Text("yyyy-MM-dd", fontSize = 10.sp) },
                singleLine = true,
                enabled = !isLoading,
                isError = mfgInvalid,
                modifier = Modifier.width(108.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    focusedLabelColor = MaterialTheme.colorScheme.primary
                )
            )
            OutlinedTextField(
                value = line.expDate,
                onValueChange = onExpDateChange,
                label = { Text("HSD", fontSize = 11.sp) },
                placeholder = { Text("yyyy-MM-dd", fontSize = 10.sp) },
                singleLine = true,
                enabled = !isLoading,
                isError = expInvalid,
                modifier = Modifier.width(108.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    focusedLabelColor = MaterialTheme.colorScheme.primary
                )
            )
            IconButton(
                onClick = onRemove,
                enabled = canRemove && !isLoading,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Xóa dòng",
                    tint = if (canRemove) MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
                           else MaterialTheme.colorScheme.outlineVariant,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
        // Inline hints
        Row(
            modifier = Modifier.padding(start = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (selectedProduct != null) {
                val color = if (incoming > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                Text(
                    "Tồn: ${selectedProduct.stockQuantity} → ${selectedProduct.stockQuantity + incoming} ${selectedProduct.unit}",
                    fontSize = 11.sp,
                    color = color
                )
            }
            if (isDuplicate) Text("⚠ Trùng với dòng khác", fontSize = 11.sp, color = MaterialTheme.colorScheme.error)
            if (mfgInvalid) Text("NSX không hợp lệ", fontSize = 11.sp, color = MaterialTheme.colorScheme.error)
            if (expInvalid) Text("HSD không hợp lệ", fontSize = 11.sp, color = MaterialTheme.colorScheme.error)
        }
    }
}

@Composable
private fun ProductStockDropdown(
    products: List<Product>,
    selectedProduct: Product?,
    onProductSelected: (String) -> Unit,
    enabled: Boolean,
    isDuplicate: Boolean = false,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    val filteredProducts = remember(products, searchQuery) {
        if (searchQuery.isBlank()) products.sortedBy { it.name }
        else products.filter {
            it.name.contains(searchQuery, ignoreCase = true) ||
                (it.sku?.contains(searchQuery, ignoreCase = true) == true)
        }.sortedBy { it.name }
    }

    Box(modifier = modifier) {
        OutlinedButton(
            onClick = { if (enabled) { expanded = true; searchQuery = "" } },
            enabled = enabled,
            modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp),
            shape = RoundedCornerShape(8.dp),
            border = if (isDuplicate) BorderStroke(1.5.dp, MaterialTheme.colorScheme.error)
                     else BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                if (selectedProduct != null) {
                    Text(selectedProduct.name, color = MaterialTheme.colorScheme.onSurface, maxLines = 1, overflow = TextOverflow.Ellipsis, fontSize = 13.sp)
                    Text(
                        "Tồn: ${selectedProduct.stockQuantity} ${selectedProduct.unit}" +
                            (selectedProduct.sku?.let { " · $it" } ?: ""),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp,
                        maxLines = 1
                    )
                } else {
                    Text("Chọn sản phẩm...", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
                }
            }
            Icon(Icons.Default.ArrowDropDown, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false; searchQuery = "" },
            modifier = Modifier.width(360.dp).heightIn(max = 400.dp)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Tìm tên / mã SKU...", fontSize = 12.sp) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().padding(8.dp),
                leadingIcon = { Icon(Icons.Default.Search, null, modifier = Modifier.size(16.dp)) },
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MaterialTheme.colorScheme.primary)
            )
            HorizontalDivider()
            if (filteredProducts.isEmpty()) {
                Text("Không tìm thấy sản phẩm", modifier = Modifier.padding(16.dp), fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                filteredProducts.forEach { product ->
                    DropdownMenuItem(
                        text = {
                            Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                                Text(product.name, maxLines = 1, overflow = TextOverflow.Ellipsis, fontSize = 13.sp)
                                Text(
                                    "Tồn: ${product.stockQuantity} ${product.unit}" +
                                        (product.sku?.let { " · $it" } ?: ""),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 11.sp
                                )
                            }
                        },
                        onClick = {
                            onProductSelected(product.id)
                            expanded = false
                            searchQuery = ""
                        }
                    )
                }
            }
        }
    }
}

private fun sanitizeDecimalInput(value: String): String {
    var dotUsed = false
    return value.filter { c ->
        when {
            c.isDigit() -> true
            c == '.' && !dotUsed -> { dotUsed = true; true }
            else -> false
        }
    }
}

private fun isValidCalendarDate(value: String): Boolean {
    if (value.isBlank()) return true
    return try { java.time.LocalDate.parse(value); true } catch (_: Exception) { false }
}
@Composable
private fun StockReceiptDialog(
    product: Product,
    isLoading: Boolean,
    onSave: (mfgDate: String?, expDate: String?, quantity: Int, importPrice: Double?) -> Unit,
    onCancel: () -> Unit
) {
    var mfgDate by remember { mutableStateOf("") }
    var expDate by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("") }
    var importPrice by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val incomingQuantity = quantity.toIntOrNull() ?: 0
    val projectedStock = product.stockQuantity + incomingQuantity

    AlertDialog(
        onDismissRequest = onCancel,
        title = { Text("Nhập thêm tồn kho") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (errorMessage != null) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            errorMessage!!,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(12.dp),
                            fontSize = 12.sp
                        )
                    }
                }

                Text(product.name, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Tồn hiện tại: ${product.stockQuantity} ${product.unit}", fontSize = 13.sp)
                        Text("Sau nhập: $projectedStock ${product.unit}", fontWeight = FontWeight.SemiBold)
                        Text(
                            "Ngày sản xuất/hạn dùng sẽ cập nhật trực tiếp vào sản phẩm để quản lý tồn kho đơn giản.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 12.sp
                        )
                    }
                }

                OutlinedTextField(
                    value = quantity,
                    onValueChange = { quantity = it.filter { c -> c.isDigit() } },
                    label = { Text("Số lượng nhập thêm", fontSize = 13.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    enabled = !isLoading,
                    textStyle = androidx.compose.material3.LocalTextStyle.current.copy(fontSize = 14.sp)
                )

                OutlinedTextField(
                    value = importPrice,
                    onValueChange = { importPrice = it.filter { c -> c.isDigit() || c == '.' } },
                    label = { Text("Giá nhập (VND)", fontSize = 13.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    placeholder = { Text("Ví dụ: 1500000", fontSize = 12.sp) },
                    enabled = !isLoading,
                    textStyle = androidx.compose.material3.LocalTextStyle.current.copy(fontSize = 14.sp)
                )

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = mfgDate,
                        onValueChange = { mfgDate = it },
                        label = { Text("Ngày sản xuất", fontSize = 13.sp) },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        placeholder = { Text("yyyy-MM-dd", fontSize = 12.sp) },
                        enabled = !isLoading,
                        textStyle = androidx.compose.material3.LocalTextStyle.current.copy(fontSize = 14.sp)
                    )

                    OutlinedTextField(
                        value = expDate,
                        onValueChange = { expDate = it },
                        label = { Text("Hạn sử dụng", fontSize = 13.sp) },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        placeholder = { Text("yyyy-MM-dd", fontSize = 12.sp) },
                        enabled = !isLoading,
                        textStyle = androidx.compose.material3.LocalTextStyle.current.copy(fontSize = 14.sp)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    when {
                        quantity.isBlank() -> errorMessage = "Nhập số lượng"
                        quantity.toIntOrNull() == null || quantity.toInt() <= 0 ->
                            errorMessage = "Số lượng phải > 0"
                        else -> {
                            onSave(
                                mfgDate.ifBlank { null },
                                expDate.ifBlank { null },
                                quantity.toInt(),
                                importPrice.toDoubleOrNull()
                            )
                        }
                    }
                },
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text("Cập nhật tồn kho")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onCancel,
                enabled = !isLoading
            ) {
                Text("Hủy")
            }
        }
    )
}

@Composable
private fun DatePickerModal(
    onDateSelected: (String) -> Unit,
    onDismiss: () -> Unit,
    currentDate: String = ""
) {
    var selectedYear by remember { mutableStateOf(2026) }
    var selectedMonth by remember { mutableStateOf(1) }
    var selectedDay by remember { mutableStateOf(1) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Chọn ngày (yyyy-MM-dd)", fontSize = 14.sp) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Year
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Năm:", fontSize = 13.sp, modifier = Modifier.width(60.dp))
                    OutlinedTextField(
                        value = selectedYear.toString(),
                        onValueChange = { selectedYear = it.toIntOrNull() ?: 2026 },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        textStyle = androidx.compose.material3.LocalTextStyle.current.copy(fontSize = 12.sp)
                    )
                }

                // Month
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Tháng:", fontSize = 13.sp, modifier = Modifier.width(60.dp))
                    OutlinedTextField(
                        value = selectedMonth.toString().padStart(2, '0'),
                        onValueChange = { 
                            val month = it.toIntOrNull() ?: 1
                            selectedMonth = month.coerceIn(1, 12)
                        },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        textStyle = androidx.compose.material3.LocalTextStyle.current.copy(fontSize = 12.sp)
                    )
                }

                // Day
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Ngày:", fontSize = 13.sp, modifier = Modifier.width(60.dp))
                    OutlinedTextField(
                        value = selectedDay.toString().padStart(2, '0'),
                        onValueChange = { 
                            val day = it.toIntOrNull() ?: 1
                            selectedDay = day.coerceIn(1, 31)
                        },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        textStyle = androidx.compose.material3.LocalTextStyle.current.copy(fontSize = 12.sp)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val formattedDate = String.format("%04d-%02d-%02d", selectedYear, selectedMonth, selectedDay)
                    onDateSelected(formattedDate)
                }
            ) {
                Text("OK", fontSize = 13.sp)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Hủy", fontSize = 13.sp)
            }
        }
    )
}
