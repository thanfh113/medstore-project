package com.example.nhathuoc.ui.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBackIos
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowBackIosNew
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

private val cartLocale: Locale = Locale.Builder().setLanguage("vi").setRegion("VN").build()
private fun Double.fmtVnd() = NumberFormat.getCurrencyInstance(cartLocale)
    .format(this).replace("₫", "đ")

// ── Screen ────────────────────────────────────────────────────────────────
@Composable
fun CartScreen(
    modifier: Modifier = Modifier,
    navController: NavController? = null,
    showBackButton: Boolean = true
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
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(20.dp),
            icon = {
                Icon(Icons.Filled.Delete, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(28.dp))
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
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.removeCartItem(itemToRemove!!.id)
                        itemToRemove = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    shape = RoundedCornerShape(50)
                ) {
                    Text("Xoá", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { itemToRemove = null }) {
                    Text("Huỷ", color = MaterialTheme.colorScheme.onSurfaceVariant)
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
            containerColor = MaterialTheme.colorScheme.surface,
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
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
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
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
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
                    Text("Hủy", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = { CartTopBar(itemCount = uiState.totalItems, onBack = { navController?.popBackStack() }, showBackButton = showBackButton) },
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
        containerColor = MaterialTheme.colorScheme.background
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
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    }
                }
            }
        }
    }
}

// ── TopBar ─────────────────────────────────────────────────────────────────
@Composable
private fun CartTopBar(itemCount: Int, onBack: () -> Unit, showBackButton: Boolean = true) {
    val gradient = Brush.horizontalGradient(listOf(Color(0xFF2E7D32), Color(0xFF66BB6A)))
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(gradient)
            .statusBarsPadding()
            .height(64.dp)
    ) {
        if (showBackButton) {
            IconButton(
                onClick = onBack,
                modifier = Modifier.align(Alignment.CenterStart)
            ) {
                Icon(
                    Icons.Filled.ArrowBackIosNew,
                    contentDescription = "Quay lại",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
        Row(
            modifier = Modifier.align(Alignment.Center),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Giỏ hàng",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
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
                        "$itemCount",
                        fontSize = 12.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

// ── BottomBar ──────────────────────────────────────────────────────────────
@Composable
private fun CartBottomBar(
    totalAmount: Double,
    itemCount: Int,
    isLoading: Boolean,
    onCheckout: () -> Unit
) {
    val enabled = itemCount > 0 && !isLoading
    val gradient = Brush.horizontalGradient(listOf(Color(0xFF2E7D32), Color(0xFF66BB6A)))
    Surface(color = MaterialTheme.colorScheme.surface, shadowElevation = 16.dp) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Tổng thanh toán",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    totalAmount.fmtVnd(),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(if (enabled) gradient else Brush.horizontalGradient(listOf(Color(0xFFBDBDBD), Color(0xFFBDBDBD))))
                    .then(if (enabled) Modifier.clickable(onClick = onCheckout) else Modifier)
                    .padding(vertical = 14.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp
                        )
                    }
                    Text(
                        if (itemCount == 0) "Chưa có sản phẩm" else "Đặt hàng ngay",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    if (!isLoading) {
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
        color = MaterialTheme.colorScheme.surface,
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
                        checkedColor = MaterialTheme.colorScheme.primary,
                        uncheckedColor = Color(0xFFBDBDBD)
                    )
                )
                Text(
                    "Chọn tất cả",
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Text(
                "$selectedCount/$totalCount sản phẩm",
                color = MaterialTheme.colorScheme.primary,
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
        color = MaterialTheme.colorScheme.surface,
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
                        checkedColor = MaterialTheme.colorScheme.primary,
                        uncheckedColor = Color(0xFFBDBDBD)
                    )
                )
                Spacer(Modifier.width(4.dp))
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer),
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
                            tint = MaterialTheme.colorScheme.primary,
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
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        item.unit,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        item.unitPrice.fmtVnd(),
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.primary
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
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(Modifier.height(12.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        "Tồn: ${item.stock}",
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    // Stepper gọn: nút tròn − | số | nút tròn +
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(0.dp),
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        // Nút giảm
                        IconButton(
                            onClick = { onQuantityChange(item.quantity - 1) },
                            enabled = item.quantity > 1,
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                Icons.Filled.Remove,
                                contentDescription = "Giảm",
                                tint = if (item.quantity > 1) MaterialTheme.colorScheme.primary else Color(0xFFBDBDBD),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        // Số lượng — bấm để nhập tay
                        Surface(
                            color = MaterialTheme.colorScheme.surface,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .defaultMinSize(minWidth = 42.dp)
                                .padding(vertical = 4.dp)
                        ) {
                            androidx.compose.foundation.text.BasicTextField(
                                value = quantityText,
                                onValueChange = { input ->
                                    quantityText = input.filter { it.isDigit() }.take(4)
                                },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                textStyle = androidx.compose.ui.text.TextStyle(
                                    textAlign = TextAlign.Center,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = if (quantityError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                                ),
                                modifier = Modifier
                                    .width(42.dp)
                                    .padding(vertical = 6.dp),
                                decorationBox = { inner ->
                                    Box(contentAlignment = Alignment.Center) { inner() }
                                }
                            )
                        }
                        // Nút tăng
                        IconButton(
                            onClick = { onQuantityChange(item.quantity + 1) },
                            enabled = item.quantity < maxQuantity,
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                Icons.Filled.Add,
                                contentDescription = "Tăng",
                                tint = if (item.quantity < maxQuantity) MaterialTheme.colorScheme.primary else Color(0xFFBDBDBD),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                    // Nút Lưu nhỏ nếu người dùng chỉnh tay
                    if (canSaveQuantity) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.primary,
                            onClick = { onQuantityChange(draftQuantity!!) }
                        ) {
                            Text(
                                "Lưu",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                // Item total
                Text(
                    item.totalPrice.fmtVnd(),
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.primary
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
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Tóm tắt đơn hàng",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("$itemCount sản phẩm", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
                Text(subtotal.fmtVnd(), fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)
            }
            Spacer(Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Phí vận chuyển", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
                Text("Sẽ tính khi thanh toán", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Spacer(Modifier.height(10.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
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
                    color = MaterialTheme.colorScheme.primary
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
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary, strokeWidth = 3.dp)
            Spacer(Modifier.height(16.dp))
            Text("Đang tải giỏ hàng...", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
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
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(8.dp))
            Text(
                message,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 13.sp,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(20.dp))
            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
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
                        Brush.radialGradient(listOf(MaterialTheme.colorScheme.primaryContainer, MaterialTheme.colorScheme.background))
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Outlined.ShoppingCart,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(60.dp)
                )
            }
            Spacer(Modifier.height(24.dp))
            Text(
                "Giỏ hàng trống",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "Thêm vật tư y tế vào giỏ để tiếp tục mua sắm!",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp
            )
            Spacer(Modifier.height(24.dp))
            Button(
                onClick = onShop,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
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
