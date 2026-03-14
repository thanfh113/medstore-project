package com.example.nhathuoc.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.nhathuoc.ui.theme.NhathuocTheme
import com.example.nhathuoc.ui.theme.GreenLight
import com.example.nhathuoc.util.AuthenticatedAction
// ── Colors ────────────────────────────────────────────────────────
private val GreenTop   = Color(0xFF2E7D32)

private val GoldColor = Color(0xFFFFAB00)

// ── Data ──────────────────────────────────────────────────────────
data class CartItem(
    val id: Int,
    val icon: ImageVector,
    val iconTint: Color,
    val iconBg: Color,
    val name: String,
    val price: Int,
    var quantity: Int = 1,
    var unit: String = "Hộp",
    var selected: Boolean = false
)

data class RecentProduct(
    val icon: ImageVector,
    val iconTint: Color,
    val iconBg: Color,
    val name: String,
    val hasGift: Boolean = false
)

private val recentProducts = listOf(
    RecentProduct(Icons.Outlined.MedicalServices, Color(0xFF2E7D32), Color(0xFFE3F2FD), "Thuốc Natrofen"),
    RecentProduct(Icons.Outlined.Air,             Color(0xFF00838F), Color(0xFFE0F7FA), "Muối rửa mũi xoang", hasGift = true),
    RecentProduct(Icons.Outlined.HealthAndSafety, Color(0xFF2E7D32), Color(0xFFE8F5E9), "Viên nang cứng"),
    RecentProduct(Icons.Outlined.Science,         Color(0xFF6A1B9A), Color(0xFFF3E5F5), "Vitamin tổng hợp"),
    RecentProduct(Icons.Outlined.Favorite,        Color(0xFFE53935), Color(0xFFFFEBEE), "Omega-3 tim mạch"),
)

// ── Screen ────────────────────────────────────────────────────────
@Composable
fun CartScreen(
    modifier: Modifier = Modifier,
    navController: NavController = rememberNavController()
) {
    // State
    var cartItems by remember {
        mutableStateOf(
            listOf(
                CartItem(
                    id = 1,
                    icon = Icons.Outlined.Air,
                    iconTint = Color(0xFF00838F),
                    iconBg = Color(0xFFE0F7FA),
                    name = "Muối rửa mũi xoang Sinufresh Cát Linh (30 gói và 1 chai 180ml) giúp làm sạch, giảm ngạt mũi, sổ mũi",
                    price = 170_000,
                    quantity = 1,
                    unit = "Hộp",
                    selected = false
                )
            )
        )
    }
    var usePoints by remember { mutableStateOf(false) }
    val rewardPoints = 246
    val pointValue   = rewardPoints * 10  // 2.460đ

    val selectedItems  = cartItems.filter { it.selected }
    val subtotal       = selectedItems.sumOf { it.price * it.quantity }
    val pointDiscount  = if (usePoints && selectedItems.isNotEmpty()) pointValue else 0
    val total          = maxOf(0, subtotal - pointDiscount)
    val allSelected    = cartItems.isNotEmpty() && cartItems.all { it.selected }

    Column(modifier = modifier.fillMaxSize()) {

        // ── TopAppBar ─────────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.horizontalGradient(listOf(GreenTop, GreenLight)))
                .statusBarsPadding()
                .height(56.dp),
            contentAlignment = Alignment.Center
        ) {
            IconButton(
                onClick = {},
                modifier = Modifier.align(Alignment.CenterStart).padding(start = 4.dp)
            ) {
                Icon(Icons.Filled.ArrowBackIos, "Quay lại", tint = Color.White, modifier = Modifier.size(20.dp))
            }
            Text("Giỏ hàng", color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
        }

        // ── Scrollable body ───────────────────────────────────────
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .background(Color(0xFFF5F7FA))
        ) {

            // ── Select-all row ────────────────────────────────────
            Surface(color = Color.White) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable {
                            cartItems = cartItems.map { it.copy(selected = !allSelected) }
                        }
                    ) {
                        RadioButton(
                            selected = allSelected,
                            onClick = { cartItems = cartItems.map { it.copy(selected = !allSelected) } },
                            colors = RadioButtonDefaults.colors(selectedColor = GreenTop)
                        )
                        Text(
                            "Chọn tất cả (${cartItems.size})",
                            fontSize = 14.sp,
                            color = Color(0xFF1A1A1A)
                        )
                    }
                    TextButton(onClick = {}) {
                        Text("Tiếp tục mua sắm", color = GreenTop, fontSize = 13.sp)
                    }
                }
            }

            HorizontalDivider(color = Color(0xFFEEEEEE))

            // ── Cart items ────────────────────────────────────────
            Surface(color = Color.White) {
                Column {
                    cartItems.forEachIndexed { index, item ->
                        CartItemRow(
                            item = item,
                            onSelectToggle = {
                                cartItems = cartItems.toMutableList().also { list ->
                                    list[index] = list[index].copy(selected = !item.selected)
                                }
                            },
                            onIncrement = {
                                cartItems = cartItems.toMutableList().also { list ->
                                    list[index] = list[index].copy(quantity = item.quantity + 1)
                                }
                            },
                            onDecrement = {
                                if (item.quantity > 1) {
                                    cartItems = cartItems.toMutableList().also { list ->
                                        list[index] = list[index].copy(quantity = item.quantity - 1)
                                    }
                                }
                            },
                            onDelete = {
                                cartItems = cartItems.toMutableList().also { it.removeAt(index) }
                            },
                            onUnitChange = { newUnit ->
                                cartItems = cartItems.toMutableList().also { list ->
                                    list[index] = list[index].copy(unit = newUnit)
                                }
                            }
                        )
                        if (index < cartItems.lastIndex) {
                            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = Color(0xFFEEEEEE))
                        }
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            // ── Terms notice ──────────────────────────────────────
            Surface(color = Color.White) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        buildAnnotatedString {
                            append("Bằng việc tiến hành đặt mua hàng, bạn đồng ý với ")
                            withStyle(SpanStyle(color = GreenTop, textDecoration = TextDecoration.Underline, fontWeight = FontWeight.SemiBold)) {
                                append("Điều khoản dịch vụ")
                            }
                            append(" của Nhà thuốc FPT Long Châu")
                        },
                        fontSize = 12.sp,
                        color = Color.Gray,
                        lineHeight = 17.sp
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            // ── Sản phẩm vừa xem ─────────────────────────────────
            Surface(color = Color.White) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Sản phẩm vừa xem", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A1A1A))
                    Spacer(Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        recentProducts.forEach { product ->
                            RecentProductCard(product = product)
                        }
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            // ── Promo & points ────────────────────────────────────
            Surface(
                color = Color.White,
                shape = RoundedCornerShape(0.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // Áp dụng ưu đãi
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { }
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Outlined.LocalOffer, null, tint = GreenTop, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Áp dụng ưu đãi để được giảm giá", fontSize = 14.sp, color = GreenTop)
                        }
                        Icon(Icons.Outlined.ChevronRight, null, tint = GreenTop, modifier = Modifier.size(20.dp))
                    }

                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = Color(0xFFEEEEEE))

                    // Đổi điểm
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.EmojiEvents, null, tint = GoldColor, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(
                                buildAnnotatedString {
                                    append("Đổi ")
                                    withStyle(SpanStyle(color = GoldColor, fontWeight = FontWeight.Bold)) { append("$rewardPoints") }
                                    append(" điểm ")
                                    withStyle(SpanStyle(color = Color.Gray)) { append("(≈${"%,d".format(pointValue).replace(",", ".")}đ)") }
                                },
                                fontSize = 14.sp,
                                color = Color(0xFF1A1A1A)
                            )
                            Spacer(Modifier.width(4.dp))
                            Icon(Icons.Outlined.Info, null, tint = Color.Gray, modifier = Modifier.size(15.dp))
                        }
                        Switch(
                            checked = usePoints,
                            onCheckedChange = { usePoints = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = GreenTop)
                        )
                    }
                }
            }

            Spacer(Modifier.height(80.dp)) // space for bottom bar
        }

        // ── Bottom checkout bar ───────────────────────────────────
        Surface(
            color = Color.White,
            shadowElevation = 8.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Thành tiền", fontSize = 13.sp, color = Color.Gray)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = if (total == 0) "0đ" else "%,dđ".format(total).replace(",", "."),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = GreenTop
                        )
                        Spacer(Modifier.width(4.dp))
                        Icon(
                            if (selectedItems.isEmpty()) Icons.Filled.KeyboardArrowDown else Icons.Filled.KeyboardArrowUp,
                            null, tint = GreenTop, modifier = Modifier.size(18.dp)
                        )
                    }
                    if (usePoints && pointDiscount > 0) {
                        Text(
                            "Tiết kiệm ${"%,dđ".format(pointDiscount).replace(",", ".")} điểm thưởng",
                            fontSize = 11.sp, color = GoldColor
                        )
                    }
                }

                Button(
                    onClick = AuthenticatedAction(
                        context = LocalContext.current,
                        navController = navController
                    ) {
                        // Proceed to checkout - authenticated user only
                        // TODO: Navigate to checkout screen
                        // navController.navigate("CheckoutScreen")
                    },
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(containerColor = GreenTop),
                    contentPadding = PaddingValues(horizontal = 28.dp, vertical = 14.dp),
                    enabled = selectedItems.isNotEmpty()
                ) {
                    Text("Mua hàng", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// ── Cart item row ─────────────────────────────────────────────────
@Composable
private fun CartItemRow(
    item: CartItem,
    onSelectToggle: () -> Unit,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
    onDelete: () -> Unit,
    onUnitChange: (String) -> Unit
) {
    var showUnitMenu by remember { mutableStateOf(false) }
    val units = listOf("Viên", "Hộp", "Chai", "Gói", "Tuýp")

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 14.dp),
        verticalAlignment = Alignment.Top
    ) {
        // Checkbox
        RadioButton(
            selected = item.selected,
            onClick = onSelectToggle,
            colors = RadioButtonDefaults.colors(selectedColor = GreenTop),
            modifier = Modifier.padding(top = 8.dp)
        )

        Spacer(Modifier.width(4.dp))

        // Icon image
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(item.iconBg),
            contentAlignment = Alignment.Center
        ) {
            Icon(item.icon, null, tint = item.iconTint, modifier = Modifier.size(42.dp))
        }

        Spacer(Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            // Name
            Text(item.name, fontSize = 13.sp, color = Color(0xFF1A1A1A), lineHeight = 18.sp, maxLines = 3)
            Spacer(Modifier.height(6.dp))

            // Price
            Text(
                "%,d".format(item.price).replace(",", "."),
                fontSize = 16.sp,
                fontWeight = FontWeight.ExtraBold,
                color = GreenTop
            )
            Spacer(Modifier.height(8.dp))

            // Quantity + unit
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Delete
                OutlinedIconButton(
                    onClick = onDelete,
                    shape = CircleShape,
                    modifier = Modifier.size(34.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFDDDDDD))
                ) {
                    Icon(Icons.Outlined.DeleteOutline, "Xóa", tint = Color.Gray, modifier = Modifier.size(16.dp))
                }

                Spacer(Modifier.width(8.dp))

                // Decrement
                OutlinedIconButton(
                    onClick = onDecrement,
                    shape = CircleShape,
                    modifier = Modifier.size(34.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFDDDDDD))
                ) {
                    Icon(Icons.Filled.Remove, null, tint = Color(0xFF1A1A1A), modifier = Modifier.size(16.dp))
                }

                // Quantity
                Text(
                    "${item.quantity}",
                    modifier = Modifier.padding(horizontal = 12.dp),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF1A1A1A)
                )

                // Increment
                OutlinedIconButton(
                    onClick = onIncrement,
                    shape = CircleShape,
                    modifier = Modifier.size(34.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFDDDDDD))
                ) {
                    Icon(Icons.Filled.Add, null, tint = Color(0xFF1A1A1A), modifier = Modifier.size(16.dp))
                }

                Spacer(Modifier.width(10.dp))

                // Unit dropdown
                Box {
                    Surface(
                        onClick = { showUnitMenu = true },
                        shape = RoundedCornerShape(50),
                        color = Color.White,
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFDDDDDD))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(item.unit, fontSize = 13.sp, color = Color(0xFF1A1A1A))
                            Spacer(Modifier.width(2.dp))
                            Icon(Icons.Filled.KeyboardArrowDown, null, tint = Color.Gray, modifier = Modifier.size(16.dp))
                        }
                    }
                    DropdownMenu(
                        expanded = showUnitMenu,
                        onDismissRequest = { showUnitMenu = false }
                    ) {
                        units.forEach { u ->
                            DropdownMenuItem(
                                text = { Text(u) },
                                onClick = { onUnitChange(u); showUnitMenu = false },
                                leadingIcon = if (u == item.unit) ({
                                    Icon(Icons.Filled.Check, null, tint = GreenTop, modifier = Modifier.size(16.dp))
                                }) else null
                            )
                        }
                    }
                }
            }
        }
    }
}

// ── Recent product card ───────────────────────────────────────────
@Composable
private fun RecentProductCard(product: RecentProduct) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color.White,
        shadowElevation = 2.dp,
        modifier = Modifier.width(140.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)
                    .background(product.iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(product.icon, null, tint = product.iconTint, modifier = Modifier.size(52.dp))

                if (product.hasGift) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = Color(0xFFE53935),
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Outlined.CardGiftcard, null, tint = Color.White, modifier = Modifier.size(11.dp))
                            Spacer(Modifier.width(3.dp))
                            Text("Quà tặng", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
            Text(
                product.name,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF1A1A1A),
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                maxLines = 2,
                lineHeight = 16.sp
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun CartScreenPreview() {
    NhathuocTheme {
        CartScreen(navController = rememberNavController())
    }
}