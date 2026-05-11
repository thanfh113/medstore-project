package com.example.nhathuoc.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.outlined.Replay
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.nhathuoc.data.model.OrderDto
import com.example.nhathuoc.ui.theme.GreenTop

@Composable
fun RecentOrdersRow(
    orders: List<OrderDto>,
    onSeeAll: () -> Unit = {},
    onReorder: (OrderDto) -> Unit = {},
    navController: NavController? = null,
    modifier: Modifier = Modifier
) {
    if (orders.isEmpty()) return

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Mua lại nhanh chóng", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A1A1A))
            TextButton(onClick = onSeeAll) {
                Text("Xem tất cả", color = GreenTop, fontSize = 13.sp)
            }
        }

        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            orders.forEach { order ->
                RecentOrderCard(
                    order = order,
                    onViewDetail = { navController?.navigate("OrderDetailScreen/${order.id}") },
                    onReorder = { onReorder(order) }
                )
            }
        }
    }
}

@Composable
private fun RecentOrderCard(
    order: OrderDto,
    onViewDetail: () -> Unit,
    onReorder: () -> Unit
) {
    val firstItem = order.items.firstOrNull()
    val imageUrl = firstItem?.imageUrl ?: firstItem?.product?.imageUrl
    val formattedDate = formatOrderDate(order.createdAt)
    val formattedTotal = "${String.format("%,d", order.total.toLong()).replace(',', '.')}đ"

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        shadowElevation = 3.dp,
        modifier = Modifier.width(185.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text("Đơn $formattedDate", fontSize = 11.sp, color = Color(0xFF9E9E9E), fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Spacer(Modifier.height(10.dp))

            // Product image
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFFF0F4FF)),
                contentAlignment = Alignment.Center
            ) {
                if (!imageUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = imageUrl,
                        contentDescription = firstItem?.name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit
                    )
                } else {
                    Icon(
                        Icons.Filled.ShoppingBag,
                        contentDescription = null,
                        tint = Color(0xFFBBCCEE),
                        modifier = Modifier.size(44.dp)
                    )
                }
                // Extra items badge
                if (order.items.size > 1) {
                    Surface(
                        shape = RoundedCornerShape(bottomStart = 10.dp),
                        color = Color(0x99000000),
                        modifier = Modifier.align(Alignment.TopEnd)
                    ) {
                        Text(
                            "+${order.items.size - 1}",
                            color = Color.White,
                            fontSize = 10.sp,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            // First product name
            if (firstItem != null) {
                Text(firstItem.name, fontSize = 11.sp, color = Color(0xFF424242), maxLines = 1, overflow = TextOverflow.Ellipsis)
                Spacer(Modifier.height(4.dp))
            }

            Surface(shape = RoundedCornerShape(20.dp), color = Color(0xFF1A1A2E)) {
                Text(
                    "${order.items.size} sản phẩm",
                    color = Color.White,
                    fontSize = 10.sp,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                )
            }
            Spacer(Modifier.height(4.dp))
            Text(formattedTotal, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A1A1A))
            Spacer(Modifier.height(10.dp))

            Button(
                onClick = onReorder,
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE8F5E9)),
                contentPadding = PaddingValues(horizontal = 0.dp, vertical = 8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Outlined.Replay, contentDescription = null, tint = GreenTop, modifier = Modifier.size(15.dp))
                Spacer(Modifier.width(4.dp))
                Text("Mua lại", color = GreenTop, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

private fun formatOrderDate(dateStr: String): String {
    return try {
        val date = dateStr.take(10) // "2025-10-28"
        val parts = date.split("-")
        if (parts.size == 3) "${parts[2]}/${parts[1]}/${parts[0]}" else date
    } catch (_: Exception) {
        dateStr.take(10)
    }
}
