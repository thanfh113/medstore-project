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
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
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
    var quantityEditorItem by remember { mutableStateOf<CartItemDto?>(null) }
    var quantityDraft by rememberSaveable { mutableStateOf("") }
    var quantityEditorError by remember { mutableStateOf<String?>(null) }
    var selectedItemIds by rememberSaveable { mutableStateOf<List<String>>(emptyList()) }
    var hasInitializedSelection by rememberSaveable { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val cartItemIds = remember(uiState.items) { uiState.items.map { it.id } }
    val selectedItems = remember(uiState.items, selectedItemIds) {
        val selectedSet = selectedItemIds.toSet()
        uiState.items.filter { it.id in selectedSet }
    }
    val selectedQuantity = selectedItems.sumOf { it.quantity }
    val selectedTotal = selectedItems.sumOf { it.totalPrice }

    LaunchedEffect(cartItemIds) {
        if (cartItemIds.isEmpty()) {
            selectedItemIds = emptyList()
            hasInitializedSelection = false
        } else if (!hasInitializedSelection) {
            selectedItemIds = cartItemIds
            hasInitializedSelection = true
        } else {
            selectedItemIds = selectedItemIds.filter { it in cartItemIds }
        }
    }

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

    if (quantityEditorItem != null) {
        val editingItem = quantityEditorItem!!
        AlertDialog(
            onDismissRequest = {
                quantityEditorItem = null
                quantityEditorError = null
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(20.dp),
            title = {
                Text(
                    "Nhập số lượng",
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        editingItem.displayName,
                        color = Color(0xFF555555),
                        fontSize = 14.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    OutlinedTextField(
                        value = quantityDraft,
                        onValueChange = { input ->
                            quantityDraft = input.filter { it.isDigit() }
                            quantityEditorError = null
                        },
                        label = { Text("Số lượng") },
                        singleLine = true,
                        isError = quantityEditorError != null,
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (quantityEditorError != null) {
                        Text(
                            quantityEditorError!!,
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 12.sp
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val newQuantity = quantityDraft.toIntOrNull()
                        if (newQuantity == null || newQuantity < 1) {
                            quantityEditorError = "Số lượng phải lớn hơn 0"
                            return@Button
                        }
                        if (editingItem.stock > 0 && newQuantity > editingItem.stock) {
                            quantityEditorError = "Số lượng vượt tồn kho"
                            return@Button
                        }
                        viewModel.updateQuantity(editingItem.id, newQuantity)
                        quantityEditorItem = null
                        quantityEditorError = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CartGreen),
                    shape = RoundedCornerShape(50)
                ) {
                    Text("Lưu", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    quantityEditorItem = null
                    quantityEditorError = null
                }) {
                    Text("Hủy", color = Color.Gray)
                }
            }
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = { CartTopBar(itemCount = uiState.totalItems, onBack = { navController?.popBackStack() }) },
        bottomBar = {
            CartBottomBar(
                totalAmount = selectedTotal,
                itemCount = selectedItems.size,
                isLoading = uiState.isLoading,
                onCheckout = {
                    navController?.currentBackStackEntry
                        ?.savedStateHandle
                        ?.set("selectedCartItemIds", ArrayList(selectedItemIds))
                    navController?.navigate("CheckoutScreen")
                }
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
                        item {
                            CartSelectionHeader(
                                selectedCount = selectedItems.size,
                                totalCount = uiState.items.size,
                                allSelected = selectedItems.size == uiState.items.size,
                                onToggleAll = {
                                    selectedItemIds = if (selectedItems.size == uiState.items.size) {
                                        emptyList()
                                    } else {
                                        cartItemIds
                                    }
                                }
                            )
                        }

                        items(uiState.items, key = { it.id }) { item ->
                            val isSelected = item.id in selectedItemIds
                            CartItemCard(
                                item = item,
                                selected = isSelected,
                                onSelectedChange = { checked ->
                                    selectedItemIds = if (checked) {
                                        (selectedItemIds + item.id).distinct()
                                    } else {
                                        selectedItemIds.filterNot { it == item.id }
                                    }
                                },
                                onQuantityChange = { quantity ->
                                    viewModel.updateQuantity(item.id, quantity)
                                },
                                onRemove = {
                                    quantityEditorItem = null
                                    quantityEditorError = null
                                    itemToRemove = item
                                }
                            )
                        }

                        // Summary card
                        item {
                            Spacer(Modifier.height(4.dp))
                            CartSummarySection(
                                subtotal = selectedTotal,
                                itemCount = selectedQuantity
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
                Text("Tổng cộng", fontSize = 12.sp, color = Color.Gray)
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
                    "Đặt hàng",
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
private fun CartSelectionHeader(
    selectedCount: Int,
    totalCount: Int,
    allSelected: Boolean,
    onToggleAll: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        shadowElevation = 1.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = allSelected && totalCount > 0,
                    onCheckedChange = { onToggleAll() },
                    colors = CheckboxDefaults.colors(
                        checkedColor = CartGreen,
                        uncheckedColor = Color(0xFFBDBDBD)
                    )
                )
                Text(
                    "Chọn tất cả",
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF1A1A1A)
                )
            }
            Text(
                "$selectedCount/$totalCount sản phẩm",
                color = CartGreen,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun CartItemCard(
    item: CartItemDto,
    selected: Boolean,
    onSelectedChange: (Boolean) -> Unit,
    onQuantityChange: (Int) -> Unit,
    onRemove: () -> Unit
) {
    val maxQuantity = if (item.stock > 0) item.stock else item.quantity.coerceAtLeast(1)
    var quantityText by remember(item.id) { mutableStateOf(item.quantity.toString()) }
    LaunchedEffect(item.id, item.quantity) {
        quantityText = item.quantity.toString()
    }
    val draftQuantity = quantityText.toIntOrNull()
    val quantityError = quantityText.isNotBlank() && (draftQuantity == null || draftQuantity !in 1..maxQuantity)
    val canSaveQuantity = draftQuantity != null &&
        draftQuantity in 1..maxQuantity &&
        draftQuantity != item.quantity

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
                Checkbox(
                    checked = selected,
                    onCheckedChange = onSelectedChange,
                    colors = CheckboxDefaults.colors(
                        checkedColor = CartGreen,
                        uncheckedColor = Color(0xFFBDBDBD)
                    )
                )
                Spacer(Modifier.width(4.dp))
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
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        "Tồn: ${item.stock}",
                        color = CartGreen,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        IconButton(
                            onClick = { onQuantityChange(item.quantity - 1) },
                            enabled = item.quantity > 1,
                            modifier = Modifier.size(34.dp)
                        ) {
                            Icon(Icons.Filled.Remove, contentDescription = "Giảm", tint = CartGreen)
                        }
                        OutlinedTextField(
                            value = quantityText,
                            onValueChange = { input ->
                                quantityText = input.filter { it.isDigit() }.take(4)
                            },
                            singleLine = true,
                            isError = quantityError,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            textStyle = MaterialTheme.typography.bodyMedium.copy(
                                textAlign = TextAlign.Center,
                                fontWeight = FontWeight.Bold
                            ),
                            modifier = Modifier
                                .width(68.dp)
                                .height(48.dp)
                        )
                        IconButton(
                            onClick = { onQuantityChange(item.quantity + 1) },
                            enabled = item.quantity < maxQuantity,
                            modifier = Modifier.size(34.dp)
                        ) {
                            Icon(Icons.Filled.Add, contentDescription = "Tăng", tint = CartGreen)
                        }
                        if (canSaveQuantity) {
                            TextButton(onClick = { onQuantityChange(draftQuantity!!) }) {
                                Text("Lưu", color = CartGreen, fontWeight = FontWeight.Bold)
                            }
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
