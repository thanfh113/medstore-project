package com.example.nhathuoc.ui.screen.miniscreen

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material.icons.outlined.CreditCard
import androidx.compose.material.icons.outlined.LocalShipping
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.nhathuoc.data.model.CartItemDto
import com.example.nhathuoc.data.model.PaymentStatusDto
import com.example.nhathuoc.data.model.UserAddress
import com.example.nhathuoc.ui.theme.GreenTop
import com.example.nhathuoc.viewmodel.CheckoutState
import com.example.nhathuoc.viewmodel.CheckoutViewModel
import java.text.NumberFormat
import java.util.Locale

private val checkoutLocale = Locale("vi", "VN")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutRealScreen(
    modifier: Modifier = Modifier,
    navController: NavController,
    viewModel: CheckoutViewModel = hiltViewModel()
) {
    val state by viewModel.checkoutState.collectAsState()
    val orderCreated by viewModel.orderCreatedEvent.collectAsState()
    val paymentUrl by viewModel.paymentUrlEvent.collectAsState()
    val snackbars = remember { SnackbarHostState() }

    var activeGatewayUrl by rememberSaveable { mutableStateOf<String?>(null) }
    var activeOrderId by rememberSaveable { mutableStateOf<String?>(null) }

    val selectedAddress = state.addresses.firstOrNull { it.id == state.selectedAddressId }
    val actionLabel = if (state.paymentMethod == "COD") "Đặt hàng" else "Tiếp tục thanh toán"

    LaunchedEffect(orderCreated?.id, state.paymentMethod) {
        val created = orderCreated ?: return@LaunchedEffect
        if (state.paymentMethod == "COD") {
            viewModel.consumeOrderCreatedEvent()
            navController.navigate("OrderConfirmationScreen/${created.id}") {
                launchSingleTop = true
                popUpTo("CheckoutScreen") { inclusive = true }
            }
        } else {
            activeOrderId = created.id
        }
    }

    LaunchedEffect(paymentUrl) {
        val url = paymentUrl ?: return@LaunchedEffect
        activeGatewayUrl = url
        viewModel.consumePaymentUrl()
    }

    LaunchedEffect(state.error) {
        val message = state.error ?: return@LaunchedEffect
        snackbars.showSnackbar(message)
        viewModel.clearError()
    }

    LaunchedEffect(state.paymentStatus?.status, activeOrderId) {
        val status = state.paymentStatus?.status ?: return@LaunchedEffect
        val orderId = activeOrderId ?: return@LaunchedEffect
        if (status == "COMPLETED") {
            activeGatewayUrl = null
            activeOrderId = null
            viewModel.consumeOrderCreatedEvent()
            navController.navigate("OrderDetailScreen/$orderId") {
                launchSingleTop = true
                popUpTo("CheckoutScreen") { inclusive = true }
            }
        }
    }

    if (activeGatewayUrl != null && activeOrderId != null) {
        CheckoutGatewayWebView(
            title = paymentTitleFor(state.paymentMethod),
            url = activeGatewayUrl!!,
            orderId = activeOrderId!!,
            paymentStatus = state.paymentStatus,
            onClose = { activeGatewayUrl = null },
            onCheckStatus = { viewModel.pollPaymentStatus(activeOrderId!!, maxAttempts = 8) },
            onReturnUrlDetected = { viewModel.pollPaymentStatus(activeOrderId!!, maxAttempts = 8) }
        )
        return
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Thanh toán") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Quay lại"
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = GreenTop,
                    navigationIconContentColor = GreenTop
                )
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbars) },
        bottomBar = {
            Surface(
                color = Color.White,
                shadowElevation = 8.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Tổng thanh toán: ${formatCurrency(state.total)}",
                        color = GreenTop,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Button(
                        onClick = { viewModel.createOrder() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        enabled = !state.isLoading && selectedAddress != null && state.cartItems.isNotEmpty(),
                        colors = ButtonDefaults.buttonColors(containerColor = GreenTop),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        if (state.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(actionLabel, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        },
        containerColor = Color(0xFFF5F7FA)
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                if (state.isLoading && state.cartItems.isEmpty()) {
                    ElevatedCard(
                        colors = CardDefaults.elevatedCardColors(containerColor = Color.White)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = GreenTop)
                        }
                    }
                }
            }

            item {
                CheckoutSectionCard(
                    title = "Địa chỉ giao hàng",
                    icon = Icons.Filled.LocationOn,
                    action = {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            TextButton(onClick = { navController.navigate("AddressSelectionScreen") }) {
                                Text(if (selectedAddress == null) "Chọn địa chỉ" else "Thay đổi")
                            }
                            TextButton(onClick = { navController.navigate("AddAddressScreen") }) {
                                Text("Thêm mới")
                            }
                        }
                    }
                ) {
                    if (selectedAddress == null) {
                        Text(
                            text = "Bạn chưa chọn địa chỉ giao hàng.",
                            color = Color(0xFF8A8F98)
                        )
                    } else {
                        SelectedAddressCard(selectedAddress)
                    }
                }
            }

            item {
                CheckoutSectionCard(
                    title = "Phương thức thanh toán",
                    icon = Icons.Filled.Payments
                ) {
                    PaymentMethodSection(
                        selectedMethod = state.paymentMethod,
                        onSelected = viewModel::setPaymentMethod
                    )
                }
            }

            item {
                CheckoutSectionCard(
                    title = "Mã giảm giá và điểm thưởng",
                    icon = Icons.Filled.LocalOffer
                ) {
                    OutlinedTextField(
                        value = state.promoCode,
                        onValueChange = viewModel::setPromoCode,
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        label = { Text("Mã giảm giá") },
                        shape = RoundedCornerShape(14.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = state.pointsInput,
                        onValueChange = viewModel::setPointsInput,
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        label = { Text("Điểm thưởng sử dụng") },
                        supportingText = {
                            Text("Điểm khả dụng: ${state.availableRewardPoints}")
                        },
                        shape = RoundedCornerShape(14.dp)
                    )
                }
            }

            item {
                CheckoutSectionCard(
                    title = "Ghi chú cho đơn hàng",
                    icon = Icons.Filled.Notes
                ) {
                    OutlinedTextField(
                        value = state.note,
                        onValueChange = viewModel::setNote,
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                        label = { Text("Ghi chú") },
                        shape = RoundedCornerShape(14.dp)
                    )
                }
            }

            item {
                CheckoutSectionCard(
                    title = "Sản phẩm trong giỏ",
                    icon = Icons.Outlined.Storefront,
                    action = {
                        AssistChip(
                            onClick = {},
                            enabled = false,
                            label = { Text("${state.totalItems} sản phẩm") },
                            colors = AssistChipDefaults.assistChipColors(
                                disabledContainerColor = Color(0xFFE8F5E9),
                                disabledLabelColor = GreenTop
                            )
                        )
                    }
                ) {
                    if (state.cartItems.isEmpty()) {
                        Text("Giỏ hàng của bạn đang trống.")
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            state.cartItems.forEach { item ->
                                CartItemRow(item)
                            }
                        }
                    }
                }
            }

            item {
                CheckoutSectionCard(
                    title = "Tóm tắt thanh toán",
                    icon = Icons.Outlined.CreditCard
                ) {
                    SummaryRow("Tạm tính", state.subtotal)
                    SummaryRow("Giảm giá", state.discount)
                    SummaryRow("Điểm thưởng", state.pointsToUse * 1000.0)
                    SummaryRow("Phí vận chuyển", state.shipping)
                    SummaryRow("Thuế VAT", state.tax)
                    Spacer(modifier = Modifier.height(8.dp))
                    SummaryRow("Tổng cộng", state.total, emphasize = true)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = deliveryDescriptionFor(state.paymentMethod),
                        color = Color(0xFF6B7280),
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun CheckoutSectionCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    action: @Composable (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    ElevatedCard(
        colors = CardDefaults.elevatedCardColors(containerColor = Color.White),
        shape = RoundedCornerShape(18.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(icon, contentDescription = null, tint = GreenTop)
                    Spacer(modifier = Modifier.size(10.dp))
                    Text(title, fontWeight = FontWeight.Bold, color = GreenTop, fontSize = 18.sp)
                }
                action?.invoke()
            }
            Spacer(modifier = Modifier.height(16.dp))
            content()
        }
    }
}

@Composable
private fun SelectedAddressCard(address: UserAddress) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color(0xFFF7FBF7),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = address.recipientName,
                    fontWeight = FontWeight.Bold,
                    color = GreenTop
                )
                if (address.isDefault) {
                    AssistChip(
                        onClick = {},
                        enabled = false,
                        label = { Text("Mặc định") },
                        colors = AssistChipDefaults.assistChipColors(
                            disabledContainerColor = Color(0xFFE8F5E9),
                            disabledLabelColor = GreenTop
                        )
                    )
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(address.recipientPhone)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = listOf(address.fullAddress, address.ward, address.district, address.province)
                    .filter { it.isNotBlank() }
                    .joinToString(", "),
                color = Color(0xFF4B5563)
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PaymentMethodSection(
    selectedMethod: String,
    onSelected: (String) -> Unit
) {
    val methods = listOf(
        "COD" to "Thanh toán khi nhận hàng",
        "MOMO" to "MoMo",
        "ZALOPAY" to "ZaloPay"
    )
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        methods.forEach { (value, label) ->
            FilterChip(
                selected = selectedMethod == value,
                onClick = { onSelected(value) },
                label = { Text(label) }
            )
        }
    }
}

@Composable
private fun CartItemRow(item: CartItemDto) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color(0xFFF8FAFC),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0))
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.displayName,
                    fontWeight = FontWeight.Bold,
                    color = GreenTop,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text("SKU: ${item.product?.sku ?: "Đang cập nhật"}", color = Color(0xFF6B7280))
                Text(
                    "${item.quantity} x ${formatCurrency(item.unitPrice)} • ${item.unit}",
                    color = Color(0xFF6B7280)
                )
            }
            Spacer(modifier = Modifier.size(12.dp))
            Text(
                text = formatCurrency(item.totalPrice),
                fontWeight = FontWeight.Bold,
                color = GreenTop
            )
        }
    }
}

@Composable
private fun SummaryRow(label: String, value: Double, emphasize: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = if (emphasize) 17.sp else 15.sp,
            fontWeight = if (emphasize) FontWeight.Bold else FontWeight.Medium,
            color = if (emphasize) GreenTop else Color(0xFF374151)
        )
        Text(
            text = formatCurrency(value),
            fontSize = if (emphasize) 18.sp else 15.sp,
            fontWeight = if (emphasize) FontWeight.Bold else FontWeight.SemiBold,
            color = if (emphasize) GreenTop else Color(0xFF111827)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CheckoutGatewayWebView(
    title: String,
    url: String,
    orderId: String,
    paymentStatus: PaymentStatusDto?,
    onClose: () -> Unit,
    onCheckStatus: () -> Unit,
    onReturnUrlDetected: () -> Unit
) {
    BackHandler(onBack = onClose)

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    IconButton(onClick = onClose) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Đóng"
                        )
                    }
                },
                actions = {
                    TextButton(onClick = onCheckStatus) {
                        Text("Kiểm tra")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = GreenTop,
                    navigationIconContentColor = GreenTop,
                    actionIconContentColor = GreenTop
                )
            )
        },
        containerColor = Color.White
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color(0xFFF8FAFC)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Đơn hàng: $orderId", color = GreenTop, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = when (paymentStatus?.status) {
                            "COMPLETED" -> "Thanh toán đã hoàn tất."
                            "PENDING" -> "Đang chờ xác nhận từ cổng thanh toán."
                            else -> "Hoàn tất thanh toán rồi quay lại ứng dụng."
                        },
                        color = Color(0xFF6B7280)
                    )
                }
            }
            GatewayWebView(
                modifier = Modifier.weight(1f),
                url = url,
                onReturnUrlDetected = onReturnUrlDetected
            )
        }
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
private fun GatewayWebView(
    modifier: Modifier = Modifier,
    url: String,
    onReturnUrlDetected: () -> Unit
) {
    val context = LocalContext.current

    AndroidView(
        modifier = modifier.fillMaxSize(),
        factory = {
            WebView(context).apply {
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                webChromeClient = WebChromeClient()
                webViewClient = object : WebViewClient() {
                    override fun shouldOverrideUrlLoading(
                        view: WebView?,
                        request: WebResourceRequest?
                    ): Boolean {
                        val target = request?.url?.toString().orEmpty()
                        if (target.startsWith("app://payment/callback")) {
                            onReturnUrlDetected()
                            return true
                        }
                        return false
                    }

                    override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                        if (url?.startsWith("app://payment/callback") == true) {
                            onReturnUrlDetected()
                            return true
                        }
                        return false
                    }

                    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                        super.onPageStarted(view, url, favicon)
                        if (url?.startsWith("app://payment/callback") == true) {
                            onReturnUrlDetected()
                        }
                    }
                }
                loadUrl(url)
            }
        },
        update = { webView ->
            if (webView.url != url) {
                webView.loadUrl(url)
            }
        }
    )
}

private fun paymentTitleFor(method: String): String = when (method) {
    "MOMO" -> "Thanh toán MoMo"
    "ZALOPAY" -> "Thanh toán ZaloPay"
    else -> "Thanh toán"
}

private fun deliveryDescriptionFor(method: String): String = when (method) {
    "COD" -> "Bạn sẽ thanh toán khi đơn hàng được giao tới."
    "MOMO" -> "Bạn sẽ được chuyển sang trang thanh toán MoMo."
    "ZALOPAY" -> "Bạn sẽ được chuyển sang cổng thanh toán ZaloPay."
    else -> "Vui lòng kiểm tra kỹ thông tin trước khi xác nhận."
}

private fun formatCurrency(value: Double): String {
    return NumberFormat.getCurrencyInstance(checkoutLocale)
        .format(value)
        .replace("₫", "đ")
}
