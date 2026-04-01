package com.example.nhathuoc.ui.screen.miniscreen

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.nhathuoc.data.model.OrderItemDto
import com.example.nhathuoc.ui.theme.GreenTop
import com.example.nhathuoc.ui.theme.GreenLight
import com.example.nhathuoc.ui.theme.BgColor
import com.example.nhathuoc.util.PriceUtils

@Composable
fun OrderConfirmationScreen(
    modifier: Modifier = Modifier,
    navController: NavController? = null,
    orderId: String = "ORD-123456"
) {
    val infiniteTransition = rememberInfiniteTransition(label = "check animation")
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    // Mock data
    val orderItems = remember {
        listOf(
            OrderItemDto("1", "Vitamin D3 1000IU", 2, 150000.0),
            OrderItemDto("2", "Muối rửa mũi xoang", 1, 170000.0)
        )
    }

    val subtotal = orderItems.sumOf { it.price * it.quantity }
    val shipping = 30000.0
    val tax = 50000.0
    val rewardPoints = 120
    val finalTotal = subtotal + shipping + tax

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF5F7FA))
    ) {
        // Header
        HeaderConfirmation()

        // Content
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Spacer(Modifier.height(16.dp))

            // Success icon with animation
            Surface(
                modifier = Modifier
                    .size(120.dp)
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                    },
                color = Color(0xFFE3F2FD),
                shape = RoundedCornerShape(60.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Filled.CheckCircle,
                        contentDescription = null,
                        modifier = Modifier.size(80.dp),
                        tint = Color(0xFF4CAF50)
                    )
                }
            }

            // Success message
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    "Đơn hàng đã được đặt thành công!",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2E7D32),
                    textAlign = TextAlign.Center
                )
                Text(
                    "Cảm ơn bạn đã mua sắm cùng NhàThuốc HELLO",
                    fontSize = 14.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )
            }

            // Order ID card
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color.White,
                shape = RoundedCornerShape(12.dp),
                shadowElevation = 2.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        "Mã đơn hàng",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            orderId,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = GreenTop
                        )
                        IconButton(onClick = {}, modifier = Modifier.size(28.dp)) {
                            Icon(
                                Icons.Outlined.ContentCopy,
                                contentDescription = "Copy",
                                tint = GreenTop,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            // Timeline
            OrderTimeline()

            // Order summary
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color.White,
                shape = RoundedCornerShape(12.dp),
                shadowElevation = 2.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        "Chi tiết đơn hàng",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )

                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(orderItems) { item ->
                            OrderItemRow(item)
                        }
                    }

                    Divider()

                    // Pricing summary
                    PricingRow("Tạm tính", PriceUtils.formatPrice(subtotal))
                    PricingRow("Phí vận chuyển", PriceUtils.formatPrice(shipping))
                    PricingRow("Thuế (10%)", PriceUtils.formatPrice(tax))
                    Divider()
                    PricingRow(
                        "Tổng cộng",
                        PriceUtils.formatPrice(finalTotal),
                        isTotal = true
                    )
                }
            }

            // Reward points card
            RewardPointsCard(points = rewardPoints)

            Spacer(Modifier.height(16.dp))
        }

        // Action buttons
        ActionButtons(
            onTrackOrderClick = {
                navController?.navigate("MyOrdersScreen")
            },
            onShoppingContinueClick = {
                navController?.navigate("ProductListScreen")
            }
        )
    }
}

@Composable
private fun HeaderConfirmation() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Brush.verticalGradient(listOf(GreenTop, GreenLight)))
            .statusBarsPadding()
            .padding(16.dp)
    ) {
        Text(
            "Xác nhận đơn hàng",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.align(Alignment.Center)
        )
    }
}

@Composable
private fun OrderTimeline() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.White,
        shape = RoundedCornerShape(12.dp),
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "Quá trình xử lý",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )

            val steps = listOf(
                TimelineStep(
                    title = "Đơn hàng đã được đặt",
                    subtitle = "Vừa xong",
                    isCompleted = true,
                    isActive = true
                ),
                TimelineStep(
                    title = "Đơn hàng được xác nhận",
                    subtitle = "trong vòng 1 tiếng",
                    isCompleted = false,
                    isActive = false
                ),
                TimelineStep(
                    title = "Đơn hàng được giao",
                    subtitle = "trong vòng 2-3 ngày",
                    isCompleted = false,
                    isActive = false
                ),
                TimelineStep(
                    title = "Giao hàng thành công",
                    subtitle = "Sắp tới",
                    isCompleted = false,
                    isActive = false
                )
            )

            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                steps.forEachIndexed { index, step ->
                    Column {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            // Timeline marker
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(
                                        if (step.isCompleted || step.isActive) GreenTop else Color.LightGray
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                if (step.isCompleted) {
                                    Icon(
                                        Icons.Filled.Check,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .background(Color.White, RoundedCornerShape(4.dp))
                                    )
                                }
                            }

                            // Timeline content
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                Text(
                                    step.title,
                                    fontWeight = FontWeight.Medium,
                                    color = if (step.isCompleted || step.isActive) Color.Black else Color.Gray
                                )
                                Text(
                                    step.subtitle,
                                    fontSize = 12.sp,
                                    color = Color.Gray
                                )
                            }
                        }

                        // Line connector
                        if (index < steps.size - 1) {
                            Box(
                                modifier = Modifier
                                    .padding(start = 15.dp)
                                    .width(2.dp)
                                    .height(24.dp)
                                    .background(
                                        if (steps[index + 1].isCompleted || steps[index + 1].isActive) GreenTop else Color.LightGray
                                    )
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun OrderItemRow(item: OrderItemDto) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(item.name, fontWeight = FontWeight.Medium, fontSize = 13.sp)
            Text("x${item.quantity}", fontSize = 11.sp, color = Color.Gray)
        }
        Text(
            PriceUtils.formatPrice(item.price * item.quantity),
            fontWeight = FontWeight.Bold,
            color = GreenTop
        )
    }
}

@Composable
private fun PricingRow(
    label: String,
    value: String,
    isTotal: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            label,
            fontSize = if (isTotal) 15.sp else 13.sp,
            fontWeight = if (isTotal) FontWeight.Bold else FontWeight.Normal,
            color = if (isTotal) Color.Black else Color.Gray
        )
        Text(
            value,
            fontSize = if (isTotal) 16.sp else 13.sp,
            fontWeight = if (isTotal) FontWeight.Bold else FontWeight.Medium,
            color = if (isTotal) GreenTop else Color.Black
        )
    }
}

@Composable
private fun RewardPointsCard(points: Int) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Brush.horizontalGradient(
            listOf(Color(0xFFFFF3E0), Color(0xFFFFE0B2))
        ).let {
            Color(0xFFFFF3E0)
        },
        shape = RoundedCornerShape(12.dp),
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    "Điểm thưởng",
                    fontSize = 12.sp,
                    color = Color(0xFF825400)
                )
                Text(
                    "$points điểm",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFE65100)
                )
            }
            Icon(
                Icons.Filled.LocalFireDepartment,
                contentDescription = null,
                tint = Color(0xFFFF6F00),
                modifier = Modifier.size(40.dp)
            )
        }
    }
}

@Composable
private fun ActionButtons(
    onTrackOrderClick: () -> Unit,
    onShoppingContinueClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
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
                onClick = onTrackOrderClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = GreenTop),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Outlined.LocalShipping, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text("Theo dõi đơn hàng", color = Color.White, fontWeight = FontWeight.SemiBold)
            }

            OutlinedButton(
                onClick = onShoppingContinueClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Outlined.ShoppingCart, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text("Tiếp tục mua sắm", color = GreenTop, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

private data class TimelineStep(
    val title: String,
    val subtitle: String,
    val isCompleted: Boolean,
    val isActive: Boolean
)

@Preview
@Composable
fun OrderConfirmationScreenPreview() {
    OrderConfirmationScreen()
}
