package com.example.nhathuoc.ui.screen.miniscreen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Redeem
import androidx.compose.material.icons.outlined.CreditCard
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.nhathuoc.PaymentReturnBus
import coil.compose.AsyncImage
import com.example.nhathuoc.data.model.CartItemDto
import com.example.nhathuoc.data.model.RewardVoucherDto
import com.example.nhathuoc.data.model.UserAddress
import com.example.nhathuoc.ui.theme.GreenTop
import com.example.nhathuoc.viewmodel.CheckoutViewModel
import java.text.NumberFormat
import java.util.Locale
import kotlinx.coroutines.delay

private val checkoutFlowLocale: Locale = Locale.Builder()
    .setLanguage("vi")
    .setRegion("VN")
    .build()

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutFlowScreen(
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
    var activeOrderPaymentMethod by rememberSaveable { mutableStateOf<String?>(null) }
    var checkoutNotice by rememberSaveable { mutableStateOf<String?>(null) }
    var showVoucherSheet by rememberSaveable { mutableStateOf(false) }

    val selectedAddress = state.addresses.firstOrNull { it.id == state.selectedAddressId }
    val selectedVoucher = state.availableVouchers.firstOrNull {
        it.code.equals(state.selectedVoucherCode, ignoreCase = true)
    }
    val hasPendingGatewayOrder = activeOrderId != null &&
            activeOrderPaymentMethod == state.paymentMethod &&
            state.paymentMethod != "COD" &&
            state.paymentStatus?.status != "COMPLETED"
    val primaryActionLabel = when {
        hasPendingGatewayOrder -> "Tiếp tục thanh toán"
        state.paymentMethod == "COD" -> "Đặt hàng"
        else -> "Tiếp tục thanh toán"
    }

    LaunchedEffect(orderCreated?.id, state.paymentMethod) {
        val created = orderCreated ?: return@LaunchedEffect
        if (state.paymentMethod == "COD") {
            activeOrderId = null
            activeOrderPaymentMethod = null
            viewModel.consumeOrderCreatedEvent()
            navController.navigate("OrderConfirmationScreen/${created.id}") {
                launchSingleTop = true
                popUpTo("CheckoutScreen") { inclusive = true }
            }
        } else {
            activeOrderId = created.id
            activeOrderPaymentMethod = state.paymentMethod
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

    LaunchedEffect(Unit) {
        val previousHandle = navController.previousBackStackEntry?.savedStateHandle
        val resumeOrderId = previousHandle?.get<String>("resumeOrderId")
        if (!resumeOrderId.isNullOrBlank()) {
            val resumeMethod = previousHandle.get<String>("resumePaymentMethod")
                ?.uppercase()
                ?.takeIf { it != "COD" }
                ?: "MOMO"
            previousHandle.remove<String>("resumeOrderId")
            previousHandle.remove<String>("resumePaymentMethod")
            viewModel.setPaymentMethod(resumeMethod)
            activeOrderId = resumeOrderId
            activeOrderPaymentMethod = resumeMethod
            viewModel.resumeOnlinePayment(resumeOrderId)
            return@LaunchedEffect
        }

        val directProductId = previousHandle?.get<String>("directProductId")
        if (!directProductId.isNullOrBlank()) {
            val directQuantity = previousHandle.get<Int>("directQuantity") ?: 1
            val directUnit = previousHandle.get<String>("directUnit")
            previousHandle.remove<String>("directProductId")
            previousHandle.remove<Int>("directQuantity")
            previousHandle.remove<String>("directUnit")
            viewModel.startDirectCheckout(directProductId, directQuantity, directUnit)
        } else {
            val selectedCartItemIds = previousHandle?.get<ArrayList<String>>("selectedCartItemIds")
            if (!selectedCartItemIds.isNullOrEmpty()) {
                previousHandle.remove<ArrayList<String>>("selectedCartItemIds")
                viewModel.startSelectedCartCheckout(selectedCartItemIds)
            }
        }
    }

    LaunchedEffect(checkoutNotice) {
        val message = checkoutNotice ?: return@LaunchedEffect
        snackbars.showSnackbar(message)
        checkoutNotice = null
    }

    LaunchedEffect(activeGatewayUrl, activeOrderId) {
        val gatewayUrl = activeGatewayUrl ?: return@LaunchedEffect
        val orderId = activeOrderId ?: return@LaunchedEffect
        while (activeGatewayUrl == gatewayUrl && activeOrderId == orderId) {
            viewModel.refreshPaymentStatus(orderId)
            delay(2500)
        }
    }

    LaunchedEffect(Unit) {
        PaymentReturnBus.events.collect { uri ->
            val orderId = activeOrderId ?: return@collect
            val returnedOrderId = uri.getQueryParameter("orderId")
            if (returnedOrderId == null || returnedOrderId == orderId) {
                activeGatewayUrl = null
                viewModel.pollPaymentStatus(orderId, maxAttempts = 8)
            }
        }
    }

    LaunchedEffect(state.paymentStatus?.status, activeOrderId) {
        val status = state.paymentStatus?.status ?: return@LaunchedEffect
        val orderId = activeOrderId ?: return@LaunchedEffect
        if (status == "COMPLETED") {
            activeGatewayUrl = null
            activeOrderId = null
            activeOrderPaymentMethod = null
            viewModel.consumeOrderCreatedEvent()
            navController.navigate("OrderDetailScreen/$orderId") {
                launchSingleTop = true
                popUpTo("CheckoutScreen") { inclusive = true }
            }
        }
    }

    if (activeGatewayUrl != null && activeOrderId != null) {
        PaymentWebViewScreen(
            title = paymentTitleFor(state.paymentMethod),
            url = activeGatewayUrl!!,
            orderId = activeOrderId!!,
            paymentStatus = state.paymentStatus,
            onClose = {
                activeGatewayUrl = null
                checkoutNotice = "Đã quay lại checkout. Bạn có thể tiếp tục hoặc đổi phương thức thanh toán."
            },
            onCheckStatus = { viewModel.refreshPaymentStatus(activeOrderId!!, showErrorOnFailure = true) },
            onReturnUrlDetected = { viewModel.refreshPaymentStatus(activeOrderId!!, showErrorOnFailure = true) }
        )
        return
    }

    if (showVoucherSheet) {
        VoucherBottomSheet(
            vouchers = state.availableVouchers,
            selectedVoucherCode = state.selectedVoucherCode,
            subtotal = state.subtotal,
            onDismiss = { showVoucherSheet = false },
            onSelectVoucher = { code ->
                viewModel.selectVoucher(code)
                showVoucherSheet = false
            }
        )
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Thanh toán") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Quay lại")
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
            Surface(color = Color.White, shadowElevation = 8.dp) {
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
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Button(
                        onClick = {
                            if (hasPendingGatewayOrder && activeOrderId != null) {
                                viewModel.resumeOnlinePayment(activeOrderId!!)
                            } else {
                                activeOrderId = null
                                activeOrderPaymentMethod = null
                                viewModel.clearPaymentProgress()
                                viewModel.createOrder()
                            }
                        },
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
                            Text(primaryActionLabel, fontSize = 16.sp, fontWeight = FontWeight.Bold)
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
            if (state.isLoading && state.cartItems.isEmpty()) {
                item {
                    ElevatedCard {
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
                ) {
                    if (selectedAddress == null) {
                        Text("Bạn chưa chọn địa chỉ giao hàng.", color = Color(0xFF8A8F98))
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
                    VoucherPickerSection(
                        vouchers = state.availableVouchers,
                        selectedVoucher = selectedVoucher,
                        selectedVoucherCode = state.selectedVoucherCode,
                        onOpenVoucherSheet = { showVoucherSheet = true },
                        onClearVoucher = { viewModel.selectVoucher(null) }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    RewardInfoCard(
                        availablePoints = state.availableRewardPoints,
                        estimatedPoints = state.estimatedRewardPoints,
                        pointsToUse = state.pointsToUse,
                        maxUsablePoints = state.maxUsableRewardPoints,
                        useRewardPoints = state.useRewardPoints,
                        onToggleUsePoints = viewModel::toggleUseRewardPoints
                    )
                }
            }

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
                            state.cartItems.forEach { item -> CartItemRow(item) }
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
                    if (state.pointsToUse > 0) {
                        SummaryRow("Điểm thưởng đã dùng", -(state.pointsToUse * 1000.0))
                    }
                    RewardSummaryRow("Điểm thưởng nhận được", "+${state.estimatedRewardPoints} điểm")
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
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Title chiếm phần còn lại SAU khi action đã đo xong
                Row(
                    modifier = Modifier.weight(1f, fill = false),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(icon, contentDescription = null, tint = GreenTop, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        title,
                        fontWeight = FontWeight.Bold,
                        color = GreenTop,
                        fontSize = 15.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                // Action buttons không bị clip và không đẩy title
                if (action != null) {
                    Spacer(modifier = Modifier.width(8.dp))
                    action()
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
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
                Text(address.recipientName, fontWeight = FontWeight.Bold, color = GreenTop)
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

@Composable
private fun PaymentMethodSection(
    selectedMethod: String,
    onSelected: (String) -> Unit
) {
    val methods = listOf(
        Triple("COD", "Thanh toán khi nhận hàng", "Trả tiền mặt khi nhận hàng. Đơn sẽ được nhân viên xác nhận."),
        Triple("MOMO", "Ví MoMo", "Mở app MoMo UAT hoặc web thanh toán để hoàn tất."),
        Triple("ZALOPAY", "ZaloPay", "Mở ZaloPay sandbox hoặc trang thanh toán ZaloPay.")
    )
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        methods.forEach { (value, title, description) ->
            PaymentMethodItem(
                title = title,
                description = description,
                selected = selectedMethod == value,
                onClick = { onSelected(value) }
            )
        }
    }
}

@Composable
private fun PaymentMethodItem(
    title: String,
    description: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        color = if (selected) Color(0xFFF0F9F1) else Color(0xFFF8FAFC),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(
            1.dp,
            if (selected) GreenTop else Color(0xFFE2E8F0)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                color = if (selected) GreenTop else Color.White,
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, if (selected) GreenTop else Color(0xFFE2E8F0))
            ) {
                Icon(
                    imageVector = Icons.Filled.Payments,
                    contentDescription = null,
                    tint = if (selected) Color.White else GreenTop,
                    modifier = Modifier
                        .padding(10.dp)
                        .size(22.dp)
                )
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = title,
                    color = GreenTop,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = description,
                    color = Color(0xFF6B7280),
                    fontSize = 12.sp
                )
            }
            RadioButton(selected = selected, onClick = null)
        }
    }
}

@Composable
private fun VoucherPickerSection(
    vouchers: List<RewardVoucherDto>,
    selectedVoucher: RewardVoucherDto?,
    selectedVoucherCode: String?,
    onOpenVoucherSheet: () -> Unit,
    onClearVoucher: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color(0xFFF8FAFC),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0))
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Filled.Redeem, contentDescription = null, tint = GreenTop)
                    Spacer(modifier = Modifier.size(8.dp))
                    Column {
                        Text("Voucher của tôi", color = GreenTop, fontWeight = FontWeight.Bold)
                        Text(
                            text = "${vouchers.size} voucher khả dụng",
                            color = Color(0xFF6B7280),
                            fontSize = 12.sp
                        )
                    }
                }
                TextButton(onClick = onOpenVoucherSheet) {
                    Text(if (selectedVoucherCode.isNullOrBlank()) "Chọn" else "Đổi")
                }
            }

            if (selectedVoucher != null) {
                Surface(
                    color = Color(0xFFF0F9F1),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, GreenTop)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(selectedVoucher.name, color = GreenTop, fontWeight = FontWeight.SemiBold)
                                Text(selectedVoucher.code, color = Color(0xFF6B7280), fontSize = 12.sp)
                            }
                            TextButton(onClick = onClearVoucher) {
                                Text("Bỏ chọn")
                            }
                        }
                        Text(buildVoucherRuleText(selectedVoucher), color = Color(0xFF374151), fontSize = 13.sp)
                    }
                }
            } else {
                Text(
                    text = if (vouchers.isEmpty()) {
                        "Bạn chưa có voucher đổi điểm đã được duyệt. Có thể nhập mã coupon thường ở ô phía trên."
                    } else {
                        "Chọn voucher đã đổi để app tự áp mã vào đơn hàng."
                    },
                    color = Color(0xFF6B7280),
                    fontSize = 12.sp
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun VoucherBottomSheet(
    vouchers: List<RewardVoucherDto>,
    selectedVoucherCode: String?,
    subtotal: Double,
    onDismiss: () -> Unit,
    onSelectVoucher: (String?) -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp)
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Voucher của tôi",
                color = GreenTop,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Chọn voucher đã đổi bằng điểm. App sẽ tự dùng mã voucher này khi tạo đơn.",
                color = Color(0xFF6B7280),
                fontSize = 13.sp
            )

            if (!selectedVoucherCode.isNullOrBlank()) {
                TextButton(onClick = { onSelectVoucher(null) }) {
                    Text("Bỏ chọn voucher hiện tại")
                }
            }

            if (vouchers.isEmpty()) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color(0xFFF8FAFC),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                ) {
                    Text(
                        text = "Chưa có voucher nào khả dụng. Sau khi đổi điểm và được duyệt, voucher sẽ xuất hiện ở đây.",
                        modifier = Modifier.padding(16.dp),
                        color = Color(0xFF6B7280)
                    )
                }
            } else {
                vouchers.forEach { voucher ->
                    VoucherBottomSheetItem(
                        voucher = voucher,
                        selected = selectedVoucherCode.equals(voucher.code, ignoreCase = true),
                        qualified = subtotal >= (voucher.minOrderTotal ?: 0.0),
                        subtotal = subtotal,
                        onClick = { onSelectVoucher(voucher.code) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun VoucherBottomSheetItem(
    voucher: RewardVoucherDto,
    selected: Boolean,
    qualified: Boolean,
    subtotal: Double,
    onClick: () -> Unit
) {
    val previewDiscount = estimateVoucherPreviewDiscount(voucher, subtotal)
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = qualified, onClick = onClick),
        color = when {
            selected -> Color(0xFFF0F9F1)
            qualified -> Color.White
            else -> Color(0xFFF8FAFC)
        },
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(
            1.dp,
            when {
                selected -> GreenTop
                qualified -> Color(0xFFE2E8F0)
                else -> Color(0xFFF1F5F9)
            }
        )
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(voucher.name, color = GreenTop, fontWeight = FontWeight.Bold)
                    Text(voucher.code, color = Color(0xFF6B7280), fontSize = 12.sp)
                }
                Text(
                    text = when {
                        selected -> "Đã chọn"
                        qualified -> "Chọn"
                        else -> "Chưa đủ điều kiện"
                    },
                    color = when {
                        selected -> GreenTop
                        qualified -> Color(0xFF374151)
                        else -> Color(0xFFD97706)
                    },
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Text(buildVoucherRuleText(voucher), color = Color(0xFF374151), fontSize = 13.sp)
            voucher.minOrderTotal?.let {
                Text("Đơn tối thiểu: ${formatCurrency(it)}", color = Color(0xFF6B7280), fontSize = 12.sp)
            }
            Text(
                text = if (qualified) {
                    "Ước tính giảm: ${formatCurrency(previewDiscount)}"
                } else {
                    "Đơn hiện tại chưa đạt điều kiện áp dụng voucher này."
                },
                color = if (qualified) GreenTop else Color(0xFFD97706),
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun RewardInfoCard(
    availablePoints: Int,
    estimatedPoints: Int,
    pointsToUse: Int,
    maxUsablePoints: Int,
    useRewardPoints: Boolean,
    onToggleUsePoints: (Boolean) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color(0xFFFFFBEB),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, Color(0xFFFDE68A))
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text("Điểm thưởng", fontWeight = FontWeight.Bold, color = GreenTop)
            Text(
                "Sẽ nhận từ đơn này: +$estimatedPoints điểm",
                color = Color(0xFFFF8F00),
                fontWeight = FontWeight.SemiBold
            )
            Text("Điểm hiện có: $availablePoints", color = Color(0xFF6B7280))
            if (maxUsablePoints > 0) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Dùng tối đa $maxUsablePoints điểm",
                            fontWeight = FontWeight.SemiBold,
                            color = GreenTop
                        )
                        Text(
                            text = "Giảm ${formatCurrency(maxUsablePoints * 1000.0)} cho tiền hàng.",
                            fontSize = 12.sp,
                            color = Color(0xFF6B7280)
                        )
                    }
                    Switch(
                        checked = useRewardPoints,
                        onCheckedChange = onToggleUsePoints
                    )
                }

                if (pointsToUse > 0) {
                    Text(
                        text = "Đang áp dụng: -${formatCurrency(pointsToUse * 1000.0)}",
                        fontSize = 12.sp,
                        color = Color(0xFFFF8F00),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            } else {
                Text(
                    "Đơn hiện tại chưa đủ điều kiện áp dụng điểm hoặc bạn chưa có điểm khả dụng.",
                    fontSize = 12.sp,
                    color = Color(0xFF6B7280)
                )
            }
            Text(
                "Điểm nhận được tính theo từng sản phẩm. Khi cần dùng điểm, app sẽ tự áp mức tối đa hợp lệ thay vì nhập tay.",
                fontSize = 12.sp,
                color = Color(0xFF6B7280)
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
            Surface(
                modifier = Modifier.size(52.dp),
                color = Color(0xFFE8F5E9),
                shape = RoundedCornerShape(12.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
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
                            imageVector = Icons.Outlined.Storefront,
                            contentDescription = null,
                            tint = GreenTop,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.size(12.dp))
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
                    text = "${item.quantity} x ${formatCurrency(item.unitPrice)} • ${item.unit}",
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

@Composable
private fun RewardSummaryRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, fontSize = 15.sp, fontWeight = FontWeight.Medium, color = Color(0xFF374151))
        Text(value, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFFFF8F00))
    }
}

private fun buildVoucherRuleText(voucher: RewardVoucherDto): String {
    return when (voucher.discountType.uppercase()) {
        "PERCENT" -> {
            val cap = voucher.maxDiscountAmount?.takeIf { it > 0.0 }?.let {
                " tối đa ${formatCurrency(it)}"
            }.orEmpty()
            "Giảm ${voucher.discountValue}%$cap"
        }
        else -> "Giảm ${formatCurrency(voucher.discountValue)}"
    }
}

private fun estimateVoucherPreviewDiscount(voucher: RewardVoucherDto, subtotal: Double): Double {
    if (subtotal <= 0.0) return 0.0
    val minOrderTotal = voucher.minOrderTotal ?: 0.0
    if (subtotal < minOrderTotal) return 0.0

    val rawDiscount = when (voucher.discountType.uppercase()) {
        "PERCENT" -> subtotal * (voucher.discountValue / 100.0)
        else -> voucher.discountValue
    }
    val cappedDiscount = voucher.maxDiscountAmount?.let { maxAmount ->
        kotlin.math.min(rawDiscount, maxAmount)
    } ?: rawDiscount
    return cappedDiscount.coerceIn(0.0, subtotal)
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
    return NumberFormat.getCurrencyInstance(checkoutFlowLocale)
        .format(value)
        .replace("₫", "đ")
}