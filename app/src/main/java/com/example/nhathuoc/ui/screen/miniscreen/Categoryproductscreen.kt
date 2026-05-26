package com.example.nhathuoc.ui.screen.miniscreen

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.nhathuoc.data.model.ProductDto
import com.example.nhathuoc.data.model.UiState

import com.example.nhathuoc.viewmodel.CategoryProductViewModel



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryProductScreen(
    categoryId: String,
    categoryTitle: String,
    navController: NavController? = null,
    onBack: () -> Unit = {}
) {
    val viewModel: CategoryProductViewModel = hiltViewModel()

    val state by viewModel.state.collectAsState()
    val products by viewModel.allProducts.collectAsState()

    LaunchedEffect(categoryId) { viewModel.loadProductsByCategory(categoryId) }
    var isRefreshing by remember { mutableStateOf(false) }
    LaunchedEffect(state) { if (state !is UiState.Loading) isRefreshing = false }

    var sortMode by remember { mutableStateOf("price_asc") }
    val sortOptions = listOf("price_asc" to "Giá thấp", "price_desc" to "Giá cao", "name" to "Tên A-Z")

    var searchActive by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    val displayedProducts = remember(products, searchQuery) {
        if (searchQuery.isBlank()) products
        else products.filter {
            it.name.contains(searchQuery, ignoreCase = true) ||
            it.brand.contains(searchQuery, ignoreCase = true)
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0),
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = innerPadding.calculateBottomPadding())
        ) {
            // ── TopBar ─────────────────────────────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Brush.horizontalGradient(listOf(Color(0xFF2E7D32), Color(0xFF66BB6A))))
                    .statusBarsPadding()
                    .padding(bottom = if (searchActive) 10.dp else 0.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.align(Alignment.CenterStart)
                    ) {
                        Icon(Icons.Filled.ArrowBackIosNew, null, tint = Color.White, modifier = Modifier.size(20.dp))
                    }
                    Text(
                        categoryTitle,
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        lineHeight = 20.sp,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(horizontal = 80.dp)
                    )
                    Row(
                        modifier = Modifier.align(Alignment.CenterEnd),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = {
                            searchActive = !searchActive
                            if (!searchActive) searchQuery = ""
                        }) {
                            Icon(
                                if (searchActive) Icons.Filled.Close else Icons.Outlined.Search,
                                null, tint = Color.White, modifier = Modifier.size(22.dp)
                            )
                        }
                        IconButton(onClick = { navController?.navigate("CartScreen") }) {
                            Icon(Icons.Outlined.ShoppingCart, null, tint = Color.White, modifier = Modifier.size(22.dp))
                        }
                    }
                }
                if (searchActive) {
                    androidx.compose.foundation.text.BasicTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        singleLine = true,
                        textStyle = androidx.compose.ui.text.TextStyle(color = Color.White, fontSize = 14.sp),
                        cursorBrush = androidx.compose.ui.graphics.SolidColor(Color.White),
                        decorationBox = { innerTextField ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp)
                                    .background(Color.White.copy(alpha = 0.2f), RoundedCornerShape(24.dp))
                                    .padding(horizontal = 14.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Outlined.Search, null, tint = Color.White.copy(alpha = 0.8f), modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(8.dp))
                                Box(Modifier.weight(1f)) {
                                    if (searchQuery.isEmpty()) Text("Tìm trong danh mục...", color = Color.White.copy(alpha = 0.6f), fontSize = 14.sp)
                                    innerTextField()
                                }
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(onClick = { searchQuery = "" }, modifier = Modifier.size(20.dp)) {
                                        Icon(Icons.Filled.Close, null, tint = Color.White, modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // ── Content ────────────────────────────────────────────────────
            PullToRefreshBox(
                modifier = Modifier.fillMaxSize().weight(1f),
                isRefreshing = isRefreshing,
                onRefresh = { isRefreshing = true; viewModel.loadProductsByCategory(categoryId) }
            ) {
            when (state) {
                is UiState.Loading -> {
                    if (!isRefreshing) Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator()
                            Spacer(Modifier.height(12.dp))
                            Text("Đang tải sản phẩm...", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
                        }
                    }
                }
                is UiState.Error -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Outlined.WarningAmber, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(48.dp))
                            Spacer(Modifier.height(12.dp))
                            Text("Không thể tải danh mục", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Spacer(Modifier.height(8.dp))
                            Text(
                                (state as UiState.Error).message,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 24.dp)
                            )
                            Spacer(Modifier.height(16.dp))
                            Button(onClick = { viewModel.loadProductsByCategory(categoryId) }) {
                                Text("Thử lại")
                            }
                        }
                    }
                }
                is UiState.Success -> {
                    if (displayedProducts.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    Icons.Outlined.SearchOff,
                                    null,
                                    tint = MaterialTheme.colorScheme.outline,
                                    modifier = Modifier.size(64.dp)
                                )
                                Spacer(Modifier.height(12.dp))
                                Text(
                                    if (searchQuery.isNotBlank()) "Không tìm thấy sản phẩm phù hợp"
                                    else "Không có sản phẩm trong danh mục này",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    } else {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 10.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            contentPadding = PaddingValues(top = 10.dp, bottom = 24.dp)
                        ) {
                            // Sort bar
                            item(span = { GridItemSpan(2) }) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Sắp xếp:", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)
                                    sortOptions.forEach { (key, label) ->
                                        val isSelected = sortMode == key
                                        FilterChip(
                                            selected = isSelected,
                                            onClick = {
                                                sortMode = key
                                                viewModel.updateSort(key)
                                            },
                                            label = {
                                                Text(label, fontSize = 12.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
                                            }
                                        )
                                    }
                                }
                            }

                            // Product list
                            items(displayedProducts) { product ->
                                CategoryProductCard(
                                    product = product,
                                    onClick = {
                                        navController?.navigate("ProductDetailScreen/${product.id}")
                                    }
                                )
                            }
                        }
                    }
                }
                else -> {}
            }
            } // end PullToRefreshBox
        }
    }
}

@Composable
private fun CategoryProductCard(
    product: ProductDto,
    onClick: () -> Unit
) {
    val normalizedRisk = product.riskClassification.uppercase()
    val canOrderOnline = normalizedRisk != "C" && normalizedRisk != "D"
    val hidePrice = product.contactForPrice || !canOrderOnline

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 2.dp,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier
                .padding(10.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Product image / fallback icon
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
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
                        null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(40.dp)
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            // Brand
            if (product.brand.isNotBlank()) {
                Text(
                    text = product.brand,
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Product name
            Text(
                text = product.name,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Start,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(6.dp))

            // Origin
            if (product.origin.isNotBlank()) {
                Text(
                    text = product.origin,
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(Modifier.height(4.dp))

            // Price row — ẩn với sản phẩm liên hệ giá hoặc C/D
            if (!hidePrice) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${String.format("%,d", product.price.toLong()).replace(',', '.')}đ",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    if (product.originalPrice != null && product.originalPrice > product.price) {
                        Text(
                            text = "${String.format("%,d", product.originalPrice.toLong()).replace(',', '.')}đ",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textDecoration = TextDecoration.LineThrough
                        )
                    }
                }
            }

            if (!canOrderOnline) {
                Spacer(Modifier.height(4.dp))
                Surface(
                    shape = RoundedCornerShape(50),
                    color = Color(0xFFFFF3E0),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Loại $normalizedRisk - Cần tư vấn",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFFE65100),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            } else if (product.contactForPrice) {
                Spacer(Modifier.height(4.dp))
                Surface(
                    shape = RoundedCornerShape(50),
                    color = Color(0xFFFFF3E0),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Liên hệ để biết giá",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFFE65100),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(Modifier.height(4.dp))

            Surface(
                shape = RoundedCornerShape(50),
                color = if (product.stock > 0) Color(0xFFE8F5E9) else Color(0xFFFFEBEE),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = if (hidePrice) {
                        if (product.stock > 0) "Còn hàng" else "Hết hàng"
                    } else {
                        if (product.stock > 0) "Còn ${product.stock} ${product.unit}" else "Hết hàng"
                    },
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (product.stock > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }

            if (!hidePrice) {
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "/ ${product.unit}",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
