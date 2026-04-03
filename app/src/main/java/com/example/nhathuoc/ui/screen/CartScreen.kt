package com.example.nhathuoc.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.nhathuoc.data.model.CartDto
import com.example.nhathuoc.data.model.CartItemDto
import com.example.nhathuoc.data.model.UiState
import com.example.nhathuoc.data.repository.CartRepository
import com.example.nhathuoc.ui.theme.GreenLight
import com.example.nhathuoc.ui.theme.GreenTop
import com.example.nhathuoc.viewmodel.CartViewModel
import com.example.nhathuoc.viewmodel.CartViewModelFactory
import android.util.Log

private val GreenTopColor = Color(0xFF2E7D32)
private val BgColor = Color(0xFFF5F7FA)

@Composable
fun CartScreen(
    modifier: Modifier = Modifier,
    navController: NavController? = null
) {
    val context = LocalContext.current
    val cartViewModel: CartViewModel = viewModel(
        factory = CartViewModelFactory(
            CartRepository()
        )
    )

    var showRemoveDialog by remember { mutableStateOf(false) }
    var selectedItemToRemove by remember { mutableStateOf<String?>(null) }

    val cartState by cartViewModel.cartState.collectAsState()
    val removeState by cartViewModel.removeItemState.collectAsState()

    Log.d("CartScreen", "🛒 CartScreen rendered, cartState=$cartState")

    // Load cart on screen enter
    LaunchedEffect(Unit) {
        Log.d("CartScreen", "📦 Loading cart...")
        cartViewModel.loadCart()
    }

    // Show remove confirmation dialog
    if (showRemoveDialog && selectedItemToRemove != null) {
        AlertDialog(
            onDismissRequest = { showRemoveDialog = false },
            title = { Text("Xóa khỏi giỏ hàng?") },
            text = { Text("Bạn có chắc muốn xóa sản phẩm này?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        cartViewModel.removeCartItem(selectedItemToRemove!!)
                        showRemoveDialog = false
                        selectedItemToRemove = null
                    }
                ) {
                    Text("Xóa", color = Color(0xFFE53935))
                }
            },
            dismissButton = {
                TextButton(onClick = { showRemoveDialog = false }) {
                    Text("Hủy")
                }
            }
        )
    }

    Column(modifier = modifier.fillMaxSize()) {
        // ── TopAppBar ─────────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.horizontalGradient(listOf(GreenTopColor, GreenLight)))
                .statusBarsPadding()
                .height(56.dp),
            contentAlignment = Alignment.Center
        ) {
            IconButton(
                onClick = { navController?.popBackStack() },
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = 4.dp)
            ) {
                Icon(Icons.Filled.ArrowBackIos, "Quay lại", tint = Color.White, modifier = Modifier.size(20.dp))
            }
            Text("Giỏ hàng", color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
        }

        // ── Body ───────────────────────────────────────────────────
        when (cartState) {
            is UiState.Loading -> {
                // Loading state
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(color = GreenTopColor)
                        Spacer(Modifier.height(16.dp))
                        Text("Đang tải giỏ hàng...", fontSize = 14.sp, color = Color.Gray)
                    }
                }
            }

            is UiState.Error -> {
                // Error state
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .background(BgColor),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Icon(
                            Icons.Outlined.ErrorOutline,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = Color(0xFFE53935)
                        )
                        Spacer(Modifier.height(16.dp))
                        Text(
                            "Lỗi tải giỏ hàng",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF1A1A1A)
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            (cartState as UiState.Error).message,
                            fontSize = 13.sp,
                            color = Color.Gray,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Spacer(Modifier.height(24.dp))
                        Button(
                            onClick = { cartViewModel.loadCart() },
                            colors = ButtonDefaults.buttonColors(containerColor = GreenTopColor)
                        ) {
                            Text("Thử lại")
                        }
                    }
                }
            }

            is UiState.Success -> {
                val cart = (cartState as UiState.Success<CartDto>).data
                Log.d("CartScreen", "✅ Cart loaded: ${cart.items.size} items, total=${cart.totalAmount}")

                if (cart.items.isEmpty()) {
                    // Empty cart state
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .background(BgColor),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.padding(24.dp)
                        ) {
                            Icon(
                                Icons.Outlined.ShoppingCart,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = Color(0xFFBDBDBD)
                            )
                            Spacer(Modifier.height(16.dp))
                            Text(
                                "Giỏ hàng trống",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF1A1A1A)
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                "Thêm sản phẩm để bắt đầu mua hàng",
                                fontSize = 13.sp,
                                color = Color.Gray
                            )
                        }
                    }
                } else {
                    // Cart items list
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .background(BgColor),
                        contentPadding = PaddingValues(vertical = 12.dp)
                    ) {
                        items(cart.items) { item ->
                            CartItemCard(
                                item = item,
                                onQuantityChange = { newQty ->
                                    cartViewModel.updateCartItem(item.id, newQty, item.unit)
                                },
                                onRemove = {
                                    selectedItemToRemove = item.id
                                    showRemoveDialog = true
                                }
                            )
                        }
                    }
                }

                // ── Bottom: Summary & Checkout ────────────────────
                if (cart.items.isNotEmpty()) {
                    CartSummary(
                        subtotal = cart.subtotal.toLong(),
                        discount = cart.discount.toLong(),
                        total = cart.totalAmount.toLong(),
                        onCheckout = {
                            Log.d("CartScreen", "💳 Proceeding to checkout...")
                            navController?.navigate("CheckoutScreen")
                        }
                    )
                }
            }

            else -> {
                // Idle state
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .background(BgColor)
                )
            }
        }
    }
}

@Composable
private fun CartItemCard(
    item: CartItemDto,
    onQuantityChange: (Int) -> Unit,
    onRemove: () -> Unit
) {
    Surface(
        color = Color.White,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Product icon/image
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFF5F5F5)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Outlined.MedicalServices,
                    contentDescription = null,
                    modifier = Modifier.size(40.dp),
                    tint = GreenTopColor
                )
            }

            // Product details
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Name & price
                Text(
                    item.product.name,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF1A1A1A),
                    maxLines = 2
                )

                Spacer(Modifier.height(4.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "${item.unitPrice.toLong()}đ",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = GreenTopColor
                    )
                }

                Spacer(Modifier.height(8.dp))

                // Quantity control & remove
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Quantity selector
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(4.dp))
                            .background(Color(0xFFFAFAFA)),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { if (item.quantity > 1) onQuantityChange(item.quantity - 1) },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(Icons.Filled.Remove, null, modifier = Modifier.size(14.dp))
                        }
                        Text("${item.quantity}", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        IconButton(
                            onClick = { onQuantityChange(item.quantity + 1) },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(Icons.Filled.Add, null, modifier = Modifier.size(14.dp))
                        }
                    }

                    // Remove button
                    IconButton(
                        onClick = onRemove,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            Icons.Outlined.Delete,
                            contentDescription = "Xóa",
                            modifier = Modifier.size(18.dp),
                            tint = Color(0xFFE53935)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CartSummary(
    subtotal: Long,
    discount: Long,
    total: Long,
    onCheckout: () -> Unit
) {
    Surface(
        color = Color.White,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Summary rows
            PriceRow("Tạm tính", subtotal)
            PriceRow("Giảm giá", discount)

            Divider(modifier = Modifier.padding(vertical = 8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Tổng cộng", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A1A1A))
                Text("${total}đ", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = GreenTopColor)
            }

            Spacer(Modifier.height(8.dp))

            // Checkout button
            Button(
                onClick = onCheckout,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = GreenTopColor),
                shape = RoundedCornerShape(24.dp)
            ) {
                Text(
                    "Tiến hành thanh toán",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
private fun PriceRow(label: String, value: Long) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontSize = 13.sp, color = Color.Gray)
        Text("${value}đ", fontSize = 13.sp, color = Color(0xFF1A1A1A))
    }
}