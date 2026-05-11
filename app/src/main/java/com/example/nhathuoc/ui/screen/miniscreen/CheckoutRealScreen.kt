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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
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
import com.example.nhathuoc.viewmodel.CheckoutViewModel
import java.text.NumberFormat
import java.util.Locale

private val checkoutLocale = Locale("vi", "VN")

// Design tokens
private val GreenLight = Color(0xFFE8F5E9)
private val GreenDark = Color(0xFF2E7D32)
private val TextPrimary = Color(0xFF1B2B1F)
private val TextSecondary = Color(0xFF5A7A62)
private val BgGray = Color(0xFFF3F7F4)
private val DividerColor = Color(0xFFE0EDE3)
private val CardBg = Color.White

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
    val actionLabel = if (state.paymentMethod == "COD") "Đặt hàng ngay" else "Tiếp tục thanh toán"

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
            Surface(shadowElevation = 2.dp, color = CardBg) {
                CenterAlignedTopAppBar(
                    title = {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                "Xác nhận đơn hàng",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            if (state.totalItems > 0) {
                                Text(
                                    "${state.totalItems} sản phẩm",
                                    fontSize = 12.sp,
                                    color = TextSecondary
                                )
                            }
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(BgGray),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Quay lại",
                                    tint = GreenTop,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = CardBg,
                        titleContentColor = Color(0xFF1B2B1F),
                        navigationIconContentColor = Color(0xFF1B2B1F)
                    )
                )
            }
        },
        snackbarHost = { SnackbarHost(hostState = snackbars) },
        bottomBar = {
            Surface(color = CardBg, shadowElevation = 12.dp) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 20.dp, vertical = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Total summary line
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Tổng thanh toán", fontSize = 13.sp, color = TextSecondary)
                        Text(
                            formatCurrency(state.total),
                            fontSize = 22.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = GreenTop
                        )
                    }
                    Button(
                        onClick = { viewModel.createOrder() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        enabled = !state.isLoading && selectedAddress != null && state.cartItems.isNotEmpty(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = GreenTop,
                            disabledContainerColor = Color(0xFFB0C4B1)
                        ),
                        shape = RoundedCornerShape(16.dp),
                        elevation = ButtonDefaults.buttonElevation(0.dp)
                    ) {
                        if (state.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(22.dp),
                                color = Color.White,
                                strokeWidth = 2.5.dp
                            )
                        } else {
                            Text(
                                actionLabel,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.3.sp
                            )
                        }
                    }
                }
            }
        },
        containerColor = BgGray
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Loading skeleton
            if (state.isLoading && state.cartItems.isEmpty()) {
                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        color = CardBg,
                        shadowElevation = 1.dp
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = GreenTop)
                        }
                    }
                }
            }

            // Delivery address
            item {
                CheckoutSectionCard(
                    title = "Địa chỉ giao hàng",
                    icon = Icons.Filled.LocationOn,
                    action = {
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            TextButton(
                                onClick = { navController.navigate("AddressSelectionScreen") },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    if (selectedAddress == null) "Chọn" else "Đổi",
                                    color = GreenTop,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            if (selectedAddress == null) {
                                TextButton(
                                    onClick = { navController.navigate("AddAddressScreen") },
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        "+ Thêm",
                                        color = GreenTop,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                ) {
                    if (selectedAddress == null) {
                        NoAddressPlaceholder()
                    } else {
                        SelectedAddressCard(selectedAddress)
                    }
                }
            }

            // Payment method
            item {
                CheckoutSectionCard(
                    title = "Phương thức thanh toán",
                    icon = Icons.Outlined.CreditCard
                ) {
                    PaymentMethodSection(
                        selectedMethod = state.paymentMethod,
                        onSelected = viewModel::setPaymentMethod
                    )
                }
            }

            // Promo / points
            item {
                CheckoutSectionCard(
                    title = "Ưu đãi & Điểm thưởng",
                    icon = Icons.Filled.LocalOffer
                ) {
                    OutlinedTextField(
                        value = state.promoCode,
                        onValueChange = viewModel::setPromoCode,
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        label = { Text("Mã giảm giá", fontSize = 13.sp) },
                        leadingIcon = {
                            Icon(
                                Icons.Filled.LocalOffer,
                                contentDescription = null,
                                tint = GreenTop,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GreenTop,
                            unfocusedBorderColor = Color(0xFFE0EDE3),
                            focusedLabelColor = GreenTop,
                            unfocusedLabelColor = Color(0xFF5A7A62),
                            focusedTextColor = Color(0xFF1B2B1F),
                            unfocusedTextColor = Color(0xFF1B2B1F),
                            cursorColor = GreenTop,
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color(0xFFFAFCFA)
                        )
                    )
                    Spacer(Modifier.height(10.dp))
                    OutlinedTextField(
                        value = state.pointsInput,
                        onValueChange = viewModel::setPointsInput,
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        label = { Text("Điểm thưởng sử dụng", fontSize = 13.sp) },
                        leadingIcon = {
                            Icon(
                                Icons.Filled.Stars,
                                contentDescription = null,
                                tint = Color(0xFFFFA000),
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        supportingText = {
                            Text(
                                "Khả dụng: ${state.availableRewardPoints} điểm",
                                color = TextSecondary,
                                fontSize = 12.sp
                            )
                        },
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GreenTop,
                            unfocusedBorderColor = Color(0xFFE0EDE3),
                            focusedLabelColor = GreenTop,
                            unfocusedLabelColor = Color(0xFF5A7A62),
                            focusedTextColor = Color(0xFF1B2B1F),
                            unfocusedTextColor = Color(0xFF1B2B1F),
                            cursorColor = GreenTop,
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color(0xFFFAFCFA)
                        )
                    )
                }
            }

            // Note
            item {
                CheckoutSectionCard(
                    title = "Ghi chú đơn hàng",
                    icon = Icons.Filled.Notes
                ) {
                    OutlinedTextField(
                        value = state.note,
                        onValueChange = viewModel::setNote,
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                        label = { Text("Ví dụ: Gọi trước khi giao...", fontSize = 13.sp) },
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GreenTop,
                            unfocusedBorderColor = Color(0xFFE0EDE3),
                            focusedLabelColor = GreenTop,
                            unfocusedLabelColor = Color(0xFF5A7A62),
                            focusedTextColor = Color(0xFF1B2B1F),
                            unfocusedTextColor = Color(0xFF1B2B1F),
                            cursorColor = GreenTop,
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color(0xFFFAFCFA)
                        )
                    )
                }
            }

            // Cart items
            item {
                CheckoutSectionCard(
                    title = "Sản phẩm",
                    icon = Icons.Outlined.Storefront,
                    action = {
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = GreenLight
                        ) {
                            Text(
                                "${state.totalItems} sản phẩm",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = GreenTop,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }
                ) {
                    if (state.cartItems.isEmpty()) {
                        Text("Giỏ hàng đang trống.", color = TextSecondary)
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            state.cartItems.forEach { CartItemRow(it) }
                        }
                    }
                }
            }

            // Order summary
            item {
                CheckoutSectionCard(
                    title = "Tóm tắt thanh toán",
                    icon = Icons.Outlined.Receipt
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        SummaryRow("Tạm tính", state.subtotal)
                        SummaryRow("Giảm giá", -state.discount)
                        if (state.pointsToUse > 0)
                            SummaryRow("Điểm thưởng", -state.pointsToUse.toDouble())
                        SummaryRow("Phí vận chuyển", state.shipping)
                        SummaryRow("Thuế VAT", state.tax)
                        Spacer(Modifier.height(4.dp))
                        HorizontalDivider(color = DividerColor)
                        Spacer(Modifier.height(4.dp))
                        SummaryRow("Tổng cộng", state.total, emphasize = true)
                        Spacer(Modifier.height(8.dp))
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            color = GreenLight
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    Icons.Outlined.LocalShipping,
                                    contentDescription = null,
                                    tint = GreenTop,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = deliveryDescriptionFor(state.paymentMethod),
                                    color = TextSecondary,
                                    fontSize = 12.sp,
                                    lineHeight = 17.sp
                                )
                            }
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun NoAddressPlaceholder() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFFFFF8E1))
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            Icons.Filled.LocationOn,
            contentDescription = null,
            tint = Color(0xFFFF8F00),
            modifier = Modifier.size(24.dp)
        )
        Text(
            "Bạn chưa chọn địa chỉ giao hàng",
            color = Color(0xFF795548),
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun CheckoutSectionCard(
    title: String,
    icon: ImageVector,
    action: (@Composable () -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = CardBg,
        shadowElevation = 2.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(GreenLight),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            icon,
                            contentDescription = null,
                            tint = GreenTop,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Text(
                        title,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        fontSize = 15.sp
                    )
                }
                action?.invoke()
            }
            Spacer(Modifier.height(14.dp))
            HorizontalDivider(color = DividerColor)
            Spacer(Modifier.height(14.dp))
            content()
        }
    }
}

@Composable
private fun SelectedAddressCard(address: UserAddress) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(GreenLight),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Filled.LocationOn,
                contentDescription = null,
                tint = GreenTop,
                modifier = Modifier.size(22.dp)
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    address.recipientName,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = TextPrimary
                )
                if (address.isDefault) {
                    Surface(shape = RoundedCornerShape(20.dp), color = GreenLight) {
                        Text(
                            "Mặc định",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = GreenTop,
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                        )
                    }
                }
            }
            Spacer(Modifier.height(3.dp))
            Text(address.recipientPhone, fontSize = 13.sp, color = TextSecondary)
            Spacer(Modifier.height(4.dp))
            Text(
                text = listOf(address.fullAddress, address.ward, address.district, address.province)
                    .filter { it.isNotBlank() }
                    .joinToString(", "),
                color = Color(0xFF4B5563),
                fontSize = 13.sp,
                lineHeight = 19.sp
            )
        }
    }
}

@Composable
private fun PaymentMethodSection(
    selectedMethod: String,
    onSelected: (String) -> Unit
) {
    val methods = listOf(
        Triple("COD", "Thanh toán khi nhận hàng", Icons.Outlined.Money),
        Triple("MOMO", "Ví MoMo", Icons.Outlined.AccountBalanceWallet),
        Triple("ZALOPAY", "ZaloPay", Icons.Outlined.AccountBalance)
    )
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        methods.forEach { (value, label, icon) ->
            val isSelected = selectedMethod == value
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp)),
                shape = RoundedCornerShape(14.dp),
                color = if (isSelected) GreenLight else Color(0xFFF8FAF8),
                border = BorderStroke(
                    if (isSelected) 2.dp else 1.dp,
                    if (isSelected) GreenTop else DividerColor
                )
            ) {
                Row(
                    modifier = Modifier
                        .clickable { onSelected(value) }
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSelected) GreenTop else Color(0xFFEEF2EE)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            icon,
                            contentDescription = null,
                            tint = if (isSelected) Color.White else TextSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Text(
                        label,
                        fontSize = 14.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) GreenDark else TextPrimary,
                        modifier = Modifier.weight(1f)
                    )
                    RadioButton(
                        selected = isSelected,
                        onClick = { onSelected(value) },
                        colors = RadioButtonDefaults.colors(selectedColor = GreenTop, unselectedColor = Color(0xFF9CA3AF))
                    )
                }
            }
        }
    }
}

@Composable
private fun CartItemRow(item: CartItemDto) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Product index indicator
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(GreenLight),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Outlined.MedicalServices,
                contentDescription = null,
                tint = GreenTop,
                modifier = Modifier.size(18.dp)
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.displayName,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                color = TextPrimary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(2.dp))
            Text(
                "${item.quantity} ${item.unit} × ${formatCurrency(item.unitPrice)}",
                fontSize = 12.sp,
                color = TextSecondary
            )
        }
        Text(
            text = formatCurrency(item.totalPrice),
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            color = GreenTop
        )
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
            fontSize = if (emphasize) 16.sp else 14.sp,
            fontWeight = if (emphasize) FontWeight.Bold else FontWeight.Normal,
            color = if (emphasize) TextPrimary else TextSecondary
        )
        Text(
            text = formatCurrency(value),
            fontSize = if (emphasize) 18.sp else 14.sp,
            fontWeight = if (emphasize) FontWeight.ExtraBold else FontWeight.SemiBold,
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
            Surface(shadowElevation = 2.dp, color = CardBg) {
                CenterAlignedTopAppBar(
                    title = { Text(title, fontWeight = FontWeight.Bold, color = TextPrimary) },
                    navigationIcon = {
                        IconButton(onClick = onClose) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(BgGray),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.AutoMirrored.Filled.ArrowBack,
                                    tint = GreenTop,
                                    modifier = Modifier.size(20.dp),
                                    contentDescription = "Đóng"
                                )
                            }
                        }
                    },
                    actions = {
                        TextButton(onClick = onCheckStatus) {
                            Text("Kiểm tra", color = GreenTop, fontWeight = FontWeight.Bold)
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = CardBg,
                        titleContentColor = Color(0xFF1B2B1F),
                        navigationIconContentColor = Color(0xFF1B2B1F)
                    )
                )
            }
        },
        containerColor = CardBg
    ) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = when (paymentStatus?.status) {
                    "COMPLETED" -> Color(0xFFE8F5E9)
                    "PENDING" -> Color(0xFFFFF8E1)
                    else -> BgGray
                }
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        when (paymentStatus?.status) {
                            "COMPLETED" -> Icons.Filled.CheckCircle
                            else -> Icons.Outlined.Info
                        },
                        contentDescription = null,
                        tint = when (paymentStatus?.status) {
                            "COMPLETED" -> GreenTop
                            else -> Color(0xFFFF8F00)
                        },
                        modifier = Modifier.size(20.dp)
                    )
                    Column {
                        Text(
                            "Đơn hàng: $orderId",
                            color = GreenTop,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                        Text(
                            text = when (paymentStatus?.status) {
                                "COMPLETED" -> "Thanh toán đã hoàn tất."
                                "PENDING" -> "Đang chờ xác nhận từ cổng thanh toán."
                                else -> "Hoàn tất thanh toán rồi quay lại ứng dụng."
                            },
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                    }
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
                    override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                        if (request?.url?.toString()?.startsWith("app://payment/callback") == true) {
                            onReturnUrlDetected(); return true
                        }
                        return false
                    }
                    override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                        if (url?.startsWith("app://payment/callback") == true) {
                            onReturnUrlDetected(); return true
                        }
                        return false
                    }
                    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                        super.onPageStarted(view, url, favicon)
                        if (url?.startsWith("app://payment/callback") == true) onReturnUrlDetected()
                    }
                }
                loadUrl(url)
            }
        },
        update = { webView -> if (webView.url != url) webView.loadUrl(url) }
    )
}

private fun paymentTitleFor(method: String) = when (method) {
    "MOMO" -> "Thanh toán MoMo"
    "ZALOPAY" -> "Thanh toán ZaloPay"
    else -> "Thanh toán"
}

private fun deliveryDescriptionFor(method: String) = when (method) {
    "COD" -> "Bạn sẽ thanh toán trực tiếp khi nhận được hàng."
    "MOMO" -> "Bạn sẽ được chuyển sang ví MoMo để thanh toán."
    "ZALOPAY" -> "Bạn sẽ được chuyển sang cổng ZaloPay để thanh toán."
    else -> "Vui lòng kiểm tra kỹ thông tin trước khi xác nhận."
}

private fun formatCurrency(value: Double): String {
    return NumberFormat.getCurrencyInstance(checkoutLocale).format(value).replace("₫", "đ")
}
