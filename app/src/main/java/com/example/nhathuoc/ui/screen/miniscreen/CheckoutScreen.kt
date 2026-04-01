package com.example.nhathuoc.ui.screen.miniscreen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.nhathuoc.data.model.*
import com.example.nhathuoc.ui.theme.NhathuocTheme
import com.example.nhathuoc.ui.theme.GreenLight
import com.example.nhathuoc.util.PriceUtils
import com.example.nhathuoc.util.AuthenticatedAction

// ── Colors ────────────────────────────────────────────────────────
private val GreenTop = Color(0xFF2E7D32)
private val GoldColor = Color(0xFFFFAB00)

// ── Data Models ───────────────────────────────────────────────────
data class CheckoutStep(
    val number: Int,
    val title: String,
    val icon: ImageVector,
    val isCompleted: Boolean = false,
    val isActive: Boolean = false
)

data class CheckoutItem(
    val id: String,
    val name: String,
    val price: Double,
    val quantity: Int,
    val unit: String = "Hộp",
    val icon: ImageVector = Icons.Outlined.MedicalServices,
    val iconTint: Color = Color(0xFF2E7D32),
    val iconBg: Color = Color(0xFFE3F2FD)
)

data class UserAddress(
    val id: String,
    val fullName: String,
    val phone: String,
    val address: String,
    val district: String,
    val city: String,
    val type: AddressType = AddressType.HOME,
    val isDefault: Boolean = false
)

data class CheckoutState(
    val currentStep: Int = 0,
    val selectedAddress: UserAddress? = null,
    val pickupType: PickupType = PickupType.DELIVERY,
    val paymentMethod: PaymentMethod = PaymentMethod.COD,
    val note: String = "",
    val pointsToUse: Int = 0,
    val isLoading: Boolean = false
)

// ── Screen ────────────────────────────────────────────────────────
@Composable
fun CheckoutScreen(
    modifier: Modifier = Modifier,
    navController: NavController = rememberNavController(),
    cartItems: List<CheckoutItem> = defaultCartItems(),
    userAddresses: List<UserAddress> = defaultAddresses(),
    availablePoints: Int = 246
) {
    var checkoutState by remember {
        mutableStateOf(CheckoutState(
            selectedAddress = userAddresses.firstOrNull { it.isDefault } ?: userAddresses.firstOrNull()
        ))
    }

    val steps = remember {
        listOf(
            CheckoutStep(0, "Địa chỉ", Icons.Outlined.LocationOn),
            CheckoutStep(1, "Giao hàng", Icons.Outlined.LocalShipping),
            CheckoutStep(2, "Thanh toán", Icons.Outlined.Payment),
            CheckoutStep(3, "Xác nhận", Icons.Outlined.CheckCircle)
        ).mapIndexed { index, step ->
            step.copy(
                isCompleted = index < checkoutState.currentStep,
                isActive = index == checkoutState.currentStep
            )
        }
    }

    // Calculate totals
    val subtotal = cartItems.sumOf { it.price * it.quantity }
    val shippingFee = if (checkoutState.pickupType == PickupType.DELIVERY) 30000.0 else 0.0
    val pointValue = checkoutState.pointsToUse * 10.0 // 1 point = 10đ
    val discount = pointValue
    val total = subtotal + shippingFee - discount

    Column(modifier = modifier.fillMaxSize()) {

        // ── TopAppBar ─────────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.horizontalGradient(listOf(GreenTop, GreenLight)))
                .statusBarsPadding()
                .height(56.dp),
            contentAlignment = Alignment.Center
        ) {
            IconButton(
                onClick = { navController.popBackStack() },
                modifier = Modifier.align(Alignment.CenterStart).padding(start = 4.dp)
            ) {
                Icon(
                    Icons.Filled.ArrowBackIos,
                    "Quay lại",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
            Text(
                "Thanh toán",
                color = Color.White,
                fontSize = 17.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        // ── Stepper ───────────────────────────────────────────────
        Surface(
            color = Color.White,
            shadowElevation = 2.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                steps.forEachIndexed { index, step ->
                    CheckoutStepItem(
                        step = step,
                        modifier = Modifier.weight(1f),
                        showLine = index < steps.lastIndex
                    )
                }
            }
        }

        // ── Scrollable Content ────────────────────────────────────
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .background(Color(0xFFF5F7FA))
        ) {

            when (checkoutState.currentStep) {
                0 -> AddressStep(
                    selectedAddress = checkoutState.selectedAddress,
                    addresses = userAddresses,
                    onAddressSelected = { address ->
                        checkoutState = checkoutState.copy(selectedAddress = address)
                    },
                    onAddNewAddress = {
                        // Navigate to add address screen
                        // navController.navigate("AddAddressScreen")
                    }
                )

                1 -> DeliveryStep(
                    pickupType = checkoutState.pickupType,
                    onPickupTypeChanged = { type ->
                        checkoutState = checkoutState.copy(pickupType = type)
                    }
                )

                2 -> PaymentStep(
                    paymentMethod = checkoutState.paymentMethod,
                    onPaymentMethodChanged = { method ->
                        checkoutState = checkoutState.copy(paymentMethod = method)
                    },
                    availablePoints = availablePoints,
                    pointsToUse = checkoutState.pointsToUse,
                    onPointsChanged = { points ->
                        checkoutState = checkoutState.copy(
                            pointsToUse = maxOf(0, minOf(points, availablePoints))
                        )
                    }
                )

                3 -> ReviewStep(
                    cartItems = cartItems,
                    selectedAddress = checkoutState.selectedAddress,
                    pickupType = checkoutState.pickupType,
                    paymentMethod = checkoutState.paymentMethod,
                    note = checkoutState.note,
                    onNoteChanged = { note ->
                        checkoutState = checkoutState.copy(note = note)
                    }
                )
            }

            Spacer(Modifier.height(100.dp)) // Space for bottom bar
        }

        // ── Bottom Action Bar ─────────────────────────────────────
        CheckoutBottomBar(
            currentStep = checkoutState.currentStep,
            subtotal = subtotal,
            shippingFee = shippingFee,
            discount = discount,
            total = total,
            isLoading = checkoutState.isLoading,
            onBack = {
                if (checkoutState.currentStep > 0) {
                    checkoutState = checkoutState.copy(
                        currentStep = checkoutState.currentStep - 1
                    )
                } else {
                    navController.popBackStack()
                }
            },
            onNext = {
                when (checkoutState.currentStep) {
                    0 -> {
                        if (checkoutState.selectedAddress != null) {
                            checkoutState = checkoutState.copy(currentStep = 1)
                        }
                    }
                    1 -> checkoutState = checkoutState.copy(currentStep = 2)
                    2 -> checkoutState = checkoutState.copy(currentStep = 3)
                    3 -> {
                        // Place order
                        checkoutState = checkoutState.copy(isLoading = true)
                        // TODO: Implement place order logic
                        navController.navigate("PaymentScreen")
                    }
                }
            },
            canProceed = when (checkoutState.currentStep) {
                0 -> checkoutState.selectedAddress != null
                1, 2 -> true
                3 -> true
                else -> false
            }
        )
    }
}

// ── Stepper Item ──────────────────────────────────────────────────
@Composable
private fun CheckoutStepItem(
    step: CheckoutStep,
    modifier: Modifier = Modifier,
    showLine: Boolean = true
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Step circle
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(
                        when {
                            step.isCompleted -> GreenTop
                            step.isActive -> GreenTop.copy(alpha = 0.2f)
                            else -> Color(0xFFE0E0E0)
                        }
                    )
                    .border(
                        width = 2.dp,
                        color = when {
                            step.isCompleted || step.isActive -> GreenTop
                            else -> Color(0xFFE0E0E0)
                        },
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (step.isCompleted) {
                    Icon(
                        Icons.Filled.Check,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                } else {
                    Text(
                        "${step.number + 1}",
                        color = if (step.isActive) GreenTop else Color.Gray,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            // Step title
            Text(
                step.title,
                fontSize = 11.sp,
                color = if (step.isActive || step.isCompleted) GreenTop else Color.Gray,
                fontWeight = if (step.isActive) FontWeight.Bold else FontWeight.Normal,
                textAlign = TextAlign.Center
            )
        }

        // Connection line
        if (showLine) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(2.dp)
                    .padding(horizontal = 8.dp)
                    .background(
                        if (step.isCompleted) GreenTop else Color(0xFFE0E0E0),
                        RoundedCornerShape(1.dp)
                    )
            )
        }
    }
}

// ── Step 1: Address ───────────────────────────────────────────────
@Composable
private fun AddressStep(
    selectedAddress: UserAddress?,
    addresses: List<UserAddress>,
    onAddressSelected: (UserAddress) -> Unit,
    onAddNewAddress: () -> Unit
) {
    Column {
        Spacer(Modifier.height(16.dp))

        // Current selected address
        selectedAddress?.let { address ->
            Surface(
                color = Color.White,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Địa chỉ giao hàng",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1A1A1A)
                        )
                        if (address.isDefault) {
                            Surface(
                                color = GreenTop,
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    "Mặc định",
                                    fontSize = 10.sp,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    Row(
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            Icons.Outlined.LocationOn,
                            contentDescription = null,
                            tint = GreenTop,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Column {
                            Text(
                                "${address.fullName} | ${address.phone}",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF1A1A1A)
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                "${address.address}, ${address.district}, ${address.city}",
                                fontSize = 13.sp,
                                color = Color.Gray,
                                lineHeight = 18.sp
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                address.type.displayName,
                                fontSize = 12.sp,
                                color = GreenTop,
                                modifier = Modifier
                                    .background(
                                        GreenTop.copy(alpha = 0.1f),
                                        RoundedCornerShape(4.dp)
                                    )
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // Change address button
        Surface(
            color = Color.White,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .clickable { /* Show address selection */ }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Outlined.EditLocation,
                        contentDescription = null,
                        tint = GreenTop,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(
                        "Thay đổi địa chỉ giao hàng",
                        fontSize = 14.sp,
                        color = GreenTop,
                        fontWeight = FontWeight.Medium
                    )
                }
                Icon(
                    Icons.Outlined.ChevronRight,
                    contentDescription = null,
                    tint = GreenTop,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(Modifier.height(8.dp))

        // Add new address button
        Surface(
            color = Color.White,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .clickable { onAddNewAddress() }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Outlined.AddLocation,
                        contentDescription = null,
                        tint = GreenTop,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(
                        "Thêm địa chỉ mới",
                        fontSize = 14.sp,
                        color = GreenTop,
                        fontWeight = FontWeight.Medium
                    )
                }
                Icon(
                    Icons.Outlined.ChevronRight,
                    contentDescription = null,
                    tint = GreenTop,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

// ── Step 2: Delivery ──────────────────────────────────────────────
@Composable
private fun DeliveryStep(
    pickupType: PickupType,
    onPickupTypeChanged: (PickupType) -> Unit
) {
    Column {
        Spacer(Modifier.height(16.dp))

        Surface(
            color = Color.White,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "Phương thức nhận hàng",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1A1A)
                )

                Spacer(Modifier.height(16.dp))

                PickupType.entries.forEach { type ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onPickupTypeChanged(type) }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = pickupType == type,
                            onClick = { onPickupTypeChanged(type) },
                            colors = RadioButtonDefaults.colors(selectedColor = GreenTop)
                        )

                        Spacer(Modifier.width(12.dp))

                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    if (type == PickupType.DELIVERY) Icons.Outlined.LocalShipping
                                    else Icons.Outlined.Store,
                                    contentDescription = null,
                                    tint = GreenTop,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    type.displayName,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF1A1A1A)
                                )

                                if (type == PickupType.DELIVERY) {
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        PriceUtils.formatPrice(30000),
                                        fontSize = 12.sp,
                                        color = GreenTop,
                                        fontWeight = FontWeight.Bold
                                    )
                                } else {
                                    Spacer(Modifier.width(8.dp))
                                    Surface(
                                        color = Color(0xFFE8F5E9),
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            "Miễn phí",
                                            fontSize = 10.sp,
                                            color = GreenTop,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(Modifier.height(4.dp))

                            Text(
                                if (type == PickupType.DELIVERY)
                                    "Giao hàng tận nơi trong 1-2 ngày làm việc"
                                else
                                    "Nhận tại cửa hàng, tiết kiệm phí giao hàng",
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                        }
                    }
                }
            }
        }

        if (pickupType == PickupType.STORE_PICKUP) {
            Spacer(Modifier.height(8.dp))

            Surface(
                color = Color.White,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .clickable { /* Navigate to pharmacy selection */ }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Outlined.Store,
                            contentDescription = null,
                            tint = GreenTop,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text(
                                "Chọn cửa hàng nhận hàng",
                                fontSize = 14.sp,
                                color = GreenTop,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                "Tìm cửa hàng gần bạn",
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                        }
                    }
                    Icon(
                        Icons.Outlined.ChevronRight,
                        contentDescription = null,
                        tint = GreenTop,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

// ── Step 3: Payment ───────────────────────────────────────────────
@Composable
private fun PaymentStep(
    paymentMethod: PaymentMethod,
    onPaymentMethodChanged: (PaymentMethod) -> Unit,
    availablePoints: Int,
    pointsToUse: Int,
    onPointsChanged: (Int) -> Unit
) {
    Column {
        Spacer(Modifier.height(16.dp))

        // Payment methods
        Surface(
            color = Color.White,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "Phương thức thanh toán",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1A1A)
                )

                Spacer(Modifier.height(16.dp))

                listOf(
                    PaymentMethod.COD,
                    PaymentMethod.VNPAY,
                    PaymentMethod.MOMO
                ).forEach { method ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onPaymentMethodChanged(method) }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = paymentMethod == method,
                            onClick = { onPaymentMethodChanged(method) },
                            colors = RadioButtonDefaults.colors(selectedColor = GreenTop)
                        )

                        Spacer(Modifier.width(12.dp))

                        Icon(
                            when (method) {
                                PaymentMethod.COD -> Icons.Outlined.Payments
                                PaymentMethod.VNPAY -> Icons.Outlined.Payment
                                PaymentMethod.MOMO -> Icons.Outlined.AccountBalanceWallet
                                else -> Icons.Outlined.Payment
                            },
                            contentDescription = null,
                            tint = when (method) {
                                PaymentMethod.MOMO -> Color(0xFFE90098)
                                else -> GreenTop
                            },
                            modifier = Modifier.size(20.dp)
                        )

                        Spacer(Modifier.width(8.dp))

                        Text(
                            method.displayName,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF1A1A1A)
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        // Reward points
        Surface(
            color = Color.White,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Filled.EmojiEvents,
                            contentDescription = null,
                            tint = GoldColor,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "Sử dụng điểm tích lũy",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF1A1A1A)
                        )
                    }
                    Text(
                        "Có $availablePoints điểm",
                        fontSize = 12.sp,
                        color = GoldColor,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (availablePoints > 0) {
                    Spacer(Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = if (pointsToUse == 0) "" else pointsToUse.toString(),
                            onValueChange = { value ->
                                val points = value.toIntOrNull() ?: 0
                                onPointsChanged(points)
                            },
                            label = { Text("Số điểm sử dụng") },
                            placeholder = { Text("0") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )

                        Spacer(Modifier.width(8.dp))

                        TextButton(
                            onClick = { onPointsChanged(availablePoints) }
                        ) {
                            Text(
                                "Dùng tối đa",
                                color = GreenTop,
                                fontSize = 12.sp
                            )
                        }
                    }

                    Spacer(Modifier.height(8.dp))

                    Text(
                        "Tiết kiệm: ${PriceUtils.formatPrice(pointsToUse * 10.0)}",
                        fontSize = 12.sp,
                        color = GoldColor,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

// ── Step 4: Review ────────────────────────────────────────────────
@Composable
private fun ReviewStep(
    cartItems: List<CheckoutItem>,
    selectedAddress: UserAddress?,
    pickupType: PickupType,
    paymentMethod: PaymentMethod,
    note: String,
    onNoteChanged: (String) -> Unit
) {
    Column {
        Spacer(Modifier.height(16.dp))

        // Order items
        Surface(
            color = Color.White,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "Sản phẩm đã chọn (${cartItems.size})",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1A1A)
                )

                Spacer(Modifier.height(12.dp))

                cartItems.forEach { item ->
                    ReviewCartItem(item = item)
                    if (item != cartItems.last()) {
                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 8.dp),
                            color = Color(0xFFEEEEEE)
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        // Delivery info summary
        Surface(
            color = Color.White,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "Thông tin giao hàng",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1A1A)
                )

                Spacer(Modifier.height(12.dp))

                // Delivery type
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "Phương thức:",
                        fontSize = 13.sp,
                        color = Color.Gray
                    )
                    Text(
                        pickupType.displayName,
                        fontSize = 13.sp,
                        color = Color(0xFF1A1A1A),
                        fontWeight = FontWeight.Medium
                    )
                }

                if (pickupType == PickupType.DELIVERY && selectedAddress != null) {
                    Spacer(Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            "Địa chỉ:",
                            fontSize = 13.sp,
                            color = Color.Gray,
                            modifier = Modifier.width(80.dp)
                        )
                        Text(
                            "${selectedAddress.fullName}, ${selectedAddress.phone}\n${selectedAddress.address}",
                            fontSize = 13.sp,
                            color = Color(0xFF1A1A1A),
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(Modifier.height(8.dp))

                // Payment method
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "Thanh toán:",
                        fontSize = 13.sp,
                        color = Color.Gray
                    )
                    Text(
                        paymentMethod.displayName,
                        fontSize = 13.sp,
                        color = Color(0xFF1A1A1A),
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        // Note
        Surface(
            color = Color.White,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "Ghi chú cho đơn hàng",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1A1A)
                )

                Spacer(Modifier.height(12.dp))

                OutlinedTextField(
                    value = note,
                    onValueChange = onNoteChanged,
                    placeholder = { Text("Nhập ghi chú (không bắt buộc)") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    maxLines = 5
                )
            }
        }
    }
}

// ── Review Cart Item ──────────────────────────────────────────────
@Composable
private fun ReviewCartItem(
    item: CheckoutItem
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        // Product icon
        Box(
            modifier = Modifier
                .size(50.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(item.iconBg),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                item.icon,
                contentDescription = null,
                tint = item.iconTint,
                modifier = Modifier.size(24.dp)
            )
        }

        Spacer(Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                item.name,
                fontSize = 13.sp,
                color = Color(0xFF1A1A1A),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 16.sp
            )

            Spacer(Modifier.height(4.dp))

            Text(
                PriceUtils.formatPrice(item.price),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = GreenTop
            )
        }

        Text(
            "SL: ${item.quantity}",
            fontSize = 13.sp,
            color = Color.Gray,
            fontWeight = FontWeight.Medium
        )
    }
}

// ── Bottom Action Bar ─────────────────────────────────────────────
@Composable
private fun CheckoutBottomBar(
    currentStep: Int,
    subtotal: Double,
    shippingFee: Double,
    discount: Double,
    total: Double,
    isLoading: Boolean,
    onBack: () -> Unit,
    onNext: () -> Unit,
    canProceed: Boolean
) {
    Surface(
        color = Color.White,
        shadowElevation = 8.dp
    ) {
        Column {
            // Order summary
            if (currentStep == 3) {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Tạm tính:", fontSize = 13.sp, color = Color.Gray)
                        Text(
                            PriceUtils.formatPrice(subtotal),
                            fontSize = 13.sp,
                            color = Color(0xFF1A1A1A)
                        )
                    }

                    if (shippingFee > 0) {
                        Spacer(Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Phí giao hàng:", fontSize = 13.sp, color = Color.Gray)
                            Text(
                                PriceUtils.formatPrice(shippingFee),
                                fontSize = 13.sp,
                                color = Color(0xFF1A1A1A)
                            )
                        }
                    }

                    if (discount > 0) {
                        Spacer(Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Giảm giá:", fontSize = 13.sp, color = Color.Gray)
                            Text(
                                "-${PriceUtils.formatPrice(discount)}",
                                fontSize = 13.sp,
                                color = GoldColor
                            )
                        }
                    }

                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 8.dp),
                        color = Color(0xFFEEEEEE)
                    )
                }
            }

            // Action buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Total price
                Column {
                    if (currentStep < 3) {
                        Text("Tổng tiền", fontSize = 13.sp, color = Color.Gray)
                    }
                    Text(
                        PriceUtils.formatPrice(total),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = GreenTop
                    )
                }

                // Action buttons
                Row {
                    if (currentStep > 0) {
                        OutlinedButton(
                            onClick = onBack,
                            modifier = Modifier.height(48.dp),
                            enabled = !isLoading
                        ) {
                            Text("Quay lại", color = GreenTop)
                        }
                        Spacer(Modifier.width(12.dp))
                    }

                    Button(
                        onClick = onNext,
                        modifier = Modifier.height(48.dp),
                        enabled = canProceed && !isLoading,
                        colors = ButtonDefaults.buttonColors(containerColor = GreenTop),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                            Spacer(Modifier.width(8.dp))
                        }
                        Text(
                            when (currentStep) {
                                0, 1, 2 -> "Tiếp tục"
                                3 -> "Đặt hàng"
                                else -> "Tiếp tục"
                            },
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

// ── Default Data ──────────────────────────────────────────────────
private fun defaultCartItems() = listOf(
    CheckoutItem(
        id = "1",
        name = "Muối rửa mũi xoang Sinufresh Cát Linh (30 gói và 1 chai 180ml)",
        price = 170000.0,
        quantity = 1,
        unit = "Hộp",
        icon = Icons.Outlined.Air,
        iconTint = Color(0xFF00838F),
        iconBg = Color(0xFFE0F7FA)
    ),
    CheckoutItem(
        id = "2",
        name = "Thuốc giảm đau Natrofen 400mg (10 viên)",
        price = 45000.0,
        quantity = 2,
        unit = "Hộp",
        icon = Icons.Outlined.MedicalServices,
        iconTint = Color(0xFF2E7D32),
        iconBg = Color(0xFFE3F2FD)
    )
)

private fun defaultAddresses() = listOf(
    UserAddress(
        id = "1",
        fullName = "Nguyễn Văn A",
        phone = "0123456789",
        address = "123 Đường ABC, Phường XYZ",
        district = "Quận 1",
        city = "TP. Hồ Chí Minh",
        type = AddressType.HOME,
        isDefault = true
    ),
    UserAddress(
        id = "2",
        fullName = "Nguyễn Văn A",
        phone = "0123456789",
        address = "456 Đường DEF, Phường UVW",
        district = "Quận 3",
        city = "TP. Hồ Chí Minh",
        type = AddressType.OFFICE,
        isDefault = false
    )
)

// ── Preview ───────────────────────────────────────────────────────
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun CheckoutScreenPreview() {
    NhathuocTheme {
        CheckoutScreen(navController = rememberNavController())
    }
}