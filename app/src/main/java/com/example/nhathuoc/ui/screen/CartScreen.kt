package com.example.nhathuoc.ui.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBackIos
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.MedicalServices
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.nhathuoc.data.model.CartItemDto
import com.example.nhathuoc.viewmodel.CartViewModel
import java.text.NumberFormat
import java.util.Locale

// ── Colors ────────────────────────────────────────────────────────────────
private val CartGreen = Color(0xFF2E7D32)
private val CartGreenLight = Color(0xFFE8F5E9)
private val CartBg = Color(0xFFF5F7FA)
private val CartRed = Color(0xFFE53935)

private val cartLocale: Locale = Locale.Builder().setLanguage("vi").setRegion("VN").build()
private fun Double.fmtVnd() = NumberFormat.getCurrencyInstance(cartLocale)
    .format(this).replace("₫", "đ")

// ── Screen ────────────────────────────────────────────────────────────────
@Composable
fun CartScreen(
    modifier: Modifier = Modifier,
    navController: NavController? = null
) {
    val viewModel: CartViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsState()

    // Remove confirmation dialog
    var itemToRemove by remember { mutableStateOf<CartItemDto?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }

    // Load cart on entry
    LaunchedEffect(Unit) {
        viewModel.loadCart()
    }

    // Show error in snackbar
    LaunchedEffect(uiState.error) {
        val err = uiState.error ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(err)
    }

    // Confirm remove dialog
    if (itemToRemove != null) {
        AlertDialog(
            onDismissRequest = { itemToRemove = null },
            containerColor = Color.White,
            shape = RoundedCornerShape(20.dp),
            icon = {
                Icon(Icons.Filled.Delete, null, tint = CartRed, modifier = Modifier.size(28.dp))
            },
            title = {
                Text(
                    "Xoá sản phẩm",
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp
                )
            },
            text = {
                Text(
                    "Bạn có chắc muốn xoá \"${itemToRemove!!.displayName}\" khỏi giỏ hàng?",
                    color = Color(0xFF555555),
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.removeCartItem(itemToRemove!!.id)
                        itemToRemove = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CartRed),
                    shape = RoundedCornerShape(50)
                ) {
                    Text("Xoá", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { itemToRemove = null }) {
                    Text("Huỷ", color = Color.Gray)
                }
            }
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = { CartTopBar(itemCount = uiState.totalItems, onBack = { navController?.popBackStack() }) },
        bottomBar = {
            CartBottomBar(
                totalAmount = uiState.totalPrice,
                itemCount = uiState.items.size,
                isLoading = uiState.isLoading,
                onCheckout = { navController?.navigate("CheckoutScreen") }
            )
        },
        containerColor = CartBg
    ) { padding ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                // ── Loading (first load, no items yet) ──────────────────
                uiState.isLoading && uiState.items.isEmpty() -> {
                    CartLoadingState()
                }
                // ── Error (no items loaded) ──────────────────────────────
                uiState.error != null && uiState.items.isEmpty() -> {
                    CartErrorState(
                        message = uiState.error!!,
                        onRetry = { viewModel.loadCart() }
                    )
                }
                // ── Empty ────────────────────────────────────────────────
                uiState.items.isEmpty() -> {
                    CartEmptyState(onShop = { navController?.navigate("HomeScreen") })
                }
                // ── Items ────────────────────────────────────────────────
                else -> {
                    LazyColumn(
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(uiState.items, key = { it.id }) { item ->
                            CartItemCard(
                                item = item,
                                onIncrease = {
                                    viewModel.updateQuantity(item.id, item.quantity + 1)
                                },
                                onDecrease = {
                                    if (item.quantity > 1) {
                                        viewModel.updateQuantity(item.id, item.quantity - 1)
                                    } else {
                                        itemToRemove = item
                                    }
                                },
                                onRemove = { itemToRemove = item }
                            )
                        }

                        // Summary card
                        item {
                            Spacer(Modifier.height(4.dp))
                            CartSummarySection(
                                subtotal = uiState.totalPrice,
                                itemCount = uiState.items.sumOf { it.quantity }
                            )
                        }
                    }

                    // Subtle loading overlay when updating qty/deleting
                    AnimatedVisibility(
                        visible = uiState.isLoading,
                        enter = fadeIn(tween(200)),
                        exit = fadeOut(tween(200)),
                        modifier = Modifier.align(Alignment.TopCenter)
                    ) {
                        LinearProgressIndicator(
                            modifier = Modifier.fillMaxWidth(),
                            color = CartGreen,
                            trackColor = CartGreenLight
                        )
                    }
                }
            }
        }
    }
}

// ── TopBar ─────────────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CartTopBar(itemCount: Int, onBack: () -> Unit) {
    TopAppBar(
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "Giỏ hàng",
                    fontWeight = FontWeight.Bold,
                    fontSize = 19.sp,
                    color = Color.White
                )
                if (itemCount > 0) {
                    Spacer(Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.25f))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            "$itemCount sản phẩm",
                            fontSize = 12.sp,
                            color = Color.White,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBackIos,
                    contentDescription = "Quay lai",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = CartGreen
        )
    )
}

// ── BottomBar ──────────────────────────────────────────────────────────────
@Composable
private fun CartBottomBar(
    totalAmount: Double,
    itemCount: Int,
    isLoading: Boolean,
    onCheckout: () -> Unit
) {
    Surface(
        color = Color.White,
        shadowElevation = 12.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Tong cong", fontSize = 12.sp, color = Color.Gray)
                Text(
                    totalAmount.fmtVnd(),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = CartGreen
                )
            }
            Button(
                onClick = onCheckout,
                enabled = itemCount > 0 && !isLoading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = CartGreen,
                    disabledContainerColor = Color(0xFFBDBDBD)
                ),
                shape = RoundedCornerShape(50),
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 14.dp)
            ) {
                Text(
                    "Dat hang",
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.width(6.dp))
                Icon(
                    Icons.AutoMirrored.Filled.ArrowForward,
                    null,
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

// ── CartItemCard ───────────────────────────────────────────────────────────
@Composable
private fun CartItemCard(
    item: CartItemDto,
    onIncrease: () -> Unit,
    onDecrease: () -> Unit,
    onRemove: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        shadowElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(CartGreenLight),
                    contentAlignment = Alignment.Center
                ) {
                    if (!item.imageUrl.isNullOrBlank()) {
                        AsyncImage(
                            model = item.imageUrl,
                            contentDescription = item.displayName,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(4.dp),
                            contentScale = ContentScale.Fit
                        )
                    } else {
                        Icon(
                            Icons.Outlined.MedicalServices,
                            contentDescription = null,
                            tint = CartGreen,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        item.displayName,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        color = Color(0xFF1A1A1A)
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        item.unit,
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        item.unitPrice.fmtVnd(),
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = CartGreen
                    )
                }
                // Delete button
                IconButton(
                    onClick = onRemove,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        Icons.Filled.Delete,
                        contentDescription = "Xoa",
                        tint = CartRed,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(Modifier.height(12.dp))
            HorizontalDivider(color = Color(0xFFF0F0F0))
            Spacer(Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Quantity stepper
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Decrease / Remove
                    Surface(
                        onClick = onDecrease,
                        shape = CircleShape,
                        color = if (item.quantity <= 1) Color(0xFFFFEBEE) else CartGreenLight,
                        modifier = Modifier.size(34.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                if (item.quantity <= 1) Icons.Filled.Delete else Icons.Filled.Remove,
                                contentDescription = null,
                                tint = if (item.quantity <= 1) CartRed else CartGreen,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Text(
                        "${item.quantity}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1A1A1A),
                        modifier = Modifier.widthIn(min = 32.dp),
                        textAlign = TextAlign.Center
                    )

                    // Increase
                    Surface(
                        onClick = onIncrease,
                        shape = CircleShape,
                        color = CartGreen,
                        modifier = Modifier.size(34.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Filled.Add,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                // Item total
                Text(
                    item.totalPrice.fmtVnd(),
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 15.sp,
                    color = CartGreen
                )
            }
        }
    }
}

// ── Summary section ────────────────────────────────────────────────────────
@Composable
private fun CartSummarySection(subtotal: Double, itemCount: Int) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        shadowElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Tóm tắt đơn hàng",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = Color(0xFF1A1A1A)
            )
            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("$itemCount sản phẩm", color = Color.Gray, fontSize = 13.sp)
                Text(subtotal.fmtVnd(), fontSize = 13.sp, color = Color(0xFF1A1A1A))
            }
            Spacer(Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Phí vận chuyển", color = Color.Gray, fontSize = 13.sp)
                Text("Sẽ tính khi thanh toán", fontSize = 12.sp, color = Color.Gray)
            }
            Spacer(Modifier.height(10.dp))
            HorizontalDivider(color = Color(0xFFF0F0F0))
            Spacer(Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Tạm tính", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Text(
                    subtotal.fmtVnd(),
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 16.sp,
                    color = CartGreen
                )
            }
        }
    }
}

// ── Loading state ──────────────────────────────────────────────────────────
@Composable
private fun CartLoadingState() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(color = CartGreen, strokeWidth = 3.dp)
            Spacer(Modifier.height(16.dp))
            Text("Đang tải giỏ hàng...", color = Color.Gray, fontSize = 14.sp)
        }
    }
}

// ── Error state ────────────────────────────────────────────────────────────
@Composable
private fun CartErrorState(message: String, onRetry: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                Icons.Outlined.ErrorOutline,
                contentDescription = null,
                tint = Color(0xFFBDBDBD),
                modifier = Modifier.size(72.dp)
            )
            Spacer(Modifier.height(16.dp))
            Text(
                "Không thể tải giỏ hàng",
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp,
                color = Color(0xFF1A1A1A)
            )
            Spacer(Modifier.height(8.dp))
            Text(
                message,
                color = Color.Gray,
                fontSize = 13.sp,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(20.dp))
            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(containerColor = CartGreen),
                shape = RoundedCornerShape(50)
            ) {
                Icon(Icons.Filled.Refresh, null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("Thử lại")
            }
        }
    }
}

// ── Empty state ────────────────────────────────────────────────────────────
@Composable
private fun CartEmptyState(onShop: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(listOf(CartGreenLight, Color(0xFFF5F7FA)))
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Outlined.ShoppingCart,
                    contentDescription = null,
                    tint = CartGreen,
                    modifier = Modifier.size(60.dp)
                )
            }
            Spacer(Modifier.height(24.dp))
            Text(
                "Giỏ hàng trống",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = Color(0xFF1A1A1A)
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "Thêm vật tư y tế vào giỏ để tiếp tục mua sắm!",
                color = Color.Gray,
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp
            )
            Spacer(Modifier.height(24.dp))
            Button(
                onClick = onShop,
                colors = ButtonDefaults.buttonColors(containerColor = CartGreen),
                shape = RoundedCornerShape(50),
                contentPadding = PaddingValues(horizontal = 28.dp, vertical = 14.dp)
            ) {
                Icon(Icons.Filled.LocalHospital, null, tint = Color.White, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Mua sắm ngay", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }
        }
    }
}
