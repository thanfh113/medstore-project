package com.example.nhathuoc.ui.screen

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.nhathuoc.data.model.CartItemDto
import com.example.nhathuoc.data.model.UiState
import com.example.nhathuoc.data.repository.CartRepository
import com.example.nhathuoc.ui.component.ErrorMessageCard
import com.example.nhathuoc.ui.component.EmptyStateDisplay
import com.example.nhathuoc.ui.component.SuccessMessageCard
import com.example.nhathuoc.ui.theme.GreenTop
import com.example.nhathuoc.ui.theme.GreenLight
import com.example.nhathuoc.ui.theme.BgColor
import com.example.nhathuoc.util.PriceUtils
import com.example.nhathuoc.viewmodel.CartViewModel
import com.example.nhathuoc.viewmodel.CartViewModelFactory

@Composable
fun CartScreen(
    modifier: Modifier = Modifier,
    navController: NavController? = null,
    viewModel: CartViewModel = viewModel(
        factory = CartViewModelFactory(CartRepository(null))
    )
) {
    val cartState by viewModel.cartState.collectAsState()
    val cartItems by viewModel.cartItems.collectAsState()
    val totalItems by viewModel.totalItems.collectAsState()
    val subtotal by viewModel.subtotal.collectAsState()
    val totalPrice by viewModel.totalPrice.collectAsState()
    val isEmpty by viewModel.isEmpty.collectAsState()
    val isLoadingCart by viewModel.isLoadingCart.collectAsState()
    val removeState by viewModel.removeFromCartState.collectAsState()

    var showRemoveConfirm by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(removeState) {
        if (removeState is UiState.Success) {
            viewModel.clearRemoveState()
        }
    }

    Column(
        modifier = modifier.fillMaxSize()
    ) {
        // Header
        HeaderCart(
            totalItems = totalItems,
            onBackClick = { navController?.popBackStack() }
        )

        // Content
        when {
            isLoadingCart -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = GreenTop)
                }
            }
            isEmpty -> {
                EmptyStateDisplay(
                    title = "Giỏ hàng trống",
                    message = "Thêm sản phẩm để bắt đầu mua sắm",
                    modifier = Modifier.fillMaxSize()
                )
            }
            cartState is UiState.Error -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    ErrorMessageCard((cartState as UiState.Error).message)
                    Button(
                        onClick = { viewModel.loadCart() },
                        modifier = Modifier
                            .padding(16.dp)
                            .align(Alignment.CenterHorizontally)
                    ) {
                        Text("Thử lại")
                    }
                }
            }
            else -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(BgColor)
                ) {
                    // Success message
                    if (removeState is UiState.Success) {
                        SuccessMessageCard(
                            "Đã xóa khỏi giỏ hàng",
                            modifier = Modifier.padding(12.dp)
                        )
                    }

                    // Cart items list
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentPadding = PaddingValues(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(
                            cartItems,
                            key = { it.id }
                        ) { item ->
                            CartItemCard(
                                item = item,
                                onQuantityChange = { newQty ->
                                    viewModel.updateQuantity(item.id, newQty)
                                },
                                onRemove = {
                                    showRemoveConfirm = item.id
                                }
                            )
                        }
                    }

                    // Summary and checkout
                    CartSummary(
                        subtotal = subtotal,
                        totalPrice = totalPrice,
                        onCheckoutClick = {
                            navController?.navigate("CheckoutScreen")
                        },
                        onContinueShoppingClick = {
                            navController?.popBackStack()
                        }
                    )
                }
            }
        }
    }

    // Remove confirmation dialog
    if (showRemoveConfirm != null) {
        AlertDialog(
            onDismissRequest = { showRemoveConfirm = null },
            title = { Text("Xóa sản phẩm") },
            text = { Text("Bạn có chắc muốn xóa sản phẩm khỏi giỏ hàng?") },
            confirmButton = {
                Button(
                    onClick = {
                        showRemoveConfirm?.let { viewModel.removeFromCart(it) }
                        showRemoveConfirm = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53935))
                ) {
                    Text("Xóa", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showRemoveConfirm = null }) {
                    Text("Hủy")
                }
            }
        )
    }
}

@Composable
private fun HeaderCart(
    totalItems: Int,
    onBackClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Brush.verticalGradient(listOf(GreenTop, GreenLight)))
            .statusBarsPadding()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Icon(Icons.Filled.ArrowBack, contentDescription = null, tint = Color.White)
            }
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "Giỏ hàng",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                if (totalItems > 0) {
                    Text(
                        "$totalItems sản phẩm",
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                }
            }
            IconButton(onClick = {}) {
                Icon(Icons.Outlined.MoreVert, contentDescription = null, tint = Color.White)
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
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(),
        color = Color.White,
        shape = RoundedCornerShape(12.dp),
        shadowElevation = 2.dp
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Product image placeholder
                Surface(
                    modifier = Modifier.size(80.dp),
                    shape = RoundedCornerShape(8.dp),
                    color = BgColor
                ) {
                    Icon(
                        Icons.Outlined.MedicalServices,
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        tint = GreenTop
                    )
                }

                // Product info
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .align(Alignment.Top)
                ) {
                    Text(
                        item.productName ?: "Sản phẩm",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 2
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        PriceUtils.formatPrice(item.price ?: 0.0),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = GreenTop
                    )
                }

                // Delete button
                IconButton(
                    onClick = onRemove,
                    modifier = Modifier
                        .align(Alignment.Top)
                        .size(28.dp)
                ) {
                    Icon(
                        Icons.Filled.Delete,
                        contentDescription = "Xóa",
                        tint = Color(0xFFE53935),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            // Quantity selector
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(BgColor, RoundedCornerShape(8.dp))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Số lượng", fontSize = 12.sp, color = Color.Gray)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { onQuantityChange(maxOf(1, item.quantity - 1)) },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            Icons.Filled.Remove,
                            contentDescription = "Giảm",
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Text(
                        item.quantity.toString(),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.width(24.dp),
                        textAlign = Alignment.Center
                    )
                    IconButton(
                        onClick = { onQuantityChange(minOf(999, item.quantity + 1)) },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            Icons.Filled.Add,
                            contentDescription = "Thêm",
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                Text(
                    PriceUtils.formatPrice((item.price ?: 0.0) * item.quantity),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = GreenTop
                )
            }
        }
    }
}

@Composable
private fun CartSummary(
    subtotal: Double,
    totalPrice: Double,
    onCheckoutClick: () -> Unit,
    onContinueShoppingClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth(),
        color = Color.White,
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        shadowElevation = 8.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Divider()

            // Summary rows
            PriceSummaryRow("Tạm tính", PriceUtils.formatPrice(subtotal))
            PriceSummaryRow("Phí vận chuyển", "Tính khi thanh toán", Color.Gray)
            PriceSummaryRow("Giảm giá", "5%", Color(0xFF4CAF50))

            Divider()

            // Total
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Tổng cộng",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    PriceUtils.formatPrice(totalPrice),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = GreenTop
                )
            }

            // Buttons
            Button(
                onClick = onCheckoutClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = GreenTop),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Thanh toán", color = Color.White, fontWeight = FontWeight.SemiBold)
            }

            OutlinedButton(
                onClick = onContinueShoppingClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Tiếp tục mua", color = GreenTop, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun PriceSummaryRow(
    label: String,
    value: String,
    valueColor: Color = Color.Black
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontSize = 13.sp, color = Color.Gray)
        Text(value, fontSize = 13.sp, color = valueColor, fontWeight = FontWeight.Medium)
    }
}

@Preview
@Composable
fun CartScreenPreview() {
    CartScreen()
}
