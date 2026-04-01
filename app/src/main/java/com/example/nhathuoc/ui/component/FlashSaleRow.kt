package com.example.nhathuoc.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.nhathuoc.ui.theme.GreenTop

data class FlashSaleProduct(
    val id: Int = 0,
    val icon: ImageVector,
    val iconTint: Color,
    val iconBg: Color,
    val name: String,
    val brand: String = "Siuuuu",
    val origin: String = "Việt Nam",
    val price: String,
    val originalPrice: String,
    val discountPercent: Int,
    val unit: String = "Hộp",
    val rewardPoints: Int = 100
)

val defaultFlashSaleProducts = listOf(
    FlashSaleProduct(
        id = 1,
        icon = Icons.Outlined.MedicalServices,
        iconTint = Color(0xFF1565C0), iconBg = Color(0xFFE3F2FD),
        name = "Vitamin C 1000mg", brand = "DHC", origin = "Nhật Bản",
        price = "89.000đ", originalPrice = "120.000đ", discountPercent = 26,
        rewardPoints = 89
    ),
    FlashSaleProduct(
        id = 2,
        icon = Icons.Outlined.Face,
        iconTint = Color(0xFFC2185B), iconBg = Color(0xFFFCE4EC),
        name = "Kem dưỡng da SPF50", brand = "Anessa", origin = "Nhật Bản",
        price = "199.000đ", originalPrice = "280.000đ", discountPercent = 29,
        rewardPoints = 199
    ),
    FlashSaleProduct(
        id = 3,
        icon = Icons.Outlined.MedicalServices,
        iconTint = Color(0xFF00838F), iconBg = Color(0xFFE0F7FA),
        name = "Máy đo huyết áp Omron", brand = "Omron", origin = "Nhật Bản",
        price = "350.000đ", originalPrice = "420.000đ", discountPercent = 17,
        rewardPoints = 350
    ),
    FlashSaleProduct(
        id = 4,
        icon = Icons.Outlined.LocalFlorist,
        iconTint = Color(0xFF2E7D32), iconBg = Color(0xFFE8F5E9),
        name = "Thảo dược gan", brand = "Boganic", origin = "Việt Nam",
        price = "145.000đ", originalPrice = "200.000đ", discountPercent = 28,
        rewardPoints = 145
    ),
    FlashSaleProduct(
        id = 5,
        icon = Icons.Outlined.RemoveRedEye,
        iconTint = Color(0xFF0277BD), iconBg = Color(0xFFE1F5FE),
        name = "Nhỏ mắt Santen", brand = "Santen", origin = "Nhật Bản",
        price = "75.000đ", originalPrice = "95.000đ", discountPercent = 21,
        rewardPoints = 75
    ),
    FlashSaleProduct(
        id = 6,
        icon = Icons.Outlined.CleanHands,
        iconTint = Color(0xFF6A1B9A), iconBg = Color(0xFFF3E5F5),
        name = "Kem đánh răng Sensodyne", brand = "Sensodyne", origin = "Anh",
        price = "65.000đ", originalPrice = "85.000đ", discountPercent = 24,
        rewardPoints = 65
    ),
)

@Composable
fun FlashSaleRow(
    title: String = "GIÁ TỐT MỖI NGÀY",
    products: List<FlashSaleProduct> = defaultFlashSaleProducts,
    onSeeAll: () -> Unit = {},
    onProductClick: (FlashSaleProduct) -> Unit = {},
    navController: NavController? = null,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        // Header gradient
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(listOf(GreenTop, Color(0xFF2E7D32))),
                    shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
                )
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Text(
                text = title,
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
                modifier = Modifier.align(Alignment.CenterStart)
            )
            TextButton(
                onClick = onSeeAll,
                modifier = Modifier.align(Alignment.CenterEnd)
            ) {
                Text("Xem thể lệ >", color = Color.White.copy(alpha = 0.85f), fontSize = 12.sp)
            }
        }

        // Scrollable product row
        Surface(
            shape = RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp),
            color = Color.White,
            shadowElevation = 2.dp
        ) {
            Row(
                modifier = Modifier
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 12.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                products.forEach { product ->
                    FlashSaleCard(
                        product = product,
                        onClick = {
                            onProductClick(product)
                            navController?.navigate("ProductDetailScreen/${product.id}")
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun FlashSaleCard(
    product: FlashSaleProduct,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFFF8F9FF),
        modifier = Modifier.width(120.dp)
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(modifier = Modifier.fillMaxWidth()) {
                // Icon circle
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                        .background(product.iconBg)
                        .align(Alignment.Center),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = product.icon,
                        contentDescription = product.name,
                        tint = product.iconTint,
                        modifier = Modifier.size(32.dp)
                    )
                }
                // Discount badge
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color(0xFFFF5252),
                    modifier = Modifier.align(Alignment.TopEnd)
                ) {
                    Text(
                        "-${product.discountPercent}%",
                        color = Color.White,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            Text(
                text = product.name,
                fontSize = 11.sp,
                color = Color(0xFF333333),
                textAlign = TextAlign.Center,
                lineHeight = 14.sp,
                minLines = 2,
                maxLines = 2
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = product.price,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFE53935)
            )
            Text(
                text = product.originalPrice,
                fontSize = 10.sp,
                color = Color.Gray,
                style = androidx.compose.ui.text.TextStyle(
                    textDecoration = TextDecoration.LineThrough
                )
            )
        }
    }
}