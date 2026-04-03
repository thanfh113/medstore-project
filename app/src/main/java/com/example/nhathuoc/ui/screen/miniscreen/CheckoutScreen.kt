package com.example.nhathuoc.ui.screen.miniscreen

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.nhathuoc.data.model.UiState
import com.example.nhathuoc.ui.theme.GreenTop
import com.example.nhathuoc.viewmodel.CheckoutViewModel

@Composable
fun CheckoutScreen(
    modifier: Modifier = Modifier,
    navController: NavController = rememberNavController(),
    viewModel: CheckoutViewModel = viewModel()
) {
    val checkoutState by viewModel.checkoutState.collectAsState()
    val currentStep by viewModel.currentStep.collectAsState()
    val orderCreated by viewModel.orderCreatedEvent.collectAsState()

    // Navigate to payment screen when order is created
    LaunchedEffect(orderCreated) {
        if (orderCreated != null) {
            when (checkoutState.paymentMethod) {
                "VNPAY", "MOMO" -> navController.navigate("PaymentScreen/${orderCreated!!.id}")
                "COD" -> navController.navigate("OrderConfirmationScreen/${orderCreated!!.id}")
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF5F7FA))
    ) {
        // Header
        CheckoutHeader(navController = navController)

        // Step indicator
        StepIndicator(currentStep = currentStep)

        // Content
        when (currentStep) {
            0 -> AddressSelectionStep(viewModel = viewModel, state = checkoutState)
            1 -> DeliverySelectionStep(viewModel = viewModel, state = checkoutState)
            2 -> PaymentSelectionStep(viewModel = viewModel, state = checkoutState)
            3 -> ConfirmationStep(state = checkoutState)
        }

        Spacer(modifier = Modifier.weight(1f))

        // Navigation buttons
        CheckoutNavigation(
            currentStep = currentStep,
            onNext = { viewModel.nextStep() },
            onPrevious = { viewModel.previousStep() },
            onCheckout = { viewModel.createOrder() },
            isLoading = checkoutState.isLoading
        )
    }
}

@Composable
private fun CheckoutHeader(navController: NavController) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        color = GreenTop
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(Icons.Filled.ArrowBack, "Back", tint = Color.White)
            }
            Text("Thanh toán", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }
    }
}

@Composable
private fun StepIndicator(currentStep: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(4) { index ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f)
            ) {
                Surface(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(50)),
                    color = if (index <= currentStep) GreenTop else Color.LightGray
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        if (index < currentStep) {
                            Icon(Icons.Filled.Check, "Done", tint = Color.White, modifier = Modifier.size(20.dp))
                        } else {
                            Text((index + 1).toString(), color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                val labels = listOf("Địa chỉ", "Giao hàng", "Thanh toán", "Xác nhận")
                Text(labels[index], fontSize = 10.sp, modifier = Modifier.padding(top = 4.dp))
            }
            if (index < 3) {
                Divider(
                    modifier = Modifier
                        .weight(0.3f)
                        .height(2.dp),
                    color = if (index < currentStep) GreenTop else Color.LightGray
                )
            }
        }
    }
}

@Composable
private fun AddressSelectionStep(
    viewModel: CheckoutViewModel,
    state: com.example.nhathuoc.viewmodel.CheckoutState
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text("Chọn địa chỉ giao hàng", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(12.dp))

        if (state.addresses.isEmpty()) {
            // Show placeholder when no addresses loaded yet
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { viewModel.selectAddress("default-address") },
                color = Color.White,
                shadowElevation = 2.dp
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Filled.LocationOn, "Location", tint = GreenTop)
                    Column(modifier = Modifier.weight(1f).padding(horizontal = 12.dp)) {
                        Text("Địa chỉ mặc định", fontWeight = FontWeight.SemiBold)
                        Text("123 Đường ABC, Quận 1, TP HCM", fontSize = 12.sp, color = Color.Gray)
                    }
                    RadioButton(
                        selected = state.selectedAddressId == "default-address",
                        onClick = { viewModel.selectAddress("default-address") }
                    )
                }
            }
        } else {
            // Display actual user addresses
            state.addresses.forEach { address ->
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { viewModel.selectAddress(address.id) },
                    color = if (state.selectedAddressId == address.id) Color(0xFFE8F5E9) else Color.White,
                    border = BorderStroke(
                        2.dp,
                        if (state.selectedAddressId == address.id) GreenTop else Color.LightGray
                    ),
                    shadowElevation = 2.dp
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Filled.LocationOn, "Location", tint = GreenTop)
                        Column(modifier = Modifier.weight(1f).padding(horizontal = 12.dp)) {
                            Text(address.recipientName, fontWeight = FontWeight.SemiBold)
                            Text(address.recipientPhone, fontSize = 12.sp, color = Color.Gray)
                            Text(
                                "${address.fullAddress}, ${address.ward}, ${address.district}, ${address.province}",
                                fontSize = 11.sp,
                                color = Color.Gray
                            )
                            if (address.isDefault) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = Color(0xFFE8F5E9)
                                ) {
                                    Text(
                                        "Mặc định",
                                        fontSize = 10.sp,
                                        color = GreenTop,
                                        modifier = Modifier.padding(4.dp)
                                    )
                                }
                            }
                        }
                        RadioButton(
                            selected = state.selectedAddressId == address.id,
                            onClick = { viewModel.selectAddress(address.id) }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

@Composable
private fun DeliverySelectionStep(
    viewModel: CheckoutViewModel,
    state: com.example.nhathuoc.viewmodel.CheckoutState
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text("Chọn hình thức giao hàng", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(12.dp))

        // Delivery option
        DeliveryOption(
            title = "Giao hàng tận nơi",
            subtitle = "Phí giao hàng: 30.000đ",
            isSelected = state.pickupType == "DELIVERY",
            onClick = { viewModel.setDeliveryType("DELIVERY") }
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Pickup option
        DeliveryOption(
            title = "Nhận tại cửa hàng",
            subtitle = "Miễn phí",
            isSelected = state.pickupType == "PICKUP",
            onClick = { viewModel.setDeliveryType("PICKUP") }
        )
    }
}

@Composable
private fun DeliveryOption(
    title: String,
    subtitle: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick),
        color = if (isSelected) Color(0xFFE8F5E9) else Color.White,
        border = androidx.compose.foundation.BorderStroke(2.dp, if (isSelected) GreenTop else Color.LightGray),
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.SemiBold)
                Text(subtitle, fontSize = 12.sp, color = Color.Gray)
            }
            RadioButton(selected = isSelected, onClick = onClick)
        }
    }
}

@Composable
private fun PaymentSelectionStep(
    viewModel: CheckoutViewModel,
    state: com.example.nhathuoc.viewmodel.CheckoutState
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text("Chọn phương thức thanh toán", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(12.dp))

        listOf(
            Triple("COD", "Thanh toán khi nhận hàng", Icons.Filled.LocalShipping),
            Triple("VNPAY", "Thẻ tín dụng / VNPay", Icons.Filled.CreditCard),
            Triple("MOMO", "Ví MoMo", Icons.Filled.Wallet)
        ).forEach { (method, label, icon) ->
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { viewModel.setPaymentMethod(method) },
                color = if (state.paymentMethod == method) Color(0xFFE8F5E9) else Color.White,
                border = androidx.compose.foundation.BorderStroke(2.dp, if (state.paymentMethod == method) GreenTop else Color.LightGray),
                shadowElevation = 2.dp
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(icon, null, tint = GreenTop)
                    Column(modifier = Modifier.weight(1f).padding(horizontal = 12.dp)) {
                        Text(method, fontWeight = FontWeight.SemiBold)
                        Text(label, fontSize = 12.sp, color = Color.Gray)
                    }
                    RadioButton(selected = state.paymentMethod == method, onClick = { viewModel.setPaymentMethod(method) })
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        // Note section
        Text("Ghi chú", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(vertical = 12.dp))
        TextField(
            value = state.note,
            onValueChange = { viewModel.setNote(it) },
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp)
                .clip(RoundedCornerShape(8.dp)),
            placeholder = { Text("Thêm ghi chú cho đơn hàng...") },
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White
            )
        )
    }
}

@Composable
private fun ConfirmationStep(state: com.example.nhathuoc.viewmodel.CheckoutState) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text("Xác nhận đơn hàng", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(12.dp))

        // Order summary
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp)),
            color = Color.White,
            shadowElevation = 2.dp
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                PricingRow("Tạm tính", state.subtotal)
                PricingRow("Giảm giá", -state.discount)
                PricingRow("Phí giao hàng", state.shipping)
                PricingRow("Thuế", state.tax)
                if (state.pointsToUse > 0) {
                    PricingRow("Điểm thưởng", -state.pointsToUse * 1000.0)
                }
                Divider(modifier = Modifier.padding(vertical = 8.dp))
                PricingRow("Tổng cộng", state.total, isBold = true, textColor = GreenTop)
            }
        }
    }
}

@Composable
private fun PricingRow(
    label: String,
    value: Double,
    isBold: Boolean = false,
    textColor: Color = Color.Black
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal)
        Text(
            "${String.format("%.0f", value.toInt())}đ",
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal,
            color = textColor
        )
    }
}

@Composable
private fun CheckoutNavigation(
    currentStep: Int,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onCheckout: () -> Unit,
    isLoading: Boolean
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding(),
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
                Button(
                    onClick = onPrevious,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.LightGray)
                ) {
                    Text("Quay lại", color = Color.Black)
                }
            }

            Button(
                onClick = if (currentStep < 3) onNext else onCheckout,
                modifier = Modifier
                    .weight(2f)
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = GreenTop),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White)
                } else {
                    Text(if (currentStep < 3) "Tiếp tục" else "Đặt hàng", color = Color.White)
                }
            }
        }
    }
}
