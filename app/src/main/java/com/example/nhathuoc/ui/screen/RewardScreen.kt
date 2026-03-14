package com.example.nhathuoc.ui.screen

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import kotlin.math.roundToInt
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nhathuoc.ui.theme.GreenLight
import com.example.nhathuoc.ui.theme.NhathuocTheme
import kotlinx.coroutines.launch

// ── Colors ───────────────────────────────────────────────────────
private val GreenTop = Color(0xFF2E7D32)

private val GoldColor = Color(0xFFFFAB00)

// ── Data ─────────────────────────────────────────────────────────
private val pointFilters = listOf("1.500 điểm", "3.000 điểm", "4.500 điểm", "6.000 điểm", "10.000 điểm")

data class RewardProduct(
    val id: Int,
    val icon: ImageVector,
    val iconTint: Color,
    val iconBg: Color,
    val name: String,
    val priceText: String,
    val pointCost: Int
)

private val rewardProducts = listOf(
    RewardProduct(1,  Icons.Outlined.Face,           Color(0xFFC2185B), Color(0xFFFCE4EC), "Mặt nạ dưỡng ẩm Hyaluronic Acid Deep Sea Water Mask Pack",  "1.000đ / Miếng", 1500),
    RewardProduct(2,  Icons.Outlined.Spa,            Color(0xFF00838F), Color(0xFFE0F7FA), "Mặt nạ dưỡng ẩm Super Aqua Mask Pack làm mềm mịn da",       "1.000đ / Miếng", 1500),
    RewardProduct(3,  Icons.Outlined.MedicalServices,Color(0xFF1565C0), Color(0xFFE3F2FD), "Vitamin C 500mg tăng sức đề kháng hộp 100 viên",            "2.000đ / Hộp",   3000),
    RewardProduct(4,  Icons.Outlined.LocalFlorist,   Color(0xFF2E7D32), Color(0xFFE8F5E9), "Trà thảo mộc hỗ trợ giảm cân Detox Green Tea 20 túi",       "2.000đ / Hộp",   3000),
    RewardProduct(5,  Icons.Outlined.Opacity,        Color(0xFF6A1B9A), Color(0xFFF3E5F5), "Serum dưỡng trắng da Niacinamide 10% + Zinc 1%",             "3.000đ / Chai",  4500),
    RewardProduct(6,  Icons.Outlined.ChildCare,      Color(0xFF00838F), Color(0xFFE0F7FA), "Siro bổ sung Canxi & Vitamin D3 cho bé từ 1 tuổi 100ml",     "3.000đ / Chai",  4500),
    RewardProduct(7,  Icons.Outlined.Science,        Color(0xFF1565C0), Color(0xFFE3F2FD), "Kẽm hữu cơ Zinc Gluconate tăng miễn dịch 60 viên",           "4.000đ / Hộp",   6000),
    RewardProduct(8,  Icons.Outlined.CleanHands,     Color(0xFF0277BD), Color(0xFFE1F5FE), "Kem dưỡng tay Neutrogena Norwegian Formula 50g",             "4.000đ / Tuýp",  6000),
    RewardProduct(9,  Icons.Outlined.Vaccines,       Color(0xFF2E7D32), Color(0xFFE8F5E9), "Men vi sinh Probiotic hỗ trợ hệ tiêu hóa 30 gói",           "4.000đ / Hộp",   6000),
    RewardProduct(10, Icons.Outlined.MonitorHeart,   Color(0xFFE53935), Color(0xFFFFEBEE), "Omega-3 hỗ trợ tim mạch DHA EPA 1000mg 100 viên",           "5.000đ / Hộp",   7500),
    RewardProduct(11, Icons.Outlined.RemoveRedEye,   Color(0xFF0277BD), Color(0xFFE1F5FE), "Nhỏ mắt Rohto Dry Aid dưỡng ẩm mắt khô 10ml",              "5.000đ / Chai",  7500),
    RewardProduct(12, Icons.Outlined.Psychology,     Color(0xFF5C6BC0), Color(0xFFE8EAF6), "Ginkgo Biloba hỗ trợ tuần hoàn não 120mg 60 viên",          "5.000đ / Hộp",   7500),
    RewardProduct(13, Icons.Outlined.Restaurant,     Color(0xFFEF6C00), Color(0xFFFFF3E0), "Enzyme tiêu hóa Pancreatin hỗ trợ hấp thu dinh dưỡng",      "6.000đ / Hộp",   9000),
    RewardProduct(14, Icons.Outlined.Shield,         Color(0xFF1565C0), Color(0xFFE3F2FD), "Echinacea tăng cường miễn dịch chiết xuất cỏ thảo mộc",     "6.000đ / Hộp",   9000),
    RewardProduct(15, Icons.Outlined.Favorite,       Color(0xFFE53935), Color(0xFFFFEBEE), "Coenzyme Q10 hỗ trợ tim mạch và chống oxy hóa 100mg",       "7.000đ / Hộp",  10000),
    RewardProduct(16, Icons.Outlined.Air,            Color(0xFF00ACC1), Color(0xFFE0F7FA), "Xịt mũi muối sinh lý NaCl 0.9% dành cho bé 100ml",          "3.000đ / Chai",  4500),
    RewardProduct(17, Icons.Outlined.Spa,            Color(0xFF7B1FA2), Color(0xFFF3E5F5), "Collagen Peptide làm đẹp da chống lão hóa Nhật Bản 30 gói", "8.000đ / Hộp",  12000),
    RewardProduct(18, Icons.Outlined.LocalPharmacy,  Color(0xFF1565C0), Color(0xFFE3F2FD), "Viên uống đẹp da trắng sáng Glutathione 500mg 60 viên",     "8.000đ / Hộp",  12000),
    RewardProduct(19, Icons.Outlined.HealthAndSafety,Color(0xFF2E7D32), Color(0xFFE8F5E9), "Bộ kit kiểm tra đường huyết tại nhà (10 que thử)",          "7.000đ / Bộ",   10000),
    RewardProduct(20, Icons.Outlined.Biotech,        Color(0xFF6A1B9A), Color(0xFFF3E5F5), "Thực phẩm bảo vệ sức khỏe TPCN hỗ trợ gan Silymarin 80mg", "6.000đ / Hộp",   9000),
)

// ── Screen ───────────────────────────────────────────────────────
@Composable
fun RewardScreen(modifier: Modifier = Modifier, onShopNow: () -> Unit = {}) {
    var selectedFilter by remember { mutableStateOf("1.500 điểm") }
    var showBanner by remember { mutableStateOf(true) }
    var showRedeemSheet by remember { mutableStateOf(false) }
    var redeemProduct by remember { mutableStateOf<RewardProduct?>(null) }

    val filteredProducts = remember(selectedFilter) {
        val pts = selectedFilter.replace(".", "").replace(" điểm", "").trim().toIntOrNull() ?: 0
        rewardProducts.filter { it.pointCost <= pts + 1500 }
    }

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF5F7FA)),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            // ── Header xanh ─────────────────────────────────────────
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Brush.verticalGradient(listOf(GreenTop, GreenLight)))
                        .padding(top = 16.dp, start = 16.dp, end = 16.dp, bottom = 32.dp)
                ) {
                    Column {
                        // Tab "Quà của tôi" + "Lịch sử"
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            HeaderTab(icon = Icons.Outlined.CardGiftcard, label = "Quà của tôi", selected = true)
                            HeaderTab(icon = Icons.Outlined.History,      label = "Lịch sử",     selected = false)
                        }

                        Spacer(Modifier.height(20.dp))

                        // Points + jar illustration
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Điểm thưởng", color = Color.White.copy(alpha = 0.85f), fontSize = 13.sp)
                                Spacer(Modifier.height(4.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Filled.EmojiEvents, null, tint = GoldColor, modifier = Modifier.size(28.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text("246", color = GoldColor, fontSize = 36.sp, fontWeight = FontWeight.ExtraBold)
                                }
                                Spacer(Modifier.height(6.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.clickable { }
                                ) {
                                    Text(
                                        "Xem thể lệ",
                                        color = Color.White,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        textDecoration = TextDecoration.Underline
                                    )
                                    Spacer(Modifier.width(2.dp))
                                    Icon(Icons.Outlined.ChevronRight, null, tint = Color.White, modifier = Modifier.size(16.dp))
                                }
                            }

                            // Coin jar illustration with icons
                            Box(
                                modifier = Modifier
                                    .size(110.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.12f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(Icons.Filled.Savings, null, tint = GoldColor, modifier = Modifier.size(52.dp))
                                    Spacer(Modifier.height(2.dp))
                                    Row {
                                        repeat(3) {
                                            Icon(Icons.Filled.MonetizationOn, null, tint = GoldColor, modifier = Modifier.size(16.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // ── Banner khuyến khích ──────────────────────────────────
            if (showBanner) {
                item {
                    Box(modifier = Modifier.padding(horizontal = 16.dp).offset(y = (-16).dp)) {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = Color.White,
                            shadowElevation = 4.dp,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        "Mua sắm để tích điểm và đổi quà tại Nhà thuốc Hà Tiến Thành nhé!",
                                        fontSize = 13.sp,
                                        color = Color(0xFF1A1A1A),
                                        lineHeight = 18.sp
                                    )
                                    Spacer(Modifier.height(10.dp))
                                    Button(
                                        onClick = onShopNow,
                                        shape = RoundedCornerShape(50),
                                        colors = ButtonDefaults.buttonColors(containerColor = GreenTop),
                                        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 8.dp)
                                    ) {
                                        Text("Mua sắm ngay", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                    }
                                }
                                Spacer(Modifier.width(12.dp))
                                // Mascot placeholder
                                Box(
                                    modifier = Modifier
                                        .size(72.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFE3F2FD)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Filled.FavoriteBorder, null, tint = GreenTop, modifier = Modifier.size(36.dp))
                                }
                            }
                        }
                        // Close button
                        IconButton(
                            onClick = { showBanner = false },
                            modifier = Modifier.align(Alignment.TopEnd).size(32.dp)
                        ) {
                            Icon(Icons.Filled.Close, null, tint = Color.Gray, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }

            // ── Quà tặng header + filter chips ───────────────────────
            item {
                Column(
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .padding(top = if (showBanner) 0.dp else 8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Quà tặng", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A1A1A))
                            Text("Tích điểm đổi quà với giá 1.000 đồng.", fontSize = 12.sp, color = Color.Gray)
                        }
                        TextButton(onClick = {}) {
                            Text("Xem tất cả", color = GreenTop, fontSize = 13.sp)
                        }
                    }

                    Spacer(Modifier.height(10.dp))

                    // Filter chips
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        pointFilters.forEach { filter ->
                            val isSelected = selectedFilter == filter
                            val bgColor by animateColorAsState(
                                if (isSelected) GreenTop else Color.White, tween(200)
                            )
                            val textColor by animateColorAsState(
                                if (isSelected) Color.White else Color(0xFF333333), tween(200)
                            )
                            Surface(
                                onClick = { selectedFilter = filter },
                                shape = RoundedCornerShape(50),
                                color = bgColor,
                                border = if (isSelected) null else androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFDDDDDD)),
                                shadowElevation = if (isSelected) 2.dp else 0.dp
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    if (isSelected) {
                                        Icon(Icons.Filled.Check, null, tint = Color.White, modifier = Modifier.size(14.dp))
                                        Spacer(Modifier.width(4.dp))
                                    }
                                    Text(filter, color = textColor, fontSize = 13.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
                                }
                            }
                        }
                    }
                }
            }

            // ── Product grid ─────────────────────────────────────────
            item {
                Spacer(Modifier.height(12.dp))
                // 2-column grid inside LazyColumn item
                val rows = filteredProducts.chunked(2)
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    rows.forEach { rowItems ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            rowItems.forEach { product ->
                                RewardProductCard(
                                    product = product,
                                    onRedeem = {
                                        redeemProduct = product
                                        showRedeemSheet = true
                                    },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            if (rowItems.size == 1) Spacer(Modifier.weight(1f))
                        }
                    }
                }
            }
        } // end LazyColumn

        RedeemBottomSheet(
            visible    = showRedeemSheet,
            product    = redeemProduct,
            userPoints = 246,
            onDismiss  = { showRedeemSheet = false }
        )
    } // end Box
}

// ── Sub-composables ───────────────────────────────────────────────
@Composable
private fun HeaderTab(icon: ImageVector, label: String, selected: Boolean) {
    Surface(
        shape = RoundedCornerShape(50),
        color = if (selected) Color.White else Color.White.copy(alpha = 0.2f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, null, tint = if (selected) GreenTop else Color.White, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(6.dp))
            Text(label, color = if (selected) GreenTop else Color.White,
                fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun RewardProductCard(
    product: RewardProduct,
    onRedeem: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        shadowElevation = 2.dp,
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Icon image area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(product.iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = product.icon,
                    contentDescription = product.name,
                    tint = product.iconTint,
                    modifier = Modifier.size(56.dp)
                )
            }

            Spacer(Modifier.height(8.dp))

            // Name
            Text(
                text = product.name,
                fontSize = 12.sp,
                color = Color(0xFF1A1A1A),
                fontWeight = FontWeight.Medium,
                lineHeight = 16.sp,
                maxLines = 3,
                modifier = Modifier.heightIn(min = 48.dp)
            )

            Spacer(Modifier.height(6.dp))

            // Price
            Text(
                text = product.priceText,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = GreenTop
            )

            Spacer(Modifier.height(4.dp))

            // Point cost
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.EmojiEvents, null, tint = GoldColor, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text(
                    text = "%,d".format(product.pointCost).replace(",", "."),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = GoldColor
                )
            }

            Spacer(Modifier.height(10.dp))

            // Exchange button
            Button(
                onClick = onRedeem,
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(containerColor = GreenTop),
                contentPadding = PaddingValues(vertical = 8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Đổi ngay", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun RewardScreenPreview() {
    NhathuocTheme { RewardScreen() }
}
// ── Redeem Bottom Sheet ───────────────────────────────────────────
@Composable
fun RedeemBottomSheet(
    visible: Boolean,
    product: RewardProduct?,
    userPoints: Int = 246,
    onDismiss: () -> Unit = {}
) {
    val sheetHeightDp = 430.dp
    val density       = LocalDensity.current
    val sheetHeightPx = with(density) { sheetHeightDp.toPx() }
    val offsetY       = remember { Animatable(sheetHeightPx) }
    val scrimAlpha    = remember { Animatable(0f) }
    val scope         = rememberCoroutineScope()
    var quantity      by remember { mutableIntStateOf(1) }

    // Reset quantity when a new product is opened
    LaunchedEffect(product?.id) { quantity = 1 }

    LaunchedEffect(visible) {
        if (visible) {
            scope.launch { launch { offsetY.animateTo(0f,            tween(320, easing = FastOutSlowInEasing))  }
                launch { scrimAlpha.animateTo(0.45f,      tween(280)) } }
        } else {
            scope.launch { launch { offsetY.animateTo(sheetHeightPx, tween(260, easing = FastOutLinearInEasing)) }
                launch { scrimAlpha.animateTo(0f,          tween(240)) } }
        }
    }

    if (offsetY.value < sheetHeightPx || visible) {
        Box(Modifier.fillMaxSize()) {

            // Scrim
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = scrimAlpha.value))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        scope.launch {
                            launch { offsetY.animateTo(sheetHeightPx, tween(260, easing = FastOutLinearInEasing)) }
                            launch { scrimAlpha.animateTo(0f, tween(240)) }
                        }
                        onDismiss()
                    }
            )

            // Sheet
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(sheetHeightDp)
                    .align(Alignment.BottomCenter)
                    .offset { IntOffset(0, offsetY.value.roundToInt()) },
                shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
                color = Color.White,
                shadowElevation = 16.dp
            ) {
                val p = product ?: return@Surface
                val totalCost  = p.pointCost * quantity
                val hasEnough  = userPoints >= totalCost

                Column(Modifier.fillMaxSize()) {

                    // Handle bar
                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .padding(top = 10.dp)
                            .width(36.dp).height(4.dp)
                            .background(Color(0xFFDDDDDD), RoundedCornerShape(50))
                    )

                    // Title + X
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 12.dp)
                    ) {
                        Text(
                            "Đổi quà",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1A1A1A),
                            modifier = Modifier.align(Alignment.Center)
                        )
                        IconButton(
                            onClick = {
                                scope.launch {
                                    launch { offsetY.animateTo(sheetHeightPx, tween(260, easing = FastOutLinearInEasing)) }
                                    launch { scrimAlpha.animateTo(0f, tween(240)) }
                                }
                                onDismiss()
                            },
                            modifier = Modifier.align(Alignment.CenterEnd)
                        ) {
                            Icon(Icons.Filled.Close, null, tint = Color(0xFF555555), modifier = Modifier.size(22.dp))
                        }
                    }

                    HorizontalDivider(color = Color(0xFFF0F0F0))

                    // Product row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(p.iconBg),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(p.icon, null, tint = p.iconTint, modifier = Modifier.size(38.dp))
                        }
                        Text(
                            p.name,
                            fontSize = 14.sp,
                            color = Color(0xFF1A1A1A),
                            lineHeight = 20.sp,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    HorizontalDivider(color = Color(0xFFF0F0F0))

                    // Quantity row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Số lượng:", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A1A1A))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Surface(
                                onClick = { if (quantity > 1) quantity-- },
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFFF0F0F0),
                                modifier = Modifier.size(34.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(Icons.Filled.Remove, null, tint = Color(0xFF333333), modifier = Modifier.size(18.dp))
                                }
                            }
                            Text(quantity.toString(), fontSize = 17.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A1A1A))
                            Surface(
                                onClick = { quantity++ },
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFFF0F0F0),
                                modifier = Modifier.size(34.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(Icons.Filled.Add, null, tint = Color(0xFF333333), modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }

                    HorizontalDivider(color = Color(0xFFF0F0F0))

                    // Points used row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Điểm sử dụng:", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A1A1A))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Box(
                                modifier = Modifier.size(20.dp).clip(CircleShape).background(GoldColor),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("F", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold)
                            }
                            Text(
                                "-${"%,d".format(totalCost).replace(",", ".")}",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = GoldColor
                            )
                        }
                    }

                    // Points available row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp)
                            .padding(bottom = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Điểm hiện có:", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A1A1A))
                        Text(userPoints.toString(), fontSize = 15.sp, color = Color(0xFF555555))
                    }

                    // Not enough warning
                    if (!hasEnough) {
                        Row(
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Filled.Error, null, tint = Color(0xFFE53935), modifier = Modifier.size(16.dp))
                            Text("Không đủ điểm thưởng.", fontSize = 13.sp, color = Color(0xFFE53935))
                        }
                    }

                    Spacer(Modifier.weight(1f))

                    // Confirm button
                    Button(
                        onClick = { if (hasEnough) onDismiss() },
                        enabled = hasEnough,
                        shape = RoundedCornerShape(50),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = GreenTop,
                            disabledContainerColor = Color(0xFFDDDDDD)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp)
                            .padding(bottom = 20.dp)
                            .height(52.dp)
                    ) {
                        Text(
                            "Xác nhận",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (hasEnough) Color.White else Color(0xFF888888)
                        )
                    }
                }
            }
        }
    }
}