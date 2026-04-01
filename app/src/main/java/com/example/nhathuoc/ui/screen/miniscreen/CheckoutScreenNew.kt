package com.example.nhathuoc.ui.screen.miniscreen

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.nhathuoc.data.model.*
import com.example.nhathuoc.data.repository.CheckoutRepository
import com.example.nhathuoc.ui.theme.GreenTop
import com.example.nhathuoc.ui.theme.GreenLight
import com.example.nhathuoc.ui.theme.BgColor
import com.example.nhathuoc.util.PriceUtils

@Composable
fun CheckoutScreenNew(
    modifier: Modifier = Modifier,
    navController: NavController? = null
) {
    var currentStep by remember { mutableStateOf(0) }
    var selectedAddress by remember { mutableStateOf<UserAddressDto?>(null) }
    var pickupType by remember { mutableStateOf(PickupType.DELIVERY) }
    var paymentMethod by remember { mutableStateOf(PaymentMethod.COD) }
    var promoCode by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var rewardPointsToUse by remember { mutableStateOf(0) }

    var addresses by remember { mutableStateOf<List<UserAddressDto>>(emptyList()) }
    var showAddAddress by remember { mutableStateOf(false) }

    // Mock data
    val cartItems = remember {
        listOf(
            OrderItemDto("1", "Vitamin D3", 2, 150000.0),
            OrderItemDto("2", "Muối rửa mũi", 1, 170000.0)
        )
    }

    val subtotal = cartItems.sumOf { it.price * it.quantity }
    val discount = if (subtotal > 1_000_000) subtotal * 0.05 else 0.0
    val shipping = if (pickupType == PickupType.DELIVERY) {
        if (subtotal <= 500_000) 30_000.0 else 0.0
    } else 0.0
    val tax = (subtotal - discount) * 0.1
    val rewardDiscount = rewardPointsToUse * 1000.0
    val finalTotal = subtotal - discount + shipping + tax - rewardDiscount

    Column(modifier = modifier.fillMaxSize()) {
        // Header
        HeaderCheckout(onBackClick = { navController?.popBackStack() })

        // Stepper
        StepperRow(currentStep = currentStep)

        // Content
        when (currentStep) {
            0 -> Step1Address(
                selectedAddress = selectedAddress,
                onAddressSelect = { selectedAddress = it },
                onAddAddressClick = { showAddAddress = true },
                navController = navController
            )
            1 -> Step2Delivery(
                pickupType = pickupType,
                onPickupTypeChange = { pickupType = it }
            )
            2 -> Step3Payment(
                paymentMethod = paymentMethod,
                onPaymentMethodChange = { paymentMethod = it }
            )
            3 -> Step4Summary(
                cartItems = cartItems,
                subtotal = subtotal,
                discount = discount,
                shipping = shipping,
                tax = tax,
                rewardPointsToUse = rewardPointsToUse,
                onRewardPointsChange = { rewardPointsToUse = it },
                promoCode = promoCode,
                onPromoCodeChange = { promoCode = it },
                notes = notes,
                onNotesChange = { notes = it },
                finalTotal = finalTotal
            )
        }

        // Navigation buttons
        CheckoutNavigationButtons(
            currentStep = currentStep,
            totalSteps = 3,
            onNextClick = {
                if (currentStep < 3) currentStep++
            },
            onPreviousClick = {
                if (currentStep > 0) currentStep--
            },
            onCheckoutClick = {
                // Proceed to payment
                navController?.navigate("PaymentScreenNew?orderId=ORD-123&amount=${finalTotal.toLong()}&method=${paymentMethod.name}")
            }
        )
    }

    // Add address dialog
    if (showAddAddress) {
        AddAddressDialog(
            onDismiss = { showAddAddress = false },
            onConfirm = { address ->
                addresses = addresses + address
                selectedAddress = address
                showAddAddress = false
            }
        )
    }
}

@Composable
private fun HeaderCheckout(onBackClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Brush.verticalGradient(listOf(GreenTop, GreenLight)))
            .statusBarsPadding()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Icon(Icons.Filled.ArrowBack, contentDescription = null, tint = Color.White)
            }
            Text(
                "Thanh toán",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Spacer(Modifier.width(48.dp))
        }
    }
}

@Composable
private fun StepperRow(currentStep: Int) {
    val steps = listOf("Địa chỉ", "Giao hàng", "Thanh toán", "Xác nhận")
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        steps.forEachIndexed { index, title ->
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Surface(
                    modifier = Modifier.size(32.dp),
                    shape = RoundedCornerShape(16.dp),
                    color = if (index <= currentStep) GreenTop else Color.LightGray
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        if (index < currentStep) {
                            Icon(Icons.Filled.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        } else {
                            Text(
                                (index + 1).toString(),
                                color = if (index <= currentStep) Color.White else Color.Gray,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
                Text(title, fontSize = 9.sp, maxLines = 1)
            }

            if (index < steps.size - 1) {
                Divider(
                    modifier = Modifier
                        .weight(0.5f)
                        .height(1.dp)
                        .align(Alignment.CenterVertically),
                    color = if (index < currentStep) GreenTop else Color.LightGray
                )
            }
        }
    }
}

@Composable
private fun Step1Address(
    selectedAddress: UserAddressDto?,
    onAddressSelect: (UserAddressDto) -> Unit,
    onAddAddressClick: () -> Unit,
    navController: NavController?
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Chọn địa chỉ nhận hàng", fontSize = 14.sp, fontWeight = FontWeight.Bold)

        // Add new address button
        OutlinedButton(
            onClick = onAddAddressClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text("Thêm địa chỉ mới")
        }

        // Address list (mock)
        Text("Địa chỉ đã lưu", fontSize = 12.sp, color = Color.Gray)

        // Default address card
        AddressCard(
            isSelected = selectedAddress?.id == "addr-1",
            fullName = "Nguyễn Văn A",
            phone = "0987654321",
            address = "123 Đường Lê Lợi, Quận 1, TP.HCM",
            isDefault = true,
            onClick = { onAddressSelect(UserAddressDto("addr-1", "Nguyễn Văn A", "0987654321", "123 Đường Lê Lợi, Quận 1, TP.HCM", "", "", AddressType.HOME, true)) }
        )
    }
}

@Composable
private fun Step2Delivery(
    pickupType: PickupType,
    onPickupTypeChange: (PickupType) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Chọn hình thức giao hàng", fontSize = 14.sp, fontWeight = FontWeight.Bold)

        // Delivery options
        DeliveryOptionCard(
            title = "Giao hàng tận nơi",
            description = "Miễn phí nếu đơn hàng > 500K",
            isSelected = pickupType == PickupType.DELIVERY,
            onClick = { onPickupTypeChange(PickupType.DELIVERY) }
        )

        DeliveryOptionCard(
            title = "Tự lấy tại nhà thuốc",
            description = "Lấy ngay trong ngày",
            isSelected = pickupType == PickupType.PICKUP,
            onClick = { onPickupTypeChange(PickupType.PICKUP) }
        )
    }
}

@Composable
private fun Step3Payment(
    paymentMethod: PaymentMethod,
    onPaymentMethodChange: (PaymentMethod) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Chọn phương thức thanh toán", fontSize = 14.sp, fontWeight = FontWeight.Bold)

        PaymentMethodCard(
            title = "Thanh toán khi nhận hàng",
            icon = Icons.Outlined.LocalShipping,
            isSelected = paymentMethod == PaymentMethod.COD,
            onClick = { onPaymentMethodChange(PaymentMethod.COD) }
        )

        PaymentMethodCard(
            title = "VNPay",
            icon = Icons.Outlined.CreditCard,
            isSelected = paymentMethod == PaymentMethod.VNPAY,
            onClick = { onPaymentMethodChange(PaymentMethod.VNPAY) }
        )

        PaymentMethodCard(
            title = "MoMo",
            icon = Icons.Outlined.CreditCard,
            isSelected = paymentMethod == PaymentMethod.MOMO,
            onClick = { onPaymentMethodChange(PaymentMethod.MOMO) }
        )
    }
}

@Composable
private fun Step4Summary(
    cartItems: List<OrderItemDto>,
    subtotal: Double,
    discount: Double,
    shipping: Double,
    tax: Double,
    rewardPointsToUse: Int,
    onRewardPointsChange: (Int) -> Unit,
    promoCode: String,
    onPromoCodeChange: (String) -> Unit,
    notes: String,
    onNotesChange: (String) -> Unit,
    finalTotal: Double
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Xác nhận đơn hàng", fontSize = 14.sp, fontWeight = FontWeight.Bold)

        // Items
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color.White,
            shape = RoundedCornerShape(8.dp)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(cartItems) { item ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(item.name, fontWeight = FontWeight.Medium)
                            Text("x${item.quantity}", fontSize = 12.sp, color = Color.Gray)
                        }
                        Text(PriceUtils.formatPrice(item.price * item.quantity), fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Promo code
        OutlinedTextField(
            value = promoCode,
            onValueChange = onPromoCodeChange,
            label = { Text("Mã khuyến mãi") },
            leadingIcon = { Icon(Icons.Outlined.Discount, contentDescription = null) },
            modifier = Modifier.fillMaxWidth()
        )

        // Reward points
        OutlinedTextField(
            value = rewardPointsToUse.toString(),
            onValueChange = { onRewardPointsChange(it.toIntOrNull() ?: 0) },
            label = { Text("Điểm thưởng") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )

        // Notes
        OutlinedTextField(
            value = notes,
            onValueChange = onNotesChange,
            label = { Text("Ghi chú") },
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp),
            maxLines = 3
        )

        // Summary
        SummaryCard(
            subtotal = subtotal,
            discount = discount,
            shipping = shipping,
            tax = tax,
            finalTotal = finalTotal
        )
    }
}

@Composable
private fun AddressCard(
    isSelected: Boolean,
    fullName: String,
    phone: String,
    address: String,
    isDefault: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .border(
                2.dp,
                if (isSelected) GreenTop else Color.LightGray,
                RoundedCornerShape(8.dp)
            )
            .clickable(onClick = onClick),
        color = Color.White
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(fullName, fontWeight = FontWeight.Bold)
                if (isDefault) {
                    Surface(
                        color = GreenTop,
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text("Mặc định", color = Color.White, fontSize = 10.sp, modifier = Modifier.padding(4.dp))
                    }
                }
            }
            Text(phone, fontSize = 12.sp, color = Color.Gray)
            Text(address, fontSize = 12.sp, color = Color.Gray, maxLines = 2)
        }
    }
}

@Composable
private fun DeliveryOptionCard(
    title: String,
    description: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .border(
                2.dp,
                if (isSelected) GreenTop else Color.LightGray,
                RoundedCornerShape(8.dp)
            )
            .clickable(onClick = onClick),
        color = Color.White
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(selected = isSelected, onClick = onClick)
            Column {
                Text(title, fontWeight = FontWeight.Medium)
                Text(description, fontSize = 12.sp, color = Color.Gray)
            }
        }
    }
}

@Composable
private fun PaymentMethodCard(
    title: String,
    icon: androidx.compose.material.icons.Icons?,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .border(
                2.dp,
                if (isSelected) GreenTop else Color.LightGray,
                RoundedCornerShape(8.dp)
            )
            .clickable(onClick = onClick),
        color = Color.White
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(selected = isSelected, onClick = onClick, colors = RadioButtonDefaults.colors(selectedColor = GreenTop))
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (icon != null) {
                    Icon(icon, contentDescription = null, tint = GreenTop, modifier = Modifier.size(20.dp))
                }
                Text(title, fontWeight = FontWeight.Medium)
            }
        }
    }
}

@Composable
private fun SummaryCard(
    subtotal: Double,
    discount: Double,
    shipping: Double,
    tax: Double,
    finalTotal: Double
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.White,
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Tạm tính", color = Color.Gray)
                Text(PriceUtils.formatPrice(subtotal), fontWeight = FontWeight.Medium)
            }
            if (discount > 0) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Giảm giá", color = Color.Gray)
                    Text("- ${PriceUtils.formatPrice(discount)}", color = Color(0xFF4CAF50), fontWeight = FontWeight.Medium)
                }
            }
            if (shipping > 0) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Phí vận chuyển", color = Color.Gray)
                    Text(PriceUtils.formatPrice(shipping), fontWeight = FontWeight.Medium)
                }
            }
            if (tax > 0) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Thuế", color = Color.Gray)
                    Text(PriceUtils.formatPrice(tax), fontWeight = FontWeight.Medium)
                }
            }
            Divider()
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Tổng cộng", fontWeight = FontWeight.Bold)
                Text(PriceUtils.formatPrice(finalTotal), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = GreenTop)
            }
        }
    }
}

@Composable
private fun CheckoutNavigationButtons(
    currentStep: Int,
    totalSteps: Int,
    onNextClick: () -> Unit,
    onPreviousClick: () -> Unit,
    onCheckoutClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.White,
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (currentStep > 0) {
                OutlinedButton(
                    onClick = onPreviousClick,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                ) {
                    Text("Quay lại")
                }
            }
            Button(
                onClick = if (currentStep == totalSteps) onCheckoutClick else onNextClick,
                modifier = Modifier
                    .weight(if (currentStep > 0) 1f else 2f)
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = GreenTop)
            ) {
                Text(
                    if (currentStep == totalSteps) "Thanh toán" else "Tiếp tục",
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun AddAddressDialog(
    onDismiss: () -> Unit,
    onConfirm: (UserAddressDto) -> Unit
) {
    var fullName by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Thêm địa chỉ mới") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = fullName,
                    onValueChange = { fullName = it },
                    label = { Text("Họ và tên") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Số điện thoại") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("Địa chỉ") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp),
                    maxLines = 2
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                onConfirm(UserAddressDto("addr-new", fullName, phone, address, "", "", AddressType.HOME, false))
            }) {
                Text("Thêm")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Hủy")
            }
        }
    )
}

@Preview
@Composable
fun CheckoutScreenNewPreview() {
    CheckoutScreenNew()
}
