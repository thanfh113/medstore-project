package com.example.nhathuoc.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nhathuoc.data.model.ProductDto

// ─────────────────────────────────────────────────────────────────────────────
// BestSellerItem – legacy UI model kept for backward compat with mock path
// ─────────────────────────────────────────────────────────────────────────────
data class BestSellerItem(
    val id: String = "0",
    val icon: androidx.compose.ui.graphics.vector.ImageVector = Icons.Filled.LocalFireDepartment,
    val iconTint: Color = Color(0xFF2E7D32),
    val iconBg: Color = Color(0xFFE8F5E9),
    val name: String,
    val price: String,
    val originalPrice: String,
    val discountAmount: String,
    val unit: String = "Hộp",
    val imageResId: Int? = null
)

// ─────────────────────────────────────────────────────────────────────────────
// BestSellerList – loads from real API via HomeViewModel
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun BestSellerList(
    // Real API data (from HomeViewModel)
    products: List<ProductDto> = emptyList(),
    isLoading: Boolean = false,
    onSeeAll: () -> Unit = {},
    onAddToCart: (ProductDto) -> Unit = {},
    navController: NavController? = null,
    modifier: Modifier = Modifier
) {
    // Show nothing when not loading and no products
    if (!isLoading && products.isEmpty()) return

    Column(modifier = modifier.fillMaxWidth()) {
        // Header gradient
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(listOf(Color(0xFFE53935), Color(0xFFFF7043))),
                    shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
                )
                .padding(vertical = 14.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.LocalFireDepartment,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = "Sản phẩm bán chạy",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )
            }
        }

        Surface(
            shape = RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 2.dp
        ) {
            if (isLoading) {
                // Shimmer placeholders (2-column grid)
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    repeat(2) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            repeat(2) {
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(200.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(MaterialTheme.colorScheme.surfaceVariant)
                                )
                            }
                        }
                    }
                }
            } else {
                Column(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // 2-column grid
                    products.chunked(2).forEach { rowItems ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            rowItems.forEach { product ->
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(300.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    ProductCard(
                                        data = product.toCardData().copy(isBestSeller = true),
                                        cardWidth = 170.dp,
                                        cardHeight = 292.dp,
                                        onProductClick = {
                                            navController?.navigate("ProductDetailScreen/${product.id}")
                                        },
                                        onAddToCart = { onAddToCart(product) }
                                    )
                                }
                            }
                            // Fill empty column if odd
                            if (rowItems.size == 1) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }

                    // See all button
                    TextButton(
                        onClick = onSeeAll,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp)
                    ) {
                        Text(
                            "Xem tất cả",
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}
