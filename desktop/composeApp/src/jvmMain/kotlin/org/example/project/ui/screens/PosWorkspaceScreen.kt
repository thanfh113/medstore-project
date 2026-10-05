package org.example.project.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TextButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import java.awt.image.BufferedImage
import java.text.NumberFormat
import java.util.Locale
import org.example.project.data.models.Product
import org.example.project.data.repositories.PosOrderStatusResult
import org.example.project.presentation.viewmodels.PosCartItem
import org.example.project.presentation.viewmodels.PosUiState
import org.example.project.presentation.viewmodels.PosViewModel
import org.example.project.ui.components.GatewayPaymentWebViewOverlay
import org.example.project.ui.components.ProductDetailDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PosWorkspaceScreen(viewModel: PosViewModel) {
    val state by viewModel.uiState.collectAsState()
    var previewProduct by remember { mutableStateOf<Product?>(null) }

    LaunchedEffect(Unit) {
        viewModel.loadProducts()
        viewModel.loadPendingOrders()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Bán tại quầy (POS)", fontWeight = FontWeight.Bold) },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        titleContentColor = MaterialTheme.colorScheme.primary
                    )
                )
            },
            containerColor = MaterialTheme.colorScheme.background
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    if ((state.currentPaymentStep >= 3 || state.hasActiveOrder) && !state.isChangingPaymentMethod) {
                        PosPaymentStage(
                            state = state,
                            viewModel = viewModel
                        )
                    } else {
                        PosCheckoutWorkspace(
                            state = state,
                            viewModel = viewModel,
                            onPreviewProduct = { previewProduct = it }
                        )
                    }
                }

                state.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                state.successMessage?.let { Text(it, color = MaterialTheme.colorScheme.primary) }
            }
        }

        GatewayPaymentWebViewOverlay(
            visible = state.isGatewayWebViewVisible,
            paymentUrl = state.gatewayPaymentUrl,
            paymentMethod = state.activeOrderPaymentMethod,
            orderCode = state.activeOrderCode,
            onClose = viewModel::closeGatewayPaymentPage,
            onRefreshPaymentStatus = viewModel::refreshPendingPaymentStatus,
            onLocationChanged = viewModel::handleGatewayPageNavigation
        )

        previewProduct?.let { product ->
            ProductDetailDialog(
                product = product,
                onDismiss = { previewProduct = null },
                onAddToCart = { viewModel.addToCart(product) },
                formatVnd = ::formatPosAmount
            )
        }
    }
}

@Composable
private fun PosCheckoutWorkspace(
    state: PosUiState,
    viewModel: PosViewModel,
    onPreviewProduct: (Product) -> Unit
) {
    val searchResults = if (state.searchQuery.isBlank()) emptyList() else state.filteredProducts.take(4)
    val selectedCount = state.cart.sumOf { it.quantity }

    Row(
        modifier = Modifier.fillMaxSize(),
        horizontalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        Card(
            modifier = Modifier
                .weight(0.62f)
                .fillMaxHeight(),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Chọn hàng", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Text(
                        if (selectedCount == 0) "Chưa chọn sản phẩm" else "$selectedCount sản phẩm đã chọn",
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                OutlinedTextField(
                    value = state.searchQuery,
                    onValueChange = viewModel::search,
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    placeholder = { Text("Tìm theo tên, SKU hoặc hãng") }
                )

                Text(
                    "Chỉ hiện kết quả khi bạn gõ tìm kiếm để màn hình gọn hơn.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (state.searchQuery.isBlank()) {
                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.28f)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(92.dp)
                                .padding(horizontal = 18.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Text(
                                "Gõ từ khóa để hiện nhanh sản phẩm cần thêm vào hóa đơn.",
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    Text("Kết quả tìm kiếm", fontWeight = FontWeight.SemiBold)
                    if (searchResults.isEmpty()) {
                        Surface(
                            shape = RoundedCornerShape(18.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.28f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(96.dp)
                                    .padding(horizontal = 18.dp),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                Text(
                                    "Không có sản phẩm khớp với từ khóa hiện tại.",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 280.dp),
                            contentPadding = PaddingValues(bottom = 6.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(searchResults, key = { it.id }) { product ->
                                PosSearchResultCard(
                                    product = product,
                                    onPreview = { onPreviewProduct(product) },
                                    onAdd = { quantity -> viewModel.addToCart(product, quantity) },
                                    onAddWithPrice = { quantity, price -> viewModel.addToCartWithPrice(product, quantity, price) }
                                )
                            }
                        }
                    }
                }

                HorizontalDivider()
                Text("Sản phẩm đã chọn", fontWeight = FontWeight.SemiBold)

                if (state.cart.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "Hóa đơn đang trống. Hãy tìm kiếm rồi thêm sản phẩm ở phía trên.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding = PaddingValues(bottom = 12.dp)
                    ) {
                        items(state.cart, key = { it.product.id }) { item ->
                            PosSelectedItemCard(
                                item = item,
                                onDecrease = { viewModel.updateQuantity(item.product.id, item.quantity - 1) },
                                onIncrease = { viewModel.updateQuantity(item.product.id, item.quantity + 1) },
                                onQuantityChange = { viewModel.updateQuantity(item.product.id, it) }
                            )
                        }
                    }
                }
            }
        }

        Card(
            modifier = Modifier
                .weight(0.38f)
                .fillMaxHeight(),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
        ) {
            PosInvoiceSidebar(state = state, viewModel = viewModel)
        }
    }
}

@Composable
private fun PosInvoiceSidebar(state: PosUiState, viewModel: PosViewModel) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text("Hóa đơn", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            if (state.pendingOrders.isNotEmpty()) {
                PendingOrdersSection(
                    orders = state.pendingOrders,
                    hasActiveOrder = state.hasActiveOrder,
                    onResume = viewModel::resumeOrder,
                    onCancel = viewModel::cancelOrderById
                )
            }

            if (state.isChangingPaymentMethod && state.activeOrderCode != null) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            "Đổi phương thức thanh toán",
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Text(
                            "Đơn ${state.activeOrderCode} • ${formatPosAmount(state.activeOrderTotal ?: 0.0)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
            }

            PaymentMethodSelector(
                selectedMethod = state.paymentMethod,
                onSelect = viewModel::setPaymentMethod
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = state.couponCode,
                    onValueChange = viewModel::setCouponCode,
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    placeholder = { Text("Mã giảm giá") }
                )
                Button(
                    onClick = viewModel::applyCoupon,
                    enabled = state.couponCode.isNotBlank()
                ) {
                    Text("Áp dụng")
                }
            }

            OutlinedTextField(
                value = state.customerCode,
                onValueChange = viewModel::setCustomerCode,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                placeholder = { Text("Mã khách hàng hoặc số điện thoại") }
            )

            Surface(
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.surface
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Sản phẩm trong hóa đơn", fontWeight = FontWeight.SemiBold)
                    if (state.cart.isEmpty()) {
                        Text(
                            "Chưa có sản phẩm nào.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        state.cart.forEach { item ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        item.product.name,
                                        fontWeight = FontWeight.Medium,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        "${item.quantity} x ${formatPosAmount(item.effectivePrice)}",
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    formatPosAmount(item.effectivePrice * item.quantity),
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }

            Surface(
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.surface
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    SummaryLine("Tạm tính", formatPosAmount(state.subtotal))
                    SummaryLine("Giảm giá", formatPosAmount(state.appliedDiscount))
                    HorizontalDivider()
                    SummaryLine("Tổng cộng", formatPosAmount(state.total), highlight = true)
                }
            }

            Text(
                when (state.paymentMethod) {
                    "CASH" -> "Xác nhận để chuyển sang bước nhập tiền khách đưa."
                    else -> "Xác nhận để tạo đơn và mở trang thanh toán ${paymentMethodLabel(state.paymentMethod)}."
                },
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Button(
            onClick = {
                if (state.isChangingPaymentMethod) viewModel.changePaymentMethodForActiveOrder()
                else viewModel.submitCheckout()
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            enabled = (state.cart.isNotEmpty() || state.isChangingPaymentMethod) && !state.isSubmitting
        ) {
            if (state.isSubmitting) {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    strokeWidth = 2.dp,
                    color = Color.White
                )
                Spacer(modifier = Modifier.width(10.dp))
            }
            Text(
                when {
                    state.isChangingPaymentMethod -> "Thanh toán bằng ${paymentMethodLabel(state.paymentMethod)}"
                    state.paymentMethod == "CASH" -> "Xác nhận thanh toán"
                    else -> "Tiếp tục thanh toán"
                }
            )
        }
    }
}

@Composable
private fun PaymentMethodSelector(
    selectedMethod: String,
    onSelect: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    modifier = Modifier.size(38.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.10f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Default.Payments,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        "Phương thức thanh toán",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        "Đang chọn: ${paymentMethodLabel(selectedMethod)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Box {
                OutlinedButton(
                    onClick = { expanded = true },
                    modifier = Modifier
                        .defaultMinSize(minWidth = 184.dp)
                        .height(46.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.24f))
                ) {
                    Text("Chọn", fontWeight = FontWeight.SemiBold, maxLines = 1)
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        Icons.Default.KeyboardArrowDown,
                        contentDescription = null
                    )
                }

                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier.widthIn(min = 184.dp)
                ) {
                    listOf("CASH", "MOMO", "ZALOPAY").forEach { method ->
                        DropdownMenuItem(
                            text = { Text(paymentMethodLabel(method)) },
                            onClick = {
                                onSelect(method)
                                expanded = false
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PosSearchResultCard(
    product: Product,
    onPreview: () -> Unit,
    onAdd: (Int) -> Unit,
    onAddWithPrice: (Int, Double) -> Unit = { _, _ -> }
) {
    var qtyInput by remember(product.id) { mutableStateOf("1") }
    val maxQuantity = product.stockQuantity.coerceAtLeast(1)
    val selectedQuantity = qtyInput.toIntOrNull()?.coerceIn(1, maxQuantity) ?: 1
    var showPriceDialog by remember { mutableStateOf(false) }

    if (showPriceDialog) {
        AgreedPriceDialog(
            productName = product.name,
            onConfirm = { price ->
                showPriceDialog = false
                onAddWithPrice(selectedQuantity, price)
            },
            onDismiss = { showPriceDialog = false }
        )
    }

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.22f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = product.images.firstOrNull()?.url,
                contentDescription = product.name,
                modifier = Modifier.size(96.dp)
            )

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(product.name, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text("SKU: ${product.sku ?: "-"}", color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("Tồn: ${product.stockQuantity} • ${product.unit}", color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (product.manufacturer.isNotBlank()) {
                    Text(product.manufacturer, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }

            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (product.contactForPrice) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer
                    ) {
                        Text(
                            "Giá liên hệ",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.secondary,
                            modifier = androidx.compose.ui.Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                } else {
                    Text(
                        formatPosAmount(product.price),
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                OutlinedButton(onClick = onPreview) {
                    Text("Chi tiết")
                }
                QuantityStepper(
                    value = qtyInput,
                    onValueChange = { value ->
                        val digits = value.filter { it.isDigit() }.take(4)
                        qtyInput = digits
                            .toIntOrNull()
                            ?.coerceIn(1, maxQuantity)
                            ?.toString()
                            ?: digits
                    },
                    onDecrease = {
                        qtyInput = (selectedQuantity - 1).coerceAtLeast(1).toString()
                    },
                    onIncrease = {
                        qtyInput = (selectedQuantity + 1).coerceAtMost(maxQuantity).toString()
                    }
                )
                Button(
                    onClick = {
                        if (product.contactForPrice) showPriceDialog = true
                        else onAdd(selectedQuantity)
                    },
                    enabled = product.stockQuantity > 0
                ) {
                    Text("Thêm")
                }
            }
        }
    }
}

@Composable
private fun AgreedPriceDialog(
    productName: String,
    onConfirm: (Double) -> Unit,
    onDismiss: () -> Unit
) {
    var priceInput by remember { mutableStateOf("") }
    val parsedPrice = priceInput.replace(".", "").replace(",", "").toDoubleOrNull()
    val isValid = parsedPrice != null && parsedPrice > 0.0

    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.widthIn(max = 380.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text("Nhập giá thỏa thuận", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text(
                    productName,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                OutlinedTextField(
                    value = priceInput,
                    onValueChange = { priceInput = it.filter { ch -> ch.isDigit() } },
                    label = { Text("Giá bán (VND)") },
                    placeholder = { Text("VD: 25000000") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = priceInput.isNotBlank() && !isValid,
                    singleLine = true
                )
                if (priceInput.isNotBlank() && isValid) {
                    Text(
                        "= ${formatPosAmount(parsedPrice!!)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) { Text("Hủy") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = { if (isValid) onConfirm(parsedPrice!!) }, enabled = isValid) {
                        Text("Thêm vào hóa đơn")
                    }
                }
            }
        }
    }
}

@Composable
private fun QuantityStepper(
    value: String,
    onValueChange: (String) -> Unit,
    onDecrease: () -> Unit,
    onIncrease: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 4.dp)
        ) {
            IconButton(onClick = onDecrease, modifier = Modifier.size(34.dp)) {
                Icon(Icons.Default.Remove, contentDescription = "Giảm", modifier = Modifier.size(16.dp))
            }
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.width(58.dp),
                singleLine = true,
                textStyle = androidx.compose.ui.text.TextStyle(
                    fontWeight = FontWeight.Bold,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                ),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
            IconButton(onClick = onIncrease, modifier = Modifier.size(34.dp)) {
                Icon(Icons.Default.Add, contentDescription = "Tăng", modifier = Modifier.size(16.dp))
            }
        }
    }
}

@Composable
private fun PosSelectedItemCard(
    item: PosCartItem,
    onDecrease: () -> Unit,
    onIncrease: () -> Unit,
    onQuantityChange: (Int) -> Unit
) {
    var qtyInput by remember(item.product.id, item.quantity) { mutableStateOf(item.quantity.toString()) }

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.18f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = item.product.images.firstOrNull()?.url,
                contentDescription = item.product.name,
                modifier = Modifier.size(72.dp)
            )

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(item.product.name, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(
                    if (item.product.contactForPrice) "Giá TT: ${formatPosAmount(item.effectivePrice)}"
                    else formatPosAmount(item.effectivePrice),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Text(
                formatPosAmount(item.effectivePrice * item.quantity),
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surface
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 4.dp)
                ) {
                    IconButton(onClick = onDecrease, modifier = Modifier.size(36.dp)) {
                        Icon(Icons.Default.Remove, contentDescription = "Giảm", modifier = Modifier.size(16.dp))
                    }
                    OutlinedTextField(
                        value = qtyInput,
                        onValueChange = { v ->
                            qtyInput = v.filter { it.isDigit() }.take(4)
                            val n = qtyInput.toIntOrNull()
                            if (n != null && n > 0) onQuantityChange(n)
                        },
                        modifier = Modifier.width(58.dp),
                        singleLine = true,
                        textStyle = androidx.compose.ui.text.TextStyle(
                            fontWeight = FontWeight.Bold,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        ),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    IconButton(onClick = onIncrease, modifier = Modifier.size(36.dp)) {
                        Icon(Icons.Default.Add, contentDescription = "Tăng", modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun SummaryLine(
    label: String,
    value: String,
    highlight: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontWeight = if (highlight) FontWeight.Bold else FontWeight.Medium)
        Text(
            value,
            fontWeight = if (highlight) FontWeight.Bold else FontWeight.SemiBold,
            color = if (highlight) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun PosPaymentStage(
    state: PosUiState,
    viewModel: PosViewModel
) {
    when {
        state.isSubmitting && !state.hasActiveOrder -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }

        state.activeOrderPaymentStatus == "COMPLETED" -> {
            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp)) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text("Thanh toán thành công", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                    Text("Đơn ${state.activeOrderCode ?: "-"} đã hoàn tất bằng ${paymentMethodLabel(state.activeOrderPaymentMethod ?: state.paymentMethod)}.")
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedButton(onClick = viewModel::openInvoiceFolder) {
                            Text("Mở thư mục hóa đơn")
                        }
                        OutlinedButton(
                            onClick = viewModel::exportLastInvoicePdf,
                            enabled = !state.isArchivingInvoice
                        ) {
                            Text("Xuất lại PDF")
                        }
                        Button(onClick = viewModel::resetPaymentFlow) {
                            Text("Tạo đơn mới")
                        }
                    }
                }
            }
        }

        state.isAwaitingCashConfirmation -> {
            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp)) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text("Xác nhận tiền mặt", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    SummaryLine("Cần thanh toán", formatPosAmount(state.activeOrderTotal ?: state.total), highlight = true)
                    OutlinedTextField(
                        value = state.cashReceivedInput,
                        onValueChange = viewModel::setCashReceivedInput,
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        placeholder = { Text("Số tiền khách đưa") }
                    )
                    state.cashChangePreview?.let {
                        SummaryLine("Tiền thừa", formatPosAmount(it), highlight = true)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = viewModel::backToCheckoutFromPendingPayment,
                            modifier = Modifier
                                .widthIn(min = 130.dp)
                                .height(52.dp),
                            enabled = !state.isSubmitting
                        ) {
                            Text("Đổi TT")
                        }
                        OutlinedButton(
                            onClick = { viewModel.cancelOrderById(state.activeOrderId!!) },
                            modifier = Modifier.height(52.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.error
                            ),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f)),
                            enabled = !state.isSubmitting
                        ) {
                            Text("Hủy đơn")
                        }
                        Button(
                            onClick = viewModel::confirmCashOrder,
                            modifier = Modifier
                                .widthIn(min = 180.dp)
                                .height(52.dp),
                            enabled = !state.isSubmitting
                        ) {
                            Text("Hoàn tất đơn tiền mặt")
                        }
                    }
                }
            }
        }

        state.isAwaitingGatewayPayment -> {
            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp)) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        "Đang chờ thanh toán ${paymentMethodLabel(state.activeOrderPaymentMethod ?: state.paymentMethod)}",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    SummaryLine("Mã đơn", state.activeOrderCode ?: "-")
                    SummaryLine("Tổng tiền", formatPosAmount(state.activeOrderTotal ?: state.total), highlight = true)
                    state.gatewayPaymentReference?.let { SummaryLine("Mã tham chiếu", it) }
                    state.gatewayQrContent?.takeIf { it.isNotBlank() }?.let { qrContent ->
                        generatePosQrBitmap(qrContent)?.let { bitmap ->
                            Image(
                                bitmap = bitmap,
                                contentDescription = "QR thanh toán",
                                modifier = Modifier
                                    .size(240.dp)
                                    .align(Alignment.CenterHorizontally)
                            )
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(onClick = viewModel::backToCheckoutFromPendingPayment) {
                            Text("Đổi TT")
                        }
                        OutlinedButton(
                            onClick = viewModel::parkActiveOrder,
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.secondary
                            )
                        ) {
                            Text("Để sang chờ")
                        }
                        OutlinedButton(
                            onClick = { viewModel.cancelOrderById(state.activeOrderId!!) },
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.error
                            ),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f))
                        ) {
                            Text("Hủy đơn")
                        }
                        if (!state.gatewayPaymentUrl.isNullOrBlank()) {
                            Button(onClick = viewModel::openGatewayPaymentPage) {
                                Text("Mở trang TT")
                            }
                        }
                        OutlinedButton(onClick = viewModel::refreshPendingPaymentStatus) {
                            Text("Kiểm tra")
                        }
                    }
                }
            }
        }

        else -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Đang chuẩn bị thanh toán...", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun PendingOrdersSection(
    orders: List<PosOrderStatusResult>,
    hasActiveOrder: Boolean,
    onResume: (PosOrderStatusResult) -> Unit,
    onCancel: (String) -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.55f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                "Đơn chờ thanh toán (${orders.size})",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onTertiaryContainer
            )
            orders.forEach { order ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            order.orderCode,
                            fontWeight = FontWeight.Medium,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                        Text(
                            "${formatPosAmount(order.total)} · ${paymentMethodLabel(order.paymentMethod ?: "?")}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.75f)
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                        TextButton(
                            onClick = { onResume(order) },
                            enabled = !hasActiveOrder
                        ) {
                            Text(
                                "Tiếp tục",
                                style = MaterialTheme.typography.labelMedium,
                                color = if (hasActiveOrder)
                                    MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.4f)
                                else
                                    MaterialTheme.colorScheme.tertiary
                            )
                        }
                        TextButton(onClick = { onCancel(order.id) }) {
                            Text(
                                "Hủy",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun paymentMethodLabel(method: String): String = when (method.uppercase()) {
    "CASH" -> "Tiền mặt"
    "MOMO" -> "MoMo"
    "ZALOPAY" -> "ZaloPay"
    else -> method
}

private fun formatPosAmount(value: Double): String {
    val currency = NumberFormat.getNumberInstance(Locale("vi", "VN"))
    return "${currency.format(value)} đ"
}

private fun generatePosQrBitmap(content: String): ImageBitmap? = runCatching {
    val matrix = QRCodeWriter().encode(content, BarcodeFormat.QR_CODE, 360, 360)
    val image = BufferedImage(matrix.width, matrix.height, BufferedImage.TYPE_INT_RGB)
    for (x in 0 until matrix.width) {
        for (y in 0 until matrix.height) {
            image.setRGB(x, y, if (matrix.get(x, y)) 0xFF000000.toInt() else 0xFFFFFFFF.toInt())
        }
    }
    image.toComposeImageBitmap()
}.getOrNull()
