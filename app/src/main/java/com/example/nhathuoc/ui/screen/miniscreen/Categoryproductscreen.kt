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
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.nhathuoc.data.model.ProductDto
import com.example.nhathuoc.data.model.UiState
import com.example.nhathuoc.ui.theme.GreenLight
import com.example.nhathuoc.viewmodel.CategoryProductViewModel
import com.example.nhathuoc.viewmodel.CategoryProductViewModelFactory
import com.example.nhathuoc.data.repository.ProductRepository

private val GreenTop = Color(0xFF2E7D32)
private val BgColor = Color(0xFFF5F7FA)

@Composable
fun CategoryProductScreen(
    categoryName: String,
    navController: NavController? = null,
    onBack: () -> Unit = {}
) {
    val viewModel: CategoryProductViewModel = viewModel(
        factory = CategoryProductViewModelFactory(ProductRepository())
    )

    val state by viewModel.state.collectAsState()
    val products by viewModel.allProducts.collectAsState()

    // Load products when screen appears
    LaunchedEffect(categoryName) {
        viewModel.loadProductsByCategory(categoryName)
    }

    var sortMode by remember { mutableStateOf("price_asc") }
    val sortOptions = listOf("price_asc" to "Giá thấp", "price_desc" to "Giá cao", "name" to "Tên A-Z")

    Scaffold(containerColor = BgColor) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // ── TopBar ──────────────────────────────────
            Surface(color = Color.White, shadowElevation = 2.dp) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Brush.horizontalGradient(listOf(GreenTop, GreenLight)))
                        .statusBarsPadding()
                        .height(56.dp)
                ) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.align(Alignment.CenterStart)
                    ) {
                        Icon(Icons.Filled.ArrowBackIosNew, null, tint = Color.White, modifier = Modifier.size(20.dp))
                    }
                    Text(
                        categoryName,
                        color = Color.White,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.align(Alignment.Center)
                    )
                    Row(
                        modifier = Modifier.align(Alignment.CenterEnd),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = {}) {
                            Icon(Icons.Outlined.Search, null, tint = Color.White, modifier = Modifier.size(22.dp))
                        }
                        BadgedBox(badge = {
                            Badge(containerColor = Color(0xFFFF6D00)) {
                                Text("1", color = Color.White, fontSize = 9.sp)
                            }
                        }) {
                            IconButton(onClick = {}) {
                                Icon(Icons.Outlined.ShoppingCart, null, tint = Color.White, modifier = Modifier.size(22.dp))
                            }
                        }
                    }
                }
            }

            // ── Content ──────────────────────────────────
            when (state) {
                is UiState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = GreenTop)
                    }
                }
                is UiState.Error -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Outlined.WarningAmber, null, tint = Color.Red, modifier = Modifier.size(48.dp))
                            Spacer(Modifier.height(12.dp))
                            Text("Lỗi tải dữ liệu", fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(8.dp))
                            Button(onClick = { viewModel.loadProductsByCategory(categoryName) }) {
                                Text("Thử lại")
                            }
                        }
                    }
                }
                is UiState.Success -> {
                    if (products.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Không có sản phẩm trong danh mục này")
                        }
                    } else {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(12.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Sort bar
                            item(span = { GridItemSpan(2) }) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 8.dp),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Sắp xếp:", fontSize = 13.sp, color = Color(0xFF555555))
                                    sortOptions.forEach { (key, label) ->
                                        val isSelected = sortMode == key
                                        Text(
                                            label,
                                            fontSize = 12.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isSelected) GreenTop else Color(0xFF777777),
                                            modifier = Modifier.clickable {
                                                sortMode = key
                                                viewModel.updateSort(key)
                                            }
                                        )
                                    }
                                }
                            }

                            // Product list
                            items(products) { product ->
                                ProductCard(product, navController)
                            }
                        }
                    }
                }
                else -> {}
            }
        }
    }
}

@Composable
private fun ProductCard(
    product: ProductDto,
    navController: NavController?
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color.White,
        shadowElevation = 2.dp,
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .padding(10.dp)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Product icon/image placeholder
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = Color(0xFFEEF2FF),
                modifier = Modifier.size(80.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Outlined.LocalShipping,
                        null,
                        tint = Color(0xFF2E7D32),
                        modifier = Modifier.size(40.dp)
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            // Product name
            Text(
                text = product.name,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF1A1A1A),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )

            Spacer(Modifier.height(6.dp))

            // Price
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "₫${product.price.toInt()}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2E7D32)
                )
                if (product.originalPrice != null && product.originalPrice > product.price) {
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = "₫${product.originalPrice.toInt()}",
                        fontSize = 11.sp,
                        color = Color.Gray,
                        textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough
                    )
                }
            }

            Spacer(Modifier.height(4.dp))

            // Unit
            Text(
                text = "/ ${product.unit}",
                fontSize = 10.sp,
                color = Color(0xFF999999)
            )
        }
    }
}
