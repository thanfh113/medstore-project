package com.example.nhathuoc.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nhathuoc.ui.theme.GreenTop

data class BestSellerItem(
    val id: Int = 0,
    val icon: ImageVector,
    val iconTint: Color,
    val iconBg: Color,
    val name: String,
    val price: String,
    val originalPrice: String,
    val discountAmount: String
)

val defaultBestSellers = listOf(
    BestSellerItem(id = 101,
        icon = Icons.Outlined.Face, iconTint = Color(0xFFC2185B), iconBg = Color(0xFFFCE4EC),
        name = "Viên uống hỗ trợ làm đẹp da, giúp da trắng sáng, cải thiện thâm nám Perfect White Jpanwell (60 viên)",
        price = "1.410.000đ", originalPrice = "1.790.000đ", discountAmount = "-380.000đ"
    ),
    BestSellerItem(id = 102,
        icon = Icons.Outlined.ChildCare, iconTint = Color(0xFF2E7D32), iconBg = Color(0xFFE8F5E9),
        name = "Siro giúp xương răng chắc khỏe, bổ sung vitamin D3 + K2 Brauer Baby & Kids D3 + K2 High Potency MK-7 Drops (10ml)",
        price = "313.000đ", originalPrice = "396.000đ", discountAmount = "-83.000đ"
    ),
    BestSellerItem(id = 103,
        icon = Icons.Outlined.Visibility, iconTint = Color(0xFF0277BD), iconBg = Color(0xFFE1F5FE),
        name = "Viên uống bổ não, tốt cho mắt và tim mạch Ultra Brain Lab Well (60 viên)",
        price = "191.200đ", originalPrice = "239.000đ", discountAmount = "-20%"
    ),
)

@Composable
fun BestSellerList(
    items: List<BestSellerItem> = defaultBestSellers,
    onSeeAll: () -> Unit = {},
    onAddToCart: (BestSellerItem) -> Unit = {},
    navController: NavController? = null,
    modifier: Modifier = Modifier
) {
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

        // Items
        Surface(
            shape = RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp),
            color = Color(0xFFFFF3F3),
            shadowElevation = 2.dp
        ) {
            Column {
                items.forEach { item ->
                    BestSellerCard(
                        item = item,
                        onAddToCart = { onAddToCart(item) },
                        onItemClick = { navController?.navigate("ProductDetailScreen/${item.id}") }
                    )
                    if (item != items.last()) {
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            color = Color(0xFFFFDDDD),
                            thickness = 0.8.dp
                        )
                    }
                }

                // See all
                TextButton(
                    onClick = onSeeAll,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Text(
                        "Xem tất cả",
                        color = Color(0xFFE53935),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
private fun BestSellerCard(
    item: BestSellerItem,
    onAddToCart: () -> Unit,
    onItemClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onItemClick() }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Discount badge + icon
        Box {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(item.iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = item.icon,
                    contentDescription = null,
                    tint = item.iconTint,
                    modifier = Modifier.size(40.dp)
                )
            }
            Surface(
                shape = RoundedCornerShape(topStart = 6.dp, bottomEnd = 6.dp),
                color = Color(0xFFE53935),
                modifier = Modifier.align(Alignment.TopStart)
            ) {
                Text(
                    text = item.discountAmount,
                    color = Color.White,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                )
            }
        }

        Spacer(Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.name,
                fontSize = 13.sp,
                color = Color(0xFF1A1A1A),
                fontWeight = FontWeight.Medium,
                lineHeight = 18.sp,
                maxLines = 3
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = "${item.price} / Hộp",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = GreenTop
            )
            Text(
                text = item.originalPrice,
                fontSize = 11.sp,
                color = Color.Gray,
                style = androidx.compose.ui.text.TextStyle(textDecoration = TextDecoration.LineThrough)
            )
        }

        Spacer(Modifier.width(8.dp))

        FilledIconButton(
            onClick = onAddToCart,
            colors = IconButtonDefaults.filledIconButtonColors(containerColor = Color(0xFFE8F0FE)),
            modifier = Modifier.size(36.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.Add,
                contentDescription = "Thêm vào giỏ",
                tint = Color(0xFF1565C0),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}