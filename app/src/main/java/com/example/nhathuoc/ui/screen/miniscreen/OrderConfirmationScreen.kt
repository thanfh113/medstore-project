package com.example.nhathuoc.ui.screen.miniscreen

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.nhathuoc.data.model.UiState
import com.example.nhathuoc.ui.theme.GreenTop
import com.example.nhathuoc.viewmodel.OrderViewModel

@Composable
fun OrderConfirmationScreen(
    modifier: Modifier = Modifier,
    orderId: String = "",
    navController: NavController = rememberNavController(),
    viewModel: OrderViewModel = viewModel()
) {
    val orderState by viewModel.orderState.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    LaunchedEffect(orderId) {
        if (orderId.isNotEmpty()) {
            viewModel.getOrderById(orderId)
        }
    }

    when (orderState) {
        is UiState.Loading -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = GreenTop)
            }
        }
        is UiState.Success<*> -> {
            val order = (orderState as? UiState.Success<*>)?.let {
                it.data as? com.example.nhathuoc.data.model.OrderDto
            }

            if (order != null) {
                OrderConfirmationContent(
                    order = order,
                    navController = navController,
                    modifier = modifier
                )
            }
        }
        is UiState.Error -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Lỗi tải đơn hàng", fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(12.dp))
                    Button(onClick = { viewModel.getOrderById(orderId) }) {
                        Text("Thử lại")
                    }
                }
            }
        }
        else -> {}
    }
}

@Composable
private fun OrderConfirmationContent(
    order: com.example.nhathuoc.data.model.OrderDto,
    navController: NavController,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF5F7FA))
    ) {
        // Success Animation
        AnimatedCheckmark()

        // Main content
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Success message
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "Đặt hàng thành công!",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Đơn hàng của bạn đã được tiếp nhận thành công",
                        fontSize = 14.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Center
                    )
                }
            }

            // Order code
            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp)),
                    color = Color.White,
                    shadowElevation = 2.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Mã đơn hàng", fontSize = 12.sp, color = Color.Gray)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                order.orderCode,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(
                                onClick = {
                                    // Copy to clipboard logic would go here
                                },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(Icons.Filled.ContentCopy, "Copy", tint = GreenTop)
                            }
                        }
                    }
                }
            }

            // Order timeline
            item {
                OrderTimeline(status = order.status)
            }

            // Order items
            if (order.items.isNotEmpty()) {
                item {
                    Text("Chi tiết đơn hàng", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }

                items(order.items) { item ->
                    OrderItemRow(
                        name = item.name,
                        quantity = item.quantity,
                        price = item.price.toInt()
                    )
                }
            }

            // Pricing breakdown
            item {
                PricingBreakdown(
                    subtotal = order.subtotal,
                    discount = order.discount,
                    shipping = order.shippingFee,
                    tax = order.subtotal * 0.1,
                    rewardPoints = order.pointsEarned,
                    total = order.total
                )
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }
        }

        // Action buttons
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding(),
            color = Color.White,
            shadowElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = { navController.navigate("MyOrdersScreen") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = GreenTop)
                ) {
                    Icon(Icons.Filled.TrackChanges, "Track", modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Theo dõi đơn hàng", color = Color.White)
                }

                Button(
                    onClick = { navController.navigate("MainScreen") {
                        popUpTo("MainScreen") { inclusive = true }
                    }},
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.LightGray)
                ) {
                    Text("Tiếp tục mua sắm", color = Color.Black)
                }
            }
        }
    }
}

@Composable
private fun AnimatedCheckmark() {
    val scale by animateFloatAsState(
        targetValue = 1f,
        animationSpec = spring(dampingRatio = 0.5f, stiffness = Spring.StiffnessLow),
        label = "checkmark"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier
                .size(100.dp)
                .scale(scale),
            color = Color(0xFFE8F5E9),
            shape = CircleShape
        ) {
            Icon(
                Icons.Filled.CheckCircle,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp),
                tint = GreenTop
            )
        }
    }
}

@Composable
private fun OrderTimeline(status: String = "PENDING") {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp)),
        color = Color.White,
        shadowElevation = 2.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            val steps = listOf(
                "Đã đặt hàng" to (if (listOf("PENDING", "CONFIRMED", "PREPARING", "SHIPPING", "DELIVERED").contains(status)) "Vừa xong" else "--"),
                "Xác nhận" to (if (listOf("CONFIRMED", "PREPARING", "SHIPPING", "DELIVERED").contains(status)) "Đã xác nhận" else "--"),
                "Chuẩn bị hàng" to (if (listOf("PREPARING", "SHIPPING", "DELIVERED").contains(status)) "Đang chuẩn bị" else "--"),
                "Đã giao" to (if (status == "DELIVERED") "Đã giao" else "--")
            )

            val completedSteps = when (status) {
                "PENDING" -> 0
                "CONFIRMED" -> 1
                "PREPARING" -> 2
                "SHIPPING" -> 3
                "DELIVERED" -> 4
                else -> 0
            }

            steps.forEachIndexed { index, (step, time) ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Timeline circle
                    Surface(
                        modifier = Modifier.size(32.dp),
                        color = if (index < completedSteps) GreenTop else Color(0xFFE8F5E9),
                        shape = CircleShape
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            if (index < completedSteps) {
                                Icon(Icons.Filled.Check, null, tint = Color.White, modifier = Modifier.size(16.dp))
                            } else {
                                Text((index + 1).toString(), color = GreenTop)
                            }
                        }
                    }

                    // Timeline text
                    Column(modifier = Modifier.weight(1f)) {
                        Text(step, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        Text(time, fontSize = 11.sp, color = Color.Gray)
                    }
                }

                if (index < steps.lastIndex) {
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }
    }
}

@Composable
private fun OrderItemRow(name: String, quantity: Int, price: Int) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp)),
        color = Color.White,
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(name, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                Text("Số lượng: $quantity", fontSize = 11.sp, color = Color.Gray)
            }
            Text("${price * quantity}đ", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = GreenTop)
        }
    }
}

@Composable
private fun PricingBreakdown(
    subtotal: Double,
    discount: Double,
    shipping: Double,
    tax: Double,
    rewardPoints: Int,
    total: Double
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp)),
        color = Color.White,
        shadowElevation = 2.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Tổng hợp", fontWeight = FontWeight.Bold, fontSize = 13.sp)
            Spacer(modifier = Modifier.height(12.dp))

            PricingRowItem("Tạm tính", "${String.format("%.0f", subtotal).toInt()}đ")
            if (discount > 0) PricingRowItem("Giảm giá", "-${String.format("%.0f", discount).toInt()}đ", Color.Red)
            if (shipping > 0) PricingRowItem("Phí giao hàng", "${String.format("%.0f", shipping).toInt()}đ")
            PricingRowItem("Thuế", "${String.format("%.0f", tax).toInt()}đ")
            if (rewardPoints > 0) PricingRowItem("Điểm thưởng", "-${rewardPoints * 1000}đ", Color.Red)

            Divider(modifier = Modifier.padding(vertical = 8.dp))

            PricingRowItem("Tổng cộng", "${String.format("%.0f", total).toInt()}đ", GreenTop, bold = true)
        }
    }
}

@Composable
private fun PricingRowItem(
    label: String,
    value: String,
    color: Color = Color.Black,
    bold: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontSize = 12.sp, fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal)
        Text(value, fontSize = 12.sp, fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal, color = color)
    }
}
