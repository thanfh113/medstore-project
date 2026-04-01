package com.example.nhathuoc.ui.screen.miniscreen

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.nhathuoc.data.model.*
import com.example.nhathuoc.data.repository.ProductRepository
import com.example.nhathuoc.ui.component.ErrorMessageCard
import com.example.nhathuoc.ui.component.EmptyStateDisplay
import com.example.nhathuoc.ui.theme.GreenTop
import com.example.nhathuoc.ui.theme.GreenLight
import com.example.nhathuoc.ui.theme.BgColor
import com.example.nhathuoc.util.PriceUtils
import com.example.nhathuoc.viewmodel.ProductListViewModel
import com.example.nhathuoc.viewmodel.ProductListViewModelFactory

@Composable
fun ProductListScreen(
    modifier: Modifier = Modifier,
    navController: NavController? = null,
    viewModel: ProductListViewModel = viewModel(
        factory = ProductListViewModelFactory(ProductRepository(null))
    )
) {
    var showFilters by remember { mutableStateOf(false) }
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val minPrice by viewModel.minPrice.collectAsState()
    val maxPrice by viewModel.maxPrice.collectAsState()
    val sortBy by viewModel.sortBy.collectAsState()
    val productsState by viewModel.productsState.collectAsState()
    val categoriesState by viewModel.categoriesState.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val isEmpty by viewModel.isEmpty.collectAsState()
    val hasMoreProducts by viewModel.hasMoreProducts.collectAsState()

    Column(modifier = modifier.fillMaxSize()) {
        // Header
        HeaderWithSearch(
            searchQuery = searchQuery,
            onSearchChange = { viewModel.searchQuery.value = it },
            onFilterClick = { showFilters = !showFilters },
            navController = navController
        )

        // Filter section
        if (showFilters) {
            FilterSection(
                selectedCategory = selectedCategory,
                categories = (categoriesState as? UiState.Success)?.data ?: emptyList(),
                onCategoryChange = { viewModel.setCategory(it) },
                minPrice = minPrice,
                maxPrice = maxPrice,
                onPriceRangeChange = { min, max -> viewModel.setPriceRange(min, max) },
                sortBy = sortBy,
                onSortChange = { viewModel.setSortBy(it) },
                onResetClick = { viewModel.resetFilters() }
            )
        }

        // Content
        when {
            isLoading && productsState is UiState.Idle -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = GreenTop)
                }
            }
            isEmpty && productsState is UiState.Success -> {
                EmptyStateDisplay(
                    title = "Không tìm thấy sản phẩm",
                    message = "Vui lòng thử lại với từ khóa khác",
                    modifier = Modifier.fillMaxSize()
                )
            }
            productsState is UiState.Error -> {
                ErrorMessageCard(
                    message = (productsState as UiState.Error).message,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                )
                Button(
                    onClick = { viewModel.retry() },
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = GreenTop)
                ) {
                    Text("Thử lại")
                }
            }
            productsState is UiState.Success -> {
                val products = (productsState as UiState.Success).data
                ProductGrid(
                    products = products,
                    onProductClick = { productId ->
                        navController?.navigate("ProductDetailScreen/$productId")
                    },
                    onLoadMore = { viewModel.loadMore() },
                    hasMore = hasMoreProducts,
                    isLoading = isLoading
                )
            }
        }
    }
}

@Composable
private fun HeaderWithSearch(
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    onFilterClick: () -> Unit,
    navController: NavController?
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Brush.verticalGradient(listOf(GreenTop, GreenLight)))
            .statusBarsPadding()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { navController?.popBackStack() }) {
                Icon(Icons.Filled.ArrowBack, contentDescription = null, tint = Color.White)
            }
            Text(
                "Mua Thuốc",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = onFilterClick) {
                Icon(Icons.Outlined.TuneVariant, contentDescription = null, tint = Color.White)
            }
        }

        Spacer(Modifier.height(12.dp))

        // Search bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchChange,
            placeholder = { Text("Tìm kiếm thuốc...") },
            leadingIcon = {
                Icon(Icons.Outlined.Search, contentDescription = null, tint = GreenTop)
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { onSearchChange("") }) {
                        Icon(Icons.Filled.Close, contentDescription = null, tint = GreenTop)
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(50.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color.White,
                unfocusedBorderColor = Color.White,
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White
            ),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Search
            )
        )
    }
}

@Composable
private fun FilterSection(
    selectedCategory: String?,
    categories: List<CategoryDto>,
    onCategoryChange: (String?) -> Unit,
    minPrice: Double,
    maxPrice: Double,
    onPriceRangeChange: (Double, Double) -> Unit,
    sortBy: String?,
    onSortChange: (String?) -> Unit,
    onResetClick: () -> Unit
) {
    var expandedCategory by remember { mutableStateOf(false) }
    var expandedSort by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(),
        color = Color.White,
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Category dropdown
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { expandedCategory = !expandedCategory }
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Danh mục", fontWeight = FontWeight.Medium)
                    Icon(
                        if (expandedCategory) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                        contentDescription = null
                    )
                }
                if (expandedCategory) {
                    LazyColumn {
                        items(categories) { category ->
                            FilterChip(
                                selected = selectedCategory == category.id,
                                onClick = { onCategoryChange(if (selectedCategory == category.id) null else category.id) },
                                label = { Text(category.name) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(4.dp)
                            )
                        }
                    }
                }
            }

            Divider()

            // Price range slider
            Column {
                Text("Khoảng giá", fontWeight = FontWeight.Medium)
                Spacer(Modifier.height(8.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = (minPrice / 1000).toInt().toString(),
                        onValueChange = {
                            if (it.isNotEmpty()) {
                                onPriceRangeChange(it.toDouble() * 1000, maxPrice)
                            }
                        },
                        label = { Text("Từ") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    Text("K", fontWeight = FontWeight.Medium)
                    OutlinedTextField(
                        value = (maxPrice / 1000).toInt().toString(),
                        onValueChange = {
                            if (it.isNotEmpty()) {
                                onPriceRangeChange(minPrice, it.toDouble() * 1000)
                            }
                        },
                        label = { Text("Đến") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    Text("K", fontWeight = FontWeight.Medium)
                }
            }

            Divider()

            // Sort dropdown
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { expandedSort = !expandedSort }
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Sắp xếp", fontWeight = FontWeight.Medium)
                    Icon(
                        if (expandedSort) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                        contentDescription = null
                    )
                }
                if (expandedSort) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        listOf(
                            "newest" to "Mới nhất",
                            "price_asc" to "Giá: Thấp đến Cao",
                            "price_desc" to "Giá: Cao đến Thấp",
                            "popular" to "Phổ biến nhất"
                        ).forEach { (value, label) ->
                            FilterChip(
                                selected = sortBy == value,
                                onClick = { onSortChange(if (sortBy == value) null else value) },
                                label = { Text(label) },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }

            Divider()

            // Reset button
            Button(
                onClick = onResetClick,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.Transparent)
            ) {
                Text("Xóa bộ lọc", color = GreenTop)
            }
        }
    }
}

@Composable
private fun ProductGrid(
    products: List<ProductDto>,
    onProductClick: (String) -> Unit,
    onLoadMore: () -> Unit,
    hasMore: Boolean,
    isLoading: Boolean
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(BgColor),
        contentPadding = PaddingValues(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(products, key = { it.id }) { product ->
            ProductGridItem(
                product = product,
                onClick = { onProductClick(product.id) }
            )
        }

        // Load more
        if (hasMore && !isLoading) {
            item {
                Button(
                    onClick = onLoadMore,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = GreenTop)
                ) {
                    Text("Xem thêm")
                }
            }
        }

        // Loading indicator
        if (isLoading) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = GreenTop)
                }
            }
        }
    }
}

@Composable
private fun ProductGridItem(
    product: ProductDto,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        color = Color.White,
        shape = RoundedCornerShape(12.dp),
        shadowElevation = 2.dp
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Image
                Surface(
                    modifier = Modifier.size(80.dp),
                    shape = RoundedCornerShape(8.dp),
                    color = BgColor
                ) {
                    Icon(
                        Icons.Outlined.MedicalServices,
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        tint = GreenTop
                    )
                }

                // Info
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .align(Alignment.Top)
                ) {
                    Text(
                        product.name,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 2
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        product.brand ?: "",
                        fontSize = 11.sp,
                        color = Color.Gray
                    )
                    Spacer(Modifier.height(8.dp))

                    // Price
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            PriceUtils.formatPrice(product.price),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = GreenTop
                        )
                        if ((product.originalPrice ?: 0.0) > product.price) {
                            Text(
                                PriceUtils.formatPrice(product.originalPrice ?: 0.0),
                                fontSize = 11.sp,
                                color = Color.Gray,
                                style = androidx.compose.material3.LocalTextStyle.current.copy(
                                    textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough
                                )
                            )
                        }
                    }
                }

                // Add button
                IconButton(onClick = onClick) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = GreenTop
                    ) {
                        Icon(
                            Icons.Filled.Add,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.padding(4.dp)
                        )
                    }
                }
            }
        }
    }
}

@Preview
@Composable
fun ProductListScreenPreview() {
    ProductListScreen()
}
