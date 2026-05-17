package com.example.nhathuoc.ui.screen.miniscreen

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBackIos
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.FilterAlt
import androidx.compose.material.icons.outlined.MedicalServices
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.example.nhathuoc.data.model.ProductDto
import com.example.nhathuoc.data.model.UiState
import com.example.nhathuoc.viewmodel.ProductListViewModel

private data class SortOption(val label: String, val value: String?)

private val sortOptions = listOf(
    SortOption("Tên A-Z", null),
    SortOption("Giá tăng dần", "price_asc"),
    SortOption("Giá giảm dần", "price_desc"),
    SortOption("Mới nhất", "created_at"),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductListScreen(
    modifier: Modifier = Modifier,
    navController: NavController = rememberNavController(),
    onBack: (() -> Unit)? = null,
    viewModel: ProductListViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val products by viewModel.allProducts.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val priceRange by viewModel.priceRange.collectAsState()
    val sortBy by viewModel.sortBy.collectAsState()
    val hasMore by viewModel.hasMore.collectAsState()

    var showFilterSheet by remember { mutableStateOf(false) }
    var searchText by remember { mutableStateOf("") }

    // Detect if any filter is active for badge on filter button
    val filterActive = priceRange.start > 0 || priceRange.endInclusive < 1000000 || sortBy != null

    // Debounce search
    LaunchedEffect(searchText) {
        kotlinx.coroutines.delay(300)
        viewModel.updateSearchQuery(searchText)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        ProductListHeader(
            searchQuery = searchText,
            onSearchChange = { searchText = it },
            filterActive = filterActive,
            onFilterClick = { showFilterSheet = true },
            onBack = onBack
        )

        when (state) {
            is UiState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.height(12.dp))
                        Text("Đang tải sản phẩm...", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
                    }
                }
            }
            is UiState.Error -> {
                ErrorState(
                    message = (state as UiState.Error).message,
                    onRetry = { viewModel.resetFilters() }
                )
            }
            else -> {
                if (products.isEmpty() && state !is UiState.Loading) {
                    EmptyState(hasFilters = searchText.isNotBlank() || filterActive, onReset = { viewModel.resetFilters(); searchText = "" })
                } else {
                    ProductGrid(
                        products = products,
                        onProductClick = { productId ->
                            navController.navigate("ProductDetailScreen/$productId")
                        },
                        onLoadMore = { viewModel.loadMoreProducts() },
                        hasMore = hasMore
                    )
                }
            }
        }
    }

    if (showFilterSheet) {
        ModalBottomSheet(
            onDismissRequest = { showFilterSheet = false },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
        ) {
            FilterSheetContent(
                priceRange = priceRange,
                currentSortBy = sortBy,
                onSortSelect = { viewModel.setSortBy(it) },
                onPriceChange = { min, max -> viewModel.setPriceRange(min, max) },
                onReset = { viewModel.resetFilters(); searchText = "" },
                onApply = { showFilterSheet = false }
            )
        }
    }
}

@Composable
private fun ProductListHeader(
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    filterActive: Boolean,
    onFilterClick: () -> Unit,
    onBack: (() -> Unit)? = null
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Brush.horizontalGradient(listOf(Color(0xFF2E7D32), Color(0xFF66BB6A))))
            .statusBarsPadding()
            .padding(horizontal = 4.dp, vertical = 12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 10.dp)
        ) {
            if (onBack != null) {
                IconButton(onClick = onBack) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBackIos,
                        contentDescription = "Quay lại",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            } else {
                Spacer(Modifier.width(16.dp))
            }
            Text(
                "Danh sách sản phẩm",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
                .padding(horizontal = 12.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Filled.Search, "Tìm kiếm", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(6.dp))
            TextField(
                value = searchQuery,
                onValueChange = onSearchChange,
                modifier = Modifier.weight(1f),
                placeholder = { Text("Tìm tên, thương hiệu sản phẩm...", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant) },
                singleLine = true,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                )
            )
            if (searchQuery.isNotBlank()) {
                IconButton(onClick = { onSearchChange("") }, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Filled.Close, "Xóa", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
                }
            }
            Box {
                IconButton(onClick = onFilterClick, modifier = Modifier.size(40.dp)) {
                    Icon(Icons.Outlined.FilterAlt, "Lọc", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                if (filterActive) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(MaterialTheme.colorScheme.error, CircleShape)
                            .align(Alignment.TopEnd)
                    )
                }
            }
        }
    }
}

@Composable
private fun ProductGrid(
    products: List<ProductDto>,
    onProductClick: (String) -> Unit,
    onLoadMore: () -> Unit,
    hasMore: Boolean
) {
    val gridState = rememberLazyGridState()

    LaunchedEffect(gridState) {
        snapshotFlow { gridState.layoutInfo.visibleItemsInfo.lastOrNull()?.index }
            .collect { lastVisible ->
                if (hasMore && lastVisible != null && lastVisible >= products.size - 4) {
                    onLoadMore()
                }
            }
    }

    LazyVerticalGrid(
        state = gridState,
        columns = GridCells.Fixed(2),
        modifier = Modifier.fillMaxSize().padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        items(products, key = { it.id }) { product ->
            ProductListCard(product = product, onClick = { onProductClick(product.id) })
        }
        if (hasMore) {
            item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(2) }) {
                Box(modifier = Modifier.fillMaxWidth().padding(8.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(modifier = Modifier.size(28.dp), color = MaterialTheme.colorScheme.primary, strokeWidth = 2.dp)
                }
            }
        }
    }
}

@Composable
private fun ProductListCard(product: ProductDto, onClick: () -> Unit) {
    val canOrderOnline = product.riskClassification.uppercase().let { it != "C" && it != "D" }
    val hasDiscount = product.discountPct > 0
    val originalPrice = if (hasDiscount) product.price / (1.0 - product.discountPct / 100.0) else null

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 2.dp,
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Box(
                modifier = Modifier.fillMaxWidth().aspectRatio(1f).clip(RoundedCornerShape(10.dp)).background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)),
                contentAlignment = Alignment.Center
            ) {
                if (!product.imageUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = product.imageUrl,
                        contentDescription = product.name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit
                    )
                } else {
                    Icon(Icons.Outlined.MedicalServices, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(44.dp))
                }
                if (hasDiscount) {
                    Surface(
                        shape = RoundedCornerShape(bottomEnd = 10.dp),
                        color = Color(0xFFE53935),
                        modifier = Modifier.align(Alignment.TopStart)
                    ) {
                        Text(
                            "-${product.discountPct.toInt()}%",
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            if (product.brand.isNotBlank()) {
                Text(product.brand, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }

            Text(
                product.name,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 18.sp,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(6.dp))

            if (hasDiscount && originalPrice != null) {
                Text(
                    "${String.format("%,d", originalPrice.toLong()).replace(',', '.')}đ",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textDecoration = TextDecoration.LineThrough
                )
            }
            Text(
                "${String.format("%,d", product.price.toLong()).replace(',', '.')}đ",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(Modifier.height(6.dp))

            if (!canOrderOnline) {
                Surface(shape = RoundedCornerShape(50), color = MaterialTheme.colorScheme.tertiaryContainer) {
                    Text(
                        "Cần tư vấn",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onTertiaryContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
                Spacer(Modifier.height(4.dp))
            }

            Surface(
                shape = RoundedCornerShape(50),
                color = if (product.stock > 0) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.errorContainer
            ) {
                Text(
                    if (product.stock > 0) "Còn ${product.stock} ${product.unit}" else "Hết hàng",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (product.stock > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                )
            }
        }
    }
}

@Composable
private fun FilterSheetContent(
    priceRange: ClosedFloatingPointRange<Double>,
    currentSortBy: String?,
    onSortSelect: (String?) -> Unit,
    onPriceChange: (Double, Double) -> Unit,
    onReset: () -> Unit,
    onApply: () -> Unit
) {
    var localMinPrice by remember { mutableStateOf(priceRange.start.toInt().toString()) }
    var localMaxPrice by remember { mutableStateOf(priceRange.endInclusive.toInt().toString()) }
    var localSort by remember { mutableStateOf(currentSortBy) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(bottom = 32.dp)
            .navigationBarsPadding()
    ) {
        Text("Bộ lọc & Sắp xếp", fontSize = 17.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(16.dp))

        // Sort
        Text("Sắp xếp theo", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(8.dp))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            sortOptions.forEach { opt ->
                val selected = localSort == opt.value
                FilterChip(
                    selected = selected,
                    onClick = { localSort = opt.value },
                    label = { Text(opt.label, fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                    )
                )
            }
        }

        Spacer(Modifier.height(20.dp))

        // Price range
        Text("Khoảng giá (đ)", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedTextField(
                value = localMinPrice,
                onValueChange = { localMinPrice = it.filter { c -> c.isDigit() } },
                label = { Text("Từ") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(10.dp)
            )
            OutlinedTextField(
                value = localMaxPrice,
                onValueChange = { localMaxPrice = it.filter { c -> c.isDigit() } },
                label = { Text("Đến") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(10.dp)
            )
        }

        Spacer(Modifier.height(24.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedButton(
                onClick = {
                    localMinPrice = "0"
                    localMaxPrice = "1000000"
                    localSort = null
                    onReset()
                },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(50)
            ) {
                Text("Đặt lại")
            }
            Button(
                onClick = {
                    onSortSelect(localSort)
                    val min = localMinPrice.toDoubleOrNull() ?: 0.0
                    val max = localMaxPrice.toDoubleOrNull() ?: 1000000.0
                    onPriceChange(min, max)
                    onApply()
                },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("Áp dụng")
            }
        }
    }
}

@Composable
private fun ErrorState(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Không thể tải sản phẩm", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
        Spacer(Modifier.height(8.dp))
        Text(message, textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(bottom = 16.dp))
        Button(onClick = onRetry, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary), shape = RoundedCornerShape(50)) {
            Text("Thử lại")
        }
    }
}

@Composable
private fun EmptyState(hasFilters: Boolean, onReset: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(Icons.Outlined.MedicalServices, contentDescription = null, tint = MaterialTheme.colorScheme.outlineVariant, modifier = Modifier.size(72.dp))
        Spacer(Modifier.height(16.dp))
        Text("Không tìm thấy sản phẩm", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
        Spacer(Modifier.height(8.dp))
        Text(
            if (hasFilters) "Thử thay đổi bộ lọc hoặc từ khóa tìm kiếm" else "Chưa có sản phẩm nào",
            fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center
        )
        if (hasFilters) {
            Spacer(Modifier.height(16.dp))
            OutlinedButton(onClick = onReset, shape = RoundedCornerShape(50)) {
                Text("Xóa bộ lọc")
            }
        }
    }
}
