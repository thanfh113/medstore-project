package org.example.project.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Remove
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
import androidx.compose.material3.Surface
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
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
fun PosCheckoutScreen(viewModel: PosViewModel) {
    val state by viewModel.uiState.collectAsState()
    var selectedProduct by remember { mutableStateOf<Product?>(null) }

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
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                PosStepHeader(state.currentPaymentStep)
                Spacer(modifier = Modifier.height(12.dp))

                Box(modifier = Modifier.weight(1f)) {
                    when (state.currentPaymentStep) {
                        0 -> PosProductSelectionStep(
                            state = state,
                            viewModel = viewModel,
                            onPreviewProduct = { selectedProduct = it }
                        )
                        1 -> PosConfirmStep(state = state)
                        else -> PosProcessPaymentStep(state = state, viewModel = viewModel)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                PosBottomActions(state = state, viewModel = viewModel)

                state.error?.let {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(it, color = MaterialTheme.colorScheme.error)
                }
                state.successMessage?.let {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(it, color = MaterialTheme.colorScheme.primary)
                }
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

        selectedProduct?.let { product ->
            ProductDetailDialog(
                product = product,
                onDismiss = { selectedProduct = null },
                onAddToCart = { viewModel.addToCart(product) },
                formatVnd = ::formatPosVnd
            )
        }
    }
}

@Composable
private fun PosStepHeader(currentStep: Int) {
    val steps = listOf("Sản phẩm", "Xác nhận", "Thanh toán", "Quét mã")

    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        steps.forEachIndexed { index, title ->
            val active = index <= currentStep
            Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .background(
                            if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                            RoundedCornerShape(999.dp)
                        )
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text("${index + 1}", color = if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline, fontWeight = FontWeight.Bold)
                Text(title, color = if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun PosProductSelectionStep(
    state: PosUiState,
    viewModel: PosViewModel,
    onPreviewProduct: (Product) -> Unit
) {
    Row(modifier = Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        Card(
            modifier = Modifier.weight(0.64f),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(modifier = Modifier.fillMaxSize().padding(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Tìm sản phẩm", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text(
                        "${state.filteredProducts.size} / ${state.products.size} sản phẩm",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = state.searchQuery,
                    onValueChange = viewModel::search,
                    label = { Text("Tên, SKU hoặc hãng") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(14.dp))

                when {
                    state.isLoading -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    }

                    state.filteredProducts.isEmpty() -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("Không tìm thấy sản phẩm phù hợp", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    else -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            contentPadding = PaddingValues(bottom = 8.dp)
                        ) {
                            items(state.filteredProducts, key = { it.id }) { product ->
                                PosProductCard(
                                    product = product,
                                    onPreview = { onPreviewProduct(product) },
                                    onAddToCart = { viewModel.addToCart(product) }
                                )
                            }
                        }
                    }
                }
            }
        }

        Card(
            modifier = Modifier.weight(0.36f),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(modifier = Modifier.fillMaxSize().padding(18.dp)) {
                Text("Hóa đơn", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(12.dp))

                if (state.cart.isEmpty()) {
                    Box(
                        modifier = Modifier.weight(1f).fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "Chưa có sản phẩm nào trong hóa đơn",
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f).fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding = PaddingValues(bottom = 8.dp)
                    ) {
                        items(state.cart, key = { it.product.id }) { item ->
                            PosCartCard(
                                item = item,
                                onDecrease = { viewModel.updateQuantity(item.product.id, item.quantity - 1) },
                                onIncrease = { viewModel.updateQuantity(item.product.id, item.quantity + 1) }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text("Tạm tính: ${formatPosVnd(state.subtotal)}", fontSize = 13.sp)
                Text("Giảm giá: ${formatPosVnd(state.appliedDiscount)}", fontSize = 13.sp)
                HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))
                Text(
                    "Tổng: ${formatPosVnd(state.total)}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun PosProductCard(
    product: Product,
    onPreview: () -> Unit,
    onAddToCart: () -> Unit
) {
    val imageUrl = product.images.firstOrNull()?.url

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.28f)),
        shape = RoundedCornerShape(18.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(78.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.surface),
                contentAlignment = Alignment.Center
            ) {
                if (imageUrl != null) {
                    AsyncImage(
                        model = imageUrl,
                        contentDescription = product.name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Text("Ảnh", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                }
            }

            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(product.name, fontWeight = FontWeight.SemiBold, maxLines = 2, overflow = TextOverflow.Ellipsis)
                Text("SKU: ${product.sku ?: "-"}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("Tồn: ${product.stockQuantity} • ${product.unit}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (product.manufacturer.isNotBlank()) {
                    Text(
                        product.manufacturer,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    formatPosVnd(product.price),
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 18.sp
                )
                OutlinedButton(onClick = onPreview, contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)) {
                    Text("Chi tiết")
                }
                Button(onClick = onAddToCart, contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)) {
                    Text("Thêm")
                }
            }
        }
    }
}

@Composable
private fun PosCartCard(
    item: PosCartItem,
    onDecrease: () -> Unit,
    onIncrease: () -> Unit
) {
    val imageUrl = item.product.images.firstOrNull()?.url

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                if (imageUrl != null) {
                    AsyncImage(
                        model = imageUrl,
                        contentDescription = item.product.name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Text("Ảnh", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(item.product.name, fontWeight = FontWeight.Medium, maxLines = 2, overflow = TextOverflow.Ellipsis)
                Text("${item.quantity} x ${formatPosVnd(item.product.price)}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onDecrease, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Default.Remove, contentDescription = "Giảm")
                }
                IconButton(onClick = onIncrease, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Default.Add, contentDescription = "Tăng")
                }
            }
        }
    }
}

@Composable
private fun PosConfirmStep(state: PosUiState) {
    Card(
        modifier = Modifier.fillMaxSize(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
            Text("Xác nhận hóa đơn", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(state.cart, key = { it.product.id }) { item ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(item.product.name, fontWeight = FontWeight.Medium)
                            Text("${item.quantity} x ${formatPosVnd(item.product.price)}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Text(formatPosVnd(item.product.price * item.quantity), fontWeight = FontWeight.SemiBold)
                    }
                    HorizontalDivider()
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            PosSummaryBlock(state.subtotal, state.appliedDiscount, state.total)
        }
    }
}

@Composable
private fun PosPaymentMethodStep(state: PosUiState, viewModel: PosViewModel) {
    val options = listOf(
        "CASH" to "Tiền mặt",
        "MOMO" to "MoMo",
        "VNPAY" to "VNPay",
        "ZALOPAY" to "ZaloPay"
    )

    Card(
        modifier = Modifier.fillMaxSize(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            Text("Chọn phương thức thanh toán", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                options.forEach { (value, label) ->
                    OutlinedButton(
                        onClick = { viewModel.setPaymentMethod(value) },
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(vertical = 14.dp)
                    ) {
                        Text(
                            label,
                            color = if (state.paymentMethod == value) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            OutlinedTextField(
                value = state.customerCode,
                onValueChange = viewModel::setCustomerCode,
                label = { Text("Mã khách hàng hoặc số điện thoại") },
                modifier = Modifier.fillMaxWidth(),
                supportingText = { Text("Có thể để trống nếu không cần tích điểm / nhận diện khách hàng") }
            )

            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Tóm tắt thanh toán", fontWeight = FontWeight.SemiBold)
                    PosSummaryBlock(state.subtotal, state.appliedDiscount, state.total)
                    Text(
                        when (state.paymentMethod) {
                            "CASH" -> "Khách thanh toán trực tiếp tại quầy."
                            "MOMO" -> "Hệ thống sẽ tạo trang hoặc mã thanh toán MoMo."
                            "VNPAY" -> "Hệ thống sẽ tạo trang hoặc mã thanh toán VNPay."
                            else -> "Hệ thống sẽ tạo trang hoặc mã thanh toán ZaloPay."
                        },
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun PosSummaryBlock(subtotal: Double, discount: Double, total: Double) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        PosInfoLine("Tạm tính", formatPosVnd(subtotal))
        PosInfoLine("Giảm giá", formatPosVnd(discount))
        HorizontalDivider()
        PosInfoLine("Tổng cộng", formatPosVnd(total), valueColor = MaterialTheme.colorScheme.primary, bold = true)
    }
}

@Composable
private fun PosInfoLine(
    label: String,
    value: String,
    valueColor: Color = MaterialTheme.colorScheme.onSurface,
    bold: Boolean = false
) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(
            value,
            color = valueColor,
            fontWeight = if (bold) FontWeight.Bold else FontWeight.Medium,
            textAlign = TextAlign.End,
            modifier = Modifier.padding(start = 16.dp)
        )
    }
}

@Composable
private fun PosProcessPaymentStep(state: PosUiState, viewModel: PosViewModel) {
    when {
        state.isSubmitting && !state.hasActiveOrder -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }

        state.paymentMethod == "CASH" && state.activeOrderPaymentStatus != "COMPLETED" -> {
            Card(
                modifier = Modifier.fillMaxSize(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize().padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text("Thanh toán tiền mặt", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)),
                        shape = RoundedCornerShape(18.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(18.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Số tiền cần thanh toán")
                            Text(
                                formatPosVnd(state.activeOrderTotal ?: state.total),
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 20.sp
                            )
                        }
                    }

                    OutlinedTextField(
                        value = state.cashReceivedInput,
                        onValueChange = viewModel::setCashReceivedInput,
                        label = { Text("Số tiền khách đưa") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    state.cashChangePreview?.let {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
                            shape = RoundedCornerShape(18.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(18.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Tiền thối lại")
                                Text(formatPosVnd(it), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }

                    Button(
                        onClick = viewModel::confirmCashOrder,
                        enabled = state.cashChangePreview != null && !state.isSubmitting,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Xác nhận thanh toán")
                    }
                }
            }
        }

        state.hasActiveOrder && state.activeOrderPaymentStatus == "COMPLETED" -> {
            Card(
                modifier = Modifier.fillMaxSize(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize().padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(18.dp)
                ) {
                    Text("Thanh toán thành công", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)),
                        shape = RoundedCornerShape(18.dp)
                    ) {
                        Column(modifier = Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            PosInfoLine("Mã đơn", state.activeOrderCode ?: "-")
                            PosInfoLine("Phương thức", state.activeOrderPaymentMethod ?: "-")
                            PosInfoLine("Số tiền", formatPosVnd(state.activeOrderTotal ?: 0.0))
                            state.gatewayPaymentReference?.let { PosInfoLine("Mã giao dịch", it) }
                            state.gatewayPaidAt?.let { PosInfoLine("Đã thanh toán lúc", it) }
                        }
                    }

                    if (state.lastCompletedOrderId != null) {
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            OutlinedButton(onClick = viewModel::openInvoiceFolder, modifier = Modifier.weight(1f)) {
                                Text("Mở thư mục hóa đơn")
                            }
                            Button(
                                onClick = viewModel::exportLastInvoicePdf,
                                enabled = !state.isArchivingInvoice,
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(if (state.isArchivingInvoice) "Đang xuất PDF..." else "Xuất hóa đơn PDF")
                            }
                        }
                    }
                }
            }
        }

        state.isAwaitingGatewayPayment -> PosGatewayPaymentPanel(state = state, viewModel = viewModel)

        else -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Đang chuẩn bị thanh toán...")
                }
            }
        }
    }
}

@Composable
private fun PosGatewayPaymentPanel(state: PosUiState, viewModel: PosViewModel) {
    val qrBitmap = remember(state.gatewayQrContent) {
        state.gatewayQrContent?.takeIf { it.isNotBlank() }?.let(::generatePosQrBitmap)
    }

    Row(modifier = Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        Card(
            modifier = Modifier.weight(0.42f),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize().padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Thông tin giao dịch", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                PosInfoLine("Mã đơn", state.activeOrderCode ?: "-")
                PosInfoLine("Phương thức", state.activeOrderPaymentMethod ?: "-")
                PosInfoLine("Số tiền", formatPosVnd(state.activeOrderTotal ?: 0.0))
                state.gatewayPaymentReference?.let { PosInfoLine("Mã tham chiếu", it) }
                Text(
                    "Có thể mở trang thanh toán ngay trong desktop hoặc cho khách quét QR ở khung bên phải.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 13.sp
                )
                Spacer(modifier = Modifier.weight(1f))
                OutlinedButton(
                    onClick = viewModel::openGatewayPaymentPage,
                    enabled = !state.gatewayPaymentUrl.isNullOrBlank(),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Mở trang thanh toán")
                }
                OutlinedButton(onClick = viewModel::initPendingGatewayPayment, modifier = Modifier.fillMaxWidth()) {
                    Text("Tạo lại phiên thanh toán")
                }
                Button(onClick = viewModel::refreshPendingPaymentStatus, modifier = Modifier.fillMaxWidth()) {
                    Text(if (state.isPollingPayment) "Đang kiểm tra..." else "Kiểm tra lại")
                }
            }
        }

        Card(
            modifier = Modifier.weight(0.58f),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize().padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text("Quét mã thanh toán", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                if (qrBitmap != null) {
                    Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(24.dp)) {
                        Box(modifier = Modifier.padding(20.dp), contentAlignment = Alignment.Center) {
                            Image(bitmap = qrBitmap, contentDescription = "QR thanh toán", modifier = Modifier.size(320.dp))
                        }
                    }
                } else {
                    Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text("Không tạo được QR. Hãy dùng nút mở trang thanh toán.", color = MaterialTheme.colorScheme.error, textAlign = TextAlign.Center)
                    }
                }
                Surface(
                    color = if (state.isPollingPayment) Color(0xFFFFF3E0) else Color(0xFFE8F5E9),
                    shape = RoundedCornerShape(999.dp)
                ) {
                    Text(
                        if (state.isPollingPayment) "Đang chờ khách thanh toán" else "Sẵn sàng",
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        color = if (state.isPollingPayment) Color(0xFFEF6C00) else Color(0xFF2E7D32),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
private fun PosBottomActions(state: PosUiState, viewModel: PosViewModel) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        if (state.currentPaymentStep in 1..2 && state.activeOrderPaymentStatus != "COMPLETED") {
            OutlinedButton(onClick = viewModel::previousStep, modifier = Modifier.weight(1f)) {
                Icon(Icons.Default.ArrowBack, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Quay lại")
            }
        }

        val buttonText = when {
            state.activeOrderPaymentStatus == "COMPLETED" -> "Hoàn tất & tạo đơn mới"
            state.currentPaymentStep == 0 -> "Tiếp tục"
            state.currentPaymentStep == 1 -> "Chọn thanh toán"
            state.currentPaymentStep == 2 -> if (state.isSubmitting) "Đang tạo đơn..." else "Tạo đơn"
            else -> "Đang xử lý..."
        }

        Button(
            onClick = {
                if (state.activeOrderPaymentStatus == "COMPLETED") {
                    viewModel.resetPaymentFlow()
                } else if (state.currentPaymentStep < 3) {
                    viewModel.nextStep()
                }
            },
            enabled = when {
                state.activeOrderPaymentStatus == "COMPLETED" -> true
                state.currentPaymentStep == 0 -> state.cart.isNotEmpty()
                state.currentPaymentStep == 1 -> state.cart.isNotEmpty()
                state.currentPaymentStep == 2 -> !state.isSubmitting
                else -> false
            },
            modifier = Modifier.weight(1f)
        ) {
            Text(buttonText)
        }
    }
}

private fun formatPosVnd(amount: Double): String {
    return NumberFormat.getNumberInstance(Locale("vi", "VN")).format(amount) + " đ"
}

private fun generatePosQrBitmap(content: String, size: Int = 480): ImageBitmap {
    val matrix = QRCodeWriter().encode(content, BarcodeFormat.QR_CODE, size, size)
    val image = BufferedImage(size, size, BufferedImage.TYPE_INT_RGB)
    for (x in 0 until size) {
        for (y in 0 until size) {
            image.setRGB(x, y, if (matrix[x, y]) 0xFF000000.toInt() else 0xFFFFFFFF.toInt())
        }
    }
    return image.toComposeImageBitmap()
}
