package com.example.nhathuoc.ui.screen.miniscreen

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.MedicalServices
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.example.nhathuoc.data.model.ProductDto
import com.example.nhathuoc.data.model.UiState
import com.example.nhathuoc.ui.theme.GreenTop
import com.example.nhathuoc.viewmodel.ProductListViewModel

@Composable
fun ProductListScreen(
    modifier: Modifier = Modifier,
    navController: NavController = rememberNavController(),
    viewModel: ProductListViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val products by viewModel.allProducts.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val priceRange by viewModel.priceRange.collectAsState()
    val hasMore by viewModel.hasMore.collectAsState()

    var showFilterSheet by remember { mutableStateOf(false) }
    var searchText by remember { mutableStateOf("") }

    // Debounce search (300ms)
    LaunchedEffect(searchText) {
        kotlinx.coroutines.delay(300)
        viewModel.updateSearchQuery(searchText)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF5F7FA))
    ) {
        // Header
        ProductListHeader(
            searchQuery = searchText,
            onSearchChange = { searchText = it },
            onFilterClick = { showFilterSheet = true }
        )

        // Content
        when (state) {
            is UiState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = GreenTop)
                        Spacer(Modifier.height(12.dp))
                        Text("Đang tải sản phẩm...", color = Color.Gray, fontSize = 14.sp)
                    }
                }
            }
            is UiState.Error -> {
                ErrorState(
                    message = (state as UiState.Error).message,
                    onRetry = { viewModel.resetFilters() }
                )
            }
            is UiState.Idle -> {
                if (products.isEmpty()) {
                    EmptyState()
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
            else -> {
                if (products.isEmpty()) {
                    EmptyState()
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

    // Filter Bottom Sheet
    if (showFilterSheet) {
        FilterBottomSheet(
            selectedCategory = selectedCategory,
            priceRange = priceRange,
            onCategorySelect = { viewModel.setCategory(it) },
            onPriceChange = { min, max -> viewModel.setPriceRange(min, max) },
            onReset = { viewModel.resetFilters() },
            onDismiss = { showFilterSheet = false }
        )
    }
}

@Composable
private fun ProductListHeader(
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    onFilterClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(GreenTop)
            .padding(16.dp)
    ) {
        Text(
            "Danh sách sản phẩm",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White, RoundedCornerShape(8.dp))
                .padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Filled.Search, "Search", tint = Color.Gray, modifier = Modifier.size(20.dp))

            TextField(
                value = searchQuery,
                onValueChange = onSearchChange,
                modifier = Modifier
                    .weight(1f)
                    .height(40.dp),
                placeholder = { Text("Tìm sản phẩm...", fontSize = 12.sp) },
                singleLine = true,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                )
            )

            IconButton(onClick = onFilterClick, modifier = Modifier.size(40.dp)) {
                Icon(Icons.Outlined.Tune, "Lọc", tint = GreenTop)
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
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        items(products) { product ->
            ProductListCard(
                product = product,
                onClick = { onProductClick(product.id) }
            )
        }

        if (hasMore) {
            item {
                Button(
                    onClick = onLoadMore,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = GreenTop)
                ) {
                    Text("Xem thêm")
                }
            }
        }
    }
}

@Composable
private fun ProductListCard(
    product: ProductDto,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color.White,
        shadowElevation = 2.dp,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Product image / placeholder
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFE8F5E9)),
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
                    Icon(
                        Icons.Outlined.MedicalServices,
                        contentDescription = null,
                        tint = GreenTop,
                        modifier = Modifier.size(48.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Brand
            if (product.brand.isNotBlank()) {
                Text(
                    product.brand,
                    fontSize = 10.sp,
                    color = Color.Gray,
                    maxLines = 1,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Product name
            Text(
                product.name,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2,
                textAlign = TextAlign.Start,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Origin chip
            if (product.origin.isNotBlank()) {
                Text(
                    product.origin,
                    fontSize = 10.sp,
                    color = Color.Gray,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Price
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "${String.format("%,d", product.price.toLong()).replace(',', '.')}đ",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = GreenTop
                )

                if (product.discountPct > 0) {
                    Text(
                        "-${product.discountPct}%",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier
                            .background(Color.Red, CircleShape)
                            .padding(4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Unit
            Text(
                "/ ${product.unit}",
                fontSize = 10.sp,
                color = Color.Gray,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun FilterBottomSheet(
    selectedCategory: String?,
    priceRange: ClosedFloatingPointRange<Double>,
    onCategorySelect: (String?) -> Unit,
    onPriceChange: (Double, Double) -> Unit,
    onReset: () -> Unit,
    onDismiss: () -> Unit
) {
    var localMinPrice by remember { mutableStateOf(priceRange.start) }
    var localMaxPrice by remember { mutableStateOf(priceRange.endInclusive) }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
            .background(Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Bộ lọc", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Filled.Close, "Đóng")
                }
            }

            HorizontalDivider()

            Spacer(modifier = Modifier.height(12.dp))

            // Price filter
            Text("Khoảng giá", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TextField(
                    value = localMinPrice.toInt().toString(),
                    onValueChange = { localMinPrice = it.toDoubleOrNull() ?: 0.0 },
                    label = { Text("Từ") },
                    modifier = Modifier.weight(1f)
                )
                TextField(
                    value = localMaxPrice.toInt().toString(),
                    onValueChange = { localMaxPrice = it.toDoubleOrNull() ?: 1000000.0 },
                    label = { Text("Đến") },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onReset,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.LightGray)
                ) {
                    Text("Đặt lại", color = Color.Black)
                }
                Button(
                    onClick = {
                        onPriceChange(localMinPrice, localMaxPrice)
                        onDismiss()
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = GreenTop)
                ) {
                    Text("Áp dụng")
                }
            }
        }
    }
}

@Composable
private fun ErrorState(
    message: String,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            "Không thể tải sản phẩm",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1A1A1A)
        )
        Spacer(Modifier.height(8.dp))
        Text(
            message,
            textAlign = TextAlign.Center,
            color = Color.Gray,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        Button(
            onClick = onRetry,
            colors = ButtonDefaults.buttonColors(containerColor = GreenTop)
        ) {
            Text("Thử lại")
        }
    }
}

@Composable
private fun EmptyState() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Outlined.MedicalServices,
            contentDescription = null,
            tint = Color.LightGray,
            modifier = Modifier.size(64.dp)
        )
        Spacer(Modifier.height(16.dp))
        Text(
            "Không tìm thấy sản phẩm",
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
            color = Color(0xFF555555)
        )
        Spacer(Modifier.height(8.dp))
        Text(
            "Thử thay đổi bộ lọc hoặc từ khóa tìm kiếm",
            fontSize = 13.sp,
            color = Color.Gray,
            textAlign = TextAlign.Center
        )
    }
}
