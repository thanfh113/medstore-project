package com.example.nhathuoc.ui.screen.miniscreen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.nhathuoc.data.model.*
import com.example.nhathuoc.ui.theme.NhathuocTheme
import com.example.nhathuoc.ui.theme.GreenLight
import com.example.nhathuoc.util.PriceUtils

// ── Colors ────────────────────────────────────────────────────────
private val GreenTop = Color(0xFF2E7D32)
private val GoldColor = Color(0xFFFFAB00)

// ── Payment Screen ────────────────────────────────────────────────
@Composable
fun PaymentScreen(
    modifier: Modifier = Modifier,
    navController: NavController = rememberNavController(),
    orderId: String = "ORD-12345",
    paymentMethod: PaymentMethod = PaymentMethod.VNPAY,
    totalAmount: Double = 200000.0
) {
    var paymentProgress by remember { mutableStateOf(PaymentProgress.PROCESSING) }
    var showDetails by remember { mutableStateOf(false) }

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

        // ── Payment Content ───────────────────────────────────────
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .background(Color(0xFFF5F7FA))
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Spacer(Modifier.height(40.dp))

            // Payment status icon
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .background(
                        when (paymentProgress) {
                            PaymentProgress.PROCESSING -> GreenTop.copy(alpha = 0.1f)
                            PaymentProgress.SUCCESS -> Color(0xFF4CAF50).copy(alpha = 0.1f)
                            PaymentProgress.FAILED -> Color(0xFFE53935).copy(alpha = 0.1f)
                        },
                        RoundedCornerShape(60.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (paymentProgress == PaymentProgress.PROCESSING) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(60.dp),
                        color = GreenTop,
                        strokeWidth = 4.dp
                    )
                } else {
                    Icon(
                        if (paymentProgress == PaymentProgress.SUCCESS) Icons.Filled.CheckCircle
                        else Icons.Filled.Cancel,
                        contentDescription = null,
                        modifier = Modifier.size(60.dp),
                        tint = if (paymentProgress == PaymentProgress.SUCCESS) Color(0xFF4CAF50)
                        else Color(0xFFE53935)
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            // Payment status text
            Text(
                when (paymentProgress) {
                    PaymentProgress.PROCESSING -> "Đang xử lý thanh toán"
                    PaymentProgress.SUCCESS -> "Thanh toán thành công"
                    PaymentProgress.FAILED -> "Thanh toán thất bại"
                },
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A1A1A),
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(8.dp))

            Text(
                when (paymentProgress) {
                    PaymentProgress.PROCESSING -> "Vui lòng chờ trong giây lát..."
                    PaymentProgress.SUCCESS -> "Đơn hàng của bạn đã được xác nhận"
                    PaymentProgress.FAILED -> "Đã có lỗi xảy ra trong quá trình thanh toán"
                },
                fontSize = 14.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 32.dp)
            )

            if (paymentProgress != PaymentProgress.PROCESSING) {
                Spacer(Modifier.height(32.dp))

                // Payment details card
                Surface(
                    color = Color.White,
                    shape = RoundedCornerShape(12.dp),
                    shadowElevation = 2.dp,
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
                                "Chi tiết giao dịch",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1A1A1A)
                            )
                            TextButton(onClick = { showDetails = !showDetails }) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        if (showDetails) "Ẩn" else "Xem chi tiết",
                                        color = GreenTop,
                                        fontSize = 13.sp
                                    )
                                    Icon(
                                        if (showDetails) Icons.Filled.KeyboardArrowUp
                                        else Icons.Filled.KeyboardArrowDown,
                                        contentDescription = null,
                                        tint = GreenTop,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }

                        Spacer(Modifier.height(12.dp))

                        PaymentDetailRow("Mã đơn hàng", orderId)
                        PaymentDetailRow("Phương thức", paymentMethod.displayName)
                        PaymentDetailRow("Số tiền", PriceUtils.formatPrice(totalAmount))

                        if (showDetails) {
                            Spacer(Modifier.height(12.dp))
                            HorizontalDivider(color = Color(0xFFEEEEEE))
                            Spacer(Modifier.height(12.dp))

                            PaymentDetailRow("Thời gian", "14:25 - 01/04/2026")
                            PaymentDetailRow("Mã giao dịch", "TXN-${System.currentTimeMillis()}")
                            PaymentDetailRow(
                                "Trạng thái",
                                if (paymentProgress == PaymentProgress.SUCCESS) "Thành công" else "Thất bại"
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(80.dp))

            // Simulate payment processing
            LaunchedEffect(Unit) {
                if (paymentProgress == PaymentProgress.PROCESSING) {
                    kotlinx.coroutines.delay(3000) // 3 second simulation
                    paymentProgress = if (paymentMethod == PaymentMethod.COD) {
                        PaymentProgress.SUCCESS
                    } else {
                        // Random success/failure for demo
                        if ((0..1).random() == 0) PaymentProgress.SUCCESS
                        else PaymentProgress.FAILED
                    }
                }
            }
        }

        // ── Bottom Action Bar ─────────────────────────────────────
        if (paymentProgress != PaymentProgress.PROCESSING) {
            Surface(
                color = Color.White,
                shadowElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = if (paymentProgress == PaymentProgress.FAILED) {
                        Arrangement.SpaceBetween
                    } else {
                        Arrangement.End
                    }
                ) {
                    if (paymentProgress == PaymentProgress.FAILED) {
                        OutlinedButton(
                            onClick = { paymentProgress = PaymentProgress.PROCESSING },
                            modifier = Modifier.height(48.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = GreenTop
                            ),
                            shape = RoundedCornerShape(24.dp)
                        ) {
                            Text("Thử lại", fontWeight = FontWeight.Bold)
                        }
                    }

                    Button(
                        onClick = {
                            if (paymentProgress == PaymentProgress.SUCCESS) {
                                // Navigate to order tracking or home
                                navController.navigate("MyOrdersScreen") {
                                    popUpTo("CheckoutScreen") { inclusive = true }
                                }
                            } else {
                                // Go back to checkout or home
                                navController.popBackStack()
                            }
                        },
                        modifier = Modifier.height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = GreenTop),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Text(
                            if (paymentProgress == PaymentProgress.SUCCESS) "Xem đơn hàng"
                            else "Quay lại",
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

// ── Payment Detail Row ────────────────────────────────────────────
@Composable
private fun PaymentDetailRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            label,
            fontSize = 13.sp,
            color = Color.Gray
        )
        Text(
            value,
            fontSize = 13.sp,
            color = Color(0xFF1A1A1A),
            fontWeight = FontWeight.Medium
        )
    }
}

// ── Enums ─────────────────────────────────────────────────────────
enum class PaymentProgress {
    PROCESSING,
    SUCCESS,
    FAILED
}

// ── Preview ───────────────────────────────────────────────────────
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun PaymentScreenPreview() {
    NhathuocTheme {
        PaymentScreen(navController = rememberNavController())
    }
}