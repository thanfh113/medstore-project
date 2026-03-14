package com.example.nhathuoc.ui.component

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
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import com.example.nhathuoc.ui.theme.GreenTop

data class RecentOrder(
    val date: String,
    val itemCount: Int,
    val totalPrice: String
)

val defaultRecentOrders = listOf(
    RecentOrder("28/10/2025", 1, "17.000đ"),
    RecentOrder("02/07/2025", 1, "51.990đ"),
    RecentOrder("15/05/2025", 3, "235.000đ"),
)

@Composable
fun RecentOrdersRow(
    orders: List<RecentOrder> = defaultRecentOrders,
    onSeeAll: () -> Unit = {},
    onReorder: (RecentOrder) -> Unit = {},
    navController: NavController? = null,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Mua lại nhanh chóng",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A1A1A)
            )
            TextButton(onClick = onSeeAll) {
                Text("Xem tất cả", color = GreenTop, fontSize = 13.sp)
            }
        }

        // Horizontal scrollable cards
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            orders.forEach { order ->
                RecentOrderCard(order = order, onReorder = {
                    onReorder(order)
                    navController?.navigate("OrderDetailScreen/${"$"}{order.date}")
                })
            }
        }
    }
}

@Composable
private fun RecentOrderCard(
    order: RecentOrder,
    onReorder: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        shadowElevation = 3.dp,
        modifier = Modifier.width(180.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = "Đơn hàng ${order.date}",
                fontSize = 12.sp,
                color = Color.Gray,
                fontWeight = FontWeight.Medium
            )
            Spacer(Modifier.height(10.dp))

            // Product image placeholder
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = Color(0xFFF5F7FF),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Filled.ShoppingBag,
                        contentDescription = null,
                        tint = Color(0xFFBBCCEE),
                        modifier = Modifier.size(44.dp)
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            // Badge + price
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color(0xFF1A1A2E)
            ) {
                Text(
                    text = "${order.itemCount} sản phẩm",
                    color = Color.White,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
            Spacer(Modifier.height(6.dp))
            Text(
                text = order.totalPrice,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A1A1A)
            )
            Spacer(Modifier.height(10.dp))

            Button(
                onClick = onReorder,
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE8F0FE)),
                contentPadding = PaddingValues(horizontal = 0.dp, vertical = 8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Outlined.Replay,
                    contentDescription = null,
                    tint = GreenTop,
                    modifier = Modifier.size(15.dp)
                )
                Spacer(Modifier.width(4.dp))
                Text("Mua lại", color = GreenTop, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}