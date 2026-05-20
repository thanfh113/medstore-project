package com.example.nhathuoc.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.MedicalServices
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.nhathuoc.data.model.ProductDto
import com.example.nhathuoc.ui.theme.GreenTop

// ─────────────────────────────────────────────────────────────────────────────
// FlashSaleProduct – legacy UI model, kept for backward compat with mock path
// ─────────────────────────────────────────────────────────────────────────────
data class FlashSaleProduct(
    val id: String = "0",
    val icon: androidx.compose.ui.graphics.vector.ImageVector = Icons.Outlined.MedicalServices,
    val iconTint: Color = Color(0xFF2E7D32),
    val iconBg: Color = Color(0xFFE8F5E9),
    val name: String,
    val brand: String = "",
    val origin: String = "",
    val price: String,
    val originalPrice: String,
    val discountPercent: Int,
    val unit: String = "Hộp",
    val rewardPoints: Int = 0,
    val imageResId: Int? = null
)

// Helper: format price display
private fun Long.formatVnd(): String = String.format("%,d", this).replace(',', '.') + "đ"
private fun Double.formatVnd(): String = this.toLong().formatVnd()

// ─────────────────────────────────────────────────────────────────────────────
// Map ProductDto → ProductCardData for reuse
// ─────────────────────────────────────────────────────────────────────────────
internal fun ProductDto.toCardData() = ProductCardData(
    id = id,
    name = name,
    brand = brand,
    price = price.formatVnd(),
    originalPrice = (originalPrice ?: price).formatVnd(),
    discountPercent = discountPct,
    unit = unit,
    stock = stock,
    imageUrl = imageUrl,
    icon = Icons.Outlined.MedicalServices,
    iconTint = Color(0xFF2E7D32),
    iconBg = Color(0xFFE8F5E9),
    isFlashSale = discountPct > 0,
    canOrderOnline = riskClassification.uppercase() != "C" && riskClassification.uppercase() != "D"
)

// ─────────────────────────────────────────────────────────────────────────────
// FlashSaleRow – loads from real API via HomeViewModel
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun FlashSaleRow(
    title: String = "VẬT TƯ Y TẾ – GIÁ TỐT MỖI NGÀY",
    // Real API data (from HomeViewModel)
    products: List<ProductDto> = emptyList(),
    isLoading: Boolean = false,
    onSeeAll: () -> Unit = {},
    onProductClick: (ProductDto) -> Unit = {},
    onAddToCart: (ProductDto) -> Unit = {},
    // Legacy: used when navController is provided for auto-navigation
    navController: NavController? = null,
    modifier: Modifier = Modifier
) {
    // Show nothing when not loading and no products
    if (!isLoading && products.isEmpty()) return

    Column(modifier = modifier.fillMaxWidth()) {
        // Header gradient
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(listOf(GreenTop, Color(0xFF2E7D32))),
                    shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
                )
                .padding(start = 16.dp, end = 4.dp, top = 12.dp, bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                fontSize = 15.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
                modifier = Modifier.weight(1f)
            )
            TextButton(
                onClick = onSeeAll,
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text("Xem tất cả >", color = Color.White.copy(alpha = 0.85f), fontSize = 12.sp)
            }
        }

        Surface(
            shape = RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 2.dp
        ) {
            if (isLoading) {
                // Shimmer placeholders
                Row(
                    modifier = Modifier
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 12.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    repeat(3) {
                        Box(
                            modifier = Modifier
                                .width(160.dp)
                                .height(240.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                        )
                    }
                }
            } else {
                Row(
                    modifier = Modifier
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 12.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    products.forEach { product ->
                        ProductCard(
                            data = product.toCardData(),
                            cardWidth = 160.dp,
                            cardHeight = 240.dp,
                            onProductClick = {
                                onProductClick(product)
                                navController?.navigate("ProductDetailScreen/${product.id}")
                            },
                            onAddToCart = { onAddToCart(product) }
                        )
                    }
                }
            }
        }
    }
}
