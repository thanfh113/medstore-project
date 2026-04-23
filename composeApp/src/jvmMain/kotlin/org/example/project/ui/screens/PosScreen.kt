package org.example.project.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import coil3.compose.AsyncImage
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import org.example.project.data.models.Product
import org.example.project.presentation.viewmodels.PosViewModel
import org.example.project.presentation.viewmodels.PosUiState
import org.example.project.ui.components.GatewayPaymentWebViewOverlay
import org.example.project.ui.components.ProductDetailDialog
import java.awt.image.BufferedImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PosScreen(viewModel: PosViewModel) {
    val state by viewModel.uiState.collectAsState()
    var selectedProductForPreview by remember { mutableStateOf<Product?>(null) }

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
        ) {
            LinearProgressIndicator(
                progress = { (state.currentPaymentStep + 1) / 4f },
                modifier = Modifier.fillMaxWidth()
            )

            StepIndicator(
                currentStep = state.currentPaymentStep,
                steps = listOf("Sản phẩm", "Xác nhận", "Thanh toán", "Quét mã")
            )

            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                when (state.currentPaymentStep) {
                    0 -> Step1_SelectProducts(state, viewModel)
                    1 -> Step2_ConfirmOrder(state, viewModel)
                    2 -> Step3_SelectPaymentMethod(state, viewModel)
                    3 -> Step4_ProcessPayment(state, viewModel)
                }
            }

            BottomNavigationButtons(state, viewModel)

            if (state.error != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    state.error!!,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(16.dp)
                )
            }

            if (state.successMessage != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    state.successMessage!!,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(16.dp)
                )
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

        selectedProductForPreview?.let { product ->
            ProductDetailDialog(
                product = product,
                onDismiss = { selectedProductForPreview = null },
                onAddToCart = { viewModel.addToCart(product) },
                formatVnd = ::formatVND
            )
        }
    }
}

@Composable
private fun StepIndicator(currentStep: Int, steps: List<String>) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        steps.forEachIndexed { index, step ->
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                val isActive = index <= currentStep
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .padding(4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        (index + 1).toString(),
                        color = if (isActive) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                }
                Text(step, fontSize = 12.sp, style = MaterialTheme.typography.bodySmall)
            }
            if (index < steps.size - 1) {
                Box(modifier = Modifier.weight(1f).height(2.dp).padding(horizontal = 4.dp))
            }
        }
    }
}

@Composable
private fun Step1_SelectProducts(state: PosUiState, viewModel: PosViewModel) {
    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            modifier = Modifier.weight(0.6f),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                Text("Tìm sản phẩm", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = state.searchQuery,
                    onValueChange = viewModel::search,
                    label = { Text("Tên/SKU") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))

                if (state.isLoading) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(state.filteredProducts) { product ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { viewModel.addToCart(product) }
                                    .padding(vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(product.name, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                    Text("SKU: ${product.sku ?: "-"} | Ton: ${product.stockQuantity}", fontSize = 11.sp)
                                }
                                Text(formatVND(product.price), color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                            }
                            HorizontalDivider()
                        }
                    }
                }
            }
        }

        Card(
            modifier = Modifier.weight(0.4f),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                Text("Hoá đơn", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(12.dp))

                LazyColumn(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(state.cart) { item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(item.product.name, fontWeight = FontWeight.Medium, fontSize = 12.sp)
                                Text("${item.quantity} x ${formatVND(item.product.price)}", fontSize = 11.sp)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(
                                    onClick = { viewModel.updateQuantity(item.product.id, item.quantity - 1) },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(Icons.Default.Remove, contentDescription = "Giảm", modifier = Modifier.size(16.dp))
                                }
                                IconButton(
                                    onClick = { viewModel.updateQuantity(item.product.id, item.quantity + 1) },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = "Tăng", modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                        HorizontalDivider()
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                Text("Tạm tính: ${formatVND(state.subtotal)}", fontSize = 12.sp)
                Text("Giảm giá: ${formatVND(state.appliedDiscount)}", fontSize = 12.sp)
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                Text(
                    "Tổng: ${formatVND(state.total)}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun Step2_ConfirmOrder(state: PosUiState, viewModel: PosViewModel) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(0.6f),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(32.dp)) {
                Text("Xác nhận hoá đơn", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(24.dp))

                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(state.cart) { item ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("${item.product.name} x${item.quantity}")
                            Text(formatVND(item.product.price * item.quantity), fontWeight = FontWeight.SemiBold)
                        }
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Tạm tính:")
                    Text(formatVND(state.subtotal), fontWeight = FontWeight.SemiBold)
                }
                
                if (state.appliedDiscount > 0) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Giảm giá:")
                        Text("-${formatVND(state.appliedDiscount)}", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("TỔNG CỘNG:", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Text(formatVND(state.total), fontWeight = FontWeight.Bold, fontSize = 18.sp, color = MaterialTheme.colorScheme.primary)
                }

                Spacer(modifier = Modifier.height(32.dp))

                Text(
                    "Vui lòng kiểm tra lại và nhấn 'Tiếp tục' để chọn phương thức thanh toán",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun Step3_SelectPaymentMethod(state: PosUiState, viewModel: PosViewModel) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(0.6f),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(32.dp)) {
                Text("Chọn phương thức thanh toán", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(24.dp))

                val paymentOptions = listOf("CASH", "MOMO", "VNPAY", "ZALOPAY")
                var paymentExpanded by remember { mutableStateOf(false) }

                ExposedDropdownMenuBox(
                    expanded = paymentExpanded,
                    onExpandedChange = { paymentExpanded = it },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = state.paymentMethod,
                        onValueChange = {},
                        label = { Text("Phương thức") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = paymentExpanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor(),
                        readOnly = true
                    )
                    ExposedDropdownMenu(
                        expanded = paymentExpanded,
                        onDismissRequest = { paymentExpanded = false }
                    ) {
                        paymentOptions.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option) },
                                onClick = {
                                    viewModel.setPaymentMethod(option)
                                    paymentExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                OutlinedTextField(
                    value = state.customerCode,
                    onValueChange = viewModel::setCustomerCode,
                    label = { Text("Mã khách hàng (tùy chọn - để tích điểm)") },
                    placeholder = { Text("Số điện thoại hoặc mã khách") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(24.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Tổng tiền:", fontWeight = FontWeight.SemiBold)
                            Text(formatVND(state.total), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            when (state.paymentMethod) {
                                "CASH" -> "Sẽ nhập tiền mặt ở bước tiếp theo"
                                "MOMO" -> "Sẽ quét mã QR MOMO"
                                "VNPAY" -> "Sẽ quét mã QR VNPAY"
                                "ZALOPAY" -> "Sẽ quét mã QR ZALOPAY"
                                else -> ""
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun Step4_ProcessPayment(state: PosUiState, viewModel: PosViewModel) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (state.isSubmitting && !state.hasActiveOrder) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Column
        }

        when {
            state.paymentMethod == "CASH" -> {
                Card(
                    modifier = Modifier.fillMaxWidth(0.7f),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Thanh toán tiền mặt", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(24.dp))

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Số tiền cần thanh toán:")
                                    Text(formatVND(state.activeOrderTotal ?: state.total), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, fontSize = 18.sp)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        OutlinedTextField(
                            value = state.cashReceivedInput,
                            onValueChange = viewModel::setCashReceivedInput,
                            label = { Text("Số tiền khách đưa") },
                            modifier = Modifier.fillMaxWidth(),
                            suffix = { Text("đ") }
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        if (state.cashChangePreview != null) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Tiền thối lại:")
                                    Text(formatVND(state.cashChangePreview!!), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, fontSize = 16.sp)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        Button(
                            onClick = viewModel::confirmCashOrder,
                            enabled = state.cashChangePreview != null && !state.isSubmitting,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Xác nhận thanh toán", fontSize = 16.sp)
                        }
                    }
                }
            }

            state.isAwaitingGatewayPayment -> {
                PosQrPaymentPanel(state, viewModel)
            }

            state.hasActiveOrder && state.activeOrderPaymentStatus == "COMPLETED" -> {
                Card(
                    modifier = Modifier.fillMaxWidth(0.7f),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("✓ Thanh toán thành công", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.height(24.dp))

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Mã đơn:")
                                    Text(state.activeOrderCode ?: "-", fontWeight = FontWeight.Bold)
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Phương thức:")
                                    Text(state.activeOrderPaymentMethod ?: "-")
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Số tiền:")
                                    Text(formatVND(state.activeOrderTotal ?: 0.0), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        if (state.lastCompletedOrderId != null) {
                            Button(
                                onClick = viewModel::exportLastInvoicePdf,
                                enabled = !state.isArchivingInvoice,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(if (state.isArchivingInvoice) "Đang xuất PDF..." else "Xuất hoá đơn PDF")
                            }
                        }
                    }
                }
            }

            else -> {
                CircularProgressIndicator()
                Spacer(modifier = Modifier.height(16.dp))
                Text("Đang chuẩn bị thanh toán...")
            }
        }
    }
}

@Composable
private fun PosQrPaymentPanel(state: PosUiState, viewModel: PosViewModel) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(0.6f),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    "Quét mã ${state.activeOrderPaymentMethod}",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )

                state.gatewayPaymentReference?.let {
                    Text(
                        "Mã tham chiếu: $it",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                val qrBitmap = remember(state.gatewayQrContent) {
                    state.gatewayQrContent?.takeIf { it.isNotBlank() }?.let(::generateQrBitmap)
                }

                if (qrBitmap != null) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                bitmap = qrBitmap,
                                contentDescription = "QR Code",
                                modifier = Modifier.size(280.dp)
                            )
                        }
                    }
                } else {
                    Text(
                        "Lỗi: Không thể tạo QR code",
                        color = MaterialTheme.colorScheme.error
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Số tiền:", fontWeight = FontWeight.SemiBold)
                        Text(formatVND(state.activeOrderTotal ?: 0.0), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, fontSize = 16.sp)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    if (state.isPollingPayment) "⏳ Đang chờ thanh toán..." else "Hoàn tất thanh toán để tiếp tục",
                    style = MaterialTheme.typography.bodyMedium
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = viewModel::openGatewayPaymentPage,
                        enabled = !state.gatewayPaymentUrl.isNullOrBlank(),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Mo trang")
                    }
                    OutlinedButton(
                        onClick = viewModel::initPendingGatewayPayment,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Tạo lại QR")
                    }
                    Button(
                        onClick = viewModel::refreshPendingPaymentStatus,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(if (state.isPollingPayment) "Đang kiểm tra..." else "Kiểm tra lại")
                    }
                }
            }
        }
    }
}

@Composable
private fun BottomNavigationButtons(state: PosUiState, viewModel: PosViewModel) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (state.currentPaymentStep > 0 && state.activeOrderPaymentStatus != "COMPLETED") {
            OutlinedButton(
                onClick = viewModel::previousStep,
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Quay lại", modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Quay lại")
            }
        }

        if (state.activeOrderPaymentStatus != "COMPLETED") {
            Button(
                onClick = viewModel::nextStep,
                enabled = !state.isSubmitting || state.hasActiveOrder,
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    when (state.currentPaymentStep) {
                        0 -> "Tiếp tục"
                        1 -> "Chọn thanh toán"
                        2 -> "Tạo đơn"
                        else -> "Đang xử lý..."
                    }
                )
            }
        } else {
            Button(
                onClick = viewModel::resetPaymentFlow,
                modifier = Modifier.weight(1f)
            ) {
                Text("Hoàn tất & Tạo đơn mới")
            }
        }
    }
}

private fun generateQrBitmap(content: String, size: Int = 400): ImageBitmap {
    val matrix = QRCodeWriter().encode(content, BarcodeFormat.QR_CODE, size, size)
    val image = BufferedImage(size, size, BufferedImage.TYPE_INT_RGB)
    for (x in 0 until size) {
        for (y in 0 until size) {
            image.setRGB(x, y, if (matrix[x, y]) 0xFF000000.toInt() else 0xFFFFFFFF.toInt())
        }
    }
    return image.toComposeImageBitmap()
}

fun formatVND(amount: Double): String {
    return "%.0f đ".format(amount).replace(Regex("\\B(?=(\\d{3})+(?!\\d))"), ".")
}
