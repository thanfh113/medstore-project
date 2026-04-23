package org.example.project.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import java.awt.image.BufferedImage
import java.text.NumberFormat
import java.util.Locale
import org.example.project.data.models.Product
import org.example.project.presentation.viewmodels.PosCartItem
import org.example.project.presentation.viewmodels.PosUiState
import org.example.project.presentation.viewmodels.PosViewModel
import org.example.project.ui.components.GatewayPaymentWebViewOverlay
import org.example.project.ui.components.ProductDetailDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PosUnifiedScreen(viewModel: PosViewModel) {
    val state by viewModel.uiState.collectAsState()
    var previewProduct by remember { mutableStateOf<Product?>(null) }

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
                PosUnifiedHeader(processing = state.currentPaymentStep >= 3 || state.hasActiveOrder, paymentMethod = state.paymentMethod)

                Box(modifier = Modifier.weight(1f)) {
                    if (state.currentPaymentStep >= 3 || state.hasActiveOrder) {
                        PosUnifiedProcessContent(state = state, viewModel = viewModel)
                    } else {
                        PosUnifiedCheckoutWorkspace(
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
private fun PosUnifiedHeader(processing: Boolean, paymentMethod: String) {
    val steps = listOf("Chọn hàng", "Xử lý thanh toán")
    val activeIndex = if (processing) 1 else 0
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        steps.forEachIndexed { index, label ->
            val active = index <= activeIndex
            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(
                    containerColor = if (active) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                    else MaterialTheme.colorScheme.surface
                ),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("${index + 1}", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    Text(label, fontWeight = FontWeight.SemiBold)
                    if (index == 1) {
                        Text("Phương thức: ${paymentMethodLabel(paymentMethod)}", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

@Composable
private fun PosUnifiedCheckoutWorkspace(
    state: PosUiState,
    viewModel: PosViewModel,
    onPreviewProduct: (Product) -> Unit
) {
    val searchResults = if (state.searchQuery.isBlank()) emptyList() else state.filteredProducts.take(4)
    val cartCount = state.cart.sumOf { it.quantity }

    Row(
        modifier = Modifier.fillMaxSize(),
        horizontalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        Card(
            modifier = Modifier
                .weight(0.62f)
                .fillMaxHeight(),
            shape = RoundedCornerShape(24.dp),
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
                        if (cartCount == 0) "Chưa chọn sản phẩm" else "$cartCount sản phẩm đã chọn",
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
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)
                        ),
                        shape = RoundedCornerShape(18.dp)
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
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)
                            ),
                            shape = RoundedCornerShape(18.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(100.dp)
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
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            contentPadding = PaddingValues(bottom = 4.dp)
                        ) {
                            items(searchResults, key = { it.id }) { product ->
                                PosSearchResultCard(
                                    product = product,
                                    onPreview = { onPreviewProduct(product) },
                                    onAdd = { viewModel.addToCart(product) }
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
                            PosCartPreviewRow(
                                item = item,
                                onDecrease = { viewModel.updateQuantity(item.product.id, item.quantity - 1) },
                                onIncrease = { viewModel.updateQuantity(item.product.id, item.quantity + 1) }
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
            shape = RoundedCornerShape(24.dp),
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
            PaymentMethodSelector(selectedMethod = state.paymentMethod, onSelect = viewModel::setPaymentMethod)

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

            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(18.dp)
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
            onClick = viewModel::submitCheckout,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            enabled = state.cart.isNotEmpty() && !state.isSubmitting
        ) {
            if (state.isSubmitting) {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    strokeWidth = 2.dp,
                    color = Color.White
                )
                Spacer(modifier = Modifier.width(10.dp))
            }
            Text(if (state.paymentMethod == "CASH") "Xác nhận thanh toán" else "Tiếp tục thanh toán")
        }
    }
}

@Composable
private fun PosUnifiedCheckoutContent(
    state: PosUiState,
    viewModel: PosViewModel,
    onPreviewProduct: (Product) -> Unit
) {
    val searchResults = if (state.searchQuery.isBlank()) emptyList() else state.filteredProducts.take(6)

    Row(modifier = Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        Card(
            modifier = Modifier.weight(0.58f),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("Sản phẩm đã chọn", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
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

                if (searchResults.isNotEmpty()) {
                    Text("Kết quả tìm kiếm", fontWeight = FontWeight.SemiBold)
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth().height(220.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding = PaddingValues(bottom = 4.dp)
                    ) {
                        items(searchResults, key = { it.id }) { product ->
                            PosSearchResultCard(product = product, onPreview = { onPreviewProduct(product) }, onAdd = { viewModel.addToCart(product) })
                        }
                    }
                }

                HorizontalDivider()
                Text("Hóa đơn tạm", fontWeight = FontWeight.SemiBold)

                if (state.cart.isEmpty()) {
                    Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                        Text("Chưa có sản phẩm nào trong hóa đơn.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth().weight(1f),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding = PaddingValues(bottom = 12.dp)
                    ) {
                        items(state.cart, key = { it.product.id }) { item ->
                            PosCartPreviewRow(item = item, onDecrease = { viewModel.updateQuantity(item.product.id, item.quantity - 1) }, onIncrease = { viewModel.updateQuantity(item.product.id, item.quantity + 1) })
                        }
                    }
                }
            }
        }

        Card(
            modifier = Modifier.weight(0.42f),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
        ) {
            PosInvoicePanel(state = state, viewModel = viewModel)
        }
    }
}

@Composable
private fun PosInvoicePanel(state: PosUiState, viewModel: PosViewModel) {
    Column(modifier = Modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text("Hóa đơn", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        PaymentMethodSelector(selectedMethod = state.paymentMethod, onSelect = viewModel::setPaymentMethod)
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = state.couponCode,
                onValueChange = viewModel::setCouponCode,
                modifier = Modifier.weight(1f),
                singleLine = true,
                placeholder = { Text("Mã giảm giá") }
            )
            Button(onClick = viewModel::applyCoupon, enabled = state.couponCode.isNotBlank()) {
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
        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), shape = RoundedCornerShape(18.dp)) {
            Column(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                SummaryLine("Tạm tính", formatPosAmount(state.subtotal))
                SummaryLine("Giảm giá", formatPosAmount(state.appliedDiscount))
                HorizontalDivider()
                SummaryLine("Tổng cộng", formatPosAmount(state.total), highlight = true)
                Text(
                    when (state.paymentMethod) {
                        "CASH" -> "Xác nhận để chuyển sang bước nhập tiền khách đưa."
                        else -> "Xác nhận để tạo đơn và mở trang thanh toán ${paymentMethodLabel(state.paymentMethod)}."
                    },
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Spacer(modifier = Modifier.weight(1f))
        Button(
            onClick = viewModel::submitCheckout,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            enabled = state.cart.isNotEmpty() && !state.isSubmitting
        ) {
            if (state.isSubmitting) {
                CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = Color.White)
                Spacer(modifier = Modifier.width(10.dp))
            }
            Text(if (state.paymentMethod == "CASH") "Xác nhận hóa đơn" else "Tiếp tục thanh toán")
        }
    }
}

@Composable
private fun PaymentMethodSelector(selectedMethod: String, onSelect: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Box(modifier = Modifier.fillMaxWidth()) {
        OutlinedButton(onClick = { expanded = true }, modifier = Modifier.fillMaxWidth().height(56.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Phương thức thanh toán")
                Text(paymentMethodLabel(selectedMethod), fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
            }
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            listOf("CASH", "MOMO", "VNPAY", "ZALOPAY").forEach { method ->
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

@Composable
private fun PosSearchResultCard(product: Product, onPreview: () -> Unit, onAdd: () -> Unit) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f))) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = product.images.firstOrNull()?.url,
                contentDescription = product.name,
                modifier = Modifier.size(88.dp)
            )
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(product.name, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text("SKU: ${product.sku ?: "-"}", color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("Tồn: ${product.stockQuantity} • ${product.unit}", color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(product.manufacturer, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            Column(verticalArrangement = Arrangement.spacedBy(10.dp), horizontalAlignment = Alignment.End) {
                Text(formatPosAmount(product.price), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                OutlinedButton(onClick = onPreview) { Text("Chi tiết") }
                Button(onClick = onAdd) { Text("Thêm") }
            }
        }
    }
}

@Composable
private fun PosCartPreviewRow(item: PosCartItem, onDecrease: () -> Unit, onIncrease: () -> Unit) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = item.product.images.firstOrNull()?.url,
                contentDescription = item.product.name,
                modifier = Modifier.size(72.dp)
            )
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(item.product.name, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text("${item.quantity} x ${formatPosAmount(item.product.price)}", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onDecrease) { Icon(Icons.Default.Remove, contentDescription = "Giảm") }
                Text(item.quantity.toString(), fontWeight = FontWeight.Bold)
                IconButton(onClick = onIncrease) { Icon(Icons.Default.Add, contentDescription = "Tăng") }
            }
        }
    }
}

@Composable
private fun SummaryLine(label: String, value: String, highlight: Boolean = false) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, fontWeight = if (highlight) FontWeight.Bold else FontWeight.Medium)
        Text(
            value,
            fontWeight = if (highlight) FontWeight.Bold else FontWeight.SemiBold,
            color = if (highlight) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun PosUnifiedProcessContent(state: PosUiState, viewModel: PosViewModel) {
    when {
        state.isSubmitting && !state.hasActiveOrder -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        state.activeOrderPaymentStatus == "COMPLETED" -> {
            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp)) {
                Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text("Thanh toán thành công", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                    Text("Đơn ${state.activeOrderCode ?: "-"} đã hoàn tất bằng ${paymentMethodLabel(state.activeOrderPaymentMethod ?: state.paymentMethod)}.")
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedButton(onClick = viewModel::openInvoiceFolder) { Text("Mở thư mục hóa đơn") }
                        OutlinedButton(onClick = viewModel::exportLastInvoicePdf, enabled = !state.isArchivingInvoice) { Text("Xuất lại PDF") }
                        Button(onClick = viewModel::resetPaymentFlow) { Text("Tạo đơn mới") }
                    }
                }
            }
        }
        state.isAwaitingCashConfirmation -> {
            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp)) {
                Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text("Xác nhận tiền mặt", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    SummaryLine("Cần thanh toán", formatPosAmount(state.activeOrderTotal ?: state.total), highlight = true)
                    OutlinedTextField(
                        value = state.cashReceivedInput,
                        onValueChange = viewModel::setCashReceivedInput,
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        placeholder = { Text("Số tiền khách đưa") }
                    )
                    state.cashChangePreview?.let { SummaryLine("Tiền thừa", formatPosAmount(it), highlight = true) }
                    Button(onClick = viewModel::confirmCashOrder, modifier = Modifier.fillMaxWidth().height(52.dp), enabled = !state.isSubmitting) {
                        Text("Hoàn tất đơn tiền mặt")
                    }
                }
            }
        }
        state.isAwaitingGatewayPayment -> {
            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp)) {
                Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text("Đang chờ thanh toán ${paymentMethodLabel(state.activeOrderPaymentMethod ?: state.paymentMethod)}", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    SummaryLine("Mã đơn", state.activeOrderCode ?: "-")
                    SummaryLine("Tổng tiền", formatPosAmount(state.activeOrderTotal ?: state.total), highlight = true)
                    state.gatewayPaymentReference?.let { SummaryLine("Mã tham chiếu", it) }
                    state.gatewayQrContent?.takeIf { it.isNotBlank() }?.let { qrContent ->
                        generatePosQrBitmap(qrContent)?.let { bitmap ->
                            androidx.compose.foundation.Image(bitmap = bitmap, contentDescription = "QR thanh toán", modifier = Modifier.size(240.dp).align(Alignment.CenterHorizontally))
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        if (!state.gatewayPaymentUrl.isNullOrBlank()) {
                            Button(onClick = viewModel::openGatewayPaymentPage) { Text("Mở trang thanh toán") }
                        }
                        OutlinedButton(onClick = viewModel::refreshPendingPaymentStatus) { Text("Kiểm tra trạng thái") }
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

private fun paymentMethodLabel(method: String): String = when (method.uppercase()) {
    "CASH" -> "Tiền mặt"
    "MOMO" -> "MoMo"
    "VNPAY" -> "VNPay"
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
