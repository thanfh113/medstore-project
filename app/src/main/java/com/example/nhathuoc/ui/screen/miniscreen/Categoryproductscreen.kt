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
import com.example.nhathuoc.ui.theme.GreenLight
import com.example.nhathuoc.viewmodel.CategoryProductViewModel

private val GreenTopCat = Color(0xFF2E7D32)
private val BgColorCat = Color(0xFFF5F7FA)

@Composable
fun CategoryProductScreen(
    categoryName: String,
    navController: NavController? = null,
    onBack: () -> Unit = {}
) {
    val viewModel: CategoryProductViewModel = hiltViewModel()

    val state by viewModel.state.collectAsState()
    val products by viewModel.allProducts.collectAsState()

    // Load products when screen appears
    LaunchedEffect(categoryName) {
        viewModel.loadProductsByCategory(categoryName)
    }

    var sortMode by remember { mutableStateOf("price_asc") }
    val sortOptions = listOf("price_asc" to "Giá thấp", "price_desc" to "Giá cao", "name" to "Tên A-Z")

    Scaffold(containerColor = BgColorCat) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // ── TopBar ─────────────────────────────────────────────────────
            Surface(color = Color.White, shadowElevation = 2.dp) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Brush.horizontalGradient(listOf(GreenTopCat, GreenLight)))
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
                        IconButton(onClick = { navController?.navigate("CartScreen") }) {
                            Icon(
                                Icons.Outlined.ShoppingCart,
                                null,
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }
            }

            // ── Content ────────────────────────────────────────────────────
            when (state) {
                is UiState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = GreenTopCat)
                            Spacer(Modifier.height(12.dp))
                            Text("Đang tải sản phẩm...", color = Color.Gray, fontSize = 14.sp)
                        }
                    }
                }
                is UiState.Error -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Outlined.WarningAmber, null, tint = Color(0xFFE53935), modifier = Modifier.size(48.dp))
                            Spacer(Modifier.height(12.dp))
                            Text("Không thể tải danh mục", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Spacer(Modifier.height(8.dp))
                            Text(
                                (state as UiState.Error).message,
                                color = Color.Gray,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 24.dp)
                            )
                            Spacer(Modifier.height(16.dp))
                            Button(
                                onClick = { viewModel.loadProductsByCategory(categoryName) },
                                colors = ButtonDefaults.buttonColors(containerColor = GreenTopCat)
                            ) {
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
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    Icons.Outlined.SearchOff,
                                    null,
                                    tint = Color.LightGray,
                                    modifier = Modifier.size(64.dp)
                                )
                                Spacer(Modifier.height(12.dp))
                                Text("Không có sản phẩm trong danh mục này", color = Color.Gray)
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
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Sắp xếp:", fontSize = 13.sp, color = Color(0xFF555555))
                                    sortOptions.forEach { (key, label) ->
                                        val isSelected = sortMode == key
                                        Surface(
                                            shape = RoundedCornerShape(16.dp),
                                            color = if (isSelected) GreenTopCat.copy(alpha = 0.12f) else Color.Transparent,
                                            modifier = Modifier.clickable {
                                                sortMode = key
                                                viewModel.updateSort(key)
                                            }
                                        ) {
                                            Text(
                                                label,
                                                fontSize = 12.sp,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                color = if (isSelected) GreenTopCat else Color(0xFF777777),
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                            )
                                        }
                                    }
                                }
                            }

                            // Product list
                            items(products) { product ->
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
        }
    }
}

@Composable
private fun CategoryProductCard(
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
                    .background(Color(0xFFEEF2FF)),
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
                        tint = GreenTopCat,
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
                    color = Color.Gray,
                    maxLines = 1,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Product name
            Text(
                text = product.name,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF1A1A1A),
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
                    color = Color.Gray,
                    maxLines = 1,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(Modifier.height(4.dp))

            // Price row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${String.format("%,d", product.price.toLong()).replace(',', '.')}đ",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = GreenTopCat
                )
                if (product.originalPrice != null && product.originalPrice > product.price) {
                    Text(
                        text = "${String.format("%,d", product.originalPrice.toLong()).replace(',', '.')}đ",
                        fontSize = 11.sp,
                        color = Color.Gray,
                        textDecoration = TextDecoration.LineThrough
                    )
                }
            }

            Spacer(Modifier.height(4.dp))

            // Unit
            Text(
                text = "/ ${product.unit}",
                fontSize = 10.sp,
                color = Color(0xFF999999),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
