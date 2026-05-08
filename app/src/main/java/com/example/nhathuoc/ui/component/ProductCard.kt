package com.example.nhathuoc.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.nhathuoc.ui.theme.GreenTop

// ─────────────────────────────────────────────────────────────────────────────
// Product Card Model
// ─────────────────────────────────────────────────────────────────────────────
data class ProductCardData(
    val id: String = "",
    val name: String = "",
    val brand: String = "",
    val price: String = "0đ",
    val originalPrice: String = "0đ",
    val discountPercent: Int = 0,
    val unit: String = "Hộp",
    val stock: Int = 0,
    val imageUrl: String? = null,
    val imageResId: Int? = null,
    val icon: ImageVector? = null,
    val iconTint: Color = Color.Gray,
    val iconBg: Color = Color.LightGray,
    val isFlashSale: Boolean = false,
    val isBestSeller: Boolean = false,
    val rating: Float = 0f,
    val reviewCount: Int = 0,
    val canOrderOnline: Boolean = true
)

// ─────────────────────────────────────────────────────────────────────────────
// Product Card Component
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun ProductCard(
    data: ProductCardData,
    modifier: Modifier = Modifier,
    cardWidth: Dp = 160.dp,
    cardHeight: Dp = 240.dp,
    onProductClick: (String) -> Unit = {},
    onAddToCart: (String) -> Unit = {}
) {
    val canAddToCart = data.canOrderOnline && data.stock > 0

    Surface(
        modifier = modifier
            .width(cardWidth)
            .height(cardHeight)
            .clip(RoundedCornerShape(12.dp))
            .clickable { onProductClick(data.id) },
        color = Color.White,
        shadowElevation = 2.dp
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // ── Image Container ────────────────────────────────────────
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(0.5f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(data.iconBg.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    if (!data.imageUrl.isNullOrBlank()) {
                        AsyncImage(
                            model = data.imageUrl,
                            contentDescription = data.name,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(4.dp),
                            contentScale = ContentScale.Fit
                        )
                    } else if (data.imageResId != null) {
                        androidx.compose.foundation.Image(
                            painter = painterResource(data.imageResId),
                            contentDescription = data.name,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(4.dp),
                            contentScale = ContentScale.Fit
                        )
                    } else if (data.icon != null) {
                        Icon(
                            imageVector = data.icon,
                            contentDescription = data.name,
                            tint = data.iconTint,
                            modifier = Modifier.size(48.dp)
                        )
                    }
                }

                // ── Product Info ───────────────────────────────────────────
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(0.5f),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    horizontalAlignment = Alignment.Start
                ) {
                    // Brand
                    if (data.brand.isNotEmpty()) {
                        Text(
                            text = data.brand,
                            fontSize = 10.sp,
                            color = Color.Gray,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    // Name
                    Text(
                        text = data.name,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        lineHeight = 13.sp
                    )

                    // Price row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = data.originalPrice,
                            fontSize = 10.sp,
                            color = Color.Gray,
                            textDecoration = TextDecoration.LineThrough,
                            maxLines = 1
                        )
                        Text(
                            text = data.price,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = GreenTop,
                            maxLines = 1
                        )
                    }

                    // Stock
                    Surface(
                        color = if (data.stock > 0) Color(0xFFE8F5E9) else Color(0xFFFFEBEE),
                        shape = RoundedCornerShape(4.dp),
                        modifier = Modifier.wrapContentSize()
                    ) {
                        Text(
                            text = if (data.stock > 0) "Còn ${data.stock}" else "Hết hàng",
                            fontSize = 9.sp,
                            color = if (data.stock > 0) GreenTop else Color(0xFFE53935),
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            // ── Discount Badge (top-right) ──────────────────────────────
            if (!data.canOrderOnline) {
                Surface(
                    color = Color(0xFFFFF3E0),
                    shape = RoundedCornerShape(topEnd = 12.dp, bottomStart = 12.dp),
                    modifier = Modifier.align(Alignment.TopEnd)
                ) {
                    Text(
                        text = "Tư vấn",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFE65100),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
                    )
                }
            } else if (data.stock <= 0) {
                Surface(
                    color = Color(0xFFE53935),
                    shape = RoundedCornerShape(topEnd = 12.dp, bottomStart = 12.dp),
                    modifier = Modifier.align(Alignment.TopEnd)
                ) {
                    Text(
                        text = "Hết",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
                    )
                }
            } else if (data.discountPercent > 0) {
                Surface(
                    color = Color(0xFFE53935),
                    shape = RoundedCornerShape(topEnd = 12.dp, bottomStart = 12.dp),
                    modifier = Modifier.align(Alignment.TopEnd)
                ) {
                    Text(
                        text = "-${data.discountPercent}%",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
                    )
                }
            }

            // ── Flash Sale Badge (top-left) ────────────────────────────
            if (data.isFlashSale) {
                Surface(
                    color = Color(0xFFE91E63),
                    shape = RoundedCornerShape(topStart = 12.dp, bottomEnd = 12.dp),
                    modifier = Modifier.align(Alignment.TopStart)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.LocalFireDepartment,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(10.dp)
                        )
                        Text(
                            text = "Sale",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }

            // ── Best Seller Star (top-center) ──────────────────────────
            if (data.isBestSeller) {
                Surface(
                    color = Color(0xFFFFB300),
                    shape = CircleShape,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 4.dp)
                ) {
                    Text(
                        text = "⭐",
                        fontSize = 16.sp,
                        modifier = Modifier.padding(4.dp)
                    )
                }
            }

            // ── Add to Cart Button (bottom-right) ───────────────────────
            if (canAddToCart) {
                FloatingActionButton(
                    onClick = { onAddToCart(data.id) },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(8.dp)
                        .size(36.dp),
                    containerColor = GreenTop,
                    shape = CircleShape,
                    elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 2.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = "Thêm vào giỏ",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Preview Sizes
// ─────────────────────────────────────────────────────────────────────────────
// Use these for different layouts:
// Horizontal (FlashSale): 160.dp × 240.dp
// Vertical (BestSeller): 150.dp × 280.dp
// Grid: 170.dp × 260.dp
