package com.example.nhathuoc.ui.screen.miniscreen

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nhathuoc.ui.theme.GreenLight
private val GreenTop = Color(0xFF2E7D32)

private val GoldColor = Color(0xFFFFAB00)
private val GreenOk   = Color(0xFF2E7D32)

// ── Data ──────────────────────────────────────────────────────────────
data class OrderProduct(
    val name: String,
    val price: String,
    val quantity: Int,
    val unit: String,
    val icon: ImageVector,
    val iconTint: Color,
    val iconBg: Color
)

data class OrderDetail(
    val orderId: String,
    val date: String,
    val status: String,                     // "Đã giao", "Đang giao", ...
    val pickupMethod: String,               // "Nhận tại cửa hàng" / "Giao tận nơi"
    val receiverName: String,
    val receiverPhone: String,
    val address: String,
    val paymentMethod: String,
    val isPaid: Boolean,
    val products: List<OrderProduct>,
    val subtotal: String,
    val discount: String,
    val voucher: String,
    val shippingFee: String,
    val total: String,
    val rewardPoints: Int
)

// ── Sample ─────────────────────────────────────────────────────────────
val sampleOrderDetail = OrderDetail(
    orderId       = "4497026",
    date          = "28/10/2025",
    status        = "Đã giao",
    pickupMethod  = "Nhận tại cửa hàng",
    receiverName  = "b bào ngọc",
    receiverPhone = "0329 645 776",
    address       = "Nhà thuốc Long Châu 1-2-3-4 Chợ Yên Xá, Phường Thanh Liệt, TP. Hà Nội",
    paymentMethod = "Thanh toán bằng tiền mặt",
    isPaid        = true,
    products      = listOf(
        OrderProduct(
            name     = "Kem bôi da Acyclovir Stella Cream (5g) điều trị nhiễm virus Herpes simplex",
            price    = "17.000đ",
            quantity = 1,
            unit     = "Tuýp",
            icon     = Icons.Outlined.MedicalServices,
            iconTint = Color(0xFFC2185B),
            iconBg   = Color(0xFFFCE4EC)
        )
    ),
    subtotal      = "17.000đ",
    discount      = "0đ",
    voucher       = "0đ",
    shippingFee   = "Miễn phí",
    total         = "17.000đ",
    rewardPoints  = 17
)

// ── Screen ────────────────────────────────────────────────────────────
@Composable
fun OrderDetailScreen(
    order: OrderDetail = sampleOrderDetail,
    onBack: () -> Unit = {},
    onSupport: (ChatProductContext) -> Unit = {},
    onReorder: () -> Unit = {}
) {
    val listState = rememberLazyListState()
    val clipboardManager = LocalClipboardManager.current

    // Collapse detection
    val scrollOffset by remember {
        derivedStateOf {
            listState.firstVisibleItemIndex * 1000 + listState.firstVisibleItemScrollOffset
        }
    }
    // 0f = top (expanded hero), 1f = collapsed (solid topbar)
    val collapseProgress by remember {
        derivedStateOf { (scrollOffset / 400f).coerceIn(0f, 1f) }
    }

    val topBarBgAlpha by animateFloatAsState(collapseProgress, tween(150))
    val heroAlpha     by animateFloatAsState(1f - collapseProgress, tween(150))
    val topBarHeight  by animateDpAsState(
        if (collapseProgress > 0.5f) 56.dp else 0.dp, tween(150)
    )

    Scaffold(
        containerColor = Color(0xFFF0F2F5),
        bottomBar = {
            OrderBottomBar(
                onSupport = {
                    val first = order.products.firstOrNull()
                    if (first != null) {
                        onSupport(
                            ChatProductContext(
                                productName     = first.name,
                                brand           = "Long Châu",
                                origin          = "Việt Nam",
                                price           = first.price,
                                originalPrice   = first.price,
                                discountPercent = 0,
                                icon            = first.icon,
                                iconTint        = first.iconTint,
                                iconBg          = first.iconBg
                            )
                        )
                    }
                },
                onReorder = onReorder
            )
        }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {

            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {

                // ── Hero header (expands at top, fades on scroll) ──
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(260.dp)
                            .background(
                                Brush.verticalGradient(
                                    listOf(Color(0xFFDEEAFF), Color(0xFFF0F4FF), Color(0xFFF0F2F5))
                                )
                            )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .alpha(heroAlpha)
                                .padding(top = 56.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // Status title
                            Text(
                                text = order.status,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = GreenTop
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                "Rất vui vì bạn đã tin tưởng và mua hàng.",
                                fontSize = 13.sp,
                                color = Color(0xFF555555)
                            )
                            Spacer(Modifier.height(16.dp))

                            // Mascot illustration (icon-based)
                            Box(
                                modifier = Modifier
                                    .size(110.dp)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.radialGradient(listOf(Color(0xFFBDD8FF), Color(0xFFDEEAFF)))
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.LocalPharmacy,
                                    contentDescription = null,
                                    tint = GreenTop,
                                    modifier = Modifier.size(60.dp)
                                )
                                // Thumbs-up badge
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.BottomEnd)
                                        .size(28.dp)
                                        .clip(CircleShape)
                                        .background(GreenTop),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Filled.ThumbUp,
                                        null,
                                        tint = Color.White,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                        }

                        // Back button always visible
                        IconButton(
                            onClick = onBack,
                            modifier = Modifier
                                .statusBarsPadding()
                                .padding(4.dp)
                                .align(Alignment.TopStart)
                        ) {
                            Icon(
                                Icons.Filled.ArrowBackIosNew,
                                "Quay lại",
                                tint = GreenTop,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                // ── Order meta card ────────────────────────────────
                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = Color.White
                    ) {
                        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    "Đơn hàng ${order.date}",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1A1A1A)
                                )
                                Icon(
                                    Icons.Outlined.Edit,
                                    null,
                                    tint = GreenTop,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Spacer(Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    order.pickupMethod,
                                    fontSize = 13.sp,
                                    color = Color(0xFF555555)
                                )
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        "#${order.orderId}",
                                        fontSize = 13.sp,
                                        color = Color(0xFF333333),
                                        fontWeight = FontWeight.Medium
                                    )
                                    TextButton(
                                        onClick = {
                                            clipboardManager.setText(AnnotatedString(order.orderId))
                                        },
                                        contentPadding = PaddingValues(0.dp)
                                    ) {
                                        Text(
                                            "Sao chép",
                                            color = GreenTop,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // ── Receiver info ──────────────────────────────────
                item {
                    Surface(color = Color.White, modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
                            SectionLabel("Thông tin nhận hàng")
                            Spacer(Modifier.height(12.dp))

                            InfoDetailRow(
                                icon = Icons.Filled.Person,
                                iconTint = GreenTop,
                                label = "Thông tin người nhận",
                                value = "${order.receiverName}  •  ${order.receiverPhone}"
                            )

                            Spacer(Modifier.height(14.dp))
                            HorizontalDivider(color = Color(0xFFF0F0F0))
                            Spacer(Modifier.height(14.dp))

                            InfoDetailRow(
                                icon = Icons.Filled.LocationOn,
                                iconTint = GreenTop,
                                label = "Nhận hàng tại",
                                value = order.address
                            )
                        }
                    }
                }

                // ── Payment method ─────────────────────────────────
                item {
                    Surface(color = Color.White, modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                SectionLabel("Phương thức thanh toán")
                                if (order.isPaid) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            Icons.Filled.CheckCircle,
                                            null,
                                            tint = GreenOk,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Text(
                                            "Đã thanh toán",
                                            fontSize = 13.sp,
                                            color = GreenOk,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }
                            }
                            Spacer(Modifier.height(12.dp))

                            // Payment icon + label
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = Color(0xFFF5F5F5),
                                    modifier = Modifier.size(44.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            Icons.Outlined.Payments,
                                            null,
                                            tint = GreenTop,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                }
                                Text(
                                    order.paymentMethod,
                                    fontSize = 14.sp,
                                    color = Color(0xFF1A1A1A)
                                )
                            }
                        }
                    }
                }

                // ── Product list ───────────────────────────────────
                item {
                    Surface(color = Color.White, modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
                            SectionLabel("Danh sách sản phẩm")
                            Spacer(Modifier.height(12.dp))

                            order.products.forEach { product ->
                                OrderProductRow(product = product)
                                if (product != order.products.last()) {
                                    HorizontalDivider(
                                        color = Color(0xFFF0F0F0),
                                        modifier = Modifier.padding(vertical = 10.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // ── Payment summary ────────────────────────────────
                item {
                    Surface(color = Color.White, modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
                            SectionLabel("Thông tin thanh toán")
                            Spacer(Modifier.height(12.dp))

                            PaymentRow("Tổng tiền",         order.subtotal,    Color(0xFF1A1A1A))
                            PaymentRow("Giảm giá trực tiếp", order.discount,   Color(0xFFFF6D00))
                            PaymentRow("Giảm giá voucher",   order.voucher,    Color(0xFFFF6D00))
                            PaymentRow("Phí vận chuyển",     order.shippingFee, GreenTop)

                            HorizontalDivider(
                                color = Color(0xFFF0F0F0),
                                modifier = Modifier.padding(vertical = 10.dp)
                            )

                            // Total
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "Thành tiền",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1A1A1A)
                                )
                                Text(
                                    order.total,
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = GreenTop
                                )
                            }

                            Spacer(Modifier.height(8.dp))

                            // Reward points
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "Điểm thưởng",
                                    fontSize = 14.sp,
                                    color = Color(0xFF555555)
                                )
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(20.dp)
                                            .clip(CircleShape)
                                            .background(GoldColor),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            "F",
                                            color = Color.White,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Text(
                                        "+${order.rewardPoints}",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = GoldColor
                                    )
                                }
                            }
                        }
                    }
                }

                item { Spacer(Modifier.height(8.dp)) }
            }

            // ── Collapsing solid TopBar (appears on scroll) ────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .alpha(topBarBgAlpha)
                    .background(Brush.horizontalGradient(listOf(GreenTop, GreenLight)))
                    .statusBarsPadding()
                    .height(56.dp),
                contentAlignment = Alignment.Center
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .padding(start = 4.dp)
                ) {
                    Icon(
                        Icons.Filled.ArrowBackIosNew,
                        "Quay lại",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Text(
                    order.status,
                    color = Color.White,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

// ── Sub-composables ───────────────────────────────────────────────────

@Composable
private fun SectionLabel(text: String) {
    Text(text, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A1A1A))
}

@Composable
private fun InfoDetailRow(
    icon: ImageVector,
    iconTint: Color,
    label: String,
    value: String
) {
    Row(
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(icon, null, tint = iconTint, modifier = Modifier.size(22.dp).padding(top = 2.dp))
        Column {
            Text(label, fontSize = 12.sp, color = Color.Gray)
            Spacer(Modifier.height(3.dp))
            Text(value, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF1A1A1A))
        }
    }
}

@Composable
private fun OrderProductRow(product: OrderProduct) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Product icon
        Box(
            modifier = Modifier
                .size(70.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(product.iconBg),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                product.icon,
                null,
                tint = product.iconTint,
                modifier = Modifier.size(36.dp)
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                product.name,
                fontSize = 13.sp,
                color = Color(0xFF1A1A1A),
                lineHeight = 18.sp
            )
            Spacer(Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    product.price,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1A1A)
                )
                Text(
                    "x${product.quantity} ${product.unit}",
                    fontSize = 13.sp,
                    color = Color.Gray
                )
            }
            Spacer(Modifier.height(6.dp))
            TextButton(
                onClick = {},
                contentPadding = PaddingValues(0.dp)
            ) {
                Text(
                    "Xem cách dùng và bảo quản",
                    fontSize = 13.sp,
                    color = GreenTop,
                    fontWeight = FontWeight.Medium
                )
                Spacer(Modifier.width(2.dp))
                Icon(
                    Icons.Filled.ChevronRight,
                    null,
                    tint = GreenTop,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
private fun PaymentRow(label: String, value: String, valueColor: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontSize = 14.sp, color = Color(0xFF555555))
        Text(value, fontSize = 14.sp, color = valueColor, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun OrderBottomBar(
    onSupport: () -> Unit,
    onReorder: () -> Unit
) {
    Surface(color = Color.White, shadowElevation = 8.dp) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onSupport,
                modifier = Modifier.weight(1f).height(50.dp),
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = GreenTop),
                border = ButtonDefaults.outlinedButtonBorder
            ) {
                Text("Hỗ trợ", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
            }
            Button(
                onClick = onReorder,
                modifier = Modifier.weight(2f).height(50.dp),
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(containerColor = GreenTop)
            ) {
                Text("Mua lại", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
    }
}